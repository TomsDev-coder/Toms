//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : UrineSampleCountMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.dto.UrineSampleCountDto;

/**
 * 尿検体数情報用マッパークラス.<br>
 * ResultSetをUrineSampleCountDtoにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class UrineSampleCountMapper implements RowMapper<UrineSampleCountDto> {

    /**
     * マッピング処理.<br>
     * ResultSetをUrineSampleCountDtoにマッピングする。
     * 
     * @return 尿検体数情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public UrineSampleCountDto mapRow(ResultSet result, int rowNum) throws SQLException {

        // 尿検体数情報
        UrineSampleCountDto urineSampleCount = new UrineSampleCountDto();

        // 競技者数（男性）総数
        urineSampleCount.setMale_competitors(result.getInt("male_competitors"));
        // 競技者数（女性）総数
        urineSampleCount.setFemale_competitors(result.getInt("female_competitors"));
        // 尿検体数（男性）総数
        urineSampleCount.setMale_urine_sample(result.getInt("male_urine_sample"));
        // 尿検体数（女性）総数
        urineSampleCount.setFemale_urine_sample(result.getInt("female_urine_sample"));

        // 結果を返却
        return urineSampleCount;
    }
}
