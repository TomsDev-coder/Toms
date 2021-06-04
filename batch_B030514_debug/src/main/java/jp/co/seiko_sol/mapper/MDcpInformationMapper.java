//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : MDcpInformationMapper.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import jp.co.seiko_sol.domain.MDcpInformation;

/**
 * DCPユーザ付加情報用マッパークラス.<br>
 * "m_dcp_information"テーブルをMDcpInformationにマッピングする。
 * 
 * @author IIM
 * @version 1.0
 */
public class MDcpInformationMapper implements RowMapper<MDcpInformation> {

    /**
     * マッピング処理.<br>
     * ResultSetをMDcpInformationにマッピングする。
     * 
     * @return DCPユーザ付加情報
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public MDcpInformation mapRow(ResultSet result, int rowNum) throws SQLException {

        // アサインヘッダ情報
        MDcpInformation mDcpInformation = new MDcpInformation();

        // ユーザID
        mDcpInformation.setUserId(result.getInt("user_id"));
        // 認定証番号
        mDcpInformation.setCertificationNo(result.getString("certification_no"));
        // 生年月日
        mDcpInformation.setBirthDate(result.getDate("birth_date"));
        // 性別 
        mDcpInformation.setGenderType(result.getString("gender_type"));
        // 職種
        mDcpInformation.setJobType(result.getString("job_type"));
        // 謝金辞退フラグ
        mDcpInformation.setIsDeclineReward(result.getBoolean("is_decline_reward"));
        // 認定日
        mDcpInformation.setQualifiiedDate(result.getDate("qualifiied_date"));
        // 使用可能言語（英語）
        mDcpInformation.setCanSpeakEnglish(result.getBoolean("can_speak_english"));
        // 使用可能言語（中国語）
        mDcpInformation.setCanSpeakChinese(result.getBoolean("can_speak_chinese"));
        // 使用可能言語（その他）
        mDcpInformation.setCanSpeakOthers(result.getString("can_speak_others"));
        // 写真
        mDcpInformation.setPortrait(result.getBytes("portrait"));
        // 郵便番号（現住所兼郵送先）
        mDcpInformation.setPostalcode1(result.getString("postalcode1"));
        // 都道府県コード（現住所兼郵送先）
        mDcpInformation.setPrefectureCode1(result.getString("prefecture_code1"));
        // 住所（現住所兼郵送先）
        mDcpInformation.setAddress1(result.getBytes("address1"));
        // 郵便番号（住民票）
        mDcpInformation.setPostalcode2(result.getString("postalcode2"));
        // 都道府県コード（住民票）
        mDcpInformation.setPrefectureCode2(result.getString("prefecture_code2"));
        // 住所（住民票）
        mDcpInformation.setAddress2(result.getBytes("address2"));
        // 郵便番号（郵送先）
        mDcpInformation.setPostalcode3(result.getString("postalcode3"));
        // 都道府県コード（郵送先）
        mDcpInformation.setPrefectureCode3(result.getString("prefecture_code3"));
        // 住所（郵送先）
        mDcpInformation.setAddress3(result.getBytes("address3"));
        // 路線名1
        mDcpInformation.setRouteName1(result.getString("route_name1"));
        // 最寄駅1
        mDcpInformation.setNearestStation1(result.getString("nearest_station1"));
        // 路線名2
        mDcpInformation.setRouteName2(result.getString("route_name2"));
        // 最寄駅2
        mDcpInformation.setNearestStation2(result.getString("nearest_station2"));
        // 配達希望時間
        mDcpInformation.setArriveTime(result.getString("arrive_time"));
        // DCPからの連絡事項 
        mDcpInformation.setDcpMessage(result.getString("dcp_message"));
        // JADA備考
        mDcpInformation.setJadaRemarks(result.getString("jada_remarks"));
        // ICT確定率
        mDcpInformation.setRatioIct(result.getBigDecimal("ratio_ict"));
        // OOCT確定率
        mDcpInformation.setRatioOoct(result.getBigDecimal("ratio_ooct"));
        // 削除フラグ
        mDcpInformation.setIsDeleted(result.getBoolean("is_deleted"));
        // システム登録日時
        mDcpInformation.setCreatedAt(result.getTimestamp("created_at"));
        // システム登録者ID
        mDcpInformation.setCreatedBy(result.getInt("created_by"));
        // システム最終更新日時
        mDcpInformation.setUpdatedAt(result.getTimestamp("updated_at"));
        // システム最終更新者ID
        mDcpInformation.setUpdatedBy(result.getInt("updated_by"));

        // 結果を返却
        return mDcpInformation;
    }
}
