//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : DcpRankType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DCPランク区分.<br>
 * DCPランク区分のコード、名称を保持する。
 *
 */
@AllArgsConstructor
@Getter
public enum ProcessStatusType {

    REQUEST("1", "処理開始要求"), START("2", "処理開始"), NORMAL_END("3", "処理正常終了"), ABNORMAL_END("9",
            "処理異常終了");

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
        ProcessStatusType[] array = values();
        for (ProcessStatusType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
