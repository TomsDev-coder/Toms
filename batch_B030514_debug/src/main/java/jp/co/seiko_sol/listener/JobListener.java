//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : JobListener.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.listener;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jp.co.seiko_sol.B030514Const;

/**
 * 自動アサインバッチ処理（ID：B030514）用Listenerクラス.<br>
 * 任意でJob前後の処理に行う処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class JobListener extends JobExecutionListenerSupport {

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(JobListener.class);

    /**
     * コンストラクタ.<BR>
     * 
     */
    public JobListener() {}

    /**
     * 自動アサインバッチ処理（ID：B030514）実施前に呼び出される処理.<BR>
     * 自動アサインバッチ処理（ID：B030514）実施前に行う処理があれば記述する。
     * 
     * @param jobExecution Job実行コンテキスト
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_JOB,
                new String[] {B030514Const.PROPERTY_KEY_JOB_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        // 実行前処理
        super.beforeJob(jobExecution);
    }

    /**
     * 自動アサインバッチ処理（ID：B030514）実施後に呼び出される処理.<BR>
     * 自動アサインバッチ処理（ID：B030514）実施後に行う処理があれば記述する。
     * 
     * @param jobExecution Job実行コンテキスト
     */
    @Override
    public void afterJob(JobExecution jobExecution) {

        // 実行後処理
        super.afterJob(jobExecution);

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_JOB,
                new String[] {B030514Const.PROPERTY_KEY_JOB_NAME}, Locale.JAPANESE);

        log.info(endMessage);
    }
}
