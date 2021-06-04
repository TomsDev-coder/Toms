//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TMissionBaseMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.domain.TMissionBase;

/**
 * ミッション基本情報用マッパークラス.<br>
 * "t_mission_base"テーブルをTMissionBaseにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class TMissionBaseMapper implements RowMapper<TMissionBase> {

    /**
     * マッピング処理.<br>
     * ResultSetをTMissionBaseにマッピングする。
     * 
     * @return ミッション基本情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public TMissionBase mapRow(ResultSet result, int rowNum) throws SQLException {

        // ミッション基本情報
        TMissionBase tMissionBase = new TMissionBase();

        // ミッション基本情報KEY
        tMissionBase.setMissionKey(result.getInt("mission_key"));
        // ミッションコード
        tMissionBase.setMissionCode(result.getString("mission_code"));
        // 年度
        tMissionBase.setFiscalYear(result.getString("fiscal_year"));
        // 検査種別
        tMissionBase.setTestingType(result.getString("testing_type"));
        // ミッションステータス
        tMissionBase.setMissionStatus(result.getString("mission_status"));
        // NFステータス
        tMissionBase.setNfStatus(result.getString("nf_status"));
        // ミッション確定フラグ（予定）
        tMissionBase.setIsMissionPlanFixed(result.getBoolean("is_mission_plan_fixed"));
        // ミッション確定日時（予定）
        tMissionBase.setMissionPlanFixedAt(result.getTimestamp("mission_plan_fixed_at"));
        // ミッション確定フラグ（実績）
        tMissionBase.setIsMissionResultFixed(result.getBoolean("is_mission_result_fixed"));
        // ミッション確定日時（実績）
        tMissionBase.setMissionResultFixedAt(result.getTimestamp("mission_result_fixed_at"));
        // 競技会名
        tMissionBase.setCompetitionName(result.getString("competition_name"));
        // 競技会名（英）
        tMissionBase.setCompetitionNameEng(result.getString("competition_name_eng"));
        // 検査管轄機関
        tMissionBase.setControlInspectionId(result.getInt("control_inspection_id"));
        // 検体採取機関
        tMissionBase.setCollectSamplesId(result.getInt("collect_samples_id"));
        // 結果管理機関
        tMissionBase.setManageResultsId(result.getInt("manage_results_id"));
        // 検査調整機関
        tMissionBase.setAdjustInspectionId(result.getInt("adjust_inspection_id"));
        // 大会関連HP
        tMissionBase.setCompetitionHomepage(result.getString("competition_homepage"));
        // 後方支援病院利用フラグ
        tMissionBase.setIsUsingSupportHospital(result.getBoolean("is_using_support_hospital"));
        // 契約先
        tMissionBase.setContractorId(result.getInt("contractor_id"));
        // ADAMSコード
        tMissionBase.setAdamsCode(result.getString("adams_code"));
        // 競技ID
        tMissionBase.setSportsId(result.getInt("sports_id"));
        // 種目ID
        tMissionBase.setDisciplineId(result.getInt("discipline_id"));
        // 団体ID
        tMissionBase.setAssociationId(result.getInt("association_id"));
        // 下部団体ID
        tMissionBase.setSubAssociationId(result.getInt("sub_association_id"));
        // 請求先区分
        tMissionBase.setBillingType(result.getString("billing_type"));
        // 検査日（FROM）
        tMissionBase.setTestingDateFrom(result.getDate("testing_date_from"));
        // 検査日（TO）
        tMissionBase.setTestingDateTo(result.getDate("testing_date_to"));
        // 検査日数
        tMissionBase.setTestingDays(result.getInt("testing_days"));
        // 検査実施区分
        tMissionBase.setTestExecuteType(result.getString("test_execute_type"));
        // 通告時刻
        tMissionBase.setNotificationTime(result.getTime("notification_time"));
        // DCP集合時刻 
        tMissionBase.setMeetingTime(result.getTime("meeting_time"));
        // DCP集合場所
        tMissionBase.setMeetingPlace(result.getString("meeting_place"));
        // 会場ID
        tMissionBase.setVenueId(result.getInt("venue_id"));
        // 検査会場都道府県コード 
        tMissionBase.setVenuePrefectureCode(result.getString("venue_prefecture_code"));
        // 検査会場市区町村
        tMissionBase.setVenueCity(result.getString("venue_city"));
        // 検査会場住所
        tMissionBase.setVenueAddress(result.getString("venue_address"));
        // プロセスルーム数
        tMissionBase.setProcessRooms(result.getInt("process_rooms"));
        // プロセスルーム数（血）
        tMissionBase.setProcessRoomsBlood(result.getInt("process_rooms_blood"));
        // 検査車両使用区分
        tMissionBase.setJadacarUseType(result.getString("jadacar_use_type"));
        // 検査車両使用日
        tMissionBase.setJadacarUseDate(result.getString("jadacar_use_date"));
        // 担当者1                                 
        tMissionBase.setUserId1(result.getInt("user_id1"));
        // 担当者2                                 
        tMissionBase.setUserId2(result.getInt("user_id2"));
        // 緊急連絡先1                                   
        tMissionBase.setEmergencyUserId1(result.getInt("emergency_user_id1"));
        // 緊急連絡先2                                   
        tMissionBase.setEmergencyUserId2(result.getInt("emergency_user_id2"));
        // 備考                                   
        tMissionBase.setRemarks(result.getString("remarks"));
        // 備考（JADA→DCP）                                 
        tMissionBase.setRemarksToDcp(result.getString("remarks_to_dcp"));
        // 検体数 尿                                    
        tMissionBase.setSamplesUrine(result.getInt("samples_urine"));
        // 検体数 血液（黄）                                    
        tMissionBase.setSamplesBloodY(result.getInt("samples_blood_y"));
        // 検体数 血液（紫）                                    
        tMissionBase.setSamplesBloodP(result.getInt("samples_blood_p"));
        // 研修DCO受け入れフラグ                                 
        tMissionBase.setIsAcceptTrainee(result.getBoolean("is_accept_trainee"));
        // 外国語区分
        tMissionBase.setLanguageType(result.getString("language_type"));
        // 発送メモ 
        tMissionBase.setKitSendMemo(result.getString("kit_send_memo"));
        // 送付先区分
        tMissionBase.setKitDestinationType(result.getString("kit_destination_type"));
        // 配送会社区分1
        tMissionBase.setDeliveryCompanyType1(result.getString("delivery_company_type1"));
        // キット発送日1
        tMissionBase.setKitSendDate1(result.getDate("kit_send_date1"));
        // キット到着日1
        tMissionBase.setKitArriveDate1(result.getDate("kit_arrive_date1"));
        // キット到着時刻1
        tMissionBase.setKitArriveTimeSlotType1(result.getString("kit_arrive_time_slot_type1"));
        // 郵便番号1
        tMissionBase.setKitSendPostalcode1(result.getString("kit_send_postalcode1"));
        // 都道府県コード1
        tMissionBase.setKitSendPrefectureCode1(result.getString("kit_send_prefecture_code1"));
        // 市区町村・番地1
        tMissionBase.setKitSendAddress1(result.getString("kit_send_address1"));
        // 施設名1
        tMissionBase.setKitSendBuilding1(result.getString("kit_send_building1"));
        // 所属名1
        tMissionBase.setKitSendOrganization1(result.getString("kit_send_organization1"));
        // 氏名1
        tMissionBase.setKitSendName1(result.getString("kit_send_name1"));
        // 電話番号1
        tMissionBase.setKitSendTelephone1(result.getString("kit_send_telephone1"));
        // メールアドレス（TO）1
        tMissionBase.setKitSendMailTo1(result.getString("kit_send_mail_to1"));
        // メールアドレス（CC）1
        tMissionBase.setKitSendMailCc1(result.getString("kit_send_mail_cc1"));
        // メールアドレス（BCC）1
        tMissionBase.setKitSendMailBcc1(result.getString("kit_send_mail_bcc1"));
        // 伝票記載事項1
        tMissionBase.setKitSendNotices1(result.getString("kit_send_notices1"));
        // 備考1
        tMissionBase.setKitSendRemarks1(result.getString("kit_send_remarks1"));
        // 発送先2表示区分
        tMissionBase.setKit2DisplayType(result.getString("kit2_display_type"));
        // 配送会社区分2
        tMissionBase.setDeliveryCompanyType2(result.getString("delivery_company_type2"));
        // キット発送日2
        tMissionBase.setKitSendDate2(result.getDate("kit_send_date2"));
        // キット到着日2
        tMissionBase.setKitArriveDate2(result.getDate("kit_arrive_date2"));
        // キット到着時刻2
        tMissionBase.setKitArriveTimeSlotType2(result.getString("kit_arrive_time_slot_type2"));
        // 郵便番号2
        tMissionBase.setKitSendPostalcode2(result.getString("kit_send_postalcode2"));
        // 都道府県コード2
        tMissionBase.setKitSendPrefectureCode2(result.getString("kit_send_prefecture_code2"));
        // 市区町村・番地2
        tMissionBase.setKitSendAddress2(result.getString("kit_send_address2"));
        // 施設名2
        tMissionBase.setKitSendBuilding2(result.getString("kit_send_building2"));
        // 所属名2
        tMissionBase.setKitSendOrganization2(result.getString("kit_send_organization2"));
        // 氏名2
        tMissionBase.setKitSendName2(result.getString("kit_send_name2"));
        // 電話番号2
        tMissionBase.setKitSendTelephone2(result.getString("kit_send_telephone2"));
        // メールアドレス（TO）2
        tMissionBase.setKitSendMailTo2(result.getString("kit_send_mail_to2"));
        // メールアドレス（CC）2
        tMissionBase.setKitSendMailCc2(result.getString("kit_send_mail_cc2"));
        // メールアドレス（BCC）2
        tMissionBase.setKitSendMailBcc2(result.getString("kit_send_mail_bcc2"));
        // 伝票記載事項2
        tMissionBase.setKitSendNotices2(result.getString("kit_send_notices2"));
        // 備考2
        tMissionBase.setKitSendRemarks2(result.getString("kit_send_remarks2"));
        // 削除フラグ                                    
        tMissionBase.setIsDeleted(result.getBoolean("is_deleted"));
        // システム登録日時
        tMissionBase.setCreatedAt(result.getTimestamp("created_at"));
        // システム登録者ID
        tMissionBase.setCreatedBy(result.getInt("created_by"));
        // システム最終更新日時
        tMissionBase.setUpdatedAt(result.getTimestamp("updated_at"));
        // システム最終更新者ID
        tMissionBase.setUpdatedBy(result.getInt("updated_by"));

        // 結果を返却
        return tMissionBase;
    }
}
