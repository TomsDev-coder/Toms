//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractLeadDco.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.tasklet;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import jp.co.seiko_sol.B030514Const;
import jp.co.seiko_sol.domain.MDcpInformation;
import jp.co.seiko_sol.domain.MDcpQualification;
import jp.co.seiko_sol.domain.MSystemDefaults;
import jp.co.seiko_sol.domain.TDcpCalendar;
import jp.co.seiko_sol.domain.TDcpProvisional;
import jp.co.seiko_sol.domain.TMissionBase;
import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.domain.TTestingDate;
import jp.co.seiko_sol.dto.AvailableDaysDto;
import jp.co.seiko_sol.dto.CompetitorsCountDto;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.ProcessDate;
import jp.co.seiko_sol.dto.User;
import jp.co.seiko_sol.enumeration.DcpRankType;
import jp.co.seiko_sol.enumeration.DcpRoleType;
import jp.co.seiko_sol.enumeration.GenderType;
import jp.co.seiko_sol.enumeration.LanguageType;
import jp.co.seiko_sol.enumeration.StrongCandidateType;
import jp.co.seiko_sol.enumeration.TimeSlotType;
import jp.co.seiko_sol.exception.DataNotFoundException;
import jp.co.seiko_sol.repository.B030514Repository;
import jp.co.seiko_sol.repository.MSystemDefaultsRepository;
import jp.co.seiko_sol.repository.SubExtractLeadDcoRepository;

/**
 * リードDCO選出処理クラス.<br>
 * リードDCOを選出する処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class SubExtractLeadDco {

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(SubExtractLeadDco.class);

    /** 処理名 */
    private static final String PROC_NAME = "リードDCO選出処理";

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** システム設定マスタアクセス用Repository */
    @Autowired
    private MSystemDefaultsRepository tSystemDefaultsRepository;

    /** ミッションソート（ICT系、OCT系）処理アクセス用のRepository */
    @Autowired
    private SubExtractLeadDcoRepository subExtractLeadDcoRepository;

    /** 自動アサインバッチ処理（ID：B030514）アクセスRepository */
    @Autowired
    private B030514Repository b030514Repository;

    /**
     * リードDCO選出処理.<br>
     * リードDCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    public Set<Integer> process() throws Exception {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_JOB,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        Set<Integer> leadDcoMissions = new TreeSet<>();

        // ICT側の編集処理
        Set<Integer> ictMissions = editIctLeadDcoProc();
        leadDcoMissions.addAll(ictMissions);

        // OOCT側の編集処理
        Set<Integer> ooctMissions = editOoctLeadDcoProc();
        leadDcoMissions.addAll(ooctMissions);

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_JOB,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(endMessage);

        return leadDcoMissions;
    }

    /**
     * ICT側のリードDCO選出処理.<br>
     * リードDCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editIctLeadDcoProc() throws Exception {

        // 処理ミッション
        Set<Integer> targetMissions = new TreeSet<>();
        // 一時リスト
        List<User> tempList = null;

        // アサイン条件：女性のみ
        boolean isAssignFemaleOnly = false;

        // システムマスタからアサイン条件（前泊後泊、DCPランク）を取得
        Optional<MSystemDefaults> optSystemDefaults =
                tSystemDefaultsRepository.findById(B030514Const.SYSTEM_CODE);
        MSystemDefaults tSystemDefaults =
                optSystemDefaults.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_SYSTEMDEFAULTS_DATA_NOTFOUND,
                                new String[] {}, Locale.JAPANESE)));
        // アサイン条件：前泊必要
        boolean isAbleStayBefore = tSystemDefaults.getIsAbleStayBefore();
        // アサイン条件：後泊必要
        boolean isAbleStayNight = tSystemDefaults.getIsAbleStayNight();
        // アサイン条件（検査期間）
        int participationRatio = tSystemDefaults.getParticipationRatio();
        // アサイン条件：ランクS1
        boolean isSelectS1 = tSystemDefaults.getIsSelectS1();
        // アサイン条件：ランクS2
        boolean isSelectS2 = tSystemDefaults.getIsSelectS2();
        // アサイン条件：ランクS3
        boolean isSelectS3 = tSystemDefaults.getIsSelectS3();

        // DCP割当対象ミッションの抽出
        List<TMissionSortIct> tMissionSortIctList =
                subExtractLeadDcoRepository.getLeadDcpAssignmentMissionsIct();

        mission_loop: for (TMissionSortIct tMissionSortIct : tMissionSortIctList) {

            Integer missionKey = tMissionSortIct.getMissionKey();

            // 視察フラグ
            boolean isInspectionMission = false;
            Date testingDateFrom = null;

            // DCPMG#161 delete 確定済であっても選出する

            // ミッション基本情報を取得
            TMissionBase tmpBase = b030514Repository.getMissionBaseByMissionkey(missionKey);
            Optional<TMissionBase> optTMissionBase = Optional.ofNullable(tmpBase);
            TMissionBase tMissionBase = optTMissionBase.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_MISSIOBASE_DATA_NOTFOUND,
                            new String[] {missionKey.toString()}, Locale.JAPANESE)));
            String languageType = tMissionBase.getLanguageType();
            Integer sportsId = tMissionBase.getSportsId();

            // ミッション基本情報とシステム設定マスタから必要な参加日数を取得する
            Integer tmpDays = b030514Repository.getRequiredDays(missionKey, participationRatio);
            Optional<Integer> optRequiredDays = Optional.ofNullable(tmpDays);
            Integer requiredDays = optRequiredDays.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_REQUIREDDAYS_DATA_NOTFOUND,
                            new String[] {missionKey.toString(), Integer.toString(participationRatio)},
                            Locale.JAPANESE)));

            // 初日が視察ミッションかどうか確認
            List<Mission> inspectionMissionList = subExtractLeadDcoRepository.isInspectionMission(missionKey);
            if(!inspectionMissionList.isEmpty()) {
                // データがある場合、視察あり
                isInspectionMission = true;
                testingDateFrom = tMissionBase.getTestingDateFrom();
            }

            // 検査日別情報を取得
            List<TTestingDate> tTestingDateList =
                    b030514Repository.getTestingDateByMissionKey(missionKey);

            date_loop: for (TTestingDate tTestingDate : tTestingDateList) {

                // 検査日毎に候補を選定
                Date testingDate = tTestingDate.getTestingDate();

                // 一旦アサイン条件を男女可に初期化
                isAssignFemaleOnly = false;

                // 現在のDCP割当状況のデータを削除
                subExtractLeadDcoRepository.deleteTDcpAssignStatus(missionKey, testingDate);
                // 空の初期データを登録
                Timestamp tDcpAssignStatusTime = new Timestamp(System.currentTimeMillis());
                // DCP割当状況の初期データを登録
                subExtractLeadDcoRepository.insertTDcpAssignStatus(
                        missionKey,
                        testingDate,
                        B030514Const.PERSON_ONE,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        Boolean.FALSE,
                        tDcpAssignStatusTime,
                        B030514Const.BATCH_CREATED_BY,
                        tDcpAssignStatusTime,
                        B030514Const.BATCH_UPDATED_BY);

                // ミッション基本情報、予定検体・分析数（DB除く）より競技者が女性のみかどうか取得
                CompetitorsCountDto countDto =
                        b030514Repository.getCompetitorsCountDto(missionKey, testingDate);
                Optional<Integer> optMales = Optional.ofNullable(countDto.getMales());
                Optional<Integer> optFemales = Optional.ofNullable(countDto.getFemales());
                Integer males = optMales.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_ANALYSISPLAN_MALE_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));
                Integer females = optFemales.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_ANALYSISPLAN_FEMALE_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));

                if ((males == 0) && (females > 0)) {
                    // 男性競技者が0 かつ 女性競技者が0より大きい場合
                    isAssignFemaleOnly = true;
                }

                // 日付、ミッション基本情報KEY、役割区分に該当するDCP選定情報（ICT）を削除（視察も削除）
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.DCO_LEAD.getCode());
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.INSPECTION.getCode());


                // 以下の条件でリードDCO候補対象を絞る
                // ・DCPカレンダー情報に当該日に終日参加可能で登録がされている
                // DCPMG#144  条件削除 start
                // ・DCPカレンダー情報にの備考欄に入力が無い
                // DCPMG#144 end
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                tempList = subExtractLeadDcoRepository.getParticipantsLeadDcoListIct(missionKey,
                        testingDate);

                // 候補者毎に追加で条件確認
                candidate_loop: for (User user : tempList) {

                    Integer userId = user.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedLeadDcoList = b030514Repository
                            .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                                    missionKey, DcpRoleType.DCO_LEAD.getCode());
                    List<TDcpProvisional> tempConfirmedInspectionLeadDcoList = b030514Repository
                            .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                                    missionKey, DcpRoleType.INSPECTION.getCode());
                    if (0 < tempConfirmedLeadDcoList.size()
                            + tempConfirmedInspectionLeadDcoList.size()) {
                        continue candidate_loop;
                    }
                    // DCPMG#154-2 add end

                    // システムマスタの設定により、追加で前泊後泊の条件を確認
                    // 後泊
                    if (isAbleStayNight) {
                        // 後泊必要の場合、DCPカレンダー情報の検査日を取得する
                        TDcpCalendar todayInfo =
                                b030514Repository.getTodayDcpCalendar(userId, testingDate);
                        if ((todayInfo != null) && (todayInfo.getIsAbleStay() != null)) {
                            // 取得出来る場合
                            if (!todayInfo.getIsAbleStay()) {
                                // 後泊不可の場合、リストに追加せず候補としない
                                continue candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue candidate_loop;
                        }
                    }

                    // 前泊
                    if (isAbleStayBefore) { 
                        // 前泊必要の場合、DCPカレンダー情報の検査日を取得する
                        TDcpCalendar yesterdayInfo =
                                b030514Repository.getYesterdayDcpCalendar(userId, testingDate);
                        if ((yesterdayInfo != null) && (yesterdayInfo.getIsAbleStay() != null)) {
                            // 取得出来る場合
                            if (!yesterdayInfo.getIsAbleStay()) {
                                // 前泊不可の場合、リストに追加せず候補としない
                                continue candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue candidate_loop;
                        }
                    }

                    // システムマスタの設定により、追加でDCPランクの条件を確認
                    MDcpQualification tmpQualification =
                            b030514Repository.getQualificationByUserId(userId);
                    Optional<MDcpQualification> optMDcpQualification =
                            Optional.ofNullable(tmpQualification);
                    MDcpQualification mDcpQualification = optMDcpQualification
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_QUALIFICATION_DCPRANK_NOTSET,
                                    new String[] {Integer.toString(userId)}, Locale.JAPANESE)));

                    // 担当者のDCPランクがアサイン条件になっているか確認
                    String dcpRank = mDcpQualification.getDcpRank();
                    if (DcpRankType.S1.getCode().equals(dcpRank)) {
                        // S1の場合
                        // アサイン条件：ランクS1が入っていない場合、リストに追加せず候補としない
                        if (!isSelectS1)
                            continue candidate_loop;
                    } else if (DcpRankType.S2.getCode().equals(dcpRank)) {
                        // S2の場合
                        // アサイン条件：ランクS2が入っていない場合、リストに追加せず候補としない
                        if (!isSelectS2)
                            continue candidate_loop;
                    } else if (DcpRankType.S3.getCode().equals(dcpRank)) {
                        // S3の場合
                        // アサイン条件：ランクS3が入っていない場合、リストに追加せず候補としない
                        if (!isSelectS3)
                            continue candidate_loop;
                    } else {
                        // ランクS1 ～ S3に入っていない場合、リストに追加せず候補としない
                        continue candidate_loop;
                    }

                    // 対象者が女性限定になる場合、女性かどうか確認
                    if (isAssignFemaleOnly) {
                        // DCPユーザ付加情報マスタで性別を確認
                        MDcpInformation tmpAddinfo =
                                b030514Repository.getAdditionalInformationByUserId(userId);
                        Optional<MDcpInformation> optMDcpInformation =
                                Optional.ofNullable(tmpAddinfo);
                        MDcpInformation mDcpInfo = optMDcpInformation.orElseThrow(
                                () -> new DataNotFoundException(messageSource.getMessage(
                                        B030514Const.ERROR_INFORMATION_GENDERTYPE_NOTSET,
                                        new String[] {Integer.toString(userId)}, Locale.JAPANESE)));
                        // 性別が女性以外場合、リストに追加せず候補としない
                        if (!(GenderType.FEMALE.getCode().equals(mDcpInfo.getGenderType())))
                            continue candidate_loop;
                    }

                    // 外国語区分が無しの場合は必ず条件をオンにする
                    Integer conditionsScoreDefault = B030514Const.CONDITIONS_SCORE_ZERO;
                    Boolean languageConditionDefault = Boolean.FALSE;
                    if (LanguageType.NONE.getCode().equals(languageType)) {
                        languageConditionDefault = Boolean.TRUE;
                        conditionsScoreDefault = 1;
                    }

                    // すべての条件が問題無い場合はDCP選定情報（ICT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    // 時間帯区分:早朝
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.EARLY.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.PAST_MISSIONS.getValue(), Boolean.FALSE,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:AM
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.MORNING.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.PAST_MISSIONS.getValue(), Boolean.FALSE,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:PM
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.AFTERNOON.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.PAST_MISSIONS.getValue(), Boolean.FALSE,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:夜間
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.EVENING.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.PAST_MISSIONS.getValue(), Boolean.FALSE,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);

                    // 候補者毎の処理ここまで
                    continue candidate_loop;
                }

                // 以下の条件で更に有力候補対象を絞る
                // ・該当ミッションの競技に過去に参加したことがある
                // ・該当ミッションの外国語に該当している
                // ・用務地とDCP在住地との距離
                // DCPMG#144 start
                // ・対象日に備考の入力が無い
                // DCPMG#144 end

                // ※ 以下は仮確定条件だが、リードDCOは仮確定を行わないので確認しない
                // ・検査期間中の参加割合
                // ・直前に別のミッションが割当されている場合

                // 取得した必要な参加日数以上参加可能なリードDCOをDCP選定情報（ICT）から取得する
                List<User> strongCandidateList = b030514Repository.getIctParticipation(missionKey,
                        testingDate, DcpRoleType.DCO_LEAD.getCode());

                // DCPMG#151 add start
                // ミッション、日付、役割が同じ仮確定情報を取得
                // 視察ミッションの初日であれば役割は視察になる
                String role_type = DcpRoleType.DCO_LEAD.getCode();
                if((isInspectionMission) && (testingDateFrom.equals(testingDate))) {
                    role_type = DcpRoleType.INSPECTION.getCode();
                }
                List<TDcpProvisional> provisionalList = b030514Repository.getIctDcpProvisionalInfo(
                        testingDate, missionKey, role_type);
                // DCPMG#151 add end

                // 有力候補者毎に処理
                strong_candidate_loop: for (User user : strongCandidateList) {

                    Integer userId = user.getUserId();
                    // 有力候補条件合致点数
                    int condScore = 0;
                    // 該当ミッションの競技に過去に参加したことがある
                    Boolean is_met_condition1 = Boolean.FALSE;
                    // 該当ミッションの外国語に該当している（その他言語は確認不要）
                    Boolean is_met_condition2 = Boolean.FALSE;
                    // 用務地とDCP在住地との距離関係が適正
                    Boolean is_met_condition3 = Boolean.FALSE;
                    // DCPMG#144 start
                    // 該当ミッション日に備考の入力がない
                    Boolean is_met_condition4 = Boolean.FALSE;
                    // DCPMG#144 end

                    // 外国語区分が無しの場合は必ず条件をオンにする
                    if (LanguageType.NONE.getCode().equals(languageType)) {
                        condScore = 1;
                        is_met_condition2 = Boolean.TRUE;
                    }

                    // 該当ミッションの競技に過去に参加したことがある
                    List<ProcessDate> previousExperienceList =
                            b030514Repository.getPreviousExperienceLeadDco(userId, sportsId);

                    if (!previousExperienceList.isEmpty()) {
                        // データが取得出来た
                        is_met_condition1 = Boolean.TRUE;
                        condScore++;
                    }

                    // 該当ミッションの外国語に該当している（その他は不要）
                    MDcpInformation tmpDcpInfo =
                            b030514Repository.getAdditionalInformationByUserId(userId);
                    Optional<MDcpInformation> optMDcpInformation =
                            Optional.ofNullable(tmpDcpInfo);
                    MDcpInformation langInfo = optMDcpInformation
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_INFORMATION_LANGUAGE_NOTSET,
                                    new String[] {Integer.toString(userId)}, Locale.JAPANESE)));
                    boolean can_speak_english = langInfo.getCanSpeakEnglish();
                    boolean can_speak_chinese = langInfo.getCanSpeakChinese();

                    if (LanguageType.ENGLISH.getCode().equals(languageType)) {

                        // 該当ミッションの外国語区分が英語
                        if (can_speak_english) {
                            // ユーザ付加情報の使用可能言語（英語）がtrueの場合
                            is_met_condition2 = Boolean.TRUE;
                            condScore++;
                        }

                    } else if (LanguageType.CHINESE.getCode().equals(languageType)) {

                        // 該当ミッションの外国語区分が中国語
                        if (can_speak_chinese) {
                            // ユーザ付加情報の使用可能言語（中国語）がtrueの場合
                            is_met_condition2 = Boolean.TRUE;
                            condScore++;
                        }

                    } else if (LanguageType.BOTH.getCode().equals(languageType)) {

                        // 該当ミッションの外国語区分が両方
                        if ((can_speak_english) && (can_speak_chinese)) {
                            // ユーザ付加情報の使用可能言語（英語）、使用可能言語（中国語）共にtrueの場合
                            is_met_condition2 = Boolean.TRUE;
                            condScore++;
                        }
                    }

                    // 用務地とDCP在住地との距離確認
                    List<User> regionalRelationshipsList =
                            b030514Repository.getRegionalRelationships(missionKey, userId);

                    if (!regionalRelationshipsList.isEmpty()) {

                        // データが取得出来た場合（=在住地優先順位マスタに該当がある）
                        is_met_condition3 = Boolean.TRUE;
                        condScore++;
                    }
                    
                    // DCPMG#144 start
                    // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
                    TDcpCalendar todayInfo =
                            b030514Repository.getTodayDcpCalendar(userId, testingDate);
                    if ((todayInfo != null) && (todayInfo.getIsRemarksWritten() != null)) {
                        if (!todayInfo.getIsRemarksWritten()) {
                            // 備考入力が無い
                            is_met_condition4 = Boolean.TRUE;
                            condScore++;
                        }
                    }
                    // DCPMG#144 end

                    // DCPMG#144  条件が増えたためスコアを増加 start
                    // 満点の場合
                    Boolean isStrongCandidate = condScore == 4 ? Boolean.TRUE : Boolean.FALSE;
                    // DCPMG#144 end

                    // 必要参加日数を満たしているか
                    AvailableDaysDto availableDaysDto =
                            b030514Repository.getIctAvailableDays(userId, missionKey);
                    Long availableDay = availableDaysDto == null ? 0 : availableDaysDto.getDays();
                    if (availableDay < requiredDays) {
                        // 有力候補フラグをfalseにする
                        isStrongCandidate = Boolean.FALSE;
                    }

                    // DCPMG#151 add start
                    // 仮確定されている場合は強制的に有力候補になる
                    if (!isStrongCandidate) {
                        provisional_loop: for(TDcpProvisional provisional : provisionalList) {
                            if (provisional.getUserId().equals(userId)) {
                                isStrongCandidate = Boolean.TRUE;
                                break provisional_loop;
                            }
                        }
                    }
                    // DCPMG#151 add end

                    // DCP選定情報（ICT）の有力候補者情報を更新
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.EARLY.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.PAST_MISSIONS.getValue(), is_met_condition1,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition2,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.MORNING.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.PAST_MISSIONS.getValue(), is_met_condition1,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition2,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.AFTERNOON.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.PAST_MISSIONS.getValue(), is_met_condition1,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition2,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.EVENING.getCode(), missionKey,
                            DcpRoleType.DCO_LEAD.getCode(), isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.PAST_MISSIONS.getValue(), is_met_condition1,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition2,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end

                    // 有力候補者毎の処理ここまで
                    continue strong_candidate_loop;
                }

                // 検査日毎の処理ここまで
                continue date_loop;
            }

            if(isInspectionMission) {
                // 視察ミッション処理
                LocalDate firstDay = tMissionBase.getTestingDateFrom().toLocalDate();
                LocalDate secondDay = tMissionBase.getTestingDateFrom().toLocalDate().plusDays(1);

                // 初日のリードDCOリストを取得する 
                List<User> userList1stday = subExtractLeadDcoRepository.getSelectIctUserList(
                        Date.valueOf(firstDay), missionKey, DcpRoleType.DCO_LEAD.getCode());
                // 2日目のリードDCOリストを取得する 
                List<User> userList2ndday = subExtractLeadDcoRepository.getSelectIctUserList(
                        Date.valueOf(secondDay), missionKey, DcpRoleType.DCO_LEAD.getCode());
                // DCPMG#154-2 add start 初日の視察リストを取得する(仮確定済)
                List<User> userListInspection = subExtractLeadDcoRepository.getSelectIctUserList(
                        Date.valueOf(firstDay), missionKey, DcpRoleType.INSPECTION.getCode());
                // DCPMG#154-2 add end

                // ユーザリスト
                List<Integer> userList1 = new ArrayList<>();
                List<Integer> userList2 = new ArrayList<>();
                // DCPMG#154-2 add 初日の視察に仮確定しているユーザを保持
                List<Integer> userInspectionList = new ArrayList<>();
                // ユーザリスト取り出し
                for (User user : userList1stday) {
                    userList1.add(user.getUserId());
                }
                for (User user : userList2ndday) {
                    userList2.add(user.getUserId());
                }
                // DCPMG#154-2 add start 初日の視察に仮確定しているユーザを保持
                for (User user : userListInspection) {
                    userInspectionList.add(user.getUserId());
                }
                // DCPMG#154-2 add end
                // 1日目を基準
                for (Integer userId : userList1) {
                    if( userList2.contains(userId) ) {
                        // 1日目も2日目も存在
                        // 初日のリードDCOを視察に更新
                        subExtractLeadDcoRepository.updateInspectionMission(userId,
                                testingDateFrom, missionKey, DcpRoleType.DCO_LEAD.getCode(),
                                DcpRoleType.INSPECTION.getCode());
                    } else {
                        // 1日目のみ存在
                        // 1日目を物理削除
                        subExtractLeadDcoRepository.deleteDcpSelectIct(userId, testingDateFrom,
                                missionKey, DcpRoleType.DCO_LEAD.getCode());
                    }
                }
                // 2日目を基準
                // DCPMG#151 add start
                // ミッション、日付、役割が同じ仮確定情報を取得
                List<TDcpProvisional> provisionalListSecondDay =
                        b030514Repository.getIctDcpProvisionalInfo(Date.valueOf(secondDay), missionKey,
                                DcpRoleType.DCO_LEAD.getCode());
                // DCPMG#151 add end
                for (Integer userId : userList2) {
                    // DCPMG#154-2 change 初日が視察の場合も何もしない(仮確定されている)
                    if ((userList1.contains(userId)) || (userInspectionList.contains(userId))) {
                        // 1日目も2日目も存在
                        // 1日目で処理しているので何もしない
                    } else {
                        // 2日目のみ存在
                        // 2日目の有力候補フラグをオフ
                        // DCPMG#151 change start
                        // 仮確定されている場合は強制的に有力候補になるためオフにしない
                        Boolean isProvisional = Boolean.FALSE;
                        for(TDcpProvisional provisional : provisionalListSecondDay) {
                            if (provisional.getUserId().equals(userId)) {
                                isProvisional = Boolean.TRUE;
                                break;
                            }
                        }
                        if (!isProvisional) {
                            subExtractLeadDcoRepository.updateIsStrongCandidateFalse(userId,
                                    Date.valueOf(secondDay), missionKey);
                        }
                        // DCPMG#151 change end
                    }
                }
            }

            // 処理ミッション
            targetMissions.add(missionKey);

            // ミッション毎の処理ここまで
            continue mission_loop;
        }

        // ICT側の処理ここまで
        return targetMissions;
    }

    /**
     * OOCT側のリードDCO選出処理.<br>
     * リードDCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editOoctLeadDcoProc() throws Exception {

        // 処理ミッション
        Set<Integer> targetMissions = new TreeSet<>();
        // 一時リスト
        List<User> tempList = null;

        // アサイン条件：女性のみ
        boolean isAssignFemaleOnly = false;

        // システムマスタからアサイン条件（前泊後泊、DCPランク）を取得
        Optional<MSystemDefaults> optSystemDefaults =
                tSystemDefaultsRepository.findById(B030514Const.SYSTEM_CODE);
        MSystemDefaults tSystemDefaults =
                optSystemDefaults.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_SYSTEMDEFAULTS_DATA_NOTFOUND,
                                new String[] {}, Locale.JAPANESE)));
        // アサイン条件：前泊必要
        boolean isAbleStayBefore = tSystemDefaults.getIsAbleStayBefore();
        // アサイン条件：後泊必要
        boolean isAbleStayNight = tSystemDefaults.getIsAbleStayNight();
        // アサイン条件：ランクS1
        boolean isSelectS1 = tSystemDefaults.getIsSelectS1();
        // アサイン条件：ランクS2
        boolean isSelectS2 = tSystemDefaults.getIsSelectS2();
        // アサイン条件：ランクS3
        boolean isSelectS3 = tSystemDefaults.getIsSelectS3();

        // DCP割当対象ミッションの抽出
        List<TMissionSortOoct> tMissionSortOoctList =
                subExtractLeadDcoRepository.getLeadDcpAssignmentMissionsOoct();

        mission_loop: for (TMissionSortOoct tMissionSortOoct : tMissionSortOoctList) {

            Integer missionKey = tMissionSortOoct.getMissionKey();

            // DCPMG#161 delete 確定済であっても選出する

            // ミッション基本情報を取得
            TMissionBase tmpBase = b030514Repository.getMissionBaseByMissionkey(missionKey);
            Optional<TMissionBase> optTMissionBase = Optional.ofNullable(tmpBase);
            TMissionBase tMissionBase = optTMissionBase.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_MISSIOBASE_DATA_NOTFOUND,
                            new String[] {missionKey.toString()}, Locale.JAPANESE)));
            Date testingDateFrom = tMissionBase.getTestingDateFrom();

            // ミッションの通告時刻より時間帯区分を判定する
            String tmpTimeSlot =
                    b030514Repository.getMissionTimeSlotType(B030514Const.SYSTEM_CODE, missionKey);
            Optional<String> optTimeSlotType = Optional.ofNullable(tmpTimeSlot);
            // 取得出来ない場合は例外
            String timeSlotType = optTimeSlotType.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_SYSTEMDEFAULTS_TIMEZONE_NOTFOUND,
                            new String[] {missionKey.toString()}, Locale.JAPANESE)));

            LocalDate dateFrom = tMissionBase.getTestingDateFrom().toLocalDate();
            LocalDate dateTo = tMissionBase.getTestingDateTo().toLocalDate().plusDays(1);

            // 検査日別情報は利用せず、1日毎に処理
            date_loop: for (LocalDate tempDate = dateFrom; tempDate.isBefore(dateTo); tempDate =
                    tempDate.plusDays(1)) {

                // 対象日
                Date targetDate = Date.valueOf(tempDate);

                // 一旦アサイン条件を男女可に初期化
                isAssignFemaleOnly = false;

                // 現在のDCP割当状況のデータを削除
                subExtractLeadDcoRepository.deleteTDcpAssignStatus(missionKey, targetDate);
                // 空の初期データを登録
                Timestamp tDcpAssignStatusTime = new Timestamp(System.currentTimeMillis());
                // DCP割当状況の初期データを登録
                subExtractLeadDcoRepository.insertTDcpAssignStatus(
                        missionKey,
                        targetDate,
                        B030514Const.PERSON_ONE,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        B030514Const.PERSON_ZERO,
                        Boolean.FALSE,
                        tDcpAssignStatusTime,
                        B030514Const.BATCH_CREATED_BY,
                        tDcpAssignStatusTime,
                        B030514Const.BATCH_UPDATED_BY);


                // ミッション基本情報、予定検体・分析数（DB除く）より競技者が女性のみかどうか取得
                CompetitorsCountDto countDto =
                        b030514Repository.getCompetitorsCountDto(missionKey, testingDateFrom);
                Optional<Integer> optMales = Optional.ofNullable(countDto.getMales());
                Optional<Integer> optFemales = Optional.ofNullable(countDto.getFemales());
                Integer males = optMales.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_ANALYSISPLAN_MALE_NOTFOUND,
                                new String[] {missionKey.toString(), testingDateFrom.toString()},
                                Locale.JAPANESE)));
                Integer females = optFemales.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_ANALYSISPLAN_FEMALE_NOTFOUND,
                                new String[] {missionKey.toString(), testingDateFrom.toString()},
                                Locale.JAPANESE)));

                if ((males == 0) && (females > 0)) {
                    // 男性競技者が0 かつ 女性競技者が0より大きい場合
                    isAssignFemaleOnly = true;
                }

                // 以下の条件でリードDCO候補対象を絞る
                // ・DCPカレンダー情報に当該日かつ該当時間帯で参加可能で登録がされている
                // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除 
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                if (TimeSlotType.EARLY.getCode().equals(timeSlotType)) {

                    // 早朝の場合
                    tempList = subExtractLeadDcoRepository
                            .getParticipantsLeadDcoListEarlyOoct(missionKey, targetDate);

                } else if (TimeSlotType.MORNING.getCode().equals(timeSlotType)) {

                    // AMの場合
                    tempList = subExtractLeadDcoRepository
                            .getParticipantsLeadDcoListMorningOoct(missionKey, targetDate);

                } else if (TimeSlotType.AFTERNOON.getCode().equals(timeSlotType)) {

                    // PMの場合
                    tempList = subExtractLeadDcoRepository
                            .getParticipantsLeadDcoListAfternoonOoct(missionKey, targetDate);

                } else if (TimeSlotType.EVENING.getCode().equals(timeSlotType)) {

                    // 夜間の場合
                    tempList = subExtractLeadDcoRepository
                            .getParticipantsLeadDcoListEveningOoct(missionKey, targetDate);
                }

                // 候補者毎に追加で条件確認
                candidate_loop: for (User user : tempList) {

                    Integer userId = user.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                            .getDcpProvisionalInfo(userId, targetDate, timeSlotType,
                                    missionKey, DcpRoleType.DCO_LEAD.getCode());
                    if (0 < tempConfirmedDcpList.size()) {
                        continue candidate_loop;
                    }
                    // DCPMG#154-2 add end

                    // システムマスタの設定により、追加で前泊後泊の条件を確認
                    // 後泊
                    if (isAbleStayNight) {
                        // 後泊必要の場合、DCPカレンダー情報の検査日を取得する
                        TDcpCalendar todayInfo =
                                b030514Repository.getTodayDcpCalendar(userId, targetDate);
                        if ((todayInfo != null) && (todayInfo.getIsAbleStay() != null)) {
                            // 取得出来る場合
                            if (!todayInfo.getIsAbleStay()) {
                                // 後泊不可の場合、リストに追加せず候補としない
                                continue candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue candidate_loop;
                        }
                    }

                    // 前泊
                    if (isAbleStayBefore) { 
                        // 前泊必要の場合、DCPカレンダー情報の検査日を取得する
                        TDcpCalendar yesterdayInfo =
                                b030514Repository.getYesterdayDcpCalendar(userId, targetDate);
                        if ((yesterdayInfo != null) && (yesterdayInfo.getIsAbleStay() != null)) {
                            // 取得出来る場合
                            if (!yesterdayInfo.getIsAbleStay()) {
                                // 前泊不可の場合、リストに追加せず候補としない
                                continue candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue candidate_loop;
                        }
                    }

                    // システムマスタの設定により、追加でDCPランクの条件を確認
                    MDcpQualification tmpQualification =
                            b030514Repository.getQualificationByUserId(userId);
                    Optional<MDcpQualification> optMDcpQualification =
                            Optional.ofNullable(tmpQualification);
                    MDcpQualification mDcpQualification = optMDcpQualification
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_QUALIFICATION_DCPRANK_NOTSET,
                                    new String[] {Integer.toString(userId)}, Locale.JAPANESE)));

                    // 担当者のDCPランクがアサイン条件になっているか確認
                    String dcpRank = mDcpQualification.getDcpRank();
                    if (DcpRankType.S1.getCode().equals(dcpRank)) {
                        // S1の場合
                        // アサイン条件：ランクS1が入っていない場合、リストに追加せず候補としない
                        if (!isSelectS1)
                            continue candidate_loop;
                    } else if (DcpRankType.S2.getCode().equals(dcpRank)) {
                        // S2の場合
                        // アサイン条件：ランクS2が入っていない場合、リストに追加せず候補としない
                        if (!isSelectS2)
                            continue candidate_loop;
                    } else if (DcpRankType.S3.getCode().equals(dcpRank)) {
                        // S3の場合
                        // アサイン条件：ランクS3が入っていない場合、リストに追加せず候補としない
                        if (!isSelectS3)
                            continue candidate_loop;
                    } else {
                        // ランクS1 ～ S3に入っていない場合、リストに追加せず候補としない
                        continue candidate_loop;
                    }

                    // 対象者が女性限定になる場合、女性かどうか確認
                    if (isAssignFemaleOnly) {
                        // DCPユーザ付加情報マスタで性別を確認
                        MDcpInformation tmpAddinfo =
                                b030514Repository.getAdditionalInformationByUserId(userId);
                        Optional<MDcpInformation> optMDcpInformation =
                                Optional.ofNullable(tmpAddinfo);
                        MDcpInformation mDcpInfo = optMDcpInformation.orElseThrow(
                                () -> new DataNotFoundException(messageSource.getMessage(
                                        B030514Const.ERROR_INFORMATION_GENDERTYPE_NOTSET,
                                        new String[] {Integer.toString(userId)}, Locale.JAPANESE)));
                        // 性別が女性以外場合、リストに追加せず候補としない
                        if (!(GenderType.FEMALE.getCode().equals(mDcpInfo.getGenderType())))
                            continue candidate_loop;
                    }

                    // すべての条件が問題無い場合はDCP選定情報（OOCT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    b030514Repository.inserteDcpSelectOoct(userId, targetDate, timeSlotType,
                            missionKey, DcpRoleType.DCO_LEAD.getCode(), Boolean.FALSE,
                            B030514Const.CONDITIONS_SCORE_ZERO,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);

                    // 候補者毎の処理ここまで
                    continue candidate_loop;
                }

                // 以下の条件で更に有力候補対象を絞る
                // ・用務地とDCP在住地との距離
                // DCPMG#144 start
                // ・対象日に備考の入力が無い
                // DCPMG#144 end

                // ※ 以下は仮確定条件だが、リードDCOは仮確定を行わないので確認しない
                // ・直前に別のミッションが割当されている場合

                // DCP選定情報（OOCT）からユーザリストを取得する
                List<User> strongCandidateList = b030514Repository.getOoctParticipation(missionKey,
                        targetDate, timeSlotType, DcpRoleType.DCO_LEAD.getCode());

                // DCPMG#151 add start
                // ミッション、日付、時間帯、役割が同じ仮確定情報を取得
                List<TDcpProvisional> provisionalList = b030514Repository.getOoctDcpProvisionalInfo(
                        targetDate, timeSlotType, missionKey, DcpRoleType.DCO_LEAD.getCode());
                // DCPMG#151 add end

                // 有力候補者毎に処理
                strong_candidate_loop: for (User user : strongCandidateList) {

                    Integer userId = user.getUserId();
                    // 有力候補条件合致点数
                    int condScore = 0;
                    // 用務地とDCP在住地との距離関係が問題ない
                    Boolean is_met_condition1 = Boolean.FALSE;
                    // DCPMG#144 start
                    // 対象日に備考の入力が無い
                    Boolean is_met_condition2 = Boolean.FALSE;
                    // DCPMG#144 end

                    // 用務地とDCP在住地との距離確認
                    List<User> regionalRelationshipsList =
                            b030514Repository.getRegionalRelationships(missionKey, userId);

					// DCPMG#144 start
                    if (!regionalRelationshipsList.isEmpty()) {

                        // データが取得出来た場合（=在住地優先順位マスタに該当がある）
                        is_met_condition1 = Boolean.TRUE;
                        condScore++;
                    }
                    // DCPMG#144 end
                    
                    // DCPMG#144 start :TODO  名前変更
                    // DCPカレンダー情報の対象日に備考入力が無い場合は必ず条件をオンにする
                    TDcpCalendar todayInfo =
                            b030514Repository.getTodayDcpCalendar(userId, targetDate);
                    if ((todayInfo != null) && (todayInfo.getIsRemarksWritten() != null)) {
                        if (!todayInfo.getIsRemarksWritten()) {
                            // 備考入力が無い
                            is_met_condition2 = Boolean.TRUE;
                            condScore++;
                        }
                    }
                    // DCPMG#144 end

                    // DCPMG#144 条件が増えたためスコアを増加 start
                    // 満点の場合
                    Boolean isStrongCandidate = condScore == 2 ? Boolean.TRUE : Boolean.FALSE;
                    // DCPMG#144 end

                    // DCPMG#151 add start
                    // 仮確定されている場合は強制的に有力候補になる
                    if (!isStrongCandidate) {
                        provisional_loop: for(TDcpProvisional provisional : provisionalList) {
                            if (provisional.getUserId().equals(userId)) {
                                isStrongCandidate = Boolean.TRUE;
                                break provisional_loop;
                            }
                        }
                    }
                    // DCPMG#151 add end

                    // DCP選定情報（OOCT）の有力候補者情報を更新
                    b030514Repository.updateDcpOoctStrongCandidate(userId, targetDate, timeSlotType,
                            missionKey, DcpRoleType.DCO_LEAD.getCode(), isStrongCandidate,
                            new Integer(condScore),
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE);

                    // 有力候補者毎の処理ここまで
                    continue strong_candidate_loop;
                }

                // 処理ミッション
                targetMissions.add(missionKey);

                // 検査日毎の処理ここまで
                continue date_loop;
            }

            // ミッション毎の処理ここまで
            continue mission_loop;
        }

        // OOCT側の処理ここまで
        return targetMissions;
    }
}
