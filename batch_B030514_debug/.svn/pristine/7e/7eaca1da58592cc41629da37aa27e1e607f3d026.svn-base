//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MissionSort.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.tasklet;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import jp.co.seiko_sol.B030514Const;
import jp.co.seiko_sol.dto.MissionSortDto;
import jp.co.seiko_sol.repository.SubMissionSortRepository;

/**
 * ミッションソート（ICT系、OCT系）処理クラス.<br>
 * ミッション基本情報、予定検体・分析数から指定条件に従いミッションを抽出してソートする処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class SubMissionSort {

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(SubMissionSort.class);

    /** 処理名 */
    private static final String PROC_NAME = "ミッションソート処理";

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** ミッションソート（ICT系、OCT系）アクセスRepository */
    @Autowired
    private SubMissionSortRepository subMissionSortRepository;

    /**
     * ミッションソート処理.<br>
     * ミッション基本情報、予定検体・分析数から指定条件に従いミッションを抽出し、ミッション情報ソート結果に出力する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    public Set<Integer> process() throws Exception {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        Set<Integer> targetMissions = new TreeSet<>();

        // ソート結果を取得する
        List<MissionSortDto> tMissionSortIctList =
                subMissionSortRepository.getTMissionSortIctList();
        List<MissionSortDto> tMissionSortOoctList =
                subMissionSortRepository.getTMissionSortOoctList();

        // 空の初期データを登録
        Timestamp systemTime = new Timestamp(System.currentTimeMillis());

        for (MissionSortDto missionSortDto : tMissionSortIctList) {
            // ソート結果（ICT）を登録
            subMissionSortRepository.insertTMissionSortIct(
                    missionSortDto.getMissionKey(),
                    systemTime,
                    B030514Const.BATCH_CREATED_BY
                );
            // 処理対象に追加
            targetMissions.add(missionSortDto.getMissionKey());
        }
        for (MissionSortDto missionSortDto : tMissionSortOoctList) {
            // ソート結果（OOCT）を登録
            subMissionSortRepository.insertTMissionSortOoct(
                    missionSortDto.getMissionKey(),
                    systemTime,
                    B030514Const.BATCH_CREATED_BY
                );
            // 処理対象に追加
            targetMissions.add(missionSortDto.getMissionKey());
        }

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(endMessage);

        return targetMissions;
    }
}
