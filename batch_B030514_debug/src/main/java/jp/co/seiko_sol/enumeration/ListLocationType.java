//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : ListLocationType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 取得場所区分.<br>
 * 取得場所区分のコード、名称を保持する。
 *
 */
@AllArgsConstructor
@Getter
public enum ListLocationType {

    FIRST("1", "最初"), MIDDLE("2", "中間"), LAST("3", "最後");

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
        ListLocationType[] array = values();
        for (ListLocationType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
