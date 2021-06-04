//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TAssignHeaderMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.domain.TAssignHeader;

/**
 * アサインヘッダ情報用マッパークラス.<br>
 * "t_assign_header"テーブルをTAssignHeaderにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class TAssignHeaderMapper implements RowMapper<TAssignHeader> {

    /**
     * マッピング処理.<br>
     * ResultSetをTAssignHeaderにマッピングする。
     * 
     * @return アサインヘッダ情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public TAssignHeader mapRow(ResultSet result, int rowNum) throws SQLException {

        // アサインヘッダ情報
        TAssignHeader assignHead = new TAssignHeader();

        // ミッション基本情報KEY
        assignHead.setMissionKey(result.getInt("mission_key"));
        // アサイン確定フラグ
        assignHead.setIsAssignFixed(result.getBoolean("is_assign_fixed"));
        // 削除フラグ
        assignHead.setIsDeleted(result.getBoolean("is_deleted"));
        // システム登録日時
        assignHead.setCreatedAt(result.getTimestamp("created_at"));
        // システム登録者ID
        assignHead.setCreatedBy(result.getInt("created_by"));
        // システム最終更新日時
        assignHead.setUpdatedAt(result.getTimestamp("updated_at"));
        // システム最終更新者ID
        assignHead.setUpdatedBy(result.getInt("updated_by"));

        // 結果を返却
        return assignHead;
    }
}
