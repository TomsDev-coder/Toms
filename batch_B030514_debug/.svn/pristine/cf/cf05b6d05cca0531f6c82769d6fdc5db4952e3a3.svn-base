//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : StepListener.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA . All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.listener;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jp.co.seiko_sol.B030514Const;

/**
 * ステップ用Listenerクラス.<br>
 * ステップ前後の処理に行う処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class StepListener extends StepExecutionListenerSupport {

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(StepListener.class);

    /**
     * コンストラクタ.<BR>
     * 
     */
    public StepListener() {}

    /**
     * ステップ実施前に呼び出される処理.<BR>
     * ステップ実施前に行う処理があれば記述する。
     * 
     * @param stepExecution step実行コンテキスト
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_STEP,
                new String[] {B030514Const.PROPERTY_KEY_STEP_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        // 実行前処理
        super.beforeStep(stepExecution);
    }

    /**
     * ステップ実施後に呼び出される処理.<BR>
     * ステップ実施後に行う処理があれば記述する。
     * 
     * @return Step処理ステータス
     * @param jobExecution Step実行コンテキスト
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        // 実行後処理
        super.afterStep(stepExecution);

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_STEP,
                new String[] {B030514Const.PROPERTY_KEY_STEP_NAME}, Locale.JAPANESE);

        log.info(endMessage);

        return ExitStatus.COMPLETED;
    }
}
