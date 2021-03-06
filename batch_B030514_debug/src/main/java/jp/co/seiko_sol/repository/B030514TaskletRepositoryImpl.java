//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514RepositoryImpl.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.co.seiko_sol.domain.TBatchStatus;

/**
 * 自動アサインバッチ処理（ID：B030514）Tasklet用のRepositoryクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class B030514TaskletRepositoryImpl implements B030514TaskletRepository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** バッチ処理ステータス情報取得SQL */
    private static final String ACTIVE_BATCH_PROCESS_QUERY =
            "SELECT"                                                                              +
            "    process_id, "                                                                    +
            "    batch_id, "                                                                      +
            "    mission_key, "                                                                   +
            "    process_status, "                                                                +
            "    processed_at, "                                                                  +
            "    process_status_details, "                                                        +
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            "FROM "                                                                               +
            "    t_batch_status A "                                                               +
            "    INNER JOIN ( "                                                                   +
            "        SELECT "                                                                     +
            "            MAX(process_id) as max_id "                                              +
            "        FROM "                                                                       +
            "            t_batch_status "                                                         +
            "        WHERE "                                                                      +
            "            batch_id = :batchId "                                                    +
            "        GROUP BY "                                                                   +
            "            batch_id "                                                               +
            "    ) B ON "                                                                         +
            "        B.max_id = A.process_id ";

    /** ミッション情報ソート結果（ICT）Truncate削除SQL */
    private static final String TRUNCATE_MISSION_SORT_ICT_QUERY =
            "TRUNCATE t_mission_sort_ict RESTART IDENTITY";

    /** ミッション情報ソート結果（OOCT）Truncate削除SQL */
    private static final String TRUNCATE_MISSION_SORT_OOCT_QUERY =
            "TRUNCATE t_mission_sort_ooct RESTART IDENTITY";

    // DCPMG#154-2 change start 非仮確定者のみ削除
    /** ミッションコードと一致するDCP選定情報（OOCT）削除SQL */
    private static final String DCP_SELECT_OOCT_MISSION_STATUS_PREPARING_DELETE =
            "DELETE "                                                                             +
            "FROM "                                                                               +
            "    t_dcp_select_ooct "                                                              +
            "WHERE "                                                                              +
            "t_dcp_select_ooct.mission_key IN "                                                   +
            "    ("                                                                               +
            "    SELECT "                                                                         +
            "        A.mission_key "                                                              +
            "    FROM "                                                                           +
            "        t_mission_sort_ooct A "                                                      +
            "      INNER JOIN t_mission_base B ON "                                               +
            "            A.mission_key = B.mission_key "                                          +
            "    WHERE "                                                                          +
            "      B.testing_type = '21' AND "                                                    +
            "      B.mission_status = '12' "                                                      +
            "    ) AND "                                                                          +
            "    ( "                                                                              +
            "        t_dcp_select_ooct.user_id, "                                                 +
            "        t_dcp_select_ooct.mission_key, "                                             +
            "        t_dcp_select_ooct.assigned_date, "                                           +
            "        t_dcp_select_ooct.time_slot_type, "                                          +
            "        t_dcp_select_ooct.dcp_role_type "                                            +
            "    ) NOT IN "                                                                       +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            C.user_id, "                                                             +
            "            C.mission_key, "                                                         +
            "            C.assigned_date, "                                                       +
            "            C.time_slot_type, "                                                      +
            "            C.dcp_role_type "                                                        +
            "        FROM "                                                                       +
            "            t_dcp_provisional C "                                                    +
            "    ) ";
    // DCPMG#154-2 change end
    
    /**
     * バッチ処理ステータス情報取得処理.<br>
     * 開始要求されたバッチ処理ステータス情報を取得する。<br>
     * （ 状態がis_requested = true、is_finished = false、 is_finished_abnormal = falseのデータ ）
     * 
     * @param batchId バッチID
     * @return バッチ処理ステータス情報リスト
     */
    public List<TBatchStatus> getActiveBatchProcess(@Param("batchId") String batchId) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("batchId", batchId);

        // レコードマップを生成
        RowMapper<TBatchStatus> mapper = new BeanPropertyRowMapper<>(TBatchStatus.class);

        // 実行
        return namedJdbcTemplate.query(ACTIVE_BATCH_PROCESS_QUERY, parameter, mapper);
    }

    /**
     * ソート結果（ICT）TRUNCATE処理.<br>
     * ソート結果（ICT）情報の全データを削除する。「t_mission_sort_ict_sort_id_seq」は初期化。<br>
     * 
     * @return ミッションソート（ICT)情報リスト
     */
    public void truncateTMissionSortIct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();

        // 実行
        namedJdbcTemplate.update(TRUNCATE_MISSION_SORT_ICT_QUERY, parameter);
    }

    /**
     * ソート結果（OOCT）TRUNCATE処理.<br>
     * ソート結果（OOCT）情報の全データを削除する。「t_mission_sort_ooct_sort_id_seq」は初期化。<br>
     * 
     * @return ミッションソート（ICT)情報リスト
     */
    public void truncateTMissionSortOoct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();

        // 実行
        namedJdbcTemplate.update(TRUNCATE_MISSION_SORT_OOCT_QUERY, parameter);
    }
    
    /**
     * DCP選定情報（OOCT）DELETE処理.<br>
     * ソート結果（OOCT）に含まれいるミッションステータスが「Preparing」の<br>
     * ミッションコードと一致するDCP選定情報（OOCT）を削除。<br>
     * DCPMG#154-2 change 非仮確定者のみを削除対象とする
     * 
     * @return 削除件数
     */
    public void deletePreparingDcpSelectOoct() {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        
        // 実行
        namedJdbcTemplate.update(DCP_SELECT_OOCT_MISSION_STATUS_PREPARING_DELETE, parameter);
    }
}
