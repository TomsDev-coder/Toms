//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractLeadDcoRepositoryImpl.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.User;

/**
 * リードDCO選出処理アクセス用Repository.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class SubExtractLeadDcoRepositoryImpl implements SubExtractLeadDcoRepository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** リードDCO選出対象ミッション（ICT)取得SQL定義 */
    private static final String LEAD_DCO_MISSION_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by "                                                                   +
            "FROM "                                                                               +
            "    t_mission_sort_ict A "                                                           +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        A.mission_key = B.mission_key "                                              +
            "WHERE "                                                                              +
            "    B.testing_type IN ( '11', '12', '13' ) AND "                                     +
            "    B.mission_status = '12' "                                                        +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** リードDCO選出対象ミッション（OOCT)取得SQL定義 */
    private static final String LEAD_DCO_MISSION_OOCT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by "                                                                   +
            "FROM "                                                                               +
            "    t_mission_sort_ooct A "                                                          +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        A.mission_key = B.mission_key "                                              +
            "WHERE "                                                                              +
            "    B.testing_type = '21' AND "                                                      +
            "    B.mission_status = '12' "                                                        +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** ICT側リードDCO参加可能対象者取得SQL定義 */
    private static final String ICT_PARTICIPANTS_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_calendar A "                                                               +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "    LEFT OUTER JOIN "                                                                +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            D.user_id "                                                              +
            "        FROM "                                                                       +
            "            m_dcp_interests D "                                                      +
            "            INNER JOIN t_mission_base E ON "                                         +
            "                E.sports_id = D.sports_id AND "                                      +
            "                E.mission_key = :missionKey AND "                                    +
            "                E.is_deleted = false "                                               +
            "        GROUP BY "                                                                   +
            "            D.user_id, "                                                             +
            "            D.sports_id "                                                            +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    INNER JOIN m_dcp_qualification D ON "                                            +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.dcp_rank IN ( '1', '2', '3' ) AND "                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側リードDCO参加可能対象者（終日）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_ALLDAY_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_calendar A "                                                               +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "    LEFT OUTER JOIN "                                                                +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            D.user_id "                                                              +
            "        FROM "                                                                       +
            "            m_dcp_interests D "                                                      +
            "            INNER JOIN t_mission_base E ON "                                         +
            "                E.sports_id = D.sports_id AND "                                      +
            "                E.mission_key = :missionKey AND "                                    +
            "                E.is_deleted = false "                                               +
            "        GROUP BY "                                                                   +
            "            D.user_id, "                                                             +
            "            D.sports_id "                                                            +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    INNER JOIN m_dcp_qualification D ON "                                            +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.dcp_rank IN ( '1', '2', '3' ) AND "                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            "    A.is_remarks_written = false AND "                                               +
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側リードDCO参加可能対象者（早朝）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EARLY_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_calendar A "                                                               +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "    LEFT OUTER JOIN "                                                                +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            D.user_id "                                                              +
            "        FROM "                                                                       +
            "            m_dcp_interests D "                                                      +
            "            INNER JOIN t_mission_base E ON "                                         +
            "                E.sports_id = D.sports_id AND "                                      +
            "                E.mission_key = :missionKey AND "                                    +
            "                E.is_deleted = false "                                               +
            "        GROUP BY "                                                                   +
            "            D.user_id, "                                                             +
            "            D.sports_id "                                                            +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    INNER JOIN m_dcp_qualification D ON "                                            +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.dcp_rank IN ( '1', '2', '3' ) AND "                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_early = true AND "                                                   +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側リードDCO参加可能対象者（AM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_MORNING_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_calendar A "                                                               +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "    LEFT OUTER JOIN "                                                                +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            D.user_id "                                                              +
            "        FROM "                                                                       +
            "            m_dcp_interests D "                                                      +
            "            INNER JOIN t_mission_base E ON "                                         +
            "                E.sports_id = D.sports_id AND "                                      +
            "                E.mission_key = :missionKey AND "                                    +
            "                E.is_deleted = false "                                               +
            "        GROUP BY "                                                                   +
            "            D.user_id, "                                                             +
            "            D.sports_id "                                                            +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    INNER JOIN m_dcp_qualification D ON "                                            +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.dcp_rank IN ( '1', '2', '3' ) AND "                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_morning = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側リードDCO参加可能対象者（PM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_AFTERNOON_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_calendar A "                                                               +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "    LEFT OUTER JOIN "                                                                +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            D.user_id "                                                              +
            "        FROM "                                                                       +
            "            m_dcp_interests D "                                                      +
            "            INNER JOIN t_mission_base E ON "                                         +
            "                E.sports_id = D.sports_id AND "                                      +
            "                E.mission_key = :missionKey AND "                                    +
            "                E.is_deleted = false "                                               +
            "        GROUP BY "                                                                   +
            "            D.user_id, "                                                             +
            "            D.sports_id "                                                            +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    INNER JOIN m_dcp_qualification D ON "                                            +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.dcp_rank IN ( '1', '2', '3' ) AND "                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_afternoon = true AND "                                               +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側リードDCO参加可能対象者（夜間）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EVENING_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_calendar A "                                                               +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "    LEFT OUTER JOIN "                                                                +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            D.user_id "                                                              +
            "        FROM "                                                                       +
            "            m_dcp_interests D "                                                      +
            "            INNER JOIN t_mission_base E ON "                                         +
            "                E.sports_id = D.sports_id AND "                                      +
            "                E.mission_key = :missionKey AND "                                    +
            "                E.is_deleted = false "                                               +
            "        GROUP BY "                                                                   +
            "            D.user_id, "                                                             +
            "            D.sports_id "                                                            +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    INNER JOIN m_dcp_qualification D ON "                                            +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.dcp_rank IN ( '1', '2', '3' ) AND "                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_evening = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** DCP割当状況登録SQL定義 */
    private static final String DCP_ASSIGN_STATUS_INSERT_QUERY =
            "INSERT INTO t_dcp_assign_status "                                                    +
            " VALUES "                                                                            +
            "( "                                                                                  +
            "    :missionKey, "                                                                   +
            "    :testingDate, "                                                                  +
            "    :requiredDcoLead, "                                                              +
            "    :requiredDco, "                                                                  +
            "    :requiredDcoMale, "                                                              +
            "    :requiredDcoFemale, "                                                            +
            "    :requiredBcoAdmin, "                                                             +
            "    :requiredBco, "                                                                  +
            "    :isDeleted, "                                                                    +
            "    :createdAt, "                                                                    +
            "    :createdBy, "                                                                    +
            "    :updatedAt, "                                                                    +
            "    :updatedBy  "                                                                    +
            ") ";

    /** DCP割当状況削除SQL定義 */
    private static final String DCP_ASSIGN_STATUS_DELETE_QUERY =
            "DELETE FROM "                                                                        +
            "    t_dcp_assign_status "                                                            +
            "WHERE "                                                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    testing_date = :testingDate ";

    /** 視察ミッション確認SQL定義 */
    private static final String INSPECTION_MISSION_CONFIRM_QUERY =
            "SELECT "                                                                             +
            "    B.mission_key "                                                                  +
            "FROM "                                                                               +
            "    t_mission_base A "                                                               +
            "    INNER JOIN t_sample_analysis_plan B ON "                                         +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_date = A.testing_date_from AND "                                   +
            "        B.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    B.mission_key = :missionKey AND "                                                +
            "    B.sample_type = '9' AND "                                                        +
            "    B.is_deleted = FALSE "                                                           +
            "ORDER BY "                                                                           +
            "    B.mission_key, "                                                                 +
            "    B.testing_date ";

    /** DCP選定情報取得SQL定義 */
    private static final String DCP_SELECT_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_select_ict A "                                                             +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_deleted = FALSE "                                                           +
            "GROUP BY "                                                                           +
            "    A.user_id "                                                                      +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 視察ミッション更新SQL定義 */
    private static final String INSPECTION_MISSION_UPDATE =
            "UPDATE "                                                                             +
            "    t_dcp_select_ict "                                                               +
            "SET "                                                                                +
            "    dcp_role_type = :dcpRoleTypeInspection "                                         +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleTypeDcoLead AND "                                        +
            "    is_deleted = FALSE ";

    /** DCP選定情報（ICT）削除SQL定義 */
    private static final String DCP_SELECT_ICT_DELETE =
            "DELETE FROM "                                                                        +
            "    t_dcp_select_ict "                                                               +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleTypeDcoLead AND "                                        +
            "    is_deleted = FALSE ";

    /** DCP選定情報（ICT）有力候補フラグ更新SQL定義 */
    private static final String IS_STRONG_CANDIDATE_UPDATE =
            "UPDATE "                                                                             +
            "    t_dcp_select_ict "                                                               +
            "SET "                                                                                +
            "    is_strong_candidate = FALSE "                                                    +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    is_deleted = FALSE ";

    /**
     * リードDCO選出対象ミッションリスト（ICT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、リードDCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return リードDCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getLeadDcpAssignmentMissionsIct() {

        // レコードマップを生成
        RowMapper<TMissionSortIct> mapper = new BeanPropertyRowMapper<>(TMissionSortIct.class);

        // 結果返却
        return namedJdbcTemplate.query(LEAD_DCO_MISSION_ICT_QUERY, mapper);
    }

    /**
     * リードDCO選出対象ミッションリスト（OOCT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、リードDCO選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return リードDCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getLeadDcpAssignmentMissionsOoct() {

        // レコードマップを生成
        RowMapper<TMissionSortOoct> mapper = new BeanPropertyRowMapper<>(TMissionSortOoct.class);

        // 結果返却
        return namedJdbcTemplate.query(LEAD_DCO_MISSION_OOCT_QUERY, mapper);
    }

    /**
     * ICT側リードDCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1/S2/S3のユーザ
     * 
     * @param missionKey   ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(ICT_PARTICIPANTS_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1/S2/S3のユーザ
     * 
     * @param missionKey   ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListAlldayOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_ALLDAY_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1/S2/S3のユーザ
     * 
     * @param missionKey   ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListEarlyOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EARLY_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④指定DCPランクのユーザ
     * 
     * @param missionKey   ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListMorningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_MORNING_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④指定DCPランクのユーザ
     * 
     * @param missionKey   ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListAfternoonOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_AFTERNOON_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④指定DCPランクのユーザ
     * 
     * @param missionKey   ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListEveningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EVENING_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * DCP割当状況登録.<br>
     * DCP割当状況を登録する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param requiredDcoLead 必要人数（リードDCO）
     * @param requiredDco 必要人数（DCO）
     * @param requiredDcoMale 必要人数（DCO男性）
     * @param requiredDcoFemale 必要人数（DCO女性）
     * @param requiredBcoAdmin 必要人数（管理者BCO）
     * @param requiredBco 必要人数（BCO）
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID
     * @return 登録件数
     */
    public int insertTDcpAssignStatus(@Param("missionKey") Integer missionKey,
            @Param("testingDate,") Date testingDate,
            @Param("requiredDcoLead,") Integer requiredDcoLead,
            @Param("requiredDco,") Integer requiredDco,
            @Param("requiredDcoMale,") Integer requiredDcoMale,
            @Param("requiredDcoFemale,") Integer requiredDcoFemale,
            @Param("requiredBcoAdmin,") Integer requiredBcoAdmin,
            @Param("requiredBco,") Integer requiredBco, @Param("isDeleted,") Boolean isDeleted,
            @Param("createdAt,") Timestamp createdAt, @Param("createdBy,") Integer createdBy,
            @Param("updatedAt,") Timestamp updatedAt, @Param("updatedBy") Integer updatedBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("testingDate", testingDate);
        parameter.put("requiredDcoLead", requiredDcoLead);
        parameter.put("requiredDco", requiredDco);
        parameter.put("requiredDcoMale", requiredDcoMale);
        parameter.put("requiredDcoFemale", requiredDcoFemale);
        parameter.put("requiredBcoAdmin", requiredBcoAdmin);
        parameter.put("requiredBco", requiredBco);
        parameter.put("isDeleted", isDeleted);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);
        parameter.put("updatedAt", updatedAt);
        parameter.put("updatedBy", updatedBy);

        // 結果返却
        return namedJdbcTemplate.update(DCP_ASSIGN_STATUS_INSERT_QUERY, parameter);
    }

    /**
     * DCP割当状況削除.<br>
     * DCP割当状況を削除する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 登録件数
     */
    public int deleteTDcpAssignStatus(@Param("missionKey") Integer missionKey,
            @Param("testingDate,") Date testingDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("testingDate", testingDate);

        // 結果返却
        return namedJdbcTemplate.update(DCP_ASSIGN_STATUS_DELETE_QUERY, parameter);
    }

    /**
     * 視察ミッション確認.<br>
     * ミッション初日に検体種別がその他のデータがあるかどうか確認する。<br>
     * 
     * @param missionKey   ミッション基本情報KEY
     * @return ユーザIDリスト
     */
    public List<Mission> isInspectionMission(@Param("missionKey") Integer missionKey) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<Mission> mapper = new BeanPropertyRowMapper<>(Mission.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);

        // 結果返却
        return namedJdbcTemplate.query(INSPECTION_MISSION_CONFIRM_QUERY, parameter, mapper);
    }

    /**
     * DCP選定情報取得.<br>
     * 日付、ミッション基本情報KEY、役割区分に該当するユーザIDリストを取得する。<br>
     * 
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getSelectIctUserList(@Param("assignedDate") Date assignedDate,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(DCP_SELECT_ICT_QUERY, parameter, mapper);
    }

    /**
     * 視察ミッション更新.<br>
     * 指定ユーザを視察ミッションの初日の役割区分をリードDCOから視察に変更する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param DcpRoleTypeDcoLead リードDCO役割区分
     * @param DcpRoleTypeInspection 視察役割区分
     * @return 登録件数
     */
    public int updateInspectionMission(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleTypeDcoLead") String dcpRoleTypeDcoLead,
            @Param("dcpRoleTypeInspection") String dcpRoleTypeInspection) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleTypeDcoLead", dcpRoleTypeDcoLead);
        parameter.put("dcpRoleTypeInspection", dcpRoleTypeInspection);

        // 結果返却
        return namedJdbcTemplate.update(INSPECTION_MISSION_UPDATE, parameter);
    }

    /**
     * 指定ユーザDCP選定情報(ICT)削除.<br>
     * 指定ユーザ、日付、ミッション基本情報KEY、役割区分のDCP選定情報(ICT)をする。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param DcpRoleTypeDcoLead リードDCO役割区分
     * @param DcpRoleTypeInspection 視察役割区分
     * @return 削除件数
     */
    public int deleteDcpSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleTypeDcoLead") String dcpRoleTypeDcoLead) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleTypeDcoLead", dcpRoleTypeDcoLead);

        // 結果返却
        return namedJdbcTemplate.update(DCP_SELECT_ICT_DELETE, parameter);
    }

    /**
     * 指定ユーザDCP選定情報(ICT)更新.<br>
     * 指定ユーザ、日付、ミッション基本情報KEY、役割区分のDCP選定情報(ICT)の有力候補フラグを更新する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @return 更新件数
     */
    public int updateIsStrongCandidateFalse(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);

        // 結果返却
        return namedJdbcTemplate.update(IS_STRONG_CANDIDATE_UPDATE, parameter);
    }
}
