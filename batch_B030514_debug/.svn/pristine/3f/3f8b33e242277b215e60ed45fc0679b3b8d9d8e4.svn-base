//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TestingType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 検査種別.<br>
 * 検査種別のコード、名称を保持する。
 *
 */
@AllArgsConstructor
@Getter
public enum TestingType {

    ICT("11", "ICT"), KOKUTAI("12", "国体"), PRE_OOCT("13", "Pre-OOCT"), OOCT("21",
            "OOCT"), OTHER("99", "その他");

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
        TestingType[] array = values();
        for (TestingType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
