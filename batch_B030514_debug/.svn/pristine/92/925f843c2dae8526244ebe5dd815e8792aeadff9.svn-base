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
 * アサインヘッダ情報用エンティティクラス.<br>
 * "t_assign_header"テーブルのデータをマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
@Entity
@Table(name = "t_assign_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TAssignHeader implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 6989379809797465125L;

    /** テーブル日本語名 */
    public static final String ENTITY_NAME = "アサインヘッダ情報";

    /** ミッション基本情報KEY */
    @Id
    @Column(name = "mission_key")
    private Integer missionKey;

    /** アサイン確定フラグ */
    @Column(name = "is_assign_fixed")
    private Boolean isAssignFixed;

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
