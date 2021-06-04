//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MDcpQualificationMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.domain.MDcpQualification;

/**
 * DCP評価資格情報用マッパークラス.<br>
 * "m_dcp_qualification"テーブルをMDcpQualificationにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class MDcpQualificationMapper implements RowMapper<MDcpQualification> {

    /**
     * マッピング処理.<br>
     * ResultSetをMDcpQualificationにマッピングする。
     * 
     * @return DCP評価資格情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public MDcpQualification mapRow(ResultSet result, int rowNum) throws SQLException {

        // アサインヘッダ情報
        MDcpQualification mDcpQualification = new MDcpQualification();

        // ユーザID
        mDcpQualification.setUserId(result.getInt("user_id"));
        // DCPランク
        mDcpQualification.setDcpRank(result.getString("dcp_rank"));
        // 検査対応評価（取り組む姿勢）
        mDcpQualification.setEvaluationScore01(result.getInt("evaluation_score01"));
        // 検査対応評価（ルールの理解）
        mDcpQualification.setEvaluationScore02(result.getInt("evaluation_score02"));
        // 検査対応評価（やりきる力）
        mDcpQualification.setEvaluationScore03(result.getInt("evaluation_score03"));
        // 検査対応評価（調整力・コミュ力）
        mDcpQualification.setEvaluationScore04(result.getInt("evaluation_score04"));
        // 検査対応評価（社会性を備えた人間力）
        mDcpQualification.setEvaluationScore05(result.getInt("evaluation_score05"));
        // 検査対応評価（評価項目06）
        mDcpQualification.setEvaluationScore06(result.getInt("evaluation_score06"));
        // 検査対応評価（評価項目07）
        mDcpQualification.setEvaluationScore07(result.getInt("evaluation_score07"));
        // 検査対応評価（評価項目08）
        mDcpQualification.setEvaluationScore08(result.getInt("evaluation_score08"));
        // 検査対応評価（評価項目09）
        mDcpQualification.setEvaluationScore09(result.getInt("evaluation_score09"));
        // 検査対応評価（評価項目10）
        mDcpQualification.setEvaluationScore10(result.getInt("evaluation_score10"));
        // 次回更新年度
        mDcpQualification.setRenewYear(result.getString("renew_year"));
        // 講習会受講フラグ
        mDcpQualification.setIsAttendWorkshop(result.getBoolean("is_attend_workshop"));
        // 講習会受講日
        mDcpQualification.setWorkshopAttendedDate(result.getDate("workshop_attended_date"));
        // 資格更新申請区分
        mDcpQualification.setRenewRequestType(result.getString("renew_request_type"));
        // 差戻理由
        mDcpQualification.setReasonForRejection(result.getString("reason_for_rejection"));
        // DCP種別（シニアDCO）
        mDcpQualification.setIsDcoSenior(result.getBoolean("is_dco_senior"));
        // DCP種別（DCO）
        mDcpQualification.setIsDco(result.getBoolean("is_dco"));
        // DCP種別（研修DCO）
        mDcpQualification.setIsDcoTrainee(result.getBoolean("is_dco_trainee"));
        // DCP種別（管理者BCO）
        mDcpQualification.setIsBcoAdmin(result.getBoolean("is_bco_admin"));
        // DCP種別（BCO）
        mDcpQualification.setIsBco(result.getBoolean("is_bco"));
        // DCP種別（メンター）
        mDcpQualification.setIsMentor(result.getBoolean("is_mentor"));
        // DCP種別（SCO）
        mDcpQualification.setIsSco(result.getBoolean("is_sco"));
        // DCP種別（IDCO）
        mDcpQualification.setIsIdco(result.getBoolean("is_idco"));
        // 削除フラグ
        mDcpQualification.setIsDeleted(result.getBoolean("is_deleted"));
        // システム登録日時
        mDcpQualification.setCreatedAt(result.getTimestamp("created_at"));
        // システム登録者ID
        mDcpQualification.setCreatedBy(result.getInt("created_by"));
        // システム最終更新日時
        mDcpQualification.setUpdatedAt(result.getTimestamp("updated_at"));
        // システム最終更新者ID
        mDcpQualification.setUpdatedBy(result.getInt("updated_by"));

        // 結果を返却
        return mDcpQualification;
    }
}
