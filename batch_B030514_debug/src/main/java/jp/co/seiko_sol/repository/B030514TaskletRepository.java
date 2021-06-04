//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514Repository.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import jp.co.seiko_sol.domain.TBatchStatus;

/**
 * 自動アサインバッチ処理（ID：B030514）Tasklet用のRepositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface B030514TaskletRepository {

    /**
     * バッチ処理ステータス情報取得処理.<br>
     * 開始要求されたバッチ処理ステータス情報を取得する。<br>
     * （ 状態がis_requested = true、is_finished = false、 is_finished_abnormal = falseのデータ ）
     * 
     * @param batchId バッチID
     * @return バッチ処理ステータス情報リスト
     */
    public List<TBatchStatus> getActiveBatchProcess(@Param("batchId") String batchId);

    /**
     * ソート結果（ICT）TRUNCATE処理.<br>
     * ソート結果（ICT）情報の全データを削除する。「t_mission_sort_ict_sort_id_seq」は初期化。<br>
     * 
     * @return ミッションソート（ICT)情報リスト
     */
    public void truncateTMissionSortIct();

    /**
     * ソート結果（OOCT）TRUNCATE処理.<br>
     * ソート結果（OOCT）情報の全データを削除する。「t_mission_sort_ooct_sort_id_seq」は初期化。<br>
     * 
     * @return ミッションソート（ICT)情報リスト
     */
    public void truncateTMissionSortOoct();
    
    /**
     * DCP選定情報（OOCT）DELETE処理.<br>
     * ソート結果（OOCT）に含まれいるミッションステータスが「Preparing」の<br>
     * ミッションコードと一致するDCP選定情報（OOCT）を削除。<br>
     * 
     * @return 削除件数
     */
    public void deletePreparingDcpSelectOoct();
}
