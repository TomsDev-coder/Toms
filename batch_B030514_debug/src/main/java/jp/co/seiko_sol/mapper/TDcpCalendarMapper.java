//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : TDcpCalendarMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.domain.TDcpCalendar;

/**
 * DCPカレンダー情報用マッパークラス.<br>
 * "t_dcp_calendar"テーブルをTDcpCalendarにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class TDcpCalendarMapper implements RowMapper<TDcpCalendar> {

    /**
     * マッピング処理.<br>
     * ResultSetをTDcpCalendarにマッピングする。
     * 
     * @return DCPカレンダー情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public TDcpCalendar mapRow(ResultSet result, int rowNum) throws SQLException {

        // アサインヘッダ情報
        TDcpCalendar tDcpCalendar = new TDcpCalendar();

        // ユーザID
        tDcpCalendar.setUserId(result.getInt("user_id"));
        // 日付
        tDcpCalendar.setAssignedDate(result.getDate("assigned_date"));
        // 参加可能フラグ（終日）
        tDcpCalendar.setIsAttendAllday(result.getBoolean("is_attend_allday"));
        // 参加可能フラグ（早朝）
        tDcpCalendar.setIsAttendEarly(result.getBoolean("is_attend_early"));
        // 参加可能フラグ（AM）
        tDcpCalendar.setIsAttendMorning(result.getBoolean("is_attend_morning"));
        // 参加可能フラグ（PM）
        tDcpCalendar.setIsAttendAfternoon(result.getBoolean("is_attend_afternoon"));
        // 参加可能フラグ（夜間）
        tDcpCalendar.setIsAttendEvening(result.getBoolean("is_attend_evening"));
        // 講習会フラグ 
        tDcpCalendar.setIsPlannedClass(result.getBoolean("is_planned_class"));
        // 宿泊可能フラグ
        tDcpCalendar.setIsAbleStay(result.getBoolean("is_able_stay"));
        // 備考記載フラグ
        tDcpCalendar.setIsRemarksWritten(result.getBoolean("is_remarks_written"));
        // 備考
        tDcpCalendar.setRemarks(result.getString("remarks"));
        // 削除フラグ
        tDcpCalendar.setIsDeleted(result.getBoolean("is_deleted"));
        // システム登録日時
        tDcpCalendar.setCreatedAt(result.getTimestamp("created_at"));
        // システム登録者ID
        tDcpCalendar.setCreatedBy(result.getInt("created_by"));
        // システム最終更新日時
        tDcpCalendar.setUpdatedAt(result.getTimestamp("updated_at"));
        // システム最終更新者ID
        tDcpCalendar.setUpdatedBy(result.getInt("updated_by"));

        // 結果を返却
        return tDcpCalendar;
    }
}
