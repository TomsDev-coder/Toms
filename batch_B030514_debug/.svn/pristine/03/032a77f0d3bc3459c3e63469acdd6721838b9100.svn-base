//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractDcoTraineeRepositoryImpl.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.sql.Date;
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
import jp.co.seiko_sol.dto.ManualRoleUserDto;

/**
 * 手動役割選出処理アクセス用Repository.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class SubExtractManualRoleRepositoryImpl implements SubExtractManualRoleRepository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** 手動役割（研修DCO用）選出対象ミッション（ICT)取得SQL定義 */
    private static final String MANUAL_ROLE_TRAINEE_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by  "                                                                  +
            "FROM "                                                                               +
            "    t_mission_sort_ict A "                                                           +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type IN ( '11', '12', '13' ) AND "                                 +
            "        B.mission_status = '12' AND "                                                +
            "        B.is_accept_trainee = true AND "                                             +
            "        B.is_deleted = false "                                                       +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** 手動役割（その他用）選出対象ミッション（ICT)取得SQL定義 */
    private static final String MANUAL_ROLE_OTHER_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by  "                                                                  +
            "FROM "                                                                               +
            "    t_mission_sort_ict A "                                                           +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type IN ( '11', '12', '13' ) AND "                                 +
            "        B.mission_status = '12' AND "                                                +
            "        B.is_deleted = false "                                                       +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** 手動役割（研修DCO用）選出対象ミッション（OOCT)取得SQL定義 */
    private static final String MANUAL_ROLE_TRAINEE_OOCT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by  "                                                                  +
            "FROM "                                                                               +
            "    t_mission_sort_ooct A "                                                          +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type = '21' AND "                                                  +
            "        B.mission_status = '12' AND "                                                +
            "        B.is_accept_trainee = true AND "                                             +
            "        B.is_deleted = false "                                                       +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** 手動役割（その他用）選出対象ミッション（OOCT)取得SQL定義 */
    private static final String MANUAL_ROLE_OTHER_OOCT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by  "                                                                  +
            "FROM "                                                                               +
            "    t_mission_sort_ooct A "                                                          +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type = '21' AND "                                                  +
            "        B.mission_status = '12' AND "                                                +
            "        B.is_deleted = false "                                                       +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** 手動役割参加可能者（終日参加用）取得SQL定義 */
    private static final String ALLDAY_PARTICIPANTS_MANUAL_ROLE_QUERY =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    D.is_dco_senior, "                                                               +
            "    D.is_dco, "                                                                      +
            "    D.is_dco_trainee, "                                                              +
            "    D.is_bco_admin, "                                                                +
            "    D.is_bco, "                                                                      +
            "    D.is_mentor, "                                                                   +
            "    D.is_sco, "                                                                      +
            "    D.is_idco "                                                                      +
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
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 手動役割参加可能者（早朝参加用）取得SQL定義 */
    private static final String EARLY_PARTICIPANTS_MANUAL_ROLE_QUERY =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    D.is_dco_senior, "                                                               +
            "    D.is_dco, "                                                                      +
            "    D.is_dco_trainee, "                                                              +
            "    D.is_bco_admin, "                                                                +
            "    D.is_bco, "                                                                      +
            "    D.is_mentor, "                                                                   +
            "    D.is_sco, "                                                                      +
            "    D.is_idco "                                                                      +
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
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_early = true AND "                                                   +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 手動役割参加可能者（AM参加用）取得SQL定義 */
    private static final String MORNING_PARTICIPANTS_MANUAL_ROLE_QUERY =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    D.is_dco_senior, "                                                               +
            "    D.is_dco, "                                                                      +
            "    D.is_dco_trainee, "                                                              +
            "    D.is_bco_admin, "                                                                +
            "    D.is_bco, "                                                                      +
            "    D.is_mentor, "                                                                   +
            "    D.is_sco, "                                                                      +
            "    D.is_idco "                                                                      +
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
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_morning = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 手動役割参加可能者（PM参加用）取得SQL定義 */
    private static final String AFTERNOON_PARTICIPANTS_MANUAL_ROLE_QUERY =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    D.is_dco_senior, "                                                               +
            "    D.is_dco, "                                                                      +
            "    D.is_dco_trainee, "                                                              +
            "    D.is_bco_admin, "                                                                +
            "    D.is_bco, "                                                                      +
            "    D.is_mentor, "                                                                   +
            "    D.is_sco, "                                                                      +
            "    D.is_idco "                                                                      +
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
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_afternoon = true AND "                                               +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 手動役割参加可能者（夜間参加用）取得SQL定義 */
    private static final String EVENING_PARTICIPANTS_MANUAL_ROLE_QUERY =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    D.is_dco_senior, "                                                               +
            "    D.is_dco, "                                                                      +
            "    D.is_dco_trainee, "                                                              +
            "    D.is_bco_admin, "                                                                +
            "    D.is_bco, "                                                                      +
            "    D.is_mentor, "                                                                   +
            "    D.is_sco, "                                                                      +
            "    D.is_idco "                                                                      +
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
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_evening = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /**
     * 研修DCO選出対象ミッションリスト（ICT)取得.<br>
     * 研修DCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return 研修DCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getManualRoleTraineeMissionsIct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortIct> mapper = new BeanPropertyRowMapper<>(TMissionSortIct.class);

        // 結果返却
        return namedJdbcTemplate.query(MANUAL_ROLE_TRAINEE_ICT_QUERY, parameter, mapper);
    }

    /**
     * 手動役割（その他）選出対象ミッションリスト（ICT)取得.<br>
     * 手動役割（その他）選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return 手動役割（その他）選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getManualRoleOtherMissionsIct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortIct> mapper = new BeanPropertyRowMapper<>(TMissionSortIct.class);

        // 結果返却
        return namedJdbcTemplate.query(MANUAL_ROLE_OTHER_ICT_QUERY, parameter, mapper);
    }

    /**
     * 研修DCO選出対象ミッションリスト（OOCT)取得.<br>
     * 研修DCO選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return 研修DCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getManualRoleTraineeMissionsOoct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortOoct> mapper = new BeanPropertyRowMapper<>(TMissionSortOoct.class);

        // 結果返却
        return namedJdbcTemplate.query(MANUAL_ROLE_TRAINEE_OOCT_QUERY, parameter, mapper);
    }

    /**
     * 手動役割（その他）選出対象ミッションリスト（OOCT)取得.<br>
     * 手動役割（その他）選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return 手動役割（その他）選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getManualRoleOtherMissionsOoct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortOoct> mapper = new BeanPropertyRowMapper<>(TMissionSortOoct.class);

        // 結果返却
        return namedJdbcTemplate.query(MANUAL_ROLE_OTHER_OOCT_QUERY, parameter, mapper);
    }

    /**
     * 手動役割参加可能者（終日）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     *  DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsAllday(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ManualRoleUserDto> mapper = new BeanPropertyRowMapper<>(ManualRoleUserDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(ALLDAY_PARTICIPANTS_MANUAL_ROLE_QUERY, parameter, mapper);
    }

    /**
     * 手動役割参加可能者（早朝）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsEarly(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ManualRoleUserDto> mapper = new BeanPropertyRowMapper<>(ManualRoleUserDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(EARLY_PARTICIPANTS_MANUAL_ROLE_QUERY, parameter, mapper);
    }

    /**
     * 手動役割参加可能者（AM）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsMorning(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ManualRoleUserDto> mapper = new BeanPropertyRowMapper<>(ManualRoleUserDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(MORNING_PARTICIPANTS_MANUAL_ROLE_QUERY, parameter, mapper);
    }

    /**
     * 手動役割参加可能者（PM）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsAfternoon(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ManualRoleUserDto> mapper = new BeanPropertyRowMapper<>(ManualRoleUserDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(AFTERNOON_PARTICIPANTS_MANUAL_ROLE_QUERY, parameter, mapper);
    }

    /**
     * 手動役割参加可能者（夜間）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsEvening(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ManualRoleUserDto> mapper = new BeanPropertyRowMapper<>(ManualRoleUserDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(EVENING_PARTICIPANTS_MANUAL_ROLE_QUERY, parameter, mapper);
    }
}
