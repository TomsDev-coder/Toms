//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : DcpRoleType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 役割区分.<br>
 * 役割区分のコード、名称を保持する。
 *
 */
@AllArgsConstructor
@Getter
public enum DcpRoleType {

    DCO_LEAD("01", "リードDCO"), DCO("02", "DCO"), DCO_TRAINEE("03", "研修DCO"), DCO_INSTRUCT("04",
            "指導DCO"), MENTOR("05", "メンター"), BCO_ADMIN("06", "管理者BCO"), BCO("07",
                    "BCO"), DCO_BCO("08", "DCO/BCO"), BCO_SCO("09", "BCO/SCO"), SCO("10",
                            "SCO"), IDCO("11", "IDCO"), INSPECTION("12",
                                    "視察"), MEETING_1H("13", "会議(1H)"), MEETING_2H("14", "会議(2H)");

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
        DcpRoleType[] array = values();
        for (DcpRoleType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
