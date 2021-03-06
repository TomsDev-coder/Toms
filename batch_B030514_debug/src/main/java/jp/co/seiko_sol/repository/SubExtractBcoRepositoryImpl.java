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
import jp.co.seiko_sol.dto.Count;
import jp.co.seiko_sol.dto.User;

/**
 * BCO選出処理アクセス用のRepositoryクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class SubExtractBcoRepositoryImpl implements SubExtractBcoRepository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** BCO選出対象ミッション（ICT)取得SQL定義 */
    private static final String BCO_MISSION_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by "                                                                   +
            "FROM "                                                                               +
            "    t_mission_sort_ict A "                                                           +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        ( "                                                                          +
            "            B.samples_blood_y > 0 OR "                                               +
            "            B.samples_blood_p > 0 "                                                  +
            "        ) AND "                                                                      +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    B.testing_type IN ( '11', '12', '13' ) AND "                                     +
            "    B.mission_status = '12' "                                                        +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** ICT側管理者BCO参加可能対象者取得SQL定義 */
    private static final String ICT_PARTICIPANTS_BCO_ADMIN_QUERY =
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
            "        D.is_bco_admin = true AND "                                                  +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** 本年度アサイン回数取得SQL */
    private static final String ASSIGNMENTS_THIS_YEAR_QUERY =
            "SELECT "                                                                             +
            "    COUNT(C.assigned_date) as count "                                                +
            "FROM "                                                                               +
            "( "                                                                                  +
            "    SELECT "                                                                         +
            "        A.assigned_date "                                                            +
            "    FROM "                                                                           +
            "        t_dcp_assign A "                                                             +
            "        INNER JOIN ( "                                                               +
            "            SELECT "                                                                 +
            "                CASE "                                                               +
            "                    WHEN SUB.CURRENT_MONTH < 4 THEN "                                +
            "                        make_date(CAST(SUB.CURRENT_YEAR as INT ) - 1, 4, 1) "        +
            "                    WHEN SUB.CURRENT_MONTH > 3 THEN "                                + 
            "                        make_date(CAST(SUB.CURRENT_YEAR as INT ), 4, 1) "            +
            "                END as FROM_DATE, "                                                  +
            "                CASE "                                                               +
            "                    WHEN SUB.CURRENT_MONTH < 4 THEN "                                +
            "                        make_date(CAST(SUB.CURRENT_YEAR as INT ), 3, 31) "           +
            "                    WHEN SUB.CURRENT_MONTH > 3 THEN "                                +
            "                        make_date(CAST(SUB.CURRENT_YEAR as INT ) + 1, 3, 31) "       +
            "                END as TO_DATE "                                                     +
            "            FROM "                                                                   +
            "                    ( "                                                              +
            "                    SELECT "                                                         +
            "                        EXTRACT(MONTH FROM CURRENT_DATE) as CURRENT_MONTH, "         +
            "                        EXTRACT(YEAR FROM CURRENT_DATE) as CURRENT_YEAR "            +
            "                ) SUB "                                                              +
            "        ) B ON "                                                                     +
            "            A.assigned_date >= B.FROM_DATE AND "                                     +
            "            A.assigned_date <= B.TO_DATE "                                           +
            "    WHERE "                                                                          +
            "        A.user_id = :userId "                                                        +
            "    GROUP BY "                                                                       +
            "        A.user_id, "                                                                 +
            "        A.assigned_date "                                                            +
            ") C ";

    /** 血液検体数取得SQL */
    private static final String BLOOD_SAMPLE_COUNT_QUERY =
            "SELECT "                                                                             +
            "    SUM( COALESCE( A.competitors_male, 0 ) ) + "                                     +
            "    SUM( COALESCE( A.competitors_female, 0 ) ) as count "                            +
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
            "                    D.sample_type IN ( '3', '4' ) AND "                              +
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

    /** ICT側BCO参加可能対象者取得SQL定義 */
    private static final String ICT_PARTICIPANTS_BCO_QUERY =
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
            "        D.is_bco = true AND "                                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** BCO選出対象ミッション（OOCT)取得SQL定義 */
    private static final String BCO_MISSION_OOCT_QUERY =
            "SELECT "                                                                             +
            "    A.sort_id, "                                                                     +
            "    A.mission_key, "                                                                 +
            "    A.created_at, "                                                                  +
            "    A.created_by "                                                                   +
            "FROM "                                                                               +
            "    t_mission_sort_ooct A "                                                          +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        ( "                                                                          +
            "            B.samples_blood_y > 0 OR "                                               +
            "            B.samples_blood_p > 0 "                                                  +
            "        ) AND "                                                                      +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    B.testing_type IN ( '21' ) AND "                                                 +
            "    B.mission_status = '12' "                                                        +
            "ORDER BY "                                                                           +
            "    A.sort_id ";

    /** OOCT側管理者BCO参加可能対象者（終日）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_ALLDAY_BCO_ADMIN_QUERY =
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
            "        D.is_bco_admin = true AND "                                                  +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側管理者BCO参加可能対象者（早朝）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EARLY_BCO_ADMIN_QUERY =
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
            "        D.is_bco_admin = true AND "                                                  +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_early = true AND "                                                   +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側管理者BCO参加可能対象者（AM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_MORNING_BCO_ADMIN_QUERY =
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
            "        D.is_bco_admin = true AND "                                                  +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_morning = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側管理者BCO参加可能対象者（PM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_AFTERNOON_BCO_ADMIN_QUERY =
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
            "        D.is_bco_admin = true AND "                                                  +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_afternoon = true AND "                                               +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側管理者BCO参加可能対象者（夜間）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EVENING_BCO_ADMIN_QUERY =
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
            "        D.is_bco_admin = true AND "                                                  +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_evening = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側BCO参加可能対象者（終日）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_ALLDAY_BCO_QUERY =
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
            "        D.is_bco = true AND "                                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_allday = true AND "                                                  +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側BCO参加可能対象者（早朝）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EARLY_BCO_QUERY =
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
            "        D.is_bco = true AND "                                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_early = true AND "                                                   +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側BCO参加可能対象者（AM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_MORNING_BCO_QUERY =
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
            "        D.is_bco = true AND "                                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_morning = true AND "                                                 +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側BCO参加可能対象者（PM）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_AFTERNOON_BCO_QUERY =
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
            "        D.is_bco = true AND "                                                        +
            "        D.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.is_attend_afternoon = true AND "                                               +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "    A.is_deleted = false AND "                                                       +
            "    C.user_id is NULL "                                                              +
            "ORDER BY "                                                                           +
            "    A.user_id ";

    /** OOCT側BCO参加可能対象者（夜間）取得SQL定義 */
    private static final String OOCT_PARTICIPANTS_EVENING_BCO_QUERY =
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
            "        D.is_bco = true AND "                                                        +
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
     * BCO選出対象ミッションリスト（ICT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、BCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return BCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getBcoAssignmentMissionsIct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortIct> mapper = new BeanPropertyRowMapper<>(TMissionSortIct.class);

        // 結果返却
        return namedJdbcTemplate.query(BCO_MISSION_ICT_QUERY, parameter, mapper);
    }

    /**
     * ICT側管理者BCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(ICT_PARTICIPANTS_BCO_ADMIN_QUERY, parameter, mapper);
    }

    /**
     * 本年度アサイン回数取得SQL取得.<br>
     * 指定ユーザが本年度アサインされた回数を取得して返却する。
     * 
     * @param userId ユーザID
     * @return アサイン回数
     */
    public List<Count> getAssignmentsThisYearCount(@Param("userId") Integer userId) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<Count> mapper = new BeanPropertyRowMapper<>(Count.class);
        // パラメータ設定
        parameter.put("userId", userId);

        // 結果返却
        return namedJdbcTemplate.query(ASSIGNMENTS_THIS_YEAR_QUERY, parameter, mapper);
    }

    /**
     * 血液検体数数取得.<br>
     * ミッション基本情報KEY、検査日の条件よりデータを抽出し、血液検体の数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 血液検体数
     */
    public List<Count> getBloodSampleCount(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<Count> mapper = new BeanPropertyRowMapper<>(Count.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("testingDate", testingDate);

        // 結果返却
        return namedJdbcTemplate.query(BLOOD_SAMPLE_COUNT_QUERY, parameter, mapper);
    }

    /**
     * ICT側BCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(ICT_PARTICIPANTS_BCO_QUERY, parameter, mapper);
    }

    /**
     * BCO選出対象ミッションリスト（OOCT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、BCO選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return BCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getBcoAssignmentMissionsOoct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TMissionSortOoct> mapper = new BeanPropertyRowMapper<>(TMissionSortOoct.class);

        // 結果返却
        return namedJdbcTemplate.query(BCO_MISSION_OOCT_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListAlldayOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_ALLDAY_BCO_ADMIN_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListEarlyOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EARLY_BCO_ADMIN_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListMorningOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_MORNING_BCO_ADMIN_QUERY, parameter,
                mapper);
    }

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_AFTERNOON_BCO_ADMIN_QUERY, parameter,
                mapper);
    }

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListEveningOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EVENING_BCO_ADMIN_QUERY, parameter,
                mapper);
    }

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListAlldayOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_ALLDAY_BCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListEarlyOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EARLY_BCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListMorningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_MORNING_BCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_AFTERNOON_BCO_QUERY, parameter, mapper);
    }

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListEveningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPANTS_EVENING_BCO_QUERY, parameter, mapper);
    }
}
