//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : DcpType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DCP種別.<br>
 * DCP種別のコード、名称を保持する。
 *
 */
@AllArgsConstructor
@Getter
public enum DcpType {

    DCO_SENIOR("1", "シニアDCO"), DCO("2", "DCO"), DCO_TRAINEE("3", "研修DCO"), BCO_ADMIN("4",
            "管理者BCO"), BCO("5", "BCO"), MENTOR("6", "メンター"), SCO("7", "SCO"), IDCO("8", "IDCO");

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
        DcpType[] array = values();
        for (DcpType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
