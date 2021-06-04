//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TSystemDefaults.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.io.Serializable;
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
 * システム設定マスタ用エンティティクラス.<br>
 * "m_system_defaults"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "m_system_defaults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MSystemDefaults implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -9096259730748777608L;

    /** テーブル日本語名 */
    public static final String ENTITY_NAME = "システム設定マスタ";

    /** システム区分 */
    @Id
    @Column(name = "system_code")
    private String systemCode;

    /** アサイン条件（前泊可能） */
    @Column(name = "is_able_stay_before")
    private Boolean isAbleStayBefore;

    /** アサイン条件（後泊可能） */
    @Column(name = "is_able_stay_night")
    private Boolean isAbleStayNight;

    /** アサイン条件（検査期間） */
    @Column(name = "participation_ratio")
    private Integer participationRatio;

    /** アサイン条件（リードDCOランク S1） */
    @Column(name = "is_select_s1")
    private Boolean isSelectS1;

    /** アサイン条件（リードDCOランク S2） */
    @Column(name = "is_select_s2")
    private Boolean isSelectS2;

    /** アサイン条件（リードDCOランク S3） */
    @Column(name = "is_select_s3")
    private Boolean isSelectS3;

    /** DCP閾値（DCO基準 ICT） */
    @Column(name = "dco_border_ict")
    private Integer dcoBorderIct;

    /** DCP閾値（DCO基準 OOCT） */
    @Column(name = "dco_border_ooct")
    private Integer dcoBorderOoct;

    /** DCP閾値（BCO基準 ICT） */
    @Column(name = "bco_border_ict")
    private Integer bcoBorderIct;

    /** DCP閾値（BCO基準 OOCT） */
    @Column(name = "bco_border_ooct")
    private Integer bcoBorderOoct;

    /** 連続アサイン可能日数 */
    @Column(name = "continuous_days_limit")
    private Integer continuousDaysLimit;

    /** 早朝時間帯（開始） */
    @Column(name = "early_time_from")
    private Time earlyTimeFrom;

    /** 早朝時間帯（終了） */
    @Column(name = "early_time_to")
    private Time earlyTimeTo;

    /** AM時間帯（開始） */
    @Column(name = "morning_time_from")
    private Time morningTimeFrom;

    /** AM時間帯（終了） */
    @Column(name = "morning_time_to")
    private Time morningTimeTo;

    /** PM時間帯（開始） */
    @Column(name = "afternoon_time_from")
    private Time afternoonTimeFrom;

    /** PM時間帯（終了） */
    @Column(name = "afternoon_time_to")
    private Time afternoonTimeTo;

    /** 夜間時間帯（開始） */
    @Column(name = "evening_time_from")
    private Time eveningTimeFrom;

    /** 夜間時間帯（終了） */
    @Column(name = "evening_time_to")
    private Time eveningTimeTo;

    /** 宿泊可能フラグ */
    @Column(name = "is_able_stay")
    private Boolean isAbleStay;

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
