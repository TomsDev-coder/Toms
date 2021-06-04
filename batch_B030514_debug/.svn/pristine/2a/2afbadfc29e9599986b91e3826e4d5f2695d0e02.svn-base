//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractBcoRepository.java
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
import jp.co.seiko_sol.dto.ComponentRatioHighDto;
import jp.co.seiko_sol.dto.ComponentRatioLowDto;
import jp.co.seiko_sol.dto.ComponentRatioOoctDto;
import jp.co.seiko_sol.dto.DcpSelectInfoDto;
import jp.co.seiko_sol.dto.UrineSampleCountDto;
import jp.co.seiko_sol.dto.User;

/**
 * BCO選出処理アクセス用Repositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface SubExtractDcoRepository {

    /**
     * DCO選出対象ミッションリスト（ICT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、DCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return DCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getDcoAssignmentMissionsIct();

    /**
     * ICT側管理者BCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * 尿検体数数取得.<br>
     * ミッション基本情報KEY、検査日の条件よりデータを抽出し、尿検体の数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 尿検体数情報DTOリスト
     */
    public List<UrineSampleCountDto> getUrineSampleCount(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate);

    /**
     * 尿検体数数取得(OOCT用).<br>
     * ミッション基本情報KEYの条件よりデータを抽出し、尿検体の数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @return 尿検体数情報DTOリスト
     */
    public List<UrineSampleCountDto> getOoctUrineSampleCount(@Param("missionKey") Integer missionKey);

    /**
     * DCP選定情報（ICT）昇順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（ICT）にグルーピング用情報の性別、DCPランクを付加して昇順で取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoIctIsStrongCandidateAsc(
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCP選定情報（ICT）降順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（ICT）にグルーピング用情報の性別、DCPランクを付加して降順で取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoIctIsStrongCandidateDesc(
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * 競技区分取得処理.<br>
     * ミッションに紐付く競技区分を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @return 競技区分
     */
    public String getMissionDisciplineType(@Param("missionKey") Integer missionKey);

    /**
     * DCPランク構成比（ハイリスク用）取得処理.<br>
     * 競技区分によるDCOランク毎の人数を取得して返却する。
     * 
     * @param asssignedDco DCO人数
     * @param disciplineType 競技区分
     * @return DCPランク構成比（ハイリスク用）情報
     */
    public List<ComponentRatioHighDto> getDcoComponentRatiohigh(
            @Param("asssignedDco") Integer asssignedDco,
            @Param("disciplineType") String disciplineType);

    /**
     * DCPランク構成比（ローリスク用）取得処理.<br>
     * 競技区分によるDCOランク毎の人数を取得して返却する。
     * 
     * @param asssignedDco DCO人数
     * @param disciplineType 競技区分
     * @return DCPランク構成比（ローリスク用）情報
     */
    public List<ComponentRatioLowDto> getDcoComponentRatioLow(
            @Param("asssignedDco") Integer asssignedDco,
            @Param("disciplineType") String disciplineType);

    /**
     * DCP仮確定情報登録処理.<br>
     * DCP選定情報（ICT） を参照してDCP仮確定情報に登録する処理。
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     */
    public void insertDcpProvisionalFromSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType, @Param("createdAt") Timestamp createdAt,
            @Param("createdBy") Integer createdBy);

    /**
     * DCO選出対象ミッションリスト（OOCT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、DCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return DCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getDcoAssignmentMissionsOoct();

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListAlldayOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListEarlyOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListMorningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCPランクがS1,S2,S3,A1,A2
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsDcoListEveningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * DCP選定情報（OOCT）昇順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（OOCT）にグルーピング用情報の性別、DCPランクを付加して昇順取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoOoctIsStrongCandidateAsc(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCP選定情報（OOCT）降順取得処理.<br>
     * DCO仮確定対象のDCP選定情報（OOCT）にグルーピング用情報の性別、DCPランクを付加して降順取得し、返却する。
     * 
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）
     */
    public List<DcpSelectInfoDto> getDcoOoctIsStrongCandidateDesc(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCPランク構成比（OOCT用）取得処理.<br>
     * DCOランク毎の人数を取得して返却する。
     * 
     * @param asssignedDco DCO人数
     * @param disciplineType 競技区分
     * @return DCPランク構成比（OOCT用）情報
     */
    public List<ComponentRatioOoctDto> getDcoComponentRatioOoct(
            @Param("asssignedDco") Integer asssignedDco);

    /**
     * DCP仮確定情報登録処理.<br>
     * DCP選定情報（OOCT） を参照してDCP仮確定情報に登録する処理。
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     */
    public void insertDcpProvisionalFromSelectOoct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy);
}
