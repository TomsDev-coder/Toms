//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TDcpCalendar.java
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
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DCPカレンダー情報用エンティティクラス.<br>
 * "t_dcp_calendar"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_dcp_calendar")
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TDcpCalendar implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 7134111048808122081L;

    /** テーブル日本語名 */
    public static final String ENTITY_NAME = "DCPカレンダー情報";

    /** ユーザID */
    @Column(name = "user_id")
    private Integer userId;

    /** 日付 */
    @Column(name = "assigned_date")
    private Date assignedDate;

    /** 参加可能フラグ（終日） */
    @Column(name = "is_attend_allday")
    private Boolean isAttendAllday;

    /** 参加可能フラグ（早朝） */
    @Column(name = "is_attend_early")
    private Boolean isAttendEarly;

    /** 参加可能フラグ（AM） */
    @Column(name = "is_attend_morning")
    private Boolean isAttendMorning;

    /** 参加可能フラグ（PM） */
    @Column(name = "is_attend_afternoon")
    private Boolean isAttendAfternoon;

    /** 参加可能フラグ（夜間） */
    @Column(name = "is_attend_evening")
    private Boolean isAttendEvening;

    /** 講習会フラグ */
    @Column(name = "is_planned_class")
    private Boolean isPlannedClass;

    /** 宿泊可能フラグ */
    @Column(name = "is_able_stay")
    private Boolean isAbleStay;

    /** 備考記載フラグ */
    @Column(name = "is_remarks_written")
    private Boolean isRemarksWritten;

    /** 備考 */
    @Column(name = "remarks")
    private String remarks;

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
