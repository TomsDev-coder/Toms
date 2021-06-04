//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : BatchException.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.exception;

/**
 * バッチ処理用例外クラス.<br>
 * バッチ処理で利用する例外を定義する。
 * 
 * @author IIM
 * @version 1.0
 */
public class BatchException extends Exception {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = -9055085836129825218L;

    /**
     * メッセージを設定して例外を作成する。
     * 
     * @param msg メッセージ
     */
    public BatchException(String msg) {
        super(msg);
    }

    /**
     * メッセージ、発生した例外を設定して例外を作成する。
     * 
     * @param msg メッセージ
     * @param err 発生した例外
     */
    public BatchException(String msg, Throwable err) {
        super(msg, err);
    }
}
