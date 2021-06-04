//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : UrineSampleCountDto.java
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
 * 自動アサインバッチ処理（ID：B030514）用の尿検体数情報DTOクラス.<br>
 * 男女競技者数及び男女毎の検体数を保持する。
 * 
 * @author IIM
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UrineSampleCountDto {

    /** 競技者数（男性）総数 */
    Integer male_competitors;

    /** 競技者数（女性）総数 */
    Integer female_competitors;

    /** 尿検体数（男性）総数 */
    Integer male_urine_sample;

    /** 尿検体数（女性）総数 */
    Integer female_urine_sample;
}
