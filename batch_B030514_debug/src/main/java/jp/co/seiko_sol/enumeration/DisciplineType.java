//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : DisciplineType.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 競技区分.<br>
 * 競技区分のコード、名称を保持する。
 *
 */
@AllArgsConstructor
@Getter
public enum DisciplineType {

    PERSONAL_RECORD("1", "個人記録"), PERSONAL_SCORE("2", "個人採点"), TOURNAMENT("3", "トーナメント"), TEAM("4",
            "チームスポーツ");

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
        DisciplineType[] array = values();
        for (DisciplineType dcptype : array) {
            if (code.equals(dcptype.getCode())) {
                return dcptype.value;
            }
        }
        return null;
    }
}
