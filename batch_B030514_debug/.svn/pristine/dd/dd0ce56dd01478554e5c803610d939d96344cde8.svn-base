//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : StrongCandidateType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA . All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 有力候補区分.<br>
 * 有力候補区分のコード値を保持する.
 *
 */
@AllArgsConstructor
@Getter
public enum StrongCandidateType {

    // DCPMG#144  備考の区分を追加 start
    PAST_MISSIONS("01", "競"), FOREIGN_LANGUAGE("02", "外"), DISTANCE_SUITABILITY("03",
            "距"), PAST_MISSIONS_DCO("04", "経"), REMARKS_WRITTEN("05","備");
    // DCPMG#144 end

    /** 区分値 */
    private final String code;
    /** 区分名 */
    private final String value;

    /**
     * 区分値に対応した区分名を返却する.
     * 
     * @param code 区分値
     * @return 区分名
     */
    public static String valueOfByName(String code) {
        StrongCandidateType[] array = values();
        for (StrongCandidateType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
