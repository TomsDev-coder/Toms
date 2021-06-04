//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : CompetitorsCountMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.dto.CompetitorsCountDto;

/**
 * 競技者数情報用マッパークラス.<br>
 * ResultSetをCompetitorsCountDtoにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class CompetitorsCountMapper implements RowMapper<CompetitorsCountDto> {

    /**
     * マッピング処理.<br>
     * ResultSetをCompetitorsCountDtoにマッピングする。
     * 
     * @return 競技者数情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public CompetitorsCountDto mapRow(ResultSet result, int rowNum) throws SQLException {

        // 競技者数情報
        CompetitorsCountDto competitorsCount = new CompetitorsCountDto();

        // ミッション基本情報KEY
        competitorsCount.setMales(result.getInt("males"));
        // アサイン確定フラグ
        competitorsCount.setFemales(result.getInt("females"));

        // 結果を返却
        return competitorsCount;
    }
}
