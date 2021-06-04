//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS //
// File Name : SubTaskMissionSortRepository.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.repository.query.Param;
import jp.co.seiko_sol.dto.MissionSortDto;

/**
 * ミッションソート（ICT系、OCT系）処理アクセス用のRepositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface SubMissionSortRepository {

    /**
     * ミッションソート（ICT)情報リスト取得.<BR>
     * 予め決められた条件のミッションソート（ICT)情報リストを取得して返却する。<BR>
     * 
     * @return ミッションソート（ICT)情報リスト
     */
    public List<MissionSortDto> getTMissionSortIctList();

    /**
     * ミッションソート（OOCT)情報リスト取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、ミッションソート（OOCT)情報リストを作成して返却する。
     * 
     * @return ミッションソート（OOCT)情報リスト
     */
    public List<MissionSortDto> getTMissionSortOoctList();

    /**
     * ミッション情報ソート結果（ICT）登録.<br>
     * ミッション情報ソート結果（ICT）登録を登録する。ソートIDは自動取得。<br>
     * 
     * @return 処理件数
     */
    public int insertTMissionSortIct(@Param("missionKey") Integer missionKey,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy);

    /**
     * ミッション情報ソート結果（OOCT）登録.<br>
     * ミッション情報ソート結果（OOCT）登録を登録する。ソートIDは自動取得。<br>
     * 
     * @return 処理件数
     */
    public int insertTMissionSortOoct(@Param("missionKey") Integer missionKey,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy);
}
