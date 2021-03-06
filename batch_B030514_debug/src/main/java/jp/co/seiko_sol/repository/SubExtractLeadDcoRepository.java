//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractLeadDcoRepository.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.repository.query.Param;

import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.User;

/**
 * リードDCO選出処理アクセス用Repositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface SubExtractLeadDcoRepository {

    /**
     * リードDCO選出対象ミッションリスト（ICT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、リードDCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return リードDCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getLeadDcpAssignmentMissionsIct();

    /**
     * リードDCO選出対象ミッションリスト（OOCT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、リードDCO選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return リードDCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getLeadDcpAssignmentMissionsOoct();

    /**
     * ICT側リードDCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1/S2/S3のユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1/S2/S3のユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListAlldayOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1/S2/S3のユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListEarlyOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④指定DCPランクのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListMorningOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④指定DCPランクのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④指定DCPランクのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsLeadDcoListEveningOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * DCP割当状況登録.<br>
     * DCP割当状況を登録する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param requiredDcoLead 必要人数（リードDCO）
     * @param requiredDco 必要人数（DCO）
     * @param requiredDcoMale 必要人数（DCO男性）
     * @param requiredDcoFemale 必要人数（DCO女性）
     * @param requiredBcoAdmin 必要人数（管理者BCO）
     * @param requiredBco 必要人数（BCO）
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID
     * @return 登録件数
     */
    public int insertTDcpAssignStatus(@Param("missionKey") Integer missionKey,
            @Param("testingDate,") Date testingDate,
            @Param("requiredDcoLead,") Integer requiredDcoLead,
            @Param("requiredDco,") Integer requiredDco,
            @Param("requiredDcoMale,") Integer requiredDcoMale,
            @Param("requiredDcoFemale,") Integer requiredDcoFemale,
            @Param("requiredBcoAdmin,") Integer requiredBcoAdmin,
            @Param("requiredBco,") Integer requiredBco, @Param("isDeleted,") Boolean isDeleted,
            @Param("createdAt,") Timestamp createdAt, @Param("createdBy,") Integer createdBy,
            @Param("updatedAt,") Timestamp updatedAt, @Param("updatedBy") Integer updatedBy);

    /**
     * DCP割当状況削除.<br>
     * DCP割当状況を削除する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 登録件数
     */
    public int deleteTDcpAssignStatus(@Param("missionKey") Integer missionKey,
            @Param("testingDate,") Date testingDate);

    /**
     * 視察ミッション確認.<br>
     * ミッション初日に検体種別がその他のデータがあるかどうか確認する。<br>
     * 
     * @param missionKey   ミッション基本情報KEY
     * @return ユーザIDリスト
     */
    public List<Mission> isInspectionMission(@Param("missionKey") Integer missionKey);

    /**
     * DCP選定情報取得.<br>
     * 日付、ミッション基本情報KEY、役割区分に該当するユーザIDリストを取得する。<br>
     * 
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getSelectIctUserList(@Param("assignedDate") Date assignedDate,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType);

    /**
     * 視察ミッション更新.<br>
     * 指定ユーザを視察ミッションの初日の役割区分をリードDCOから視察に変更する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param DcpRoleTypeDcoLead リードDCO役割区分
     * @param DcpRoleTypeInspection 視察役割区分
     * @return 登録件数
     */
    public int updateInspectionMission(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleTypeDcoLead") String dcpRoleTypeDcoLead,
            @Param("dcpRoleTypeInspection") String dcpRoleTypeInspection);

    /**
     * 指定ユーザDCP選定情報(ICT)削除.<br>
     * 指定ユーザ、日付、ミッション基本情報KEY、役割区分のDCP選定情報(ICT)をする。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param DcpRoleTypeDcoLead リードDCO役割区分
     * @param DcpRoleTypeInspection 視察役割区分
     * @return 削除件数
     */
    public int deleteDcpSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleTypeDcoLead") String dcpRoleTypeDcoLead);

    /**
     * 指定ユーザDCP選定情報(ICT)更新.<br>
     * 指定ユーザ、日付、ミッション基本情報KEY、役割区分のDCP選定情報(ICT)の有力候補フラグを更新する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @return 更新件数
     */
    public int updateIsStrongCandidateFalse(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey);
}
