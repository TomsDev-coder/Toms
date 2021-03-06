//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514RepositoryImpl.java
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import jp.co.seiko_sol.domain.MDcpInformation;
import jp.co.seiko_sol.domain.MDcpQualification;
import jp.co.seiko_sol.domain.TAssignHeader;
import jp.co.seiko_sol.domain.TDcpCalendar;
import jp.co.seiko_sol.domain.TDcpManualAssign;
import jp.co.seiko_sol.domain.TDcpProvisional;
import jp.co.seiko_sol.domain.TDcpSelectIct;
import jp.co.seiko_sol.domain.TDcpSelectOoct;
import jp.co.seiko_sol.domain.TMissionBase;
import jp.co.seiko_sol.domain.TTestingDate;
import jp.co.seiko_sol.dto.AvailableDaysDto;
import jp.co.seiko_sol.dto.CompetitorsCountDto;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.ProcessDate;
import jp.co.seiko_sol.dto.User;
import jp.co.seiko_sol.mapper.AvailableDaysMapper;
import jp.co.seiko_sol.mapper.CompetitorsCountMapper;
import jp.co.seiko_sol.mapper.MDcpInformationMapper;
import jp.co.seiko_sol.mapper.MDcpQualificationMapper;
import jp.co.seiko_sol.mapper.TAssignHeaderMapper;
import jp.co.seiko_sol.mapper.TDcpCalendarMapper;
import jp.co.seiko_sol.mapper.TMissionBaseMapper;

/**
 * 自動アサインバッチ処理（ID：B030514）アクセス用のRepositoryクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class B030514RepositoryImpl implements B030514Repository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** アサインヘッダ情報取得SQL */
    private static final String ASSIGN_HEAD_INFO_QUERY =
            "SELECT "                                                                             +
            "    A.mission_key, "                                                                 +
            "    A.is_assign_fixed, "                                                             +
            "    A.is_deleted, "                                                                  +
            "    A.created_at, "                                                                  +
            "    A.created_by, "                                                                  +
            "    A.updated_at, "                                                                  +
            "    A.updated_by "                                                                   +
            "FROM "                                                                               +
            "    t_assign_header A "                                                              +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.is_deleted = FALSE ";

    /** ミッション基本情報取得SQL */
    private static final String MISSION_BASE_INFO_QUERY =
            "SELECT "                                                                             +
            "    mission_key, "                                                                   +
            "    mission_code, "                                                                  +
            "    fiscal_year, "                                                                   +
            "    testing_type, "                                                                  +
            "    mission_status, "                                                                +
            "    nf_status, "                                                                     +
            "    is_mission_plan_fixed, "                                                         +
            "    is_mission_result_fixed, "                                                       +
            "    mission_plan_fixed_at, "                                                         +
            "    is_mission_result_fixed, "                                                       +
            "    mission_result_fixed_at, "                                                       +
            "    competition_name, "                                                              +
            "    competition_name_eng, "                                                          +
            "    control_inspection_id, "                                                         +
            "    collect_samples_id, "                                                            +
            "    manage_results_id, "                                                             +
            "    adjust_inspection_id, "                                                          +
            "    competition_homepage, "                                                          +
            "    is_using_support_hospital, "                                                     +
            "    contractor_id, "                                                                 +
            "    adams_code, "                                                                    +
            "    sports_id, "                                                                     +
            "    discipline_id, "                                                                 +
            "    association_id, "                                                                +
            "    sub_association_id, "                                                            +
            "    billing_type, "                                                                  +
            "    testing_date_from, "                                                             +
            "    testing_date_to, "                                                               +
            "    testing_days, "                                                                  +
            "    test_execute_type, "                                                             +
            "    notification_time, "                                                             +
            "    meeting_time, "                                                                  +
            "    meeting_place, "                                                                 +
            "    venue_id, "                                                                      +
            "    venue_prefecture_code, "                                                         +
            "    venue_city, "                                                                    +
            "    venue_address, "                                                                 +
            "    process_rooms, "                                                                 +
            "    process_rooms_blood, "                                                           +
            "    jadacar_use_type, "                                                              +
            "    jadacar_use_date, "                                                              +
            "    user_id1, "                                                                      +
            "    user_id2, "                                                                      +
            "    emergency_user_id1, "                                                            +
            "    emergency_user_id2, "                                                            +
            "    remarks, "                                                                       +
            "    remarks_to_dcp, "                                                                +
            "    samples_urine, "                                                                 +
            "    samples_blood_y, "                                                               +
            "    samples_blood_p, "                                                               +
            "    is_accept_trainee, "                                                             +
            "    language_type, "                                                                 +
            "    kit_send_memo, "                                                                 +
            "    kit_destination_type, "                                                          +
            "    delivery_company_type1, "                                                        +
            "    kit_send_date1, "                                                                +
            "    kit_arrive_date1, "                                                              +
            "    kit_arrive_time_slot_type1, "                                                    +
            "    kit_send_postalcode1, "                                                          +
            "    kit_send_prefecture_code1, "                                                     +
            "    kit_send_address1, "                                                             +
            "    kit_send_building1, "                                                            +
            "    kit_send_organization1, "                                                        +
            "    kit_send_name1, "                                                                +
            "    kit_send_telephone1, "                                                           +
            "    kit_send_mail_to1, "                                                             +
            "    kit_send_mail_cc1, "                                                             +
            "    kit_send_mail_bcc1, "                                                            +
            "    kit_send_notices1, "                                                             +
            "    kit_send_remarks1, "                                                             +
            "    kit2_display_type, "                                                             +
            "    delivery_company_type2, "                                                        +
            "    kit_send_date2, "                                                                +
            "    kit_arrive_date2, "                                                              +
            "    kit_arrive_time_slot_type2, "                                                    +
            "    kit_send_postalcode2, "                                                          +
            "    kit_send_prefecture_code2, "                                                     +
            "    kit_send_address2, "                                                             +
            "    kit_send_building2, "                                                            +
            "    kit_send_organization2, "                                                        +
            "    kit_send_name2, "                                                                +
            "    kit_send_telephone2, "                                                           +
            "    kit_send_mail_to2, "                                                             +
            "    kit_send_mail_cc2, "                                                             +
            "    kit_send_mail_bcc2, "                                                            +
            "    kit_send_notices2, "                                                             +
            "    kit_send_remarks2, "                                                             +
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    t_mission_base "                                                                 +
            "WHERE "                                                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    is_deleted = false";

    /** 検査日別情報取得SQL */
    private static final String TESTING_DATE_INFO_QUERY =
            "SELECT "                                                                             +
            "    mission_key, "                                                                   +
            "    testing_date, "                                                                  +
            "    planned_time_start, "                                                            +
            "    planned_time_end, "                                                              +
            "    schedule_meeting_dcp, "                                                          +
            "    schedule_meeting_chaperone, "                                                    +
            "    schedule_ending, "                                                               +
            "    schedule_testing_from, "                                                         +
            "    schedule_testing_to, "                                                           +
            "    schedule_ceremony, "                                                             +
            "    chaperone_required_male, "                                                       +
            "    chaperone_required_female, "                                                     +
            "    chaperone_assigned_male, "                                                       +
            "    chaperone_assigned_female, "                                                     +
            "    chaperone_actual_male, "                                                         +
            "    chaperone_actual_female, "                                                       +
            "    nfrep_name1, "                                                                   +
            "    nfrep_contact, "                                                                 +
            "    nfrep_name2, "                                                                   +
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    t_testing_date "                                                                 +
            "WHERE "                                                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    is_deleted = false "                                                             +
            "ORDER BY "                                                                           +
            "    testing_date ";

    /** 競技者数取得SQL定義 */
    private static final String COMPETITORS_COUNT_QUERY =
            "SELECT "                                                                             +
            "    COALESCE( SUM(competitors_male), 0 ) as males, "                                 +
            "    COALESCE( SUM(competitors_female), 0 ) as females "                              +
            "FROM "                                                                               +
            "    t_sample_analysis_plan "                                                         +
            "WHERE "                                                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    testing_date = :testingDate AND "                                                +
            "    sample_type IN ( '1', '3', '4' ) AND "                                           +
            "    sample_seq = 0 AND "                                                             +
            "    is_deleted = false ";

    /** 当日DCPカレンダー情報取得SQL定義 */
    private static final String TODAY_DCP_CALENDAR =
            "SELECT "                                                                             +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    is_attend_allday, "                                                              +
            "    is_attend_early, "                                                               +
            "    is_attend_morning, "                                                             +
            "    is_attend_afternoon, "                                                           +
            "    is_attend_evening, "                                                             +
            "    is_planned_class, "                                                              +
            "    is_able_stay, "                                                                  +
            "    is_remarks_written, "                                                            +
            "    remarks, "                                                                       +
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_calendar "                                                                 +
            "WHERE "                                                                              +
            "    assigned_date = :assignedDate AND "                                              +
            "    user_id = :userId AND "                                                          +
            "    is_deleted = false ";

    /** 前日DCPカレンダー情報取得SQL定義 */
    private static final String YESTERDAY_DCP_CALENDAR =
            "SELECT "                                                                             +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    is_attend_allday, "                                                              +
            "    is_attend_early, "                                                               +
            "    is_attend_morning, "                                                             +
            "    is_attend_afternoon, "                                                           +
            "    is_attend_evening, "                                                             +
            "    is_planned_class, "                                                              +
            "    is_able_stay, "                                                                  +
            "    is_remarks_written, "                                                            +
            "    remarks, "                                                                       +
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_calendar "                                                                 +
            "WHERE "                                                                              +
            "    assigned_date = CAST(:assignedDate as DATE) - interval '1 day' AND "             +
            "    user_id = :userId AND "                                                          +
            "    is_deleted = false ";

    /** DCP評価資格情報取得SQL定義 */
    private static final String QUALIFICATION_SELECT_QUERY =
            "SELECT "                                                                             +
            "    user_id, "                                                                       +
            "    dcp_rank, "                                                                      +
            "    evaluation_score01, "                                                            +
            "    evaluation_score02, "                                                            +
            "    evaluation_score03, "                                                            +
            "    evaluation_score04, "                                                            +
            "    evaluation_score05, "                                                            +
            "    evaluation_score06, "                                                            +
            "    evaluation_score07, "                                                            +
            "    evaluation_score08, "                                                            +
            "    evaluation_score09, "                                                            +
            "    evaluation_score10, "                                                            +
            "    renew_year, "                                                                    +
            "    is_attend_workshop, "                                                            +
            "    workshop_attended_date, "                                                        +
            "    renew_request_type, "                                                            +
            "    reason_for_rejection, "                                                          +
            "    is_dco_senior, "                                                                 +
            "    is_dco, "                                                                        +
            "    is_dco_trainee, "                                                                +
            "    is_bco_admin, "                                                                  +
            "    is_bco, "                                                                        +
            "    is_mentor, "                                                                     +
            "    is_sco, "                                                                        +
            "    is_idco, "                                                                       +
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    m_dcp_qualification "                                                            +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    is_deleted = false ";

    /** DCPユーザ付加情報取得SQL定義 */
    private static final String ADDITIONAL_INFORMATION_SELECT_QUERY =
            "SELECT"                                                                              +
            "    user_id, "                                                                       +
            "    certification_no, "                                                              +
            "    birth_date, "                                                                    +
            "    gender_type, "                                                                   +
            "    job_type, "                                                                      +
            "    is_decline_reward, "                                                             +
            "    qualifiied_date, "                                                               +
            "    can_speak_english, "                                                             +
            "    can_speak_chinese, "                                                             +
            "    can_speak_others, "                                                              +
            "    portrait, "                                                                      +
            "    postalcode1, "                                                                   +
            "    prefecture_code1, "                                                              +
            "    address1, "                                                                      +
            "    postalcode2, "                                                                   +
            "    prefecture_code2, "                                                              +
            "    address2, "                                                                      +
            "    postalcode3, "                                                                   +
            "    prefecture_code3, "                                                              +
            "    address3, "                                                                      +
            "    route_name1, "                                                                   +
            "    nearest_station1, "                                                              +
            "    route_name2, "                                                                   +
            "    nearest_station2, "                                                              +
            "    arrive_time, "                                                                   +
            "    dcp_message, "                                                                   +
            "    jada_remarks, "                                                                  +
            "    ratio_ict, "                                                                     +
            "    ratio_ooct, "                                                                    +
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    m_dcp_information "                                                              +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    is_deleted = false ";

    /** ICT仮確定情報取得SQL定義 */
    private static final String ICT_PROVISIONAL_INFO_SELECT_QUERY =
            "SELECT"                                                                              +
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
            "FROM "                                                                               +
            "    t_dcp_provisional "                                                              +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate ";
    /** ICT仮確定情報取得SQL定義 */
    private static final String OOCT_PROVISIONAL_INFO_SELECT_QUERY =
            "SELECT"                                                                              +
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
            "FROM "                                                                               +
            "    t_dcp_provisional "                                                              +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType ";

    /** 連続アサイン情報取得SQL定義 */
    private static final String CONTINUOUS_DUTY_INFO_SELECT_QUERY =
            "WITH provisional_info AS "                                                           +
            "( "                                                                                  +
            "    SELECT "                                                                         +
            "        tdp.user_id, "                                                               +
            "        tdp.assigned_date "                                                          +
            "    FROM "                                                                           +
            "        t_dcp_provisional tdp "                                                      +
            "), "                                                                                 +
            "assigned_table AS "                                                                  +
            "( "                                                                                  +
            "    SELECT "                                                                         +
            "        user_id, "                                                                   +
            "        assigned_date, "                                                             +
            "        CASE "                                                                       +
            "            WHEN assigned_date != (lag_assigned_date + 1) "                          +
            "            THEN 1 "                                                                 +
            "            ELSE 0 "                                                                 +
            "        END AS not_continuous_flg "                                                  +
            "    FROM "                                                                           +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            user_id, "                                                               +
            "            assigned_date, "                                                         +
            "            lag(assigned_date, 1) OVER "                                             +
            "            ( "                                                                      +
            "                PARTITION BY user_id ORDER BY assigned_date "                        +
            "            ) AS lag_assigned_date "                                                 +
            "        FROM "                                                                       +
            "        ( "                                                                          +
            "            SELECT "                                                                 +
            "                user_id, "                                                           +
            "                assigned_date "                                                      +
            "            FROM "                                                                   +
            "                provisional_info "                                                   +
            "            UNION ALL "                                                              +
            "            SELECT :userId, :testingDate "                                           +
            "        ) AS provisional_result "                                                    +
            "        WHERE "                                                                      +
            "            user_id = :userId AND "                                                  +
            "            assigned_date "                                                          +
            "                BETWEEN "                                                            +
            "                    (:testingDate::Date - :continuousDaysLimit) "                    +
            "                AND "                                                                +
            "                    (:testingDate::Date + :continuousDaysLimit) "                    +
            "        GROUP BY "                                                                   +
            "            user_id, "                                                               +
            "            assigned_date "                                                          +
            "    ) AS provisional_target "                                                        +
            "), "                                                                                 +
            "flg_cnt AS "                                                                         +
            "( "                                                                                  +
            "    SELECT "                                                                         +
            "        assigned_date, "                                                             +
            "        sum(not_continuous_flg) OVER (ORDER BY assigned_date) AS cnt_not_cont_date_flg " +
            "    FROM "                                                                           +
            "        assigned_table "                                                             +
            ") "                                                                                  +
            ", continuous_cnt AS "                                                                +
            "( "                                                                                  +
            "    SELECT "                                                                         +
            "        assigned_date, "                                                             +
            "        count(cnt_not_cont_date_flg) OVER "                                          +
            "        ( "                                                                          +
            "            PARTITION BY "                                                           +
            "                cnt_not_cont_date_flg "                                              +
            "            ORDER BY "                                                               +
            "                cnt_not_cont_date_flg "                                              +
            "        ) AS cnt "                                                                   +
            "    FROM "                                                                           +
            "        flg_cnt "                                                                    +
            ") "                                                                                  +
            "SELECT "                                                                             +
            "    max(cnt) AS count "                                                              +
            "FROM "                                                                               +
            "    continuous_cnt ";

    /** 必要参加日数取得SQL定義 */
    private static final String REQUIRED_DAYS_QUERY =
            "SELECT "                                                                             +
            "    CEIL( A.testing_days * ( :participationRatio / 100.00 ) ) as required_days "     +
            "FROM "                                                                               +
            "    t_mission_base A "                                                               +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.is_deleted = false ";

    // DCPMG#154-2 change start 非仮確定者のみ取得
    /** ICT参加可能者取得SQL定義 */
    private static final String ICT_PARTICIPATION_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_select_ict A "                                                             +
            "    LEFT OUTER JOIN t_dcp_provisional B ON "                                         +
            "        A.user_id = B.user_id AND "                                                  +
            "        A.mission_key = B.mission_key AND "                                          +
            "        A.assigned_date = B.assigned_date AND "                                      +
            "        A.dcp_role_type = B.dcp_role_type AND "                                      +
            "        A.time_slot_type = B.time_slot_type "                                        +
            "WHERE "                                                                              +
            "    B.user_id is null AND "                                                          +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_deleted = false "                                                           +
            "ORDER BY "                                                                           +
            "    user_id ";
    // DCPMG#154-2 change end

    /** ICT有力候補者昇順取得SQL定義 */
    private static final String BCO_ICT_IS_STRONG_CANDIDATE_QUERY_ASC =
            "SELECT "                                                                             +
            "    COALESCE(D.user_id, NULL) as beforeregist, "                                     +
            "    A.user_id, "                                                                     +
            "    B.certification_no, "                                                            +
            "    C.count, "                                                                       +
            "    I.priority_seq "                                                                 +
            "FROM "                                                                               +
            "    t_dcp_select_ict A "                                                             +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN ( "                                                                   +
            "        SELECT "                                                                     +
            "            A.user_id, "                                                             +
            "            COUNT(B.assigned_date) AS count "                                        +
            "        FROM "                                                                       +
            "            m_user_information A "                                                   +
            "            LEFT OUTER JOIN t_dcp_assign B ON "                                      +
            "                B.user_id = A.user_id AND "                                          +
            "                B.is_deleted = FALSE "                                               +
            "            LEFT OUTER JOIN ( "                                                      +
            "                SELECT "                                                             +
            "                    CASE "                                                           +
            "                        WHEN SUB.CURRENT_MONTH < 4 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT) - 1, 4, 1) "     +
            "                        WHEN SUB.CURRENT_MONTH > 3 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT), 4, 1) "         +
            "                        END AS FROM_DATE, "                                          +
            "                    CASE "                                                           +
            "                        WHEN SUB.CURRENT_MONTH < 4 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT), 3, 31) "        +
            "                        WHEN SUB.CURRENT_MONTH > 3 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT) + 1, 3, 31) "    +
            "                        END AS TO_DATE "                                             +
            "                FROM "                                                               +
            "                    ( "                                                              +
            "                        SELECT "                                                     +
            "                            EXTRACT(MONTH FROM CURRENT_DATE) AS CURRENT_MONTH, "     +
            "                            EXTRACT(YEAR FROM CURRENT_DATE)  AS CURRENT_YEAR "       +
            "                    ) SUB "                                                          +
            "            ) C ON "                                                                 +
            "                B.assigned_date >= C.FROM_DATE AND "                                 +
            "                B.assigned_date <= C.TO_DATE "                                       +
            "        GROUP BY "                                                                   +
            "            A.user_id "                                                              +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    LEFT OUTER JOIN t_dcp_provisional D ON "                                         +
            "        D.mission_key = A.mission_key AND "                                          +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.assigned_date < CAST(:assignedDate as DATE) AND "                          +
            "        D.time_slot_type = A.time_slot_type AND "                                    +
            "        D.dcp_role_type = A.dcp_role_type "                                          +
            "    INNER JOIN t_mission_base F ON "                                                 +
            "        F.mission_key = A.mission_key AND "                                          +
            "        F.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture G ON "                                                   +
            "        G.prefecture_code = F.venue_prefecture_code AND "                            +
            "        G.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture H ON "                                                   +
            "        H.prefecture_code = B.prefecture_code1 AND "                                 +
            "        H.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority I ON "                                            +
            "        I.work_area_id = G.area_id AND "                                             +
            "        I.home_area_id = H.area_id AND "                                             +
            "        I.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_strong_candidate = TRUE AND "                                               +
            "    A.is_deleted = FALSE "                                                           +
            "GROUP BY "                                                                           +
            "    D.user_id, "                                                                     +
            "    A.user_id, "                                                                     +
            "    B.certification_no, "                                                            +
            "    C.count, "                                                                       + 
            "    I.priority_seq "                                                                 +
            "ORDER BY "                                                                           +
            "    beforeRegist NULLS LAST, "                                                       +
            "    I.priority_seq, "                                                                +
            "    C.count, "                                                                       +
            "    B.certification_no ";

    /** ICT有力候補者降順取得SQL定義 */
    private static final String BCO_ICT_IS_STRONG_CANDIDATE_QUERY_DESC =
            "SELECT "                                                                             +
            "    COALESCE(D.user_id, NULL) as beforeregist, "                                     +
            "    A.user_id, "                                                                     +
            "    B.certification_no, "                                                            +
            "    C.count, "                                                                       +
            "    I.priority_seq "                                                                 +
            "FROM "                                                                               +
            "    t_dcp_select_ict A "                                                             +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN ( "                                                                   +
            "        SELECT "                                                                     +
            "            A.user_id, "                                                             +
            "            COUNT(B.assigned_date) AS count "                                        +
            "        FROM "                                                                       +
            "            m_user_information A "                                                   +
            "            LEFT OUTER JOIN t_dcp_assign B ON "                                      +
            "                B.user_id = A.user_id AND "                                          +
            "                B.is_deleted = FALSE "                                               +
            "            LEFT OUTER JOIN ( "                                                      +
            "                SELECT "                                                             +
            "                    CASE "                                                           +
            "                        WHEN SUB.CURRENT_MONTH < 4 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT) - 1, 4, 1) "     +
            "                        WHEN SUB.CURRENT_MONTH > 3 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT), 4, 1) "         +
            "                        END AS FROM_DATE, "                                          +
            "                    CASE "                                                           +
            "                        WHEN SUB.CURRENT_MONTH < 4 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT), 3, 31) "        +
            "                        WHEN SUB.CURRENT_MONTH > 3 "                                 +
            "                            THEN "                                                   +
            "                            make_date(CAST(SUB.CURRENT_YEAR AS INT) + 1, 3, 31) "    +
            "                        END AS TO_DATE "                                             +
            "                FROM "                                                               +
            "                    ( "                                                              +
            "                        SELECT "                                                     +
            "                            EXTRACT(MONTH FROM CURRENT_DATE) AS CURRENT_MONTH, "     +
            "                            EXTRACT(YEAR FROM CURRENT_DATE)  AS CURRENT_YEAR "       +
            "                    ) SUB "                                                          +
            "            ) C ON "                                                                 +
            "                B.assigned_date >= C.FROM_DATE AND "                                 +
            "                B.assigned_date <= C.TO_DATE "                                       +
            "        GROUP BY "                                                                   +
            "            A.user_id "                                                              +
            "    ) C ON "                                                                         +
            "        C.user_id = B.user_id "                                                      +
            "    LEFT OUTER JOIN t_dcp_provisional D ON "                                         +
            "        D.mission_key = A.mission_key AND "                                          +
            "        D.user_id = A.user_id AND "                                                  +
            "        D.assigned_date < CAST(:assignedDate as DATE) AND "                          +
            "        D.time_slot_type = A.time_slot_type AND "                                    +
            "        D.dcp_role_type = A.dcp_role_type "                                          +
            "    INNER JOIN t_mission_base F ON "                                                 +
            "        F.mission_key = A.mission_key AND "                                          +
            "        F.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture G ON "                                                   +
            "        G.prefecture_code = F.venue_prefecture_code AND "                            +
            "        G.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture H ON "                                                   +
            "        H.prefecture_code = B.prefecture_code1 AND "                                 +
            "        H.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority I ON "                                            +
            "        I.work_area_id = G.area_id AND "                                             +
            "        I.home_area_id = H.area_id AND "                                             +
            "        I.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_strong_candidate = TRUE AND "                                               +
            "    A.is_deleted = FALSE "                                                           +
            "GROUP BY "                                                                           +
            "    D.user_id, "                                                                     +
            "    A.user_id, "                                                                     +
            "    B.certification_no, "                                                            +
            "    C.count, "                                                                       +
            "    I.priority_seq "                                                                 +
            "ORDER BY "                                                                           +
            "    beforeRegist DESC NULLS FIRST, "                                                 +
            "    I.priority_seq DESC, "                                                           +
            "    C.count, "                                                                       +
            "    B.certification_no ";

    /** リードDCO用以前経験有り情報取得SQL定義 */
    private static final String PREVIOUS_EXPERIENCE_LEAD_DCO_QUERY =
            "SELECT "                                                                             +
            "    B.assigned_date "                                                                +
            "FROM "                                                                               +
            "    t_mission_base A "                                                               +
            "    INNER JOIN t_dcp_assign B ON "                                                   +
            "        B.assigned_date < CURRENT_DATE AND "                                         +
            "        B.user_id = :userId AND "                                                    +
            "        B.mission_key = A.mission_key AND "                                          + 
            "        B.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.sports_id = :sportsId ";

    /** DCO用以前経験有り情報取得SQL定義 */
    private static final String PREVIOUS_EXPERIENCE_DCO_QUERY =
            "SELECT "                                                                             +
            "    D.assigned_date "                                                                +
            "FROM "                                                                               +
            "    m_discipline A "                                                                 +
            "    INNER JOIN m_discipline B ON "                                                   +
            "        B.discipline_type = A.discipline_type AND "                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN t_mission_base C ON "                                                 +
            "        C.discipline_id = B.discipline_id AND "                                      +
            "        C.is_deleted = FALSE "                                                       +
            "    INNER JOIN t_dcp_assign D ON "                                                   +
            "        D.user_id = :userId AND "                                                    +
            "        D.assigned_date < CURRENT_DATE AND "                                         +
            "        D.mission_key = C.mission_key AND "                                          +
            "        D.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.discipline_id = :disciplineId AND "                                            +
            "    A.is_deleted = FALSE "                                                           +
            "GROUP BY "                                                                           +
            "    D.assigned_date ";

    /** 用務地・居住地関係確認SQL定義 */
    private static final String REGIONAL_RELATIONSHIPS_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    m_dcp_information A "                                                            +
            "    INNER JOIN m_prefecture B ON "                                                   +
            "        B.prefecture_code = A.prefecture_code1 AND "                                 +
            "        B.area_id IN "                                                               +
            "        ( "                                                                          +
            "            SELECT "                                                                 +
            "                A.home_area_id "                                                     +
            "            FROM "                                                                   +
            "                m_dcp_area_priority A "                                              +
            "                INNER JOIN m_prefecture B ON "                                       +
            "                    B.area_id = A.work_area_id AND "                                 +
            "                    B.is_deleted = false "                                           +
            "                INNER JOIN t_mission_base C ON "                                     +
            "                    C.mission_key = :missionKey AND "                                +
            "                    C.venue_prefecture_code = B.prefecture_code AND "                +
            "                    C.is_deleted = false "                                           +
            "            WHERE "                                                                  +
            "                A.is_deleted = false "                                               +
            "        ) AND "                                                                      +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.user_id = :userId AND "                                                        +
            "    A.is_deleted = false ";

    /** 連続ミッション確認（ICT）SQL定義 */
    private static final String CONSECUTIVE_MISSIONS_ICT_QUERY =
            "SELECT "                                                                             +
            "    A.mission_key "                                                                  +
            "FROM "                                                                               +
            "    t_dcp_provisional A "                                                            +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type IN ( '11', '12', '13' ) AND "                                 +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.user_id = :userId AND "                                                        +
            "    A.mission_key <> :missionKey AND "                                               +
            "    ( "                                                                              +
            "        A.assigned_date = CAST(:assignedDate as DATE) - interval '1 day' OR "        +
            "        A.assigned_date = CAST(:assignedDate as DATE) + interval '1 day' "           +
            "    ) "                                                                              +
            "UNION "                                                                              +
            "SELECT "                                                                             +
            "    A.mission_key "                                                                  +
            "FROM "                                                                               +
            "    t_dcp_provisional A "                                                            +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type IN ( '21' ) AND "                                             +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.user_id = :userId AND "                                                        +
            "    A.mission_key <> :missionKey AND "                                               +
            "    A.assigned_date = CAST(:assignedDate as DATE) - interval '1 day' AND "           +
            "    A.time_slot_type = '4' ";

    /** 有力候補者用DCP選定情報（ICT）更新SQL定義 */
    private static final String DCP_ICT_STRONG_CANDIDATE_UPDATE =
            "UPDATE "                                                                             +
            "    t_dcp_select_ict "                                                               +
            "SET "                                                                                +
            "    is_strong_candidate = :isStrongCandidate, "                                      +
            "    conditions_score = :conditionsScore, "                                           +
            "    any_condition1 = :anyCondition1, "                                               +
            "    is_met_condition1 = :isMetCondition1, "                                          +
            "    any_condition2 = :anyCondition2, "                                               +
            "    is_met_condition2 = :isMetCondition2, "                                          +
            "    any_condition3 = :anyCondition3, "                                               +
            "    is_met_condition3 = :isMetCondition3, "                                          +
            // DCPMG#144 start
            "    any_condition4 = :anyCondition4, "                                               +
            "    is_met_condition4 = :isMetCondition4 "                                           +
            // DCPMG#144 end
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** 時間帯区分取得SQL定義 */
    private static final String TIME_SLOT_TYPE_QUERY =
            "SELECT "                                                                             +
            "    CASE "                                                                           +
            "    WHEN B.notification_time BETWEEN A.early_time_from AND A.early_time_to "         +
            "        THEN '1' "                                                                   +
            "    WHEN B.notification_time BETWEEN A.morning_time_from AND A.morning_time_to "     +
            "        THEN '2' "                                                                   +
            "    WHEN B.notification_time BETWEEN A.afternoon_time_from AND A.afternoon_time_to " +
            "        THEN '3' "                                                                   +
            "    WHEN B.notification_time BETWEEN A.evening_time_from AND A.evening_time_to "     +
            "        THEN '4' "                                                                   +
            "    ELSE NULL "                                                                      +
            "    END as time_slot_type "                                                          +
            "FROM "                                                                               +
            "    m_system_defaults A, "                                                           +
            "    t_mission_base B "                                                               +
            "WHERE "                                                                              +
            "    A.system_code = :systemCode AND "                                                +
            "    B.mission_key = :missionKey AND "                                                +
            "    B.is_deleted = false ";

    // DCPMG#154-2 change start 非仮確定者のみ取得
    /** OOCT参加可能者取得SQL定義 */
    private static final String OOCT_PARTICIPATION_QUERY =
            "SELECT "                                                                             +
            "    A.user_id "                                                                      +
            "FROM "                                                                               +
            "    t_dcp_select_ooct A "                                                            +
            "    LEFT OUTER JOIN t_dcp_provisional B ON "                                         +
            "        A.user_id = B.user_id AND "                                                  +
            "        A.mission_key = B.mission_key AND "                                          +
            "        A.assigned_date = B.assigned_date AND "                                      +
            "        A.dcp_role_type = B.dcp_role_type AND "                                      +
            "        A.time_slot_type = B.time_slot_type "                                        +
            "WHERE "                                                                              +
            "    B.user_id is null AND "                                                          +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.time_slot_type = :timeSlotType AND "                                           +
            "    A.dcp_role_type = :dcpRoleType AND "                                             +
            "    A.is_deleted = false "                                                           +
            "ORDER BY "                                                                           +
            "    user_id ";
    // DCPMG#154-2 change end

    /** OOCT有力候補者昇順取得SQL定義 */
    private static final String OOCT_IS_STRONG_CANDIDATE_QUERY_ASC =
            "SELECT"                                                                              +
            "    A.user_id, "                                                                     +
            "    F.priority_seq "                                                                 +
            "FROM "                                                                               +
            "    t_dcp_select_ooct A "                                                            +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN t_mission_base C ON "                                                 +
            "        C.mission_key = A.mission_key AND "                                          +
            "        C.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture D ON "                                                   +
            "        D.prefecture_code = C.venue_prefecture_code AND "                            +
            "        D.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture E ON "                                                   +
            "        E.prefecture_code = B.prefecture_code1 AND "                                 +
            "        E.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority F ON "                                            +
            "        F.work_area_id = D.area_id AND "                                             +
            "        F.home_area_id = E.area_id AND "                                             +
            "        F.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.time_slot_type = :timeSlotType AND "                                           +
            "    A.dcp_role_type = :dcpRoleType AND "                                             + 
            "    A.is_strong_candidate = true AND "                                               +
            "    A.is_deleted = false "                                                           +
            "ORDER BY "                                                                           +
            "    F.priority_seq, "                                                                +
            "    B.certification_no ";

    /** OOCT有力候補者降順取得SQL定義 */
    private static final String OOCT_IS_STRONG_CANDIDATE_QUERY_DESC =
            "SELECT"                                                                              +
            "    A.user_id, "                                                                     +
            "    F.priority_seq "                                                                 +
            "FROM "                                                                               +
            "    t_dcp_select_ooct A "                                                            +
            "    INNER JOIN m_dcp_information B ON "                                              +
            "        B.user_id = A.user_id AND "                                                  +
            "        B.is_deleted = FALSE "                                                       +
            "    INNER JOIN t_mission_base C ON "                                                 +
            "        C.mission_key = A.mission_key AND "                                          +
            "        C.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture D ON "                                                   +
            "        D.prefecture_code = C.venue_prefecture_code AND "                            +
            "        D.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_prefecture E ON "                                                   +
            "        E.prefecture_code = B.prefecture_code1 AND "                                 +
            "        E.is_deleted = FALSE "                                                       +
            "    INNER JOIN m_dcp_area_priority F ON "                                            +
            "        F.work_area_id = D.area_id AND "                                             +
            "        F.home_area_id = E.area_id AND "                                             +
            "        F.is_deleted = FALSE "                                                       +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.time_slot_type = :timeSlotType AND "                                           +
            "    A.dcp_role_type = :dcpRoleType AND "                                             + 
            "    A.is_strong_candidate = true AND "                                               +
            "    A.is_deleted = false "                                                           +
            "ORDER BY "                                                                           +
            "    F.priority_seq DESC, "                                                           +
            "    B.certification_no ";

    /** 連続ミッション確認（OOCT）SQL定義 */
    private static final String CONSECUTIVE_MISSIONS_OOCT_QUERY =
            "SELECT "                                                                             +
            "    A.mission_key "                                                                  +
            "FROM "                                                                               +
            "    t_dcp_provisional A "                                                            +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type = '21' AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.user_id = :userId AND "                                                        +
            "    A.mission_key <> :missionKey AND "                                               +
            "    ( "                                                                              +
            "        CASE :timeSlotType "                                                         +
            "            WHEN '1' THEN "                                                          +
            "                A.assigned_date = CAST(:assignedDate as DATE) - interval '1 day' AND " +
            "                A.time_slot_type = '4' "                                             +
            "            WHEN '2' THEN "                                                          +
            "                A.assigned_date = :assignedDate AND "                                +
            "                A.time_slot_type = '1' "                                             +
            "            WHEN '3' THEN "                                                          +
            "                A.assigned_date = :assignedDate AND "                                +
            "                A.time_slot_type = '2' "                                             +
            "            WHEN '4' THEN "                                                          +
            "                A.assigned_date = :assignedDate AND "                                +
            "                A.time_slot_type = '3' "                                             +
            "        END "                                                                        +
            "    ) "                                                                              +
            "UNION "                                                                              +
            "SELECT "                                                                             +
            "    A.mission_key "                                                                  +
            "FROM "                                                                               +
            "    t_dcp_provisional A "                                                            +
            "    INNER JOIN t_mission_base B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.testing_type = '21' AND "                                                  +
            "        B.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.user_id = :userId AND "                                                        +
            "    A.mission_key <> :missionKey AND "                                               +
            "    ( "                                                                              +
            "        CASE :timeSlotType "                                                         +
            "            WHEN '1' THEN "                                                          +
            "                A.assigned_date = :assignedDate AND "                                +
            "                A.time_slot_type = '2' "                                             +
            "            WHEN '2' THEN "                                                          +
            "                A.assigned_date = :assignedDate AND "                                +
            "                A.time_slot_type = '3' "                                             +
            "            WHEN '3' THEN "                                                          +
            "                A.assigned_date = :assignedDate AND "                                +
            "                A.time_slot_type = '4' "                                             +
            "            WHEN '4' THEN "                                                          +
            "                A.assigned_date = CAST(:assignedDate as DATE) + interval '1 day' AND " +
            "                A.time_slot_type = '1' "                                             +
            "        END "                                                                        +
            "    ) ";

    /** 有力候補者用DCP選定情報（OOCT）更新SQL定義 */
    private static final String DCP_OOCT_STRONG_CANDIDATE_UPDATE =
            "UPDATE "                                                                             +
            "    t_dcp_select_ooct "                                                              +
            "SET "                                                                                +
            "    is_strong_candidate = :isStrongCandidate, "                                      +
            "    conditions_score = :conditionsScore, "                                           +
            "    any_condition1 = :anyCondition1, "                                               +
            "    is_met_condition1 = :isMetCondition1, "                                          +
            "    any_condition2 = :anyCondition2, "                                               +
            "    is_met_condition2 = :isMetCondition2, "                                          +
            "    any_condition3 = :anyCondition3, "                                               +
            "    is_met_condition3 = :isMetCondition3 "                                           +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCP仮確定情報取得SQL定義 */
    private static final String DCP_PROVISIONAL_SELECT =
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
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_provisional "                                                              +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCP仮確定情報取得（ICT）SQL定義 */
    private static final String DCP_PROVISIONAL_SELECT_ICT =
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
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_provisional "                                                              +
            "WHERE "                                                                              +
            "    assigned_date = :assignedDate AND "                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCP仮確定情報取得（OOCT）SQL定義 */
    private static final String DCP_PROVISIONAL_SELECT_OOCT =
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
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_provisional "                                                              +
            "WHERE "                                                                              +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCP手動割当情報取得SQL定義 */
    private static final String DCP_MANUAL_PROVISIONAL_SELECT =
            "SELECT "                                                                             +
            "    user_id, "                                                                       +
            "    assigned_date, "                                                                 +
            "    time_slot_type, "                                                                +
            "    mission_key, "                                                                   +
            "    dcp_role_type, "                                                                 +
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
            "FROM "                                                                               +
            "    t_dcp_manual_assign "                                                            +
            "WHERE "                                                                              +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type IN (:dcpRoleType) ";

    // DCPMG#154-2 change start 非仮確定者のみ削除
    /** 非仮確定DCP選定情報（ICT）削除SQL定義 */
    private static final String DCP_SELECT_ICT_DELETE =
            "DELETE FROM "                                                                        +
            "    t_dcp_select_ict A "                                                             +
            "WHERE "                                                                              +
            "    ( "                                                                              +
            "        A.user_id, "                                                                 +
            "        A.mission_key, "                                                             +
            "        A.assigned_date, "                                                           +
            "        A.time_slot_type, "                                                          +
            "        A.dcp_role_type "                                                            +
            "    ) NOT IN "                                                                       +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            B.user_id, "                                                             +
            "            B.mission_key, "                                                         +
            "            B.assigned_date, "                                                       +
            "            B.time_slot_type, "                                                      +
            "            B.dcp_role_type "                                                        +
            "        FROM "                                                                       +
            "            t_dcp_provisional B "                                                    +
            "    ) AND "                                                                          +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType ";

    /** 非仮確定DCP選定情報（OOCT）削除SQL定義 */
    private static final String DCP_SELECT_OOCT_DELETE =
            "DELETE FROM "                                                                        +
            "    t_dcp_select_ooct A "                                                            +
            "WHERE "                                                                              +
            "    ( "                                                                              +
            "        A.user_id, "                                                                 +
            "        A.mission_key, "                                                             +
            "        A.assigned_date, "                                                           +
            "        A.time_slot_type, "                                                          +
            "        A.dcp_role_type "                                                            +
            "    ) NOT IN "                                                                       +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            B.user_id, "                                                             +
            "            B.mission_key, "                                                         +
            "            B.assigned_date, "                                                       +
            "            B.time_slot_type, "                                                      +
            "            B.dcp_role_type "                                                        +
            "        FROM "                                                                       +
            "            t_dcp_provisional B "                                                    +
            "    ) AND "                                                                          +
            "    A.assigned_date = :assignedDate AND "                                            +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.dcp_role_type = :dcpRoleType ";
    // DCPMG#154-2 change end

    /** DCP選定情報（ICT）登録SQL */
    private static final String DCP_SELECT_ICT_INSERT =
            "INSERT INTO t_dcp_select_ict "                                                       +
            "VALUES ( "                                                                           +
            "  :userId, "                                                                         +
            "  :assignedDate, "                                                                   +
            "  :timeSlotType, "                                                                   +
            "  :missionKey, "                                                                     +
            "  :dcpRoleType, "                                                                    +
            "  :isStrongCandidate, "                                                              +
            "  :conditionsScore, "                                                                +
            "  :anyCondition1, "                                                                  +
            "  :isMetCondition1, "                                                                +
            "  :anyCondition2, "                                                                  +
            "  :isMetCondition2, "                                                                +
            "  :anyCondition3, "                                                                  +
            "  :isMetCondition3, "                                                                +
            "  :anyCondition4, "                                                                  +
            "  :isMetCondition4, "                                                                +
            "  :anyCondition5, "                                                                  +
            "  :isMetCondition5, "                                                                +
            "  :isDeleted, "                                                                      +
            "  :createdAt, "                                                                      +
            "  :createdBy, "                                                                      +
            "  :updatedAt, "                                                                      +
            "  :updatedBy "                                                                       +
            ") ";

    /** DCP選定情報（OOCT）登録SQL */
    private static final String DCP_SELECT_OOCT_INSERT =
            "INSERT INTO t_dcp_select_ooct "                                                       +
            "VALUES ( "                                                                           +
            "  :userId, "                                                                         +
            "  :assignedDate, "                                                                   +
            "  :timeSlotType, "                                                                   +
            "  :missionKey, "                                                                     +
            "  :dcpRoleType, "                                                                    +
            "  :isStrongCandidate, "                                                              +
            "  :conditionsScore, "                                                                +
            "  :anyCondition1, "                                                                  +
            "  :isMetCondition1, "                                                                +
            "  :anyCondition2, "                                                                  +
            "  :isMetCondition2, "                                                                +
            "  :anyCondition3, "                                                                  +
            "  :isMetCondition3, "                                                                +
            "  :anyCondition4, "                                                                  +
            "  :isMetCondition4, "                                                                +
            "  :anyCondition5, "                                                                  +
            "  :isMetCondition5, "                                                                +
            "  :isDeleted, "                                                                      +
            "  :createdAt, "                                                                      +
            "  :createdBy, "                                                                      +
            "  :updatedAt, "                                                                      +
            "  :updatedBy "                                                                       +
            ") ";

    /** DCP選定情報（ICT）取得SQL */
    private static final String DCP_SELECT_ICT_SELECT =
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
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_select_ict "                                                               +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCP選定情報（OOCT）取得SQL */
    private static final String DCP_SELECT_OOCT_SELECT =
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
            "    is_deleted, "                                                                    +
            "    created_at, "                                                                    +
            "    created_by, "                                                                    +
            "    updated_at, "                                                                    +
            "    updated_by "                                                                     +
            "FROM "                                                                               +
            "    t_dcp_select_ooct "                                                              +
            "WHERE "                                                                              +
            "    user_id = :userId AND "                                                          +
            "    assigned_date = :assignedDate AND "                                              +
            "    time_slot_type = :timeSlotType AND "                                             +
            "    mission_key = :missionKey AND "                                                  +
            "    dcp_role_type = :dcpRoleType ";

    /** DCP仮確定情報登録SQL */
    private static final String DCP_PROVISIONAl_INSERT =
            "INSERT INTO t_dcp_provisional "                                                      +
            "VALUES ( "                                                                           +
            "  :userId, "                                                                         +
            "  :assignedDate, "                                                                   +
            "  :timeSlotType, "                                                                   +
            "  :missionKey, "                                                                     +
            "  :dcpRoleType, "                                                                    +
            "  :isStrongCandidate, "                                                              +
            "  :conditionsScore, "                                                                +
            "  :anyCondition1, "                                                                  +
            "  :isMetCondition1, "                                                                +
            "  :anyCondition2, "                                                                  +
            "  :isMetCondition2, "                                                                +
            "  :anyCondition3, "                                                                  +
            "  :isMetCondition3, "                                                                +
            "  :anyCondition4, "                                                                  +
            "  :isMetCondition4, "                                                                +
            "  :anyCondition5, "                                                                  +
            "  :isMetCondition5, "                                                                +
            "  :createdAt, "                                                                      +
            "  :createdBy "                                                                       +
            ") ";

    /** DCP割当状況更新SQL（BCO） */
    private static final String DCP_ASSIGN_STATUS_BCO_UPDATE =
            "UPDATE "                                                                             + 
            "    t_dcp_assign_status "                                                            + 
            "SET "                                                                                + 
            "    required_bco_admin = :requiredBcoAdmin, "                                        + 
            "    required_bco = :requiredBco "                                                    +
            "WHERE "                                                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    testing_date = :testingDate ";

    /** DCP割当状況更新SQL（DCO） */
    private static final String DCP_ASSIGN_STATUS_DCO_UPDATE =
            "UPDATE "                                                                             + 
            "    t_dcp_assign_status "                                                            + 
            "SET "                                                                                + 
            "    required_dco = :requiredDco, "                                                   +
            "    required_dco_male = :requiredDcoMale, "                                          +
            "    required_dco_female = :requiredDcoFemale "                                       +
            "WHERE "                                                                              +
            "    mission_key = :missionKey AND "                                                  +
            "    testing_date = :testingDate ";

    /** DCP選定情報（ICT）参加可能日数SQL */
    private static final String ICT_AVAILABLE_DAYS_SELECT =
            "SELECT "                                                                             +
            "    COUNT(B.testing_date) as days "                                                  +
            "FROM "                                                                               +
            "    t_mission_base A "                                                               +
            "    INNER JOIN t_testing_date B ON "                                                 +
            "        B.mission_key = A.mission_key AND "                                          +
            "        B.is_deleted = false "                                                       +
            "    INNER JOIN t_dcp_calendar C ON "                                                 +
            "        C.user_id = :userId AND "                                                    +
            "        C.assigned_date = B.testing_date AND "                                       +
            "        C.is_attend_allday = true AND "                                              +
            // DCPMG#144  備考欄に入力が無い条件の削除
            "        C.is_deleted = false "                                                       +
            "WHERE "                                                                              +
            "    A.mission_key = :missionKey AND "                                                +
            "    A.is_deleted = false ";

    /**
     * アサインヘッダ情報取得処理.<br>
     * ミッション基本情報KEYを利用してアサインヘッダ情報を取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @return アサインヘッダ情報
     */
    public TAssignHeader getAssignHeaderByMissionkey(@Param("missionKey") Integer missionKey) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("missionKey", missionKey);

            // 実行
            TAssignHeader result = namedJdbcTemplate.queryForObject(ASSIGN_HEAD_INFO_QUERY,
                    parameter, new TAssignHeaderMapper());

            return result;

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * ミッション基本情報取得処理.<br>
     * ミッション基本情報KEYを利用してミッション基本情報を取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @return ミッション基本情報
     */
    public TMissionBase getMissionBaseByMissionkey(@Param("missionKey") Integer missionKey) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("missionKey", missionKey);

            // 実行
            return namedJdbcTemplate.queryForObject(MISSION_BASE_INFO_QUERY, parameter,
                    new TMissionBaseMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * 検査日別情報取得処理.<br>
     * ミッションに紐づく検査日別情報を取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @return 検査日別情報リスト
     */
    public List<TTestingDate> getTestingDateByMissionKey(@Param("missionKey") Integer missionKey) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);

        // レコードマップを生成
        RowMapper<TTestingDate> mapper = new BeanPropertyRowMapper<>(TTestingDate.class);

        // 実行
        return namedJdbcTemplate.query(TESTING_DATE_INFO_QUERY, parameter, mapper);
    }

    /**
     * 競技者数取得.<br>
     * ミッション基本情報KEY、検査日の条件よりデータを抽出し、競技者数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 競技者数情報DTO
     */
    public CompetitorsCountDto getCompetitorsCountDto(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("missionKey", missionKey);
            parameter.put("testingDate", testingDate);


            // 結果返却
            return namedJdbcTemplate.queryForObject(COMPETITORS_COUNT_QUERY, parameter,
                    new CompetitorsCountMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * 当日DCPカレンダー情報取得処理.<br>
     * 対象ユーザIDの検査日のDCPカレンダー情報を取得する。
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCPカレンダー情報
     */
    public TDcpCalendar getTodayDcpCalendar(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("userId", userId);
            parameter.put("assignedDate", assignedDate);

            // 結果返却
            return namedJdbcTemplate.queryForObject(TODAY_DCP_CALENDAR, parameter,
                    new TDcpCalendarMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * 前日DCPカレンダー情報取得処理.<br>
     * 対象ユーザIDの検査日前日のDCPカレンダー情報を取得する。
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCPカレンダー情報
     */
    public TDcpCalendar getYesterdayDcpCalendar(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("userId", userId);
            parameter.put("assignedDate", assignedDate);

            // 結果返却
            return namedJdbcTemplate.queryForObject(YESTERDAY_DCP_CALENDAR, parameter,
                    new TDcpCalendarMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * DCP評価資格情報取得処理.<br>
     * 指定ユーザIDで有効なDCP評価資格情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @return DCP評価資格情報
     */
    public MDcpQualification getQualificationByUserId(@Param("userId") Integer userId) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("userId", userId);

            // 結果返却
            return namedJdbcTemplate.queryForObject(QUALIFICATION_SELECT_QUERY, parameter,
                    new MDcpQualificationMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * DCPユーザ付加情報取得処理.<br>
     * 指定ユーザIDで有効なDCPユーザ付加情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @return DCPユーザ付加情報
     */
    public MDcpInformation getAdditionalInformationByUserId(@Param("userId") Integer userId) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("userId", userId);

            // 結果返却
            return namedJdbcTemplate.queryForObject(ADDITIONAL_INFORMATION_SELECT_QUERY, parameter,
                    new MDcpInformationMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * ICT側DCP仮確定情報取得処理.<br>
     * 指定ユーザID、日付に該当するDCP仮確定情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCP仮確定情報リスト
     */
    public List<TDcpProvisional> getIctProvisionalInfomation(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);

        // レコードマップを生成
        RowMapper<TDcpProvisional> mapper = new BeanPropertyRowMapper<>(TDcpProvisional.class);

        // 結果返却
        return namedJdbcTemplate.query(ICT_PROVISIONAL_INFO_SELECT_QUERY, parameter, mapper);
    }

    /**
     * OOCT側DCP仮確定情報取得処理.<br>
     * 指定ユーザID、日付、時間帯区分に該当するDCP仮確定情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCP仮確定情報リスト
     */
    public List<TDcpProvisional> getOoctProvisionalInfomation(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);

        // レコードマップを生成
        RowMapper<TDcpProvisional> mapper = new BeanPropertyRowMapper<>(TDcpProvisional.class);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PROVISIONAL_INFO_SELECT_QUERY, parameter, mapper);
    }

    /**
     * 連続勤務情報取得.<br>
     * 指定ユーザが何日間仮確定されているかチェックする。<br>
     * 
     * @param userId ユーザID
     * @param testingDate 検査日
     * @param continuousDaysLimit 最大勤務可能日数
     * @return 連続勤務日数
     */
    public Integer findContinuousDutyInformation(@Param("userId") Integer userId,
            @Param("testingDate") Date testingDate, @Param("continuousDaysLimit") Integer continuousDaysLimit) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("userId", userId);
            parameter.put("testingDate", testingDate);
            parameter.put("continuousDaysLimit", continuousDaysLimit);

            // 結果返却
            return namedJdbcTemplate.queryForObject(CONTINUOUS_DUTY_INFO_SELECT_QUERY, parameter, Integer.class);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * 必要参加日数取得.<br>
     * ミッション基本情報から必要参加日数を算出する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param participationRatio 必要参加割合
     * @return 必要参加日数
     */
    public Integer getRequiredDays(@Param("missionKey") Integer missionKey,
            @Param("participationRatio") Integer participationRatio) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("missionKey", missionKey);
            parameter.put("participationRatio", participationRatio);

            // 結果返却
            return namedJdbcTemplate.queryForObject(REQUIRED_DAYS_QUERY, parameter, Integer.class);

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * ICT参加可能者取得SQL定義.<br>
     * DCP選定情報（ICT）から参加可能な候補者を取得する。
     * DCPMG#154-2 change 非仮確定者のみを取得対象とする
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getIctParticipation(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);

        // 結果返却
        return namedJdbcTemplate.query(ICT_PARTICIPATION_QUERY, parameter, mapper);
    }

    /**
     * 有力候補者者(ICT)昇順取得SQL定義.<br>
     * DCP選定情報（ICT）から有力候補者者を昇順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoIctIsStrongCandidateAsc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);

        // 結果返却
        return namedJdbcTemplate.query(BCO_ICT_IS_STRONG_CANDIDATE_QUERY_ASC, parameter, mapper);
    }

    /**
     * 有力候補者者(ICT)降順取得SQL定義.<br>
     * DCP選定情報（ICT）から有力候補者者を降順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoIctIsStrongCandidateDesc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);

        // 結果返却
        return namedJdbcTemplate.query(BCO_ICT_IS_STRONG_CANDIDATE_QUERY_DESC, parameter, mapper);
    }

    /**
     * 過去参加経験情報（リードDCO用）取得.<br>
     * リードDCO用のDCPアサイン情報、ミッション基本情報から今回と同じ競技IDで参加した経験があるかどうかを抽出し返却する。<br>
     * 
     * @param userId ユーザID
     * @param sportsId 競技ID
     * @return 過去参加日情報リスト
     */
    public List<ProcessDate> getPreviousExperienceLeadDco(@Param("userId") Integer userId,
            @Param("sportsId") Integer sportsId) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ProcessDate> mapper = new BeanPropertyRowMapper<>(ProcessDate.class);
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("sportsId", sportsId);

        // 結果返却
        return namedJdbcTemplate.query(PREVIOUS_EXPERIENCE_LEAD_DCO_QUERY, parameter, mapper);
    }

    /**
     * 過去参加経験情報（DCO用）取得.<br>
     * DCO用のDCPアサイン情報、ミッション基本情報から今回と同じ競技IDで参加した経験があるかどうかを抽出し返却する。<br>
     * 
     * @param userId ユーザID
     * @param sportsId 競技ID
     * @return 過去参加日情報リスト
     */
    public List<ProcessDate> getPreviousExperienceDco(@Param("userId") Integer userId,
            @Param("disciplineId") Integer disciplineId) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<ProcessDate> mapper = new BeanPropertyRowMapper<>(ProcessDate.class);
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("disciplineId", disciplineId);

        // 結果返却
        return namedJdbcTemplate.query(PREVIOUS_EXPERIENCE_DCO_QUERY, parameter, mapper);
    }

    /**
     * 用務地・居住地関係確認.<br>
     * DCP在住地優先順位マスタ、都道府県マスタ、ミッション基本情報、DCPユーザ付加情報から用務地と居住地の関係を確認する。<br>
     * 在住地優先順位マスタに対象地域（都道府県）が無い場合は0件になる。優先になる場合はユーザIDが取得される。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param userId ユーザID
     * @return ユーザIDリスト
     */
    public List<User> getRegionalRelationships(@Param("missionKey") Integer missionKey,
            @Param("userId") Integer userId) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("userId", userId);

        // 結果返却
        return namedJdbcTemplate.query(REGIONAL_RELATIONSHIPS_QUERY, parameter, mapper);
    }

    /**
     * 連続ミッション確認（ICT）SQL.<br>
     * DCP仮確定情報、ミッション基本情報から検査日前後日にICTミッションが無い事を確認する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param userId ユーザID
     * @param assignedDate 検査日
     * @return ミッション基本情報KEYリスト
     */
    public List<Mission> getConsecutiveMissionsIct(@Param("missionKey") Integer missionKey,
            @Param("userId") Integer userId, @Param("assignedDate") Date assignedDate) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<Mission> mapper = new BeanPropertyRowMapper<>(Mission.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);

        // 結果返却
        return namedJdbcTemplate.query(CONSECUTIVE_MISSIONS_ICT_QUERY, parameter, mapper);
    }

    /**
     * 有力候補者用DCP選定情報（ICT）更新.<br>
     * DCP選定情報（ICT）情報の有力候補者用情報を更新する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * DCPMG#144 start
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * DCPMG#144 end
     * @return 更新件数
     */
    public int updateDcpIctStrongCandidate(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            // DCPMG#144 start
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4) {
            // DCPMG#144 end

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("isStrongCandidate", isStrongCandidate);
        parameter.put("conditionsScore", conditionsScore);
        parameter.put("anyCondition1", anyCondition1);
        parameter.put("isMetCondition1", isMetCondition1);
        parameter.put("anyCondition2", anyCondition2);
        parameter.put("isMetCondition2", isMetCondition2);
        parameter.put("anyCondition3", anyCondition3);
        parameter.put("isMetCondition3", isMetCondition3);
        // DCPMG#144 start
        parameter.put("anyCondition4", anyCondition4);
        parameter.put("isMetCondition4", isMetCondition4);
        // DCPMG#144 end
        // 結果返却
        return namedJdbcTemplate.update(DCP_ICT_STRONG_CANDIDATE_UPDATE, parameter);
    }

    /**
     * 時間帯区分取得.<br>
     * ミッション基本情報の通告時刻がシステム設定マスタで設定する時間帯区分のいずれに該当するかを確認する。
     * 
     * @param systemCode システム区分
     * @param missionKey ミッション基本情報KEY
     * @return 時間帯区分リスト（返却は常に1件となる予定）
     */
    public String getMissionTimeSlotType(@Param("systemCode") String systemCode,
            @Param("missionKey") Integer missionKey) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("systemCode", systemCode);
            parameter.put("missionKey", missionKey);

            // 結果返却
            return namedJdbcTemplate.queryForObject(TIME_SLOT_TYPE_QUERY, parameter, String.class);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }

    /**
     * OOCT参加可能者取得SQL定義.<br>
     * ミッション基本情報KEYに紐付くユーザIDを取得する。<br>
     * DCPMG#154-2 change 非仮確定者のみを取得対象とする
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getOoctParticipation(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_PARTICIPATION_QUERY, parameter, mapper);
    }

    /**
     * 有力候補者者(OOCT)昇順取得SQL定義.<br>
     * DCP選定情報（OOCT）から有力候補者者を昇順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoOoctIsStrongCandidateAsc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_IS_STRONG_CANDIDATE_QUERY_ASC, parameter, mapper);
    }

    /**
     * 有力候補者者(OOCT)降順取得SQL定義.<br>
     * DCP選定情報（OOCT）から有力候補者者を降順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoOoctIsStrongCandidateDesc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.query(OOCT_IS_STRONG_CANDIDATE_QUERY_DESC, parameter, mapper);
    }

    /**
     * 連続ミッション確認（OOCT）取得.<br>
     * DCP仮確定情報、ミッション基本情報から前後の時間帯区分にOOCTミッションが無い事を確認する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param userId ユーザID
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @return ミッション基本情報KEYリスト
     */
    public List<Mission> getConsecutiveMissionsOoct(@Param("missionKey") Integer missionKey,
            @Param("userId") Integer userId, @Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<Mission> mapper = new BeanPropertyRowMapper<>(Mission.class);
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);

        // 結果返却
        return namedJdbcTemplate.query(CONSECUTIVE_MISSIONS_OOCT_QUERY, parameter, mapper);
    }

    /**
     * 有力候補者用DCP選定情報（OOCT）更新.<br>
     * DCP選定情報（OOCT）情報の有力候補者用情報を更新する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @return 更新件数
     */
    public int updateDcpOoctStrongCandidate(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("isStrongCandidate", isStrongCandidate);
        parameter.put("conditionsScore", conditionsScore);
        parameter.put("anyCondition1", anyCondition1);
        parameter.put("isMetCondition1", isMetCondition1);
        parameter.put("anyCondition2", anyCondition2);
        parameter.put("isMetCondition2", isMetCondition2);
        parameter.put("anyCondition3", anyCondition3);
        parameter.put("isMetCondition3", isMetCondition3);

        // 結果返却
        return namedJdbcTemplate.update(DCP_OOCT_STRONG_CANDIDATE_UPDATE, parameter);
    }

    /**
     * 仮確定情報取得処理.<br>
     * 指定条件で仮確定情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 仮確定情報リスト
     */
    public List<TDcpProvisional> getDcpProvisionalInfo(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TDcpProvisional> mapper = new BeanPropertyRowMapper<>(TDcpProvisional.class);
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 実行
        return namedJdbcTemplate.query(DCP_PROVISIONAL_SELECT, parameter, mapper);
    }

    /**
     * ICT仮確定情報取得処理.<br>
     * 指定条件で仮確定情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 仮確定情報リスト
     */
    public List<TDcpProvisional> getIctDcpProvisionalInfo(@Param("assignedDate") Date assignedDate,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TDcpProvisional> mapper = new BeanPropertyRowMapper<>(TDcpProvisional.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 実行
        return namedJdbcTemplate.query(DCP_PROVISIONAL_SELECT_ICT, parameter, mapper);
    }

    /**
     * OOCTCT仮確定情報取得処理.<br>
     * 指定条件で仮確定情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 仮確定情報リスト
     */
    public List<TDcpProvisional> getOoctDcpProvisionalInfo(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TDcpProvisional> mapper = new BeanPropertyRowMapper<>(TDcpProvisional.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 実行
        return namedJdbcTemplate.query(DCP_PROVISIONAL_SELECT_OOCT, parameter, mapper);
    }

    /**
     * 手動割当情報取得処理.<br>
     * 指定条件で手動割当情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 手動割当情報リスト
     */
    public List<TDcpManualAssign> getDcpManualProvisionalInfo(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") List<String> dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // レコードマップを生成
        RowMapper<TDcpManualAssign> mapper = new BeanPropertyRowMapper<>(TDcpManualAssign.class);
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 実行
        return namedJdbcTemplate.query(DCP_MANUAL_PROVISIONAL_SELECT, parameter, mapper);
    }

    /**
     * DCP選定情報（ICT）削除.<br>
     * DCP選定情報（ICT）情報を削除する。<br>
     * DCPMG#154-2 change 非仮確定者のみを削除対象とする
     * 
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 更新件数
     */
    public int deleteDcpSelectIct(@Param("assignedDate") Date assignedDate,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.update(DCP_SELECT_ICT_DELETE, parameter);
    }

    /**
     * DCP選定情報（OOCT）削除.<br>
     * DCP選定情報（OOCT）情報を削除する。<br>
     * DCPMG#154-2 change 非仮確定者のみを削除対象とする
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 更新件数
     */
    public int deleteDcpSelectOoct(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);

        // 結果返却
        return namedJdbcTemplate.update(DCP_SELECT_OOCT_DELETE, parameter);
    }

    /**
     * DCP選定情報（ICT）登録.<br>
     * DCP選定情報（ICT）情報を登録する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param isStrongCandidate 有力候補フラグ
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * @param anyCondition5 個別条件5
     * @param isMetCondition5 条件合否5
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int inserteDcpSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4,
            @Param("anyCondition5") String anyCondition5,
            @Param("isMetCondition5") Boolean isMetCondition5,
            @Param("isDeleted") Boolean isDeleted, @Param("createdAt") Timestamp createdAt,
            @Param("createdBy") Integer createdBy, @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("isStrongCandidate", isStrongCandidate);
        parameter.put("conditionsScore", conditionsScore);
        parameter.put("anyCondition1", anyCondition1);
        parameter.put("isMetCondition1", isMetCondition1);
        parameter.put("anyCondition2", anyCondition2);
        parameter.put("isMetCondition2", isMetCondition2);
        parameter.put("anyCondition3", anyCondition3);
        parameter.put("isMetCondition3", isMetCondition3);
        parameter.put("anyCondition4", anyCondition4);
        parameter.put("isMetCondition4", isMetCondition4);
        parameter.put("anyCondition5", anyCondition5);
        parameter.put("isMetCondition5", isMetCondition5);
        parameter.put("isDeleted", isDeleted);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);
        parameter.put("updatedAt", updatedAt);
        parameter.put("updatedBy", updatedBy);

        // 結果返却
        return namedJdbcTemplate.update(DCP_SELECT_ICT_INSERT, parameter);
    }

    /**
     * DCP選定情報（OOCT）登録.<br>
     * DCP選定情報（OOCT）情報を登録する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param isStrongCandidate 有力候補フラグ
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * @param anyCondition5 個別条件5
     * @param isMetCondition5 条件合否5
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int inserteDcpSelectOoct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4,
            @Param("anyCondition5") String anyCondition5,
            @Param("isMetCondition5") Boolean isMetCondition5,
            @Param("isDeleted") Boolean isDeleted, @Param("createdAt") Timestamp createdAt,
            @Param("createdBy") Integer createdBy, @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("isStrongCandidate", isStrongCandidate);
        parameter.put("conditionsScore", conditionsScore);
        parameter.put("anyCondition1", anyCondition1);
        parameter.put("isMetCondition1", isMetCondition1);
        parameter.put("anyCondition2", anyCondition2);
        parameter.put("isMetCondition2", isMetCondition2);
        parameter.put("anyCondition3", anyCondition3);
        parameter.put("isMetCondition3", isMetCondition3);
        parameter.put("anyCondition4", anyCondition4);
        parameter.put("isMetCondition4", isMetCondition4);
        parameter.put("anyCondition5", anyCondition5);
        parameter.put("isMetCondition5", isMetCondition5);
        parameter.put("isDeleted", isDeleted);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);
        parameter.put("updatedAt", updatedAt);
        parameter.put("updatedBy", updatedBy);

        // 結果返却
        return namedJdbcTemplate.update(DCP_SELECT_OOCT_INSERT, parameter);
    }

    /**
     * DCP選定情報（ICT）取得.<br>
     * DCP選定情報（ICT）情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）リスト
     */
    public List<TDcpSelectIct> selectDcpSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        // レコードマップを生成
        RowMapper<TDcpSelectIct> mapper = new BeanPropertyRowMapper<>(TDcpSelectIct.class);

        // 結果返却
        return namedJdbcTemplate.query(DCP_SELECT_ICT_SELECT, parameter, mapper);
    }

    /**
     * DCP選定情報（OOCT）取得.<br>
     * DCP選定情報（OOCT）情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（OOCT）リスト
     */
    public List<TDcpSelectOoct> selectDcpSelectOoct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        // レコードマップを生成
        RowMapper<TDcpSelectOoct> mapper = new BeanPropertyRowMapper<>(TDcpSelectOoct.class);

        // 結果返却
        return namedJdbcTemplate.query(DCP_SELECT_OOCT_SELECT, parameter, mapper);
    }

    /**
     * DCP仮確定情報登録.<br>
     * DCP仮確定情報を登録する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param isStrongCandidate 有力候補フラグ
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * @param anyCondition5 個別条件5
     * @param isMetCondition5 条件合否5
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @return 登録件数
     */
    public int inserteDcpProvisional(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4,
            @Param("anyCondition5") String anyCondition5,
            @Param("isMetCondition5") Boolean isMetCondition5,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("userId", userId);
        parameter.put("assignedDate", assignedDate);
        parameter.put("timeSlotType", timeSlotType);
        parameter.put("missionKey", missionKey);
        parameter.put("dcpRoleType", dcpRoleType);
        parameter.put("isStrongCandidate", isStrongCandidate);
        parameter.put("conditionsScore", conditionsScore);
        parameter.put("anyCondition1", anyCondition1);
        parameter.put("isMetCondition1", isMetCondition1);
        parameter.put("anyCondition2", anyCondition2);
        parameter.put("isMetCondition2", isMetCondition2);
        parameter.put("anyCondition3", anyCondition3);
        parameter.put("isMetCondition3", isMetCondition3);
        parameter.put("anyCondition4", anyCondition4);
        parameter.put("isMetCondition4", isMetCondition4);
        parameter.put("anyCondition5", anyCondition5);
        parameter.put("isMetCondition5", isMetCondition5);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);

        try {
            // 結果返却
            return namedJdbcTemplate.update(DCP_PROVISIONAl_INSERT, parameter);
        } catch (DuplicateKeyException dke) {
            // 重複エラーの場合、応急処置としてエラーとはしない
            return 0;
        }
    }

    /**
     * DCP割当状況（BCO）更新.<br>
     * DCP割当状況のBCO情報を更新する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param requiredBcoAdmin 必要人数（管理者BCO）
     * @param requiredBco 必要人数（BCO）
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int updateDcpAssignStatusBco(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate,
            @Param("requiredBcoAdmin") Integer requiredBcoAdmin,
            @Param("requiredBco") Integer requiredBco, @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("testingDate", testingDate);
        parameter.put("requiredBcoAdmin", requiredBcoAdmin);
        parameter.put("requiredBco", requiredBco);
        parameter.put("updatedAt", updatedAt);
        parameter.put("updatedBy", updatedBy);

        // 結果返却
        return namedJdbcTemplate.update(DCP_ASSIGN_STATUS_BCO_UPDATE, parameter);
    }

    /**
     * DCP割当状況（DCO）更新.<br>
     * DCP割当状況のDCO情報を更新する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param requiredDco 必要人数（管理者DCO）
     * @param requiredDcoMale 必要人数（DCO男性）
     * @param requiredDcoFemale 必要人数（DCO女性）
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int updateDcpAssignStatusDco(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate,
            @Param("requiredDco") Integer requiredDco,
            @Param("requiredDcoMale") Integer requiredDcoMale,
            @Param("requiredDcoFemale") Integer requiredDcoFemale,
            @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("testingDate", testingDate);
        parameter.put("requiredDco", requiredDco);
        parameter.put("requiredDcoMale", requiredDcoMale);
        parameter.put("requiredDcoFemale", requiredDcoFemale);
        parameter.put("updatedAt", updatedAt);
        parameter.put("updatedBy", updatedBy);

        // 結果返却
        return namedJdbcTemplate.update(DCP_ASSIGN_STATUS_DCO_UPDATE, parameter);
    }

    /**
     * ICT参加可能日数取得SQL定義.<br>
     * DCPカレンダー情報から参加可能日数を取得する。
     * 
     * @param userId ユーザID
     * @param missionKey ミッション基本情報KEY
     * @return 日数
     */
    public AvailableDaysDto getIctAvailableDays(@Param("userId") Integer userId,
            @Param("missionKey") Integer missionKey) {

        try {
            // SQLパラメータ
            Map<String, Object> parameter = new HashMap<String, Object>();
            // パラメータ設定
            parameter.put("userId", userId);
            parameter.put("missionKey", missionKey);

            // 結果返却
            return namedJdbcTemplate.queryForObject(ICT_AVAILABLE_DAYS_SELECT, parameter, new AvailableDaysMapper());

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

            // QueryForObjectでデータが無い場合、nullを戻す
            return null;
        }
    }
}
