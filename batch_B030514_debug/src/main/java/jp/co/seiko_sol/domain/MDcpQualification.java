//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MDcpQualification.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.io.Serializable;
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
 * DCP評価資格情報マスタ用エンティティクラス.<br>
 * "m_dcp_qualification"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "m_dcp_qualification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MDcpQualification implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 2856863603258169976L;

    /** テーブル日本語名 */
    public static final String ENTITY_NAME = "DCP評価資格情報";

    /** ユーザID */
    @Id
    @Column(name = "user_id")
    private Integer userId;

    /** DCPランク */
    @Column(name = "dcp_rank")
    private String dcpRank;

    // 検査対応評価（取り組む姿勢）
    @Column(name = "evaluation_score01")
    private Integer evaluationScore01;

    // 検査対応評価（ルールの理解）
    @Column(name = "evaluation_score02")
    private Integer evaluationScore02;

    // 検査対応評価（やりきる力）
    @Column(name = "evaluation_score03")
    private Integer evaluationScore03;

    // 検査対応評価（調整力・コミュ力）
    @Column(name = "evaluation_score04")
    private Integer evaluationScore04;

    // 検査対応評価（社会性を備えた人間力）
    @Column(name = "evaluation_score05")
    private Integer evaluationScore05;

    // 検査対応評価（評価項目06）
    @Column(name = "evaluation_score06")
    private Integer evaluationScore06;

    // 検査対応評価（評価項目07）
    @Column(name = "evaluation_score07")
    private Integer evaluationScore07;

    // 検査対応評価（評価項目08）
    @Column(name = "evaluation_score08")
    private Integer evaluationScore08;

    // 検査対応評価（評価項目09）
    @Column(name = "evaluation_score09")
    private Integer evaluationScore09;

    // 検査対応評価（評価項目10）
    @Column(name = "evaluation_score10")
    private Integer evaluationScore10;

    /** 次回更新年度 */
    @Column(name = "renew_year")
    private String renewYear;

    /** 講習会受講フラグ */
    @Column(name = "is_attend_workshop")
    private Boolean isAttendWorkshop;

    /** 講習会受講日 */
    @Column(name = "workshop_attended_date")
    private Date workshopAttendedDate;

    /** 資格更新申請区分 */
    @Column(name = "renew_request_type")
    private String renewRequestType;

    /** 差戻理由 */
    @Column(name = "reason_for_rejection")
    private String reasonForRejection;

    /** DCP種別（シニアDCO） */
    @Column(name = "is_dco_senior")
    private Boolean isDcoSenior;

    /** DCP種別（DCO） */
    @Column(name = "is_dco")
    private Boolean isDco;

    /** DCP種別（研修DCO） */
    @Column(name = "is_dco_trainee")
    private Boolean isDcoTrainee;

    /** DCP種別（管理者BCO） */
    @Column(name = "is_bco_admin")
    private Boolean isBcoAdmin;

    /** DCP種別（BCO） */
    @Column(name = "is_bco")
    private Boolean isBco;

    /** DCP種別（メンター） */
    @Column(name = "is_mentor")
    private Boolean isMentor;

    /** DCP種別（SCO） */
    @Column(name = "is_sco")
    private Boolean isSco;

    /** DCP種別（IDCO） */
    @Column(name = "is_idco")
    private Boolean isIdco;

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
