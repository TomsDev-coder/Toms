//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MissionSortDto.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 自動アサインバッチ処理（ID：B030514）用のミッションソート結果DTOクラス.<br>
 * ミッションソート結果を保持する。
 * 
 * @author IIM
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MissionSortDto {

    /** ソートID */
    Integer sortId;

    /** JOB番号ミッション基本情報KEY */
    Integer missionKey;

    /** システム登録日時 */
    Timestamp createdAt;

    /** システム登録者ID */
    Integer createdBy;
}
