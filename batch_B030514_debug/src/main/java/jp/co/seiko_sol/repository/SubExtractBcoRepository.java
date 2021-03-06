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
import java.util.List;

import org.springframework.data.repository.query.Param;

import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.dto.Count;
import jp.co.seiko_sol.dto.User;

/**
 * BCO選出処理アクセス用Repositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface SubExtractBcoRepository {

    /**
     * BCO選出対象ミッションリスト（ICT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、BCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return BCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getBcoAssignmentMissionsIct();

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
    public List<User> getParticipantsBcoAdminListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * 本年度アサイン回数取得SQL取得.<br>
     * 指定ユーザが本年度アサインされた回数を取得して返却する。
     * 
     * @param userId ユーザID
     * @return アサイン回数
     */
    public List<Count> getAssignmentsThisYearCount(@Param("userId") Integer userId);

    /**
     * 血液検体数数取得.<br>
     * ミッション基本情報KEY、検査日の条件よりデータを抽出し、血液検体の数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 血液検体数
     */
    public List<Count> getBloodSampleCount(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate);

    /**
     * ICT側BCO参加可能対象者取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListIct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * BCO選出対象ミッションリスト（OOCT)取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、BCO選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return BCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getBcoAssignmentMissionsOoct();

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
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
    public List<User> getParticipantsBcoAdminListAlldayOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListEarlyOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListMorningOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（管理者BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoAdminListEveningOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(終日)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListAlldayOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(早朝)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListEarlyOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(AM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListMorningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(PM)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListAfternoonOoct(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側参加可能対象者(夜間)取得.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * ②DCPカレンダー情報にの備考欄に入力が無い<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * ④DCP種別（BCO）がtrueのユーザ
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return ユーザIDリスト
     */
    public List<User> getParticipantsBcoListEveningOoct(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate);
}
