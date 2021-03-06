//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : B030514Repository.java
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
import jp.co.seiko_sol.domain.MDcpInformation;
import jp.co.seiko_sol.domain.MDcpQualification;
import jp.co.seiko_sol.domain.TAssignHeader;
import jp.co.seiko_sol.domain.TDcpCalendar;
import jp.co.seiko_sol.domain.TDcpManualAssign;
import jp.co.seiko_sol.domain.TDcpProvisional;
import jp.co.seiko_sol.domain.TDcpSelectIct;
import jp.co.seiko_sol.domain.TDcpSelectOoct;
import jp.co.seiko_sol.domain.TMissionBase;
import jp.co.seiko_sol.domain.TTestingDate;
import jp.co.seiko_sol.dto.AvailableDaysDto;
import jp.co.seiko_sol.dto.CompetitorsCountDto;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.ProcessDate;
import jp.co.seiko_sol.dto.User;

/**
 * 自動アサインバッチ処理（ID：B030514）アクセス用のRepositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface B030514Repository {

    /**
     * アサインヘッダ情報取得処理.<br>
     * ミッション基本情報KEYを利用してアサインヘッダ情報を取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @return アサインヘッダ情報
     */
    public TAssignHeader getAssignHeaderByMissionkey(@Param("missionKey") Integer missionKey);

    /**
     * ミッション基本情報取得処理.<br>
     * ミッション基本情報KEYを利用してミッション基本情報を取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @return ミッション基本情報
     */
    public TMissionBase getMissionBaseByMissionkey(@Param("missionKey") Integer missionKey);

    /**
     * 検査日別情報取得処理.<br>
     * ミッションに紐づく検査日別情報を取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @return 検査日別情報リスト
     */
    public List<TTestingDate> getTestingDateByMissionKey(@Param("missionKey") Integer missionKey);

    /**
     * 競技者数取得.<br>
     * ミッション基本情報KEY、検査日の条件よりデータを抽出し、競技者数を取得して返却する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @return 競技者数情報DTO
     */
    public CompetitorsCountDto getCompetitorsCountDto(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate);

    /**
     * 当日DCPカレンダー情報取得処理.<br>
     * 対象ユーザIDの検査日のDCPカレンダー情報を取得する。
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCPカレンダー情報
     */
    public TDcpCalendar getTodayDcpCalendar(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate);

    /**
     * 前日DCPカレンダー情報取得処理.<br>
     * 対象ユーザIDの検査日前日のDCPカレンダー情報を取得する。
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCPカレンダー情報
     */
    public TDcpCalendar getYesterdayDcpCalendar(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate);

    /**
     * DCP評価資格情報取得処理.<br>
     * 指定ユーザIDで有効なDCP評価資格情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @return DCP評価資格情報
     */
    public MDcpQualification getQualificationByUserId(@Param("userId") Integer userId);

    /**
     * DCPユーザ付加情報取得処理.<br>
     * 指定ユーザIDで有効なDCPユーザ付加情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @return DCPユーザ付加情報
     */
    public MDcpInformation getAdditionalInformationByUserId(@Param("userId") Integer userId);

    /**
     * DCP仮確定情報取得処理.<br>
     * 指定ユーザID、日付に該当するDCP仮確定情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCP仮確定情報リスト
     */
    public List<TDcpProvisional> getIctProvisionalInfomation(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate);

    /**
     * OOCT側DCP仮確定情報取得処理.<br>
     * 指定ユーザID、日付、時間帯区分に該当するDCP仮確定情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate アサイン日
     * @return DCP仮確定情報リスト
     */
    public List<TDcpProvisional> getOoctProvisionalInfomation(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType);

    /**
     * 連続勤務情報取得.<br>
     * 指定ユーザが何日間仮確定されているかチェックする。<br>
     * 
     * @param userId ユーザID
     * @param testingDate 検査日
     * @param continuousDaysLimit 最大勤務可能日数
     * @return 連続勤務日数
     */
    public Integer findContinuousDutyInformation(@Param("userId") Integer userId,
            @Param("testingDate") Date testingDate, @Param("continuousDaysLimit") Integer continuousDaysLimit);

    /**
     * 必要参加日数取得.<br>
     * ミッション基本情報から必要参加日数を算出する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param participationRatio 必要参加割合
     * @return 必要参加日数
     */
    public Integer getRequiredDays(@Param("missionKey") Integer missionKey,
            @Param("participationRatio") Integer participationRatio);

    /**
     * ICT参加可能者取得SQL定義.<br>
     * DCP選定情報（ICT）から必要参加日数以上参加可能な候補者を取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getIctParticipation(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("dcpRoleType") String dcpRoleType);

    /**
     * 有力候補者者(ICT)昇順取得SQL定義.<br>
     * DCP選定情報（ICT）から有力候補者者を昇順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoIctIsStrongCandidateAsc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("dcpRoleType") String dcpRoleType);

    /**
     * 有力候補者者(ICT)降順取得SQL定義.<br>
     * DCP選定情報（ICT）から有力候補者者を降順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoIctIsStrongCandidateDesc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("dcpRoleType") String dcpRoleType);

    /**
     * 過去参加経験情報（リードDCO用）取得.<br>
     * リードDCO用のDCPアサイン情報、ミッション基本情報から今回と同じ競技IDで参加した経験があるかどうかを抽出し返却する。<br>
     * 
     * @param userId ユーザID
     * @param sportsId 競技ID
     * @return 過去参加日情報リスト
     */
    public List<ProcessDate> getPreviousExperienceLeadDco(@Param("userId") Integer userId,
            @Param("sportsId") Integer sportsId);

    /**
     * 過去参加経験情報（DCO用）取得.<br>
     * DCO用のDCPアサイン情報、ミッション基本情報から今回と同じ競技IDで参加した経験があるかどうかを抽出し返却する。<br>
     * 
     * @param userId ユーザID
     * @param sportsId 競技ID
     * @return 過去参加日情報リスト
     */
    public List<ProcessDate> getPreviousExperienceDco(@Param("userId") Integer userId,
            @Param("sportsId") Integer sportsId);

    /**
     * 用務地・居住地関係確認.<br>
     * DCP在住地優先順位マスタ、都道府県マスタ、ミッション基本情報、DCPユーザ付加情報から用務地と居住地の関係を確認する。<br>
     * 在住地優先順位マスタに対象地域（都道府県）が無い場合は0件になる。優先になる場合はユーザIDが取得される。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param userId ユーザID
     * @return ユーザIDリスト
     */
    public List<User> getRegionalRelationships(@Param("missionKey") Integer missionKey,
            @Param("userId") Integer userId);

    /**
     * 連続ミッション確認（ICT）SQL.<br>
     * DCP仮確定情報、ミッション基本情報から検査日前後日にICTミッションが無い事を確認する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param userId ユーザID
     * @param assignedDate 検査日
     * @return ミッション基本情報KEYリスト
     */
    public List<Mission> getConsecutiveMissionsIct(@Param("missionKey") Integer missionKey,
            @Param("userId") Integer userId, @Param("assignedDate") Date assignedDate);

    /**
     * 有力候補者用DCP選定情報（ICT）更新.<br>
     * DCP選定情報（ICT）情報の有力候補者用情報を更新する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * DCPMG#144 start
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * DCPMG#144 end
     * @return 更新件数
     */
    public int updateDcpIctStrongCandidate(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            // DCPMG#144 start
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4);
            // DCPMG#144 end

    /**
     * 時間帯区分取得.<br>
     * ミッション基本情報の通告時刻がシステム設定マスタで設定する時間帯区分のいずれに該当するかを確認する。
     * 
     * @param systemCode システム区分
     * @param missionKey ミッション基本情報KEY
     * @return 時間帯区分リスト（返却は常に1件となる予定）
     */
    public String getMissionTimeSlotType(@Param("systemCode") String systemCode,
            @Param("missionKey") Integer missionKey);

    /**
     * OOCT参加可能者取得SQL定義.<br>
     * ミッション基本情報KEYに紐付くユーザIDを取得する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getOoctParticipation(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * 有力候補者者(OOCT)昇順取得SQL定義.<br>
     * DCP選定情報（OOCT）から有力候補者者を昇順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoOoctIsStrongCandidateAsc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * 有力候補者者(OOCT)降順取得SQL定義.<br>
     * DCP選定情報（OOCT）から有力候補者者を降順で取得する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @return ユーザIDリスト
     */
    public List<User> getBcoOoctIsStrongCandidateDesc(@Param("missionKey") Integer missionKey,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * 連続ミッション確認（OOCT）取得.<br>
     * DCP仮確定情報、ミッション基本情報から前後の時間帯区分にOOCTミッションが無い事を確認する。
     * 
     * @param missionKey ミッション基本情報KEY
     * @param userId ユーザID
     * @param assignedDate 検査日
     * @param timeSlotType 時間帯区分
     * @return ミッション基本情報KEYリスト
     */
    public List<Mission> getConsecutiveMissionsOoct(@Param("missionKey") Integer missionKey,
            @Param("userId") Integer userId, @Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType);

    /**
     * 有力候補者用DCP選定情報（OOCT）更新.<br>
     * DCP選定情報（OOCT）情報の有力候補者用情報を更新する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @return 更新件数
     */
    public int updateDcpOoctStrongCandidate(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3);

    /**
     * 仮確定情報取得処理.<br>
     * 指定条件で仮確定情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 仮確定情報リスト
     */
    public List<TDcpProvisional> getDcpProvisionalInfo(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType);

    /**
     * ICT仮確定情報取得処理.<br>
     * 指定条件で仮確定情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 仮確定情報リスト
     */
    public List<TDcpProvisional> getIctDcpProvisionalInfo(@Param("assignedDate") Date assignedDate,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType);

    /**
     * OOCTCT仮確定情報取得処理.<br>
     * 指定条件で仮確定情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 仮確定情報リスト
     */
    public List<TDcpProvisional> getOoctDcpProvisionalInfo(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * 手動割当情報取得処理.<br>
     * 指定条件で手動割当情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 手動割当情報リスト
     */
    public List<TDcpManualAssign> getDcpManualProvisionalInfo(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") List<String> dcpRoleType);

    /**
     * DCP選定情報（ICT）削除.<br>
     * DCP選定情報（ICT）情報を削除する。<br>
     * 
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 更新件数
     */
    public int deleteDcpSelectIct(@Param("assignedDate") Date assignedDate,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCP選定情報（OOCT）削除.<br>
     * DCP選定情報（OOCT）情報を削除する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return 更新件数
     */
    public int deleteDcpSelectOoct(@Param("assignedDate") Date assignedDate,
            @Param("timeSlotType") String timeSlotType, @Param("missionKey") Integer missionKey,
            @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCP選定情報（ICT）登録.<br>
     * DCP選定情報（ICT）情報を登録する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param isStrongCandidate 有力候補フラグ
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * @param anyCondition5 個別条件5
     * @param isMetCondition5 条件合否5
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int inserteDcpSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4,
            @Param("anyCondition5") String anyCondition5,
            @Param("isMetCondition5") Boolean isMetCondition5,
            @Param("isDeleted") Boolean isDeleted, @Param("createdAt") Timestamp createdAt,
            @Param("createdBy") Integer createdBy, @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy);

    /**
     * DCP選定情報（OOCT）登録.<br>
     * DCP選定情報（OOCT）情報を登録する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param isStrongCandidate 有力候補フラグ
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * @param anyCondition5 個別条件5
     * @param isMetCondition5 条件合否5
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int inserteDcpSelectOoct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4,
            @Param("anyCondition5") String anyCondition5,
            @Param("isMetCondition5") Boolean isMetCondition5,
            @Param("isDeleted") Boolean isDeleted, @Param("createdAt") Timestamp createdAt,
            @Param("createdBy") Integer createdBy, @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy);

    /**
     * DCP選定情報（ICT）取得.<br>
     * DCP選定情報（ICT）情報を取得する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（ICT）リスト
     */
    public List<TDcpSelectIct> selectDcpSelectIct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCP選定情報（OOCT）取得.<br>
     * DCP選定情報（OOCT）情報を取得する。<br>
     * 
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @return DCP選定情報（OOCT）リスト
     */
    public List<TDcpSelectOoct> selectDcpSelectOoct(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType);

    /**
     * DCP仮確定情報登録.<br>
     * DCP仮確定情報を登録する。<br>
     * 
     * @param userId ユーザID
     * @param assignedDate 日付
     * @param timeSlotType 時間帯区分
     * @param missionKey ミッション基本情報KEY
     * @param dcpRoleType 役割区分
     * @param isStrongCandidate 有力候補フラグ
     * @param conditionsScore 条件合致点数
     * @param anyCondition1 個別条件1
     * @param isMetCondition1 条件合否1
     * @param anyCondition2 個別条件2
     * @param isMetCondition2 条件合否2
     * @param anyCondition3 個別条件3
     * @param isMetCondition3 条件合否3
     * @param anyCondition4 個別条件4
     * @param isMetCondition4 条件合否4
     * @param anyCondition5 個別条件5
     * @param isMetCondition5 条件合否5
     * @param isDeleted 削除フラグ
     * @param createdAt システム登録日時
     * @param createdBy システム登録者ID
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 登録件数
     */
    public int inserteDcpProvisional(@Param("userId") Integer userId,
            @Param("assignedDate") Date assignedDate, @Param("timeSlotType") String timeSlotType,
            @Param("missionKey") Integer missionKey, @Param("dcpRoleType") String dcpRoleType,
            @Param("isStrongCandidate") Boolean isStrongCandidate,
            @Param("conditionsScore") Integer conditionsScore,
            @Param("anyCondition1") String anyCondition1,
            @Param("isMetCondition1") Boolean isMetCondition1,
            @Param("anyCondition2") String anyCondition2,
            @Param("isMetCondition2") Boolean isMetCondition2,
            @Param("anyCondition3") String anyCondition3,
            @Param("isMetCondition3") Boolean isMetCondition3,
            @Param("anyCondition4") String anyCondition4,
            @Param("isMetCondition4") Boolean isMetCondition4,
            @Param("anyCondition5") String anyCondition5,
            @Param("isMetCondition5") Boolean isMetCondition5,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy);

    /**
     * DCP割当状況（BCO）更新.<br>
     * DCP割当状況のBCO情報を更新する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param requiredBcoAdmin 必要人数（管理者BCO）
     * @param requiredBco 必要人数（BCO）
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int updateDcpAssignStatusBco(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate,
            @Param("requiredBcoAdmin") Integer requiredBcoAdmin,
            @Param("requiredBco") Integer requiredBco, @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy);

    /**
     * DCP割当状況（DCO）更新.<br>
     * DCP割当状況のDCO情報を更新する。<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param requiredDco 必要人数（管理者DCO）
     * @param requiredDcoMale 必要人数（DCO男性）
     * @param requiredDcoFemale 必要人数（DCO女性）
     * @param updatedAt システム最終更新日時
     * @param updatedBy システム最終更新者ID

     * @return 更新件数
     */
    public int updateDcpAssignStatusDco(@Param("missionKey") Integer missionKey,
            @Param("testingDate") Date testingDate,
            @Param("requiredDco") Integer requiredDco,
            @Param("requiredDcoMale") Integer requiredDcoMale,
            @Param("requiredDcoFemale") Integer requiredDcoFemale,
            @Param("updatedAt") Timestamp updatedAt,
            @Param("updatedBy") Integer updatedBy);

    /**
     * ICT参加可能日数取得SQL定義.<br>
     * DCPカレンダー情報から参加可能日数を取得する。
     * 
     * @param userId ユーザID
     * @param missionKey ミッション基本情報KEY
     * @return 日数
     */
    public AvailableDaysDto getIctAvailableDays(@Param("userId") Integer userId,
            @Param("missionKey") Integer missionKey);
}
