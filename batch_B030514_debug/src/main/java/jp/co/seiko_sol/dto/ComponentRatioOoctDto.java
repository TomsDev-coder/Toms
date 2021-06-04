//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : CompetitorsCountDto.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 自動アサインバッチ処理（ID：B030514）用のDCPランク構成比（ハイリスク用）DTOクラス.<br>
 * DCPランク構成比（ハイリスク用）によるDCOアサイン人数を保持する。
 * 
 * @author IIM
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ComponentRatioOoctDto {

    /** S1,S2,S3,A1ランク人数 */
    Integer asssigned_upper;

    /** A2ランク人数 */
    Integer asssigned_a2;
}
