//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TDcpAssignStatus.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DCP割当状況用エンティティクラス.<br>
 * "t_dcp_assign_status"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_dcp_assign_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TDcpAssignStatus {

    /** ミッション基本情報KEY */
    @Id
    @Column(name = "mission_key")
    private Integer missionKey;

    /** 検査日 */
    @Id
    @Column(name = "testing_date")
    private Date testingDate;

    /** 必要人数（リードDCO） */
    @Column(name = "required_dco_lead")
    private Integer requiredDcoLead;

    /** 必要人数（DCO） */
    @Column(name = "required_dco")
    private Integer requiredDco;

    /** 必要人数（DCO男性） */
    @Column(name = "required_dco_male")
    private Integer requiredDcoMale;

    /** 必要人数（DCO女性） */
    @Column(name = "required_dco_female")
    private Integer requiredDcoFemale;

    /** 必要人数（管理者BCO） */
    @Column(name = "required_bco_admin")
    private Integer requiredBcoAdmin;

    /** 必要人数（BCO） */
    @Column(name = "required_bco")
    private Integer requiredBco;

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
