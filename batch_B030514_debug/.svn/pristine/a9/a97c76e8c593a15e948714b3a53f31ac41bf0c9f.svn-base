//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514Tasklet.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.tasklet;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jp.co.seiko_sol.B030514Const;
import jp.co.seiko_sol.common.LogUtilsDB;
import jp.co.seiko_sol.domain.TBatchStatus;
import jp.co.seiko_sol.enumeration.ProcessStatusType;
import jp.co.seiko_sol.repository.B030514TaskletRepository;

/**
 * 自動アサインバッチ処理（ID：B030514）用Taskletクラス.<br>
 * 自動アサインバッチ処理（ID：B030514）用の業務処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class B030514Tasklet implements Tasklet {

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(B030514Tasklet.class);

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** 自動アサインバッチ処理（ID：B030514）Tasklet用Repository */
    @Autowired
    private B030514TaskletRepository b030514TaskletRepository;

    /** ミッションソート処理 */
    @Autowired
    private SubMissionSort subMissionSort;

    /** リードDCO選出処理 */
    @Autowired
    private SubExtractLeadDco subExtractLeadDco;

    /** BCO選出処理 */
    @Autowired
    private SubExtractBco subExtractBco;

    /** DCO選出処理 */
    @Autowired
    private SubExtractDco subExtractDco;

    /** 手動役割選出処理 */
    @Autowired
    private SubExtractManualRole subExtractManualRole;

    /* 処理ミッション情報 */
    Set<Integer> missionSort;
    Set<Integer> leadDco;
    Set<Integer> bco;
    Set<Integer> dco;
    Set<Integer> manualRole;

    /**
     * 自動アサインバッチ処理（ID：B030514）用Taskletクラス.<br>
     * 以下の様な処理を行う。<br>
     * 1.ミッションソート（ICT系、OCT系）<br>
     * 2.リードDCO選出<br>
     * 3.DCOの選出<br>
     * 4.BCOの選出<br>
     * 5.手動設定役割の選出処理<br>
     * 
     * @param contribution Step情報
     * @param chunkContext chunk情報
     * @return 処理ステータス
     * @throws Exception 処理中に例外が発生した場合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {

        try {

            missionSort = new TreeSet<>();
            leadDco = new TreeSet<>();
            bco = new TreeSet<>();
            dco = new TreeSet<>();
            manualRole = new TreeSet<>();

            // 開始メッセージをログ出力
            String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_GENERAL,
                    new String[] {this.getClass().getName()}, Locale.JAPANESE);

            log.info(startMessage);

            // バッチ処理ステータスを確認
            List<TBatchStatus> tBatchStatusList =
                    b030514TaskletRepository.getActiveBatchProcess(B030514Const.BATCH_ID);

            if (tBatchStatusList.isEmpty()) {
                // 取得出来ない
                // エラーとはせずに終了
                return RepeatStatus.FINISHED;
            }

            // バッチ処理ステータス
            TBatchStatus tBatchStatus = tBatchStatusList.get(0);
            if (!(tBatchStatus.getProcessStatus().equals(ProcessStatusType.REQUEST.getCode()))) {
                // 最新レコードが開始処理要求ではない
                // エラーとはせずに終了
                return RepeatStatus.FINISHED;
            }

            // バッチ処理開始
            LogUtilsDB.info(B030514Const.BATCH_ID, ProcessStatusType.START.getCode(),
                    messageSource.getMessage(B030514Const.INFOMATION_AUTOASSIGN_PROCESS_START,
                            new String[] {this.getClass().getName()}, Locale.JAPANESE));

            // ソート結果（ICT）をTRUNCATEして初期化
            b030514TaskletRepository.truncateTMissionSortIct();
            // ソート結果（OOCT）をTRUNCATEして初期化
            b030514TaskletRepository.truncateTMissionSortOoct();

            // ミッションソート処理
            missionSort = subMissionSort.process();
            
            // ソート結果（OOCT）に含まれいるミッションステータスが「Preparing」のミッションコードと一致するDCP選定情報（OOCT）を削除
            b030514TaskletRepository.deletePreparingDcpSelectOoct();

            // リードDCO選出処理
            leadDco = subExtractLeadDco.process();

            // BCOの選出処理
            bco = subExtractBco.process();

            // DCOの選出処理
            dco = subExtractDco.process();

            // 手動設定役割の選出処理（研修DCO、指導DCO、DCO/BCO、BCO/SCO、メンター、SCO）
            manualRole = subExtractManualRole.process();

            // バッチ正常終了
            LogUtilsDB.info(B030514Const.BATCH_ID, ProcessStatusType.NORMAL_END.getCode(),
                    messageSource.getMessage(B030514Const.INFOMATION_AUTOASSIGN_PROCESS_NORMAL,
                            new String[] {missionSort.toString(), leadDco.toString(),
                                    bco.toString(), dco.toString(), manualRole.toString()},
                            Locale.JAPANESE));

            // 終了メッセージをログ出力
            String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_GENERAL,
                    new String[] {this.getClass().getName()}, Locale.JAPANESE);

            log.info(endMessage);

            // 正常に完了
            return RepeatStatus.FINISHED;

        } catch (Exception e) {

            // 例外が発生
            e.printStackTrace();

            // バッチ異常終了
            LogUtilsDB.info(B030514Const.BATCH_ID, ProcessStatusType.ABNORMAL_END.getCode(),
                    messageSource.getMessage(B030514Const.INFOMATION_AUTOASSIGN_PROCESS_ABNORMAL,
                            new String[] {missionSort.toString(), leadDco.toString(),
                                    bco.toString(), dco.toString(), manualRole.toString()},
                            Locale.JAPANESE));

            // 上位にスロー
            throw e;
        }
    }
}
