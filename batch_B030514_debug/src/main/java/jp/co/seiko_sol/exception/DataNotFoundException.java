//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : DataNotFoundException.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.exception;

import lombok.AllArgsConstructor;

/**
 * データ無し例外クラス.<br>
 * 存在する筈のデータが取得出来なかった場合の例外を定義する。
 * 
 * @author IIM
 * @version 1.0
 */
@AllArgsConstructor
public class DataNotFoundException extends RuntimeException {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -4571495806865896822L;

    /**
     * メッセージを設定して例外を作成する。
     * 
     * @param msg メッセージ
     */
    public DataNotFoundException(String msg) {
        super(msg);
    }

    /**
     * メッセージ、発生した例外を設定して例外を作成する。
     * 
     * @param msg メッセージ
     * @param err 発生した例外
     */
    public DataNotFoundException(String msg, Throwable err) {
        super(msg, err);
    }
}
