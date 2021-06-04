//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractBcoRepositoryImpl.java
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.dto.ComponentRatioHighDto;
import jp.co.seiko_sol.dto.ComponentRatioLowDto;
import jp.co.seiko_sol.dto.ComponentRatioOoctDto;
import jp.co.seiko_sol.dto.DcpSelectInfoDto;
import jp.co.seiko_sol.dto.UrineSampleCountDto;
import jp.co.seiko_sol.dto.User;

/**
 * DCO選出処理アクセス用のRepositoryクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class SubExtractDcoRepositoryImpl implements SubExtractDcoRepository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** DCO選出対象ミッション（ICT)取得SQL定義 */
    private static final String DCO_MISSION_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by "                                                                   +
            "FROM "                                                                               +
            "    t_mission_sort_ict A "                                                           +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.samples_urine > 0 AND "                                                    +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    B.testing_type IN ( '11', '12', '13' ) AND "                                     +
            "    B.mission_status = '12' "                                                        +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** ICT側DCO参加可能対象者取得SQL定義 */
    private static final String ICT_PARTICIPANTS_DCO_QUERY =
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
            "        D.dcp_rank IN ( '1', '2', '3', '4', '5' ) AND "                              +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            //DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 尿検体数取得SQL */
    private static final String URINE_SAMPLE_COUNT_QUERY =
            "SELECT "                                                                             +
            "    SUM( COALESCE( A.competitors_male, 0 ) ) as male_competitors, "                  +
            "    SUM( COALESCE( A.competitors_female, 0 ) ) as female_competitors, "              +
            "    SUM( COALESCE( A.competitors_male, 0 ) ) as male_urine_sample, "                 +
            "    SUM( COALESCE( A.competitors_female, 0 ) ) as female_urine_sample "              +
            "FROM "                                                                               +
            "    t_sample_analysis_plan A "                                                       +
            "    INNER JOIN ( "                                                                   +
            "        SELECT "                                                                     +
            "            C.mission_key, "                                                         +
            "            C.testing_date, "                                                        +
            "            C.group_seq "                                                            +
            "        FROM "                                                                       +
            "            ( "                                                                      +
            "                SELECT DISTINCT "                                                    +
            "                    D.mission_key, "                                                 +
            "                    D.testing_date, "                                                +
            "                    D.group_seq "                                                    +
            "                FROM "                                                               +
            "                    t_sample_analysis_plan D "                                       +
            "                WHERE "                                                              +
            "                    D.sample_type = '1' AND "                                        +
            "                    D.mission_key = :missionKey AND "                                +
            "                    D.testing_date = CAST(:testingDate as DATE) AND "                +
            "                    D.is_deleted = false "                                           +
            "                ORDER BY "                                                           +
            "                    D.mission_key, "                                                 +
            "                    D.testing_date, "                                                +
            "                    D.group_seq "                                                    +
            "            ) C "                                                                    +
            "        GROUP BY "                                                                   +
            "            C.mission_key, "                                                         +
            "            C.testing_date, "                                                        +
            "            C.group_seq "                                                            +
            "    ) B ON "                                                                         +
            "        A.mission_key = B.mission_key AND "                                          +
            "        A.testing_date = B.testing_date AND "                                        +
            "        A.group_seq = B.group_seq AND "                                              +
            "        A.sample_seq = 0 AND "                                                       +
            "        A.is_deleted = false "                                                       +
            "GROUP BY "                                                                           +
            "    A.mission_key, "                                                                 +
            "    A.testing_date ";

    /** 尿検体数取得SQL(OOCT用) */
    private static final String OOCT_URINE_SAMPLE_COUNT_QUERY =
            "SELECT "                                                                             +
            "    SUM( COALESCE( A.competitors_male, 0 ) ) as male_competitors, "                  +
            "    SUM( COALESCE( A.competitors_female, 0 ) ) as female_competitors, "              +
            "    SUM( COALESCE( A.competitors_male, 0 ) ) as male_urine_sample, "                 +
            "    SUM( COALESCE( A.competitors_female, 0 ) ) as female_urine_sample "              +
            "FROM "                                                                               +
            "    t_sample_analysis_plan A "                                                       +
            "    INNER JOIN ( "                                                                   +
            "        SELECT "                                                                     +
            "            C.mission_key, "                                                         +
            "            C.group_seq "                                                            +
            "        FROM "                                                                       +
            "            ( "                                                                      +
            "                SELECT DISTINCT "                                                    +
            "                    D.mission_key, "                                                 +
            "                    D.group_seq "                                                    +
            "                FROM "                                                               +
            "                    t_sample_analysis_plan D "                                       +
            "                WHERE "                                                              +
            "                    D.sample_type = '1' AND "                                        +
            "                    D.mission_key = :missionKey AND "                                +
            "                    D.is_deleted = false "                                           +
            "                ORDER BY "                                                           +
            "                    D.mission_key, "                                                 +
            "                    D.group_seq "                                                    +
            "            ) C "                                                                    +
            "        GROUP BY "                                                                   +
            "            C.mission_key, "                                                         +
            "            C.group_seq "                                                            +
            "    ) B ON "                                                                         +
            "        A.mission_key = B.mission_key AND "                                          +
            "        A.group_seq = B.group_seq AND "                                              +
            "        A.sample_seq = 0 AND "                                                       +
            "        A.is_deleted = false "                                                       +
            "GROUP BY "                                                                           +
            "    A.mission_key ";

    /** DCP選定情報（ICT）昇順取得SQL定義 */
    private static final String DCO_ICT_IS_STRONG_CANDIDATE_QUERY_ASC =
            "SELECT "                                                                             + 
            "    COALESCE(D.user_id, NULL) as beforeregist, "                                     +
            "    B.certification_no, "                                                            +
            "    A.user_id, "                                                                     +
            "    B.gender_type, "                                                                 +
            "    C.dcp_rank, "                                                                    + 
            "    H.priority_seq, "                                                                +
            "    A.is_met_condition3 "                                                            +
            "FROM "                                                                               +
            "    t_dcp_select_ict A "                                                             +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_qualification C ON "                                            +
            "        C.user_id = B.user_id AND "                                                  +
            "        C.is_deleted = FALSE "                                                       +
            "    LEFT OUTER JOIN t_dcp_provisional D ON "                                         +
            "        D.mission_key = A.mission_key AND "                                          +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.assigned_date < :assignedDate AND "                                        +
            "        D.time_slot_type = A.time_slot_type AND "                                    +
            "        D.dcp_role_type = A.dcp_role_type "                                          +
            "    INNER JOIN t_mission_base E ON "                                                 +
            "        E.mission_key = A.mission_key AND "                                          +
            "        E.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture F ON "                                                   +
            "        F.prefecture_code = E.venue_prefecture_code AND "                            +
            "        F.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture G ON "                                                   +
            "        G.prefecture_code = B.prefecture_code1 AND "                                 +
            "        G.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority H ON "                                            +
            "        H.work_area_id = F.area_id AND "                                             +
            "        H.home_area_id = G.area_id AND "                                             +
            "        H.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_strong_candidate = true AND "                                               +
            "    A.is_deleted = false "                                                           +
            "GROUP BY "                                                                           +
            "    D.user_id, "                                                                     +
            "    B.certification_no, "                                                            +
            "    A.user_id, "                                                                     +
            "    B.gender_type, "                                                                 +
            "    C.dcp_rank, "                                                                    +
            "    H.priority_seq, "                                                                +
            "    A.is_met_condition3 "                                                            +
            "ORDER BY "                                                                           +
            "    beforeRegist NULLS LAST, "                                                       +
            "    H.priority_seq, "                                                                +
            "    A.is_met_condition3, "                                                           +
            "    B.certification_no ";

    /** DCP選定情報（ICT）降順取得SQL定義 */
    private static final String DCO_ICT_IS_STRONG_CANDIDATE_QUERY_DESC =
            "SELECT "                                                                             + 
            "    COALESCE(D.user_id, NULL) as beforeregist, "                                     +
            "    B.certification_no, "                                                            +
            "    A.user_id, "                                                                     +
            "    B.gender_type, "                                                                 +
            "    C.dcp_rank, "                                                                    +
            "    H.priority_seq, "                                                                +
            "    A.is_met_condition3 "                                                            +
            "FROM "                                                                               +
            "    t_dcp_select_ict A "                                                             +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_qualification C ON "                                            +
            "        C.user_id = B.user_id AND "                                                  +
            "        C.is_deleted = FALSE "                                                       +
            "    LEFT OUTER JOIN t_dcp_provisional D ON "                                         +
            "        D.mission_key = A.mission_key AND "                                          +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.assigned_date < :assignedDate AND "                                        +
            "        D.time_slot_type = A.time_slot_type AND "                                    +
            "        D.dcp_role_type = A.dcp_role_type "                                          +
            "    INNER JOIN t_mission_base E ON "                                                 +
            "        E.mission_key = A.mission_key AND "                                          +
            "        E.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture F ON "                                                   +
            "        F.prefecture_code = E.venue_prefecture_code AND "                            +
            "        F.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture G ON "                                                   +
            "        G.prefecture_code = B.prefecture_code1 AND "                                 +
            "        G.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority H ON "                                            +
            "        H.work_area_id = F.area_id AND "                                             +
            "        H.home_area_id = G.area_id AND "                                             +
            "        H.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_strong_candidate = true AND "                                               +
            "    A.is_deleted = false "                                                           +
            "GROUP BY "                                                                           +
            "    D.user_id, "                                                                     +
            "    B.certification_no, "                                                            +
            "    A.user_id, "                                                                     +
            "    B.gender_type, "                                                                 +
            "    C.dcp_rank, "                                                                    + 
            "    H.priority_seq, "                                                                +
            "    A.is_met_condition3 "                                                            +
            "ORDER BY "                                                                           +
            "    beforeRegist DESC NULLS FIRST, "                                                 +
            "    H.priority_seq DESC, "                                                           +
            "    A.is_met_condition3 DESC, "                                                      +
            "    B.certification_no ";

    /** 競技区分取得SQL定義 */
    private static final String MISSION_DISCIPLINE_TYPE_QUERY =
            "SELECT "                                                                             +
            "    B.discipline_type "                                                              +
            "FROM "                                                                               +
            "    t_mission_base A "                                                               +
            "    INNER JOIN m_discipline B ON "                                                   +
            "        B.discipline_id = A.discipline_id AND "                                      +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.is_deleted = false ";

    /** DCPランク構成比（ハイリスク用）取得SQL定義 */
    private static final String DCO_COMPONENT_RATIO_HIGH_QUERY =
            "SELECT "                                                                             +
            "    round(:asssignedDco * component_ratio_s1 / 100.00) AS asssigned_s1, "            +
            "    round(:asssignedDco * component_ratio_s2 / 100.00) AS asssigned_s2, "            +
            "    round(:asssignedDco * component_ratio_s3 / 100.00) AS asssigned_s3, "            +
            "    round(:asssignedDco * component_ratio_a1 / 100.00) AS asssigned_a1 "             +
            "FROM "                                                                               +
            "    m_dcp_component_high "                                                           +
            "WHERE "                                                                              +
            "    discipline_type = :disciplineType ";

    /** DCPランク構成比（ローリスク用）取得SQL定義 */
    private static final String DCO_COMPONENT_RATIO_LOW_QUERY =
            "SELECT "                                                                             +
            "    round(:asssignedDco * component_ratio_s1s3 / 100.00) AS asssigned_s1s3, "        +
            "    round(:asssignedDco * component_ratio_a1 / 100.00) AS asssigned_a1, "            +
            "    round(:asssignedDco * component_ratio_a2 / 100.00) AS asssigned_a2 "             +
            "FROM "                                                                               +
            "    m_dcp_component_low "                                                            +
            "WHERE "                                                                              +
            "    discipline_type = :disciplineType ";

    /** DCP選定情報（ICT） --> DCP仮確定情報登録SQL定義 */
    private static final String DCP_SELECT_ICT_TO_DCP_PROVISIONAL_QUERY =
            "INSERT INTO t_dcp_provisional( "                                                     +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    time_slot_type, "                                                                +
            "    mission_key, "                                                                   +
            "    dcp_role_type, "                                                                 +
            "    is_strong_candidate, "                                                           +
            "    conditions_score, "                                                              +
            "    any_condition1, "                                                                +
            "    is_met_condition1, "                                                             +
            "    any_condition2, "                                                                +
            "    is_met_condition2, "                                                             +
            "    any_condition3, "                                                                +
            "    is_met_condition3, "                                                             +
            "    any_condition4, "                                                                +
            "    is_met_condition4, "                                                             +
            "    any_condition5, "                                                                +
            "    is_met_condition5, "                                                             +
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            ") "                                                                                  +
            "SELECT "                                                                             +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    time_slot_type, "                                                                +
            "    mission_key, "                                                                   +
            "    dcp_role_type, "                                                                 +
            "    is_strong_candidate, "                                                           +
            "    conditions_score, "                                                              +
            "    any_condition1, "                                                                +
            "    is_met_condition1, "                                                             +
            "    any_condition2, "                                                                +
            "    is_met_condition2, "                                                             +
            "    any_condition3, "                                                                +
            "    is_met_condition3, "                                                             +
            "    any_condition4, "                                                                +
            "    is_met_condition4, "                                                             +
            "    any_condition5, "                                                                +
            "    is_met_condition5, "                                                             +
            "    :createdAt, "                                                                    +
            "    :createdBy "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_select_ict "                                                               +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCO選出対象ミッション（OOCT)取得SQL定義 */
    private static final String DCO_MISSION_OOCT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by "                                                                   +
            "FROM "                                                                               +
            "    t_mission_sort_ooct A "                                                          +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.samples_urine > 0 AND "                                                    +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    B.testing_type IN ( '21' ) AND "                                                 +
            "    B.mission_status = '12' "                                                        +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** OOCT側DCO参加可能対象者（終日）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_ALLDAY_DCO_QUERY =
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
            "        D.dcp_rank IN ( '1', '2', '3', '4', '5' ) AND "                              +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            "    A.is_remarks_written = false AND "                                               +
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側DCO参加可能対象者（早朝）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EARLY_DCO_QUERY =
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
            "        D.dcp_rank IN ( '1', '2', '3', '4', '5' ) AND "                              +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_early = true AND "                                                   +
            // DCPMG#144 備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側DCO参加可能対象者（AM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_MORNING_DCO_QUERY =
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
            "        D.dcp_rank IN ( '1', '2', '3', '4', '5' ) AND "                              +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_morning = true AND "                                                 +
            // DCPMG#144 備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側DCO参加可能対象者（PM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_AFTERNOON_DCO_QUERY =
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
            "        D.dcp_rank IN ( '1', '2', '3', '4', '5' ) AND "                              +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_afternoon = true AND "                                               +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側DCO参加可能対象者（夜間）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EVENING_DCO_QUERY =
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
            "        D.dcp_rank IN ( '1', '2', '3', '4', '5' ) AND "                              +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_evening = true AND "                                                 +
            // DCPMG#144 備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** DCP選定情報（OOCT）取得SQL定義 */
    private static final String DCO_OOCT_IS_STRONG_CANDIDATE_QUERY_ASC =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    B.gender_type, "                                                                 +
            "    C.dcp_rank, "                                                                    + 
            "    G.priority_seq "                                                                 +
            "FROM "                                                                               +
            "    t_dcp_select_ooct A "                                                            +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_qualification C ON "                                            +
            "        C.user_id = B.user_id AND "                                                  +
            "        C.is_deleted = FALSE "                                                       +
            "    INNER JOIN t_mission_base D ON "                                                 +
            "        D.mission_key = A.mission_key AND "                                          +
            "        D.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture E ON "                                                   +
            "        E.prefecture_code = D.venue_prefecture_code AND "                            +
            "        E.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture F ON "                                                   +
            "        F.prefecture_code = B.prefecture_code1 AND "                                 +
            "        F.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority G ON "                                            +
            "        G.work_area_id = E.area_id AND "                                             +
            "        G.home_area_id = F.area_id AND "                                             +
            "        G.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.time_slot_type = :timeSlotType AND "                                           +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_strong_candidate = true AND "                                               +
            "    A.is_deleted = false "                                                           +
            "ORDER BY "                                                                           +
            "    G.priority_seq, "                                                                +
            "    B.certification_no ";

    /** DCP選定情報（OOCT）取得SQL定義 */
    private static final String DCO_OOCT_IS_STRONG_CANDIDATE_QUERY_DESC =
            "SELECT "                                                                             +
            "    A.user_id, "                                                                     +
            "    B.gender_type, "                                                                 +
            "    C.dcp_rank, "                                                                    + 
            "    G.priority_seq "                                                                 +
            "FROM "                                                                               +
            "    t_dcp_select_ooct A "                                                            +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_qualification C ON "                                            +
            "        C.user_id = B.user_id AND "                                                  +
            "        C.is_deleted = FALSE "                                                       +
            "    INNER JOIN t_mission_base D ON "                                                 +
            "        D.mission_key = A.mission_key AND "                                          +
            "        D.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture E ON "                                                   +
            "        E.prefecture_code = D.venue_prefecture_code AND "                            +
            "        E.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture F ON "                                                   +
            "        F.prefecture_code = B.prefecture_code1 AND "                                 +
            "        F.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority G ON "                                            +
            "        G.work_area_id = E.area_id AND "                                             +
            "        G.home_area_id = F.area_id AND "                                             +
            "        G.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.time_slot_type = :timeSlotType AND "                                           +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_strong_candidate = true AND "                                               +
            "    A.is_deleted = false "                                                           +
            "ORDER BY "                                                                           +
            "    G.priority_seq DESC, "                                                           +
            "    B.certification_no ";

    /** DCPランク構成比（OOCT用）取得SQL定義 */
    private static final String DCO_COMPONENT_RATIO_OOCT_QUERY =
            "SELECT "                                                                             +
            "    ceil( :asssignedDco * ( component_ratio_upper / 100.00 ) ) AS asssigned_upper, " +
            "    :asssignedDco - ( "                                                              +
            "        ceil( :asssignedDco * ( component_ratio_upper / 100.00 ) ) "                 +
            "    ) AS asssigned_a2 "                                                              +
            "FROM "                                                                               +
            "    m_dcp_component_ooct A ";

    /** DCP選定情報（ICT） --> DCP仮確定情報登録SQL定義 */
    private static final String DCP_SELECT_OOCT_TO_DCP_PROVISIONAL_QUERY =
            "INSERT INTO t_dcp_provisional( "                                                     +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    time_slot_type, "                                                                +
            "    mission_key, "                                                                   +
            "    dcp_role_type, "                                                                 +
            "    is_strong_candidate, "                                                           +
            "    conditions_score, "                                                              +
            "    any_condition1, "                                                                +
            "    is_met_condition1, "                                                             +
            "    any_condition2, "                                                                +
            "    is_met_condition2, "                                                             +
            "    any_condition3, "                                                                +
            "    is_met_condition3, "                                                             +
            "    any_condition4, "                                                                +
            "    is_met_condition4, "                                                             +
            "    any_condition5, "                                                                +
            "    is_met_condition5, "                                                             +
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            ") "                                                                                  +
            "SELECT "                                                                             +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    time_slot_type, "                                                                +
            "    mission_key, "                                                                   +
            "    dcp_role_type, "                                                                 +
            "    is_strong_candidate, "                                                           +
            "    conditions_score, "                                                              +
            "    any_condition1, "                                                                +
            "    is_met_condition1, "                                                             +
            "    any_condition2, "                                                                +
            "    is_met_condition2, "                                                             +
            "    any_condition3, "                                                                +
            "    is_met_condition3, "                                                             +
            "    any_condition4, "                                                                +
            "    is_met_condition4, "                                                             +
            "    any_condition5, "                                                                +
            "    is_met_condition5, "                                                             +
            "    :createdAt, "                                                                    +
            "    :createdBy "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_select_ooct "                                                              +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /**
     * DCO選出対象ミッションリスト（ICT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、DCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return DCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getDcoAssignmentMissionsIct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortIct> mapper = new BeanPropertyRowMapper<>(TMissionSortIct.class);

        // 結果返却
        return namedJdbcTemplate.query(DCO_MISSION_ICT_QUERY, parameter, mapper);
    }

    /**
     * ICT側DCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(ICT_PARTICIPANTS_DCO_QUERY, parameter, mapper);
    }

    /**
     * 尿検体数数取得.<br>
     * ミッション基本情報KEY、検査日の条件よりデータを抽出し、尿検体の数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 尿検体数情報DTO
     */
    public List<UrineSampleCountDto> getUrineSampleCount(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<UrineSampleCountDto> mapper =
                new BeanPropertyRowMapper<>(UrineSampleCountDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("testingDate", testingDate);

        // 結果返却
        return namedJdbcTemplate.query(URINE_SAMPLE_COUNT_QUERY, parameter, mapper);
    }

    /**
     * 尿検体数数取得(OOCT用).<br>
     * ミッション基本情報KEYの条件よりデータを抽出し、尿検体の数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @return 尿検体数情報DTO
     */
    public List<UrineSampleCountDto> getOoctUrineSampleCount(@Param("missionKey") Integer missionKey) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<UrineSampleCountDto> mapper =
                new BeanPropertyRowMapper<>(UrineSampleCountDto.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_URINE_SAMPLE_COUNT_QUERY, parameter, mapper);
    }

    /**
     * DCP選定情報（ICT）昇順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（ICT）にグルーピング用情報の性別、DCPランクを付加して昇順で取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoIctIsStrongCandidateAsc(
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<DcpSelectInfoDto> mapper = new BeanPropertyRowMapper<>(DcpSelectInfoDto.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(DCO_ICT_IS_STRONG_CANDIDATE_QUERY_ASC, parameter, mapper);
    }

    /**
     * DCP選定情報（ICT）降順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（ICT）にグルーピング用情報の性別、DCPランクを付加して降順で取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoIctIsStrongCandidateDesc(
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<DcpSelectInfoDto> mapper = new BeanPropertyRowMapper<>(DcpSelectInfoDto.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(DCO_ICT_IS_STRONG_CANDIDATE_QUERY_DESC, parameter, mapper);
    }

    /**
     * 競技区分取得処理.<br>
     * ミッションに紐付く競技区分を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @return 競技区分
     */
    public String getMissionDisciplineType(@Param("missionKey") Integer missionKey) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("missionKey", missionKey);

            // 結果返却
            return namedJdbcTemplate.queryForObject(MISSION_DISCIPLINE_TYPE_QUERY, parameter, String.class);

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * DCPランク構成比（ハイリスク用）取得処理.<br>
     * 競技区分によるDCOランク毎の人数を取得して返却する。
     * 
     * @param asssignedDco DCO人数
     * @param disciplineType 競技区分
     * @return DCPランク構成比（ハイリスク用）情報
     */
    public List<ComponentRatioHighDto> getDcoComponentRatiohigh(
            @Param("asssignedDco") Integer asssignedDco,
            @Param("disciplineType") String disciplineType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ComponentRatioHighDto> mapper =
                new BeanPropertyRowMapper<>(ComponentRatioHighDto.class);
        // パラメータ設定
        parameter.put("asssignedDco", asssignedDco);
        parameter.put("disciplineType", disciplineType);

        // 結果返却
        return namedJdbcTemplate.query(DCO_COMPONENT_RATIO_HIGH_QUERY, parameter, mapper);
    }

    /**
     * DCPランク構成比（ローリスク用）取得処理.<br>
     * 競技区分によるDCOランク毎の人数を取得して返却する。
     * 
     * @param asssignedDco DCO人数
     * @param disciplineType 競技区分
     * @return DCPランク構成比（ローリスク用）情報
     */
    public List<ComponentRatioLowDto> getDcoComponentRatioLow(
            @Param("asssignedDco") Integer asssignedDco,
            @Param("disciplineType") String disciplineType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ComponentRatioLowDto> mapper =
                new BeanPropertyRowMapper<>(ComponentRatioLowDto.class);
        // パラメータ設定
        parameter.put("asssignedDco", asssignedDco);
        parameter.put("disciplineType", disciplineType);

        // 結果返却
        return namedJdbcTemplate.query(DCO_COMPONENT_RATIO_LOW_QUERY, parameter, mapper);
    }

    /**
     * DCP仮確定情報登録処理.<br>
     * DCP選定情報（ICT） を参照してDCP仮確定情報に登録する処理。
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     */
    public void insertDcpProvisionalFromSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType, @Param("createdAt") Timestamp createdAt,
            @Param("createdBy") Integer createdBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);

        // 実行
        namedJdbcTemplate.update(DCP_SELECT_ICT_TO_DCP_PROVISIONAL_QUERY, parameter);
    }

    /**
     * DCO選出対象ミッションリスト（OOCT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、DCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return DCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getDcoAssignmentMissionsOoct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortOoct> mapper = new BeanPropertyRowMapper<>(TMissionSortOoct.class);

        // 結果返却
        return namedJdbcTemplate.query(DCO_MISSION_OOCT_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListAlldayOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_ALLDAY_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListEarlyOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EARLY_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListMorningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_MORNING_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_AFTERNOON_DCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListEveningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EVENING_DCO_QUERY, parameter, mapper);
    }

    /**
     * DCP選定情報（OOCT）昇順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（OOCT）にグルーピング用情報の性別、DCPランクを付加して昇順取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoOoctIsStrongCandidateAsc(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<DcpSelectInfoDto> mapper = new BeanPropertyRowMapper<>(DcpSelectInfoDto.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(DCO_OOCT_IS_STRONG_CANDIDATE_QUERY_ASC, parameter, mapper);
    }

    /**
     * DCP選定情報（OOCT）降順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（OOCT）にグルーピング用情報の性別、DCPランクを付加して降順取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoOoctIsStrongCandidateDesc(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<DcpSelectInfoDto> mapper = new BeanPropertyRowMapper<>(DcpSelectInfoDto.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(DCO_OOCT_IS_STRONG_CANDIDATE_QUERY_DESC, parameter, mapper);
    }

    /**
     * DCPランク構成比（OOCT用）取得処理.<br>
     * DCOランク毎の人数を取得して返却する。
     * 
     * @param asssignedDco DCO人数
     * @param disciplineType 競技区分
     * @return DCPランク構成比（OOCT用）情報
     */
    public List<ComponentRatioOoctDto> getDcoComponentRatioOoct(
            @Param("asssignedDco") Integer asssignedDco) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ComponentRatioOoctDto> mapper =
                new BeanPropertyRowMapper<>(ComponentRatioOoctDto.class);
        // パラメータ設定
        parameter.put("asssignedDco", asssignedDco);

        // 結果返却
        return namedJdbcTemplate.query(DCO_COMPONENT_RATIO_OOCT_QUERY, parameter, mapper);
    }

    /**
     * DCP仮確定情報登録処理.<br>
     * DCP選定情報（OOCT） を参照してDCP仮確定情報に登録する処理。
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     */
    public void insertDcpProvisionalFromSelectOoct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);

        // 実行
        namedJdbcTemplate.update(DCP_SELECT_OOCT_TO_DCP_PROVISIONAL_QUERY, parameter);
    }
}
