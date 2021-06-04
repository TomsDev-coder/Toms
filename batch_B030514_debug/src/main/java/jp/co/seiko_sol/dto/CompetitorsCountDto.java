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
 * 自動アサインバッチ処理（ID：B030514）用の競技者数情報DTOクラス.<br>
 * ミッション、検査日毎の男女競技者数を保持する。
 * 
 * @author IIM
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CompetitorsCountDto {

    /** 競技者数（男性）総数 */
    Integer males;

    /** 競技者数（女性）総数 */
    Integer females;
}
