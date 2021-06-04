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
import jp.co.seiko_sol.dto.AvailableDaysDto;

/**
 * 参加可能日数用マッパークラス.<br>
 * ResultSetをCompetitorsCountDtoにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class AvailableDaysMapper implements RowMapper<AvailableDaysDto> {

    /**
     * マッピング処理.<br>
     * ResultSetをAvailableDaysDtoにマッピングする。
     * 
     * @return 参加可能日数
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public AvailableDaysDto mapRow(ResultSet result, int rowNum) throws SQLException {

        // 参加可能日数
        AvailableDaysDto availableDays = new AvailableDaysDto();

        // 参加可能日数
        availableDays.setDays(result.getLong("days"));

        // 結果を返却
        return availableDays;
    }
}
