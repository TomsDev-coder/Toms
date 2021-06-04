//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : AvailableDays.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright ©2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 自動アサインバッチ処理（ID：B030514）用の回数情報DTOクラス.<br>
 * 参加可能日数を保持する。
 * 
 * @author IIM
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AvailableDaysDto {

    /** 参加可能日数 */
    Long days;
}
