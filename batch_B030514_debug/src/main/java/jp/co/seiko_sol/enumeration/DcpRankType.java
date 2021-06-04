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
public enum DcpRankType {

    S1("1", "S1"), S2("2", "S2"), S3("3", "S3"), A1("4", "A1"), A2("5", "A2"), B("6", "B");

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
        DcpRankType[] array = values();
        for (DcpRankType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
