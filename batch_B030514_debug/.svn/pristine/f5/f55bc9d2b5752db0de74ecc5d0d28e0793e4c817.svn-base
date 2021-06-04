//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MDcpSelectOoct.java
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
 * DCP選定情報（OOCT）情報用エンティティクラス.<br>
 * "t_dcp_select_ooct"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_dcp_select_ooct")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TDcpSelectOoct implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -23700648987922577L;

    /** ユーザID */
    @Id
    @Column(name = "user_id")
    private Integer userId;

    /** 日付 */
    @Id
    @Column(name = "assigned_date")
    private Date assignedDate;

    /** 時間帯区分 */
    @Id
    @Column(name = "time_slot_type")
    private String timeSlotType;

    /** ミッション基本情報KEY */
    @Id
    @Column(name = "mission_key")
    private Integer missionKey;

    /** 役割区分 */
    @Id
    @Column(name = "dcp_role_type")
    private String dcpRoleType;

    /** 有力候補フラグ */
    @Column(name = "is_strong_candidate")
    private Boolean isStrongCandidate;

    /** 条件合致点数 */
    @Column(name = "conditions_score")
    private Integer conditionsScore;

    /** 個別条件1 */
    @Column(name = "any_condition1")
    private String anyCondition1;

    /** 条件合否1 */
    @Column(name = "is_met_condition1")
    private Boolean isMetCondition1;

    /** 個別条件2 */
    @Column(name = "any_condition2")
    private String anyCondition2;

    /** 条件合否2 */
    @Column(name = "is_met_condition2")
    private Boolean isMetCondition2;

    /** 個別条件3 */
    @Column(name = "any_condition3")
    private String anyCondition3;

    /** 条件合否3 */
    @Column(name = "is_met_condition3")
    private Boolean isMetCondition3;

    /** 個別条件4 */
    @Column(name = "any_condition4")
    private String anyCondition4;

    /** 条件合否4 */
    @Column(name = "is_met_condition4")
    private Boolean isMetCondition4;

    /** 個別条件5 */
    @Column(name = "any_condition5")
    private String anyCondition5;

    /** 条件合否5 */
    @Column(name = "is_met_condition5")
    private Boolean isMetCondition5;

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
