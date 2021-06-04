//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : GenderType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性別区分.<br>
 * 性別区分のコード、名称を保持する.
 *
 */
@AllArgsConstructor
@Getter
public enum GenderType {

    MALE("1", "男性"), FEMALE("2", "女性"), BOTH("3", "両方");

    /** コード */
    private final String code;
    /** 名称 */
    private final String value;

    /**
     * コードに対応した名称を返却する.
     * 
     * @param code コード
     * @return 名称
     */
    public static String valueOfByName(String code) {
        GenderType[] array = values();
        for (GenderType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
