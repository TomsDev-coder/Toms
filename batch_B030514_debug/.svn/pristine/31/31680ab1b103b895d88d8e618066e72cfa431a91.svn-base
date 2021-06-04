//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514Configuration.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import jp.co.seiko_sol.listener.JobListener;
import jp.co.seiko_sol.listener.StepListener;
import jp.co.seiko_sol.tasklet.B030514Tasklet;

/**
 * 自動アサインバッチ処理（ID：B030514）用Configurationクラス.<br>
 * 自動アサインバッチ処理設定を定義する。
 * 
 * @author IIM
 * @version 1.0
 */
@Configuration
@EnableBatchProcessing
public class B030514Configuration {

    /** Job用Factoryクラス */
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    /** Step用Factoryクラス */
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    /**
     * MessageSource定義.<BR>
     * MessageSourceの配置場所、ファイル名やエンコードを定義する。
     * 
     * @return メッセージ情報
     */
    @Bean
    public MessageSource messageSource() {
        // MessageSource
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // BaseName（ディレクトリ、ファイル名）を設定
        messageSource.setBasename(B030514Const.MESSAGE_BASENAME);
        // エンコードを設定
        messageSource.setDefaultEncoding(B030514Const.DEFAULT_ENCODING);
        return messageSource;
    }

    /**
     * タスクレットJobの処理内容を定義する.<BR>
     * タスクレットJobの実行情報や処理内容を記述する。
     * 
     * @param jobListener JOBリスナー
     * @param taskletStep Step情報
     * @return Job情報
     */
    @Bean
    public Job jobB030514(JobListener jobListener, Step stepB030514) {
        return jobBuilderFactory.get(B030514Const.JOB_ID).incrementer(new RunIdIncrementer())
                .listener(jobListener).start(stepB030514).build();
    }

    /**
     * 自動アサインバッチ処理のステップの処理内容を定義する.<BR>
     * ステップの実行情報や処理内容を記述する。
     * 
     * @param stepListener Stepリスナー
     * @param taskletB030514 Tasklet処理
     * @return Step情報
     */
    @Bean
    public Step stepB030514(StepListener stepListener, B030514Tasklet taskletB030514) {
        return stepBuilderFactory.get(B030514Const.STEP_B030514_ID).listener(stepListener)
                .tasklet(taskletB030514).build();
    }
}
