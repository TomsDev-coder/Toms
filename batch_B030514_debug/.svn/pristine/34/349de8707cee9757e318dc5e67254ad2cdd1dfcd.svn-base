//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : LanguageType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 外国語区分.<br>
 * 外国語区分のコード、名称を保持する.
 *
 */
@AllArgsConstructor
@Getter
public enum LanguageType {

    ENGLISH("1", "英語"), CHINESE("2", "中国語"), BOTH("3", "両方"), NONE("9", "なし");

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
        LanguageType[] array = values();
        for (LanguageType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
