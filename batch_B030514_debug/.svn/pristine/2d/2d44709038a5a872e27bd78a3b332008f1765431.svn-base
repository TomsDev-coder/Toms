//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TMissionSortIct.java
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
 * ソート結果（ICT）用エンティティクラス.<br>
 * "t_mission_sort_ict"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_mission_sort_ict")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TMissionSortIct implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 3767563769363000220L;

    /** ソートID */
    @Id
    @Column(name = "sort_id")
    Integer sortId;

    /** JOB番号ミッション基本情報KEY */
    @Column(name = "mission_key")
    Integer missionKey;

    /** システム登録日時 */
    @Column(name = "created_at")
    Timestamp createdAt;

    /** システム登録者ID */
    @Column(name = "created_by")
    Integer createdBy;
}
