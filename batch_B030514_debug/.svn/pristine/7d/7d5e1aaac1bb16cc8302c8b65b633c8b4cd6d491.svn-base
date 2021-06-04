//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TTestingDate.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * 検査日別情報用エンティティクラス.<br>
 * "t_batch_status"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_testing_date")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TTestingDate implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -4466559321018963530L;

    /** ミッション基本情報KEY */
    @Id
    @Column(name = "mission_key")
    private Integer missionKey;

    /** 検査日 */
    @Id
    @Column(name = "testing_date")
    private Date testingDate;

    /** 競技時刻（開始予定） */
    @Column(name = "planned_time_start")
    private Time planned_time_start;

    /** 競技時刻（終了予定） */
    @Column(name = "planned_time_end")
    private Time planned_time_end;

    /** DCP集合時刻 */
    @Column(name = "schedule_meeting_dcp")
    private Time schedule_meeting_dcp;

    /** シャペロン集合時刻 */
    @Column(name = "schedule_meeting_chaperone")
    private Time schedule_meeting_chaperone;

    /** 競技終了時刻 */
    @Column(name = "schedule_ending")
    private Time schedule_ending;

    /** 検査時間（FROM） */
    @Column(name = "schedule_testing_from")
    private Time schedule_testing_from;

    /** 検査時間（TO） */
    @Column(name = "schedule_testing_to")
    private Time schedule_testing_to;

    /** 表彰式開始時刻 */
    @Column(name = "schedule_ceremony")
    private String schedule_ceremony;

    /** シャペロン必要人数（男性） */
    @Column(name = "chaperone_required_male")
    private BigDecimal chaperone_required_male;

    /** シャペロン必要人数（女性） */
    @Column(name = "chaperone_required_female")
    private BigDecimal chaperone_required_female;

    /** シャペロン手配人数（男性） */
    @Column(name = "chaperone_assigned_male")
    private BigDecimal chaperone_assigned_male;

    /** シャペロン手配人数（女性） */
    @Column(name = "chaperone_assigned_female")
    private BigDecimal chaperone_assigned_female;

    /** シャペロン実績人数（男性） */
    @Column(name = "chaperone_actual_male")
    private BigDecimal chaperone_actual_male;

    /** シャペロン実績人数（女性） */
    @Column(name = "chaperone_actual_female")
    private BigDecimal chaperone_actual_female;

    /** 氏名1 */
    @Column(name = "nfrep_name1")
    private String nfrep_name1;

    /** 当日連絡先 */
    @Column(name = "nfrep_contact")
    private String nfrep_contact;

    /** 氏名2 */
    @Column(name = "nfrep_name2")
    private String nfrep_name2;

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
