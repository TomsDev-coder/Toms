//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : BatchRunTimeException.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.exception;

import lombok.AllArgsConstructor;

/**
 * バッチ用実行例外クラス.<br>
 * バッチで実行時に発生する例外を定義する。
 * 
 * @author IIM
 * @version 1.0
 */
@AllArgsConstructor
public class BatchRunTimeException extends RuntimeException {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 2460039804749006210L;

    /**
     * メッセージを設定して例外を作成する。
     * 
     * @param msg メッセージ
     */
    public BatchRunTimeException(String msg) {
        super(msg);
    }

    /**
     * メッセージ、発生した例外を設定して例外を作成する。
     * 
     * @param msg メッセージ
     * @param err 発生した例外
     */
    public BatchRunTimeException(String msg, Throwable err) {
        super(msg, err);
    }
}
