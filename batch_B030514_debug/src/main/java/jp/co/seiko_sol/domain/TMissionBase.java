//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TMissionBase.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * ミッション基本情報用エンティティクラス.<br>
 * "t_mission_base"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_mission_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TMissionBase implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -8995417693514027510L;

    /** テーブル日本語名 */
    public static final String ENTITY_NAME = "ミッション基本情報";

    /** ミッション基本情報KEY */
    @Id
    @Column(name = "mission_key")
    private Integer missionKey;

    /** ミッションコード */
    @Column(name = "mission_code")
    private String missionCode;

    /** 年度 */
    @Column(name = "fiscal_year")
    private String fiscalYear;

    /** 検査種別 */
    @Column(name = "testing_type")
    private String testingType;

    /** ミッションステータス */
    @Column(name = "mission_status")
    private String missionStatus;

    /** NFステータス */
    @Column(name = "nf_status")
    private String nfStatus;

    /** ミッション確定フラグ（予定） */
    @Column(name = "is_mission_plan_fixed")
    private Boolean isMissionPlanFixed;

    /** ミッション確定日時（予定） */
    @Column(name = "mission_plan_fixed_at")
    private Timestamp missionPlanFixedAt;

    /** ミッション確定フラグ（実績） */
    @Column(name = "is_mission_result_fixed")
    private Boolean isMissionResultFixed;

    /** ミッション確定日時（実績） */
    @Column(name = "mission_result_fixed_at")
    private Timestamp missionResultFixedAt;

    /** 競技会名 */
    @Column(name = "competition_name")
    private String competitionName;

    /** 競技会名（英） */
    @Column(name = "competition_name_eng")
    private String competitionNameEng;

    /** 検査管轄機関 */
    @Column(name = "control_inspection_id")
    private Integer controlInspectionId;

    /** 検体採取機関 */
    @Column(name = "collect_samples_id")
    private Integer collectSamplesId;

    /** 結果管理機関 */
    @Column(name = "manage_results_id")
    private Integer manageResultsId;

    /** 検査調整機関 */
    @Column(name = "adjust_inspection_id")
    private Integer adjustInspectionId;

    /** 大会関連HP */
    @Column(name = "competition_homepage")
    private String competitionHomepage;

    /** 後方支援病院利用フラグ */
    @Column(name = "is_using_support_hospital")
    private Boolean isUsingSupportHospital;

    /** 契約先 */
    @Column(name = "contractor_id")
    private Integer contractorId;

    /** ADAMSコード */
    @Column(name = "adams_code")
    private String adamsCode;

    /** 競技ID */
    @Column(name = "sports_id")
    private Integer sportsId;

    /** 種目ID */
    @Column(name = "discipline_id")
    private Integer disciplineId;

    /** 団体ID */
    @Column(name = "association_id")
    private Integer associationId;

    /** 下部団体ID */
    @Column(name = "sub_association_id")
    private Integer subAssociationId;

    /** 請求先区分 */
    @Column(name = "billing_type")
    private String billingType;

    /** 検査日（FROM） */
    @Column(name = "testing_date_from")
    private Date testingDateFrom;

    /** 検査日（TO） */
    @Column(name = "testing_date_to")
    private Date testingDateTo;

    /** 検査日数 */
    @Column(name = "testing_days")
    private Integer testingDays;

    /** 検査実施区分 */
    @Column(name = "test_execute_type")
    private String testExecuteType;

    /** 通告時刻 */
    @Column(name = "notification_time")
    private Time notificationTime;

    /** DCP集合時刻 */
    @Column(name = "meeting_time")
    private Time meetingTime;

    /** DCP集合場所 */
    @Column(name = "meeting_place")
    private String meetingPlace;

    /** 会場ID */
    @Column(name = "venue_id")
    private Integer venueId;

    /** 検査会場都道府県コード */
    @Column(name = "venue_prefecture_code")
    private String venuePrefectureCode;

    /** 検査会場市区町村 */
    @Column(name = "venue_city")
    private String venueCity;

    /** 検査会場住所 */
    @Column(name = "venue_address")
    private String venueAddress;

    /** プロセスルーム数 */
    @Column(name = "process_rooms")
    private Integer processRooms;

    /** プロセスルーム数（血） */
    @Column(name = "process_rooms_blood")
    private Integer processRoomsBlood;

    /** 検査車両使用区分 */
    @Column(name = "jadacar_use_type")
    private String jadacarUseType;

    /** 検査車両使用日 */
    @Column(name = "jadacar_use_date")
    private String jadacarUseDate;

    /** 担当者1 */
    @Column(name = "user_id1")
    private Integer userId1;

    /** 担当者2 */
    @Column(name = "user_id2")
    private Integer userId2;

    /** 緊急連絡先1 */
    @Column(name = "emergency_user_id1")
    private Integer emergencyUserId1;

    /** 緊急連絡先2 */
    @Column(name = "emergency_user_id2")
    private Integer emergencyUserId2;

    /** 備考 */
    @Column(name = "remarks")
    private String remarks;

    /** 備考（JADA→DCP） */
    @Column(name = "remarks_to_dcp")
    private String remarksToDcp;

    /** 検体数 尿 */
    @Column(name = "samples_urine")
    private Integer samplesUrine;

    /** 検体数 血液（黄） */
    @Column(name = "samples_blood_y")
    private Integer samplesBloodY;

    /** 検体数 血液（紫） */
    @Column(name = "samples_blood_p")
    private Integer samplesBloodP;

    /** 研修DCO受け入れフラグ */
    @Column(name = "is_accept_trainee")
    private Boolean isAcceptTrainee;

    /** 外国語区分 */
    @Column(name = "language_type")
    private String languageType;

    /** 発送メモ */
    @Column(name = "kit_send_memo")
    private String kitSendMemo;

    /** 送付先区分 */
    @Column(name = "kit_destination_type")
    private String kitDestinationType;

    /** 配送会社区分1 */
    @Column(name = "delivery_company_type1")
    private String deliveryCompanyType1;

    /** キット発送日1 */
    @Column(name = "kit_send_date1")
    private Date kitSendDate1;

    /** キット到着日1 */
    @Column(name = "kit_arrive_date1")
    private Date kitArriveDate1;

    /** キット到着時刻1 */
    @Column(name = "kit_arrive_time_slot_type1")
    private String kitArriveTimeSlotType1;

    /** 郵便番号1 */
    @Column(name = "kit_send_postalcode1")
    private String kitSendPostalcode1;

    /** 都道府県コード1 */
    @Column(name = "kit_send_prefecture_code1")
    private String kitSendPrefectureCode1;

    /** 市区町村・番地1 */
    @Column(name = "kit_send_address1")
    private String kitSendAddress1;

    /** 施設名1 */
    @Column(name = "kit_send_building1")
    private String kitSendBuilding1;

    /** 所属名1 */
    @Column(name = "kit_send_organization1")
    private String kitSendOrganization1;

    /** 氏名1 */
    @Column(name = "kit_send_name1")
    private String kitSendName1;

    /** 電話番号1 */
    @Column(name = "kit_send_telephone1")
    private String kitSendTelephone1;

    /** メールアドレス（TO）1 */
    @Column(name = "kit_send_mail_to1")
    private String kitSendMailTo1;

    /** メールアドレス（CC）1 */
    @Column(name = "kit_send_mail_cc1")
    private String kitSendMailCc1;

    /** メールアドレス（BCC）1 */
    @Column(name = "kit_send_mail_bcc1")
    private String kitSendMailBcc1;

    /** 伝票記載事項1 */
    @Column(name = "kit_send_notices1")
    private String kitSendNotices1;

    /** 備考1 */
    @Column(name = "kit_send_remarks1")
    private String kitSendRemarks1;

    /** 発送先2表示区分 */
    @Column(name = "kit2_display_type")
    private String kit2DisplayType;

    /** 配送会社区分2 */
    @Column(name = "delivery_company_type2")
    private String deliveryCompanyType2;

    /** キット発送日2 */
    @Column(name = "kit_send_date2")
    private Date kitSendDate2;

    /** キット到着日2 */
    @Column(name = "kit_arrive_date2")
    private Date kitArriveDate2;

    /** キット到着時刻2 */
    @Column(name = "kit_arrive_time_slot_type2")
    private String kitArriveTimeSlotType2;

    /** 郵便番号2 */
    @Column(name = "kit_send_postalcode2")
    private String kitSendPostalcode2;

    /** 都道府県コード2 */
    @Column(name = "kit_send_prefecture_code2")
    private String kitSendPrefectureCode2;

    /** 市区町村・番地2 */
    @Column(name = "kit_send_address2")
    private String kitSendAddress2;

    /** 施設名2 */
    @Column(name = "kit_send_building2")
    private String kitSendBuilding2;

    /** 所属名2 */
    @Column(name = "kit_send_organization2")
    private String kitSendOrganization2;

    /** 氏名2 */
    @Column(name = "kit_send_name2")
    private String kitSendName2;

    /** 電話番号2 */
    @Column(name = "kit_send_telephone2")
    private String kitSendTelephone2;

    /** メールアドレス（TO）2 */
    @Column(name = "kit_send_mail_to2")
    private String kitSendMailTo2;

    /** メールアドレス（CC）2 */
    @Column(name = "kit_send_mail_cc2")
    private String kitSendMailCc2;

    /** メールアドレス（BCC）2 */
    @Column(name = "kit_send_mail_bcc2")
    private String kitSendMailBcc2;

    /** 伝票記載事項2 */
    @Column(name = "kit_send_notices2")
    private String kitSendNotices2;

    /** 備考2 */
    @Column(name = "kit_send_remarks2")
    private String kitSendRemarks2;

    /** 削除フラグ */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    /** システム登録日時 */
    @Column(name = "created_at")
    private Timestamp createdAt;

    /** システム登録者ID */
    @Column(name = "created_by")
    private Integer createdBy;

    /** システム最終更新日時 */
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    /** システム最終更新者ID */
    @Column(name = "updated_by")
    private Integer updatedBy;
}
