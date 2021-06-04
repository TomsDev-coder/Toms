//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MDcpInformation.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
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
 * DCPユーザ付加情報マスタ用エンティティクラス.<br>
 * "m_dcp_information"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "m_dcp_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MDcpInformation implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -6781284335604452470L;

    /** テーブル日本語名 */
    public static final String ENTITY_NAME = "DCPユーザ付加情報";

    /** ユーザID */
    @Id
    @Column(name = "user_id")
    private Integer userId;

    /** 認定証番号 */
    @Column(name = "certification_no")
    private String certificationNo;

    /** 生年月日 */
    @Column(name = "birth_date")
    private Date birthDate;

    /** 性別 */
    @Column(name = "gender_type")
    private String genderType;

    /** 職種 */
    @Column(name = "job_type")
    private String jobType;

    /** 謝金辞退フラグ */
    @Column(name = "is_decline_reward")
    private Boolean isDeclineReward;

    /** 認定日 */
    @Column(name = "qualifiied_date")
    private Date qualifiiedDate;

    /** 使用可能言語（英語） */
    @Column(name = "can_speak_english")
    private Boolean canSpeakEnglish;

    /** 使用可能言語（中国語） */
    @Column(name = "can_speak_chinese")
    private Boolean canSpeakChinese;

    /** 使用可能言語（その他） */
    @Column(name = "can_speak_others")
    private String canSpeakOthers;

    /** 写真 */
    @Column(name = "portrait")
    private byte[] portrait;

    /** 郵便番号（現住所兼郵送先） */
    @Column(name = "postalcode1")
    private String postalcode1;

    /** 都道府県コード（現住所兼郵送先） */
    @Column(name = "prefecture_code1")
    private String prefectureCode1;

    /** 住所（現住所兼郵送先） */
    @Column(name = "address1")
    private byte[] address1;

    /** 郵便番号（住民票） */
    @Column(name = "postalcode2")
    private String postalcode2;

    /** 都道府県コード（住民票） */
    @Column(name = "prefecture_code2")
    private String prefectureCode2;

    /** 住所（住民票） */
    @Column(name = "address2")
    private byte[] address2;

    /** 郵便番号（郵送先） */
    @Column(name = "postalcode3")
    private String postalcode3;

    /** 都道府県コード（郵送先） */
    @Column(name = "prefecture_code3")
    private String prefectureCode3;

    /** 住所（郵送先） */
    @Column(name = "address3")
    private byte[] address3;

    /** 路線名1 */
    @Column(name = "route_name1")
    private String routeName1;

    /** 最寄駅1 */
    @Column(name = "nearest_station1")
    private String nearestStation1;

    /** 路線名2 */
    @Column(name = "route_name2")
    private String routeName2;

    /** 最寄駅2 */
    @Column(name = "nearest_station2")
    private String nearestStation2;

    /** 配達希望時間 */
    @Column(name = "arrive_time")
    private String arriveTime;

    /** DCPからの連絡事項 */
    @Column(name = "dcp_message")
    private String dcpMessage;

    /** JADA備考 */
    @Column(name = "jada_remarks")
    private String jadaRemarks;

    /** ICT確定率 */
    @Column(name = "ratio_ict")
    private BigDecimal ratioIct;

    /** OOCT確定率 */
    @Column(name = "ratio_ooct")
    private BigDecimal ratioOoct;

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
