//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TBatchStatus.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.domain;

import java.io.Serializable;
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
 * バッチ処理ステータス用エンティティクラス.<br>
 * "t_batch_status"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_batch_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TBatchStatus implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 3767563769363000220L;

    /** 処理ID */
    @Id
    @Column(name = "process_id")
    private Integer processId;

    /** バッチID */
    @Column(name = "batch_id")
    private String batchId;

    /** ミッション基本情報KEY */
    @Column(name = "mission_key")
    private String missionKey;

    /** 処理ステータス */
    @Column(name = "process_status")
    private String processStatus;

    /** 処理ステータス日時 */
    @Column(name = "processed_at")
    private Timestamp processedAt;

    /** 処理ステータス詳細 */
    @Column(name = "process_status_details")
    private String processStatusDetails;

    /** システム登録日時 */
    @Column(name = "created_at")
    private Timestamp createdAt;

    /** システム登録者ID */
    @Column(name = "created_by")
    private String createdBy;
}
