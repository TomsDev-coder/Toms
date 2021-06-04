//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TimeSlotType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 時間帯区分.<br>
 * 時間帯区分のコード、名称を保持する.
 *
 */
@AllArgsConstructor
@Getter
public enum TimeSlotType {

    ALLDAY("0", "終日"), EARLY("1", "早朝"), MORNING("2", "AM"), AFTERNOON("3", "PM"), EVENING("4",
            "夜間");

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
        TimeSlotType[] array = values();
        for (TimeSlotType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
