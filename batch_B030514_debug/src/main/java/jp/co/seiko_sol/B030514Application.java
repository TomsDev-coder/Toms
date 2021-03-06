//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514Application.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 自動アサインバッチ処理（ID：B030514）用Applicationクラス.<br>
 * アプリケーションのメイン処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@SpringBootApplication
public class B030514Application {

	/**
	 * メイン処理.<BR>
	 * ここからApplicationが起動される。
	 * 
	 * @param args 起動パラメータ
	 * @throws Exception 処理中に例外が発生した場合
	 */
	public static void main(String[] args) throws Exception {
		// アプリケーション開始
		SpringApplication.run(B030514Application.class, args);
	}
}
