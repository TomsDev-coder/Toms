//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubTaskExtractDco.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA . All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.tasklet;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import jp.co.seiko_sol.B030514Const;
import jp.co.seiko_sol.domain.MDcpInformation;
import jp.co.seiko_sol.domain.MDcpQualification;
import jp.co.seiko_sol.domain.MSystemDefaults;
import jp.co.seiko_sol.domain.TAssignHeader;
import jp.co.seiko_sol.domain.TDcpCalendar;
import jp.co.seiko_sol.domain.TDcpManualAssign;
import jp.co.seiko_sol.domain.TDcpProvisional;
import jp.co.seiko_sol.domain.TMissionBase;
import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.domain.TTestingDate;
import jp.co.seiko_sol.dto.AvailableDaysDto;
import jp.co.seiko_sol.dto.ComponentRatioHighDto;
import jp.co.seiko_sol.dto.ComponentRatioLowDto;
import jp.co.seiko_sol.dto.ComponentRatioOoctDto;
import jp.co.seiko_sol.dto.DcpSelectInfoDto;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.ProcessDate;
import jp.co.seiko_sol.dto.UrineSampleCountDto;
import jp.co.seiko_sol.dto.User;
import jp.co.seiko_sol.enumeration.DcpRankType;
import jp.co.seiko_sol.enumeration.DcpRoleType;
import jp.co.seiko_sol.enumeration.DisciplineType;
import jp.co.seiko_sol.enumeration.GenderType;
import jp.co.seiko_sol.enumeration.LanguageType;
import jp.co.seiko_sol.enumeration.ListLocationType;
import jp.co.seiko_sol.enumeration.StrongCandidateType;
import jp.co.seiko_sol.enumeration.TimeSlotType;
import jp.co.seiko_sol.exception.BatchRunTimeException;
import jp.co.seiko_sol.exception.DataNotFoundException;
import jp.co.seiko_sol.repository.B030514Repository;
import jp.co.seiko_sol.repository.MSystemDefaultsRepository;
import jp.co.seiko_sol.repository.SubExtractDcoRepository;

/**
 * DCO選出処理クラス.<br>
 * DCOを条件によって選出する処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class SubExtractDco {

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(SubExtractDco.class);

    /** 処理名 */
    private static final String PROC_NAME = "DCO選出処理";

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** システム設定マスタアクセス用Repository */
    @Autowired
    private MSystemDefaultsRepository tSystemDefaultsRepository;

    /** DCO選出処理アクセス用Repository */
    @Autowired
    private SubExtractDcoRepository subExtractDcoRepository;

    /** 自動アサインバッチ処理（ID：B030514）アクセスRepository */
    @Autowired
    private B030514Repository b030514Repository;

    /** 取得場所（0：最初、1：最後、2：中間） */
    private String listLocationDco;

    /** 男性DCO必要人数 */
    private int requiredMaleDco;

    /** 女性DCO必要人数 */
    private int requiredFemaleDco;

    /** 男性DCOアサイン済人数 */
    private int assignedMaleDco;

    /** 女性DCOアサイン済人数 */
    private int assignedFemaleDco;

    /**
     * DCO選出処理.<br>
     * DCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    public Set<Integer> process() throws Exception {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        Set<Integer> dcoMissions = new TreeSet<>();

        // 取得場所初期値：最初
        listLocationDco = ListLocationType.FIRST.getCode();

        // ICT側の編集処理
        Set<Integer> ictMissions = editIctDcoProc();
        dcoMissions.addAll(ictMissions);

        // OOCT側の編集処理
        Set<Integer> ooctMissions = editOoctDcoProc();
        dcoMissions.addAll(ooctMissions);

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(endMessage);

        return dcoMissions;
    }

    /**
     * ICT側のDCO選出処理.<br>
     * DCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editIctDcoProc() throws Exception {

        // 処理ミッション
        Set<Integer> targetMissions = new TreeSet<>();

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
        // 連続アサイン可能日数
        int continuousDaysLimit = tSystemDefaults.getContinuousDaysLimit();
        // DCP閾値（DCO基準 ICT）
        int dcoBorderIct = tSystemDefaults.getDcoBorderIct();

        // DCP割当対象ミッションの抽出
        List<TMissionSortIct> tMissionSortIctList =
                subExtractDcoRepository.getDcoAssignmentMissionsIct();

        mission_loop: for (TMissionSortIct tMissionSortIct : tMissionSortIctList) {

            Integer missionKey = tMissionSortIct.getMissionKey();

            // DCPMG#161 delete 確定済であっても選出する

            // ミッション基本情報を取得
            TMissionBase tmpBase = b030514Repository.getMissionBaseByMissionkey(missionKey);
            Optional<TMissionBase> optTMissionBase = Optional.ofNullable(tmpBase);
            TMissionBase tMissionBase = optTMissionBase.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_MISSIOBASE_DATA_NOTFOUND,
                            new String[] {missionKey.toString()}, Locale.JAPANESE)));
            String languageType = tMissionBase.getLanguageType();
            Integer disciplineId = tMissionBase.getDisciplineId();

            // ミッション基本情報とシステム設定マスタから必要な参加日数を取得する
            Integer tmpDays = b030514Repository.getRequiredDays(missionKey, participationRatio);
            Optional<Integer> optRequiredDays = Optional.ofNullable(tmpDays);
            Integer requiredDays = optRequiredDays.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_REQUIREDDAYS_DATA_NOTFOUND,
                            new String[] {missionKey.toString(), Integer.toString(participationRatio)},
                            Locale.JAPANESE)));

            // 検査日別情報を取得
            List<TTestingDate> tTestingDateList =
                    b030514Repository.getTestingDateByMissionKey(missionKey);

            date_loop: for (TTestingDate tTestingDate : tTestingDateList) {

                // 検査日毎に候補を選定
                Date testingDate = tTestingDate.getTestingDate();

                // DCO仮確定人数算出の為、尿検体数を取得
                List<UrineSampleCountDto> urineCountDtoList =
                        subExtractDcoRepository.getUrineSampleCount(missionKey, testingDate);

                // 尿検体採取が無い場合、選出しない
                if (urineCountDtoList.isEmpty())
                    continue date_loop;

                UrineSampleCountDto urineCountDto = urineCountDtoList.get(0);
                Optional<Integer> optMaleCompetitors =
                        Optional.ofNullable(urineCountDto.getMale_competitors());
                Optional<Integer> optFemaleCompetitors =
                        Optional.ofNullable(urineCountDto.getFemale_competitors());
                Optional<Integer> optMaleUrineSample =
                        Optional.ofNullable(urineCountDto.getMale_urine_sample());
                Optional<Integer> optFemaleUrineSample =
                        Optional.ofNullable(urineCountDto.getFemale_urine_sample());

                int maleCompetitors = optMaleCompetitors
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));
                int femaleCompetitors = optFemaleCompetitors
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));
                int maleUrineSample = optMaleUrineSample
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));
                int femaleUrineSample = optFemaleUrineSample
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));

                // DCO人数算出
                int urineSampleCount = maleUrineSample + femaleUrineSample;

                // 尿検体採取が無い場合、選出しない
                if (urineSampleCount == 0)
                    continue date_loop;

                // 必要DCO人数
                int requiredDco = urineSampleCount >= dcoBorderIct ? B030514Const.PERSON_TEN
                      : urineSampleCount;
                int competitors = maleCompetitors + femaleCompetitors;
                int male = (int) Math.ceil(
                        ((double) maleCompetitors / (double) competitors) * (double) requiredDco);
                int female = (int) Math.ceil(
                        ((double) femaleCompetitors / (double) competitors) * (double) requiredDco);

                // 切り上げで整数値化する為、全体人数から少ない性別の人数を除算して数を合わせる
                // 必要男性DCO人数
                requiredMaleDco = (male > female) ? (int) (requiredDco - female) : (int) male;
                // 必要女性DCO人数
                requiredFemaleDco = (female > male) ? (int) (requiredDco - male) : (int) female;

                // リードDCOの処理で作成したDCO割当情報を更新
                Timestamp assignStatusSystemTime = new Timestamp(System.currentTimeMillis());
                b030514Repository.updateDcpAssignStatusDco(missionKey, testingDate, requiredDco,
                        requiredMaleDco, requiredFemaleDco, assignStatusSystemTime,
                        B030514Const.BATCH_UPDATED_BY);

                // 日付、ミッション基本情報KEY、役割区分に該当するDCP選定情報（ICT）を削除
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.DCO.getCode());

                // 以下の条件でDCO候補対象を絞る
                // ・DCPカレンダー情報に当該日に終日参加可能で登録がされている
                // DCPMG#144 　DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                // ・DCPランクがS1,S2,S3,A1,A2
                List<User> selectDcoList =
                        subExtractDcoRepository.getParticipantsDcoListIct(missionKey, testingDate);

                // 候補者毎に追加で条件確認
                dco_candidate_loop: for (User user : selectDcoList) {

                    Integer userId = user.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                            .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                                    missionKey, DcpRoleType.DCO.getCode());
                    if (0 < tempConfirmedDcpList.size()) {
                        continue dco_candidate_loop;
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
                                continue dco_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue dco_candidate_loop;
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
                                continue dco_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue dco_candidate_loop;
                        }
                    }

                    // 外国語区分が無しの場合は必ず条件をオンにする
                    Integer conditionsScoreDefault = B030514Const.CONDITIONS_SCORE_ZERO;
                    Boolean languageConditionDefault = Boolean.FALSE;
                    if (LanguageType.NONE.getCode().equals(languageType)) {
                        languageConditionDefault = Boolean.TRUE;
                        conditionsScoreDefault++;
                    }

                    // DCPランクがS1～S3の場合は競技区分の経験数不問で必ず条件をオンにする
                    MDcpQualification tmpQualification =
                            b030514Repository.getQualificationByUserId(userId);
                    Optional<MDcpQualification> optMDcpQualification =
                            Optional.ofNullable(tmpQualification);
                    MDcpQualification mDcpQualification = optMDcpQualification
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_QUALIFICATION_DCPRANK_NOTSET,
                                    new String[] {Integer.toString(userId)}, Locale.JAPANESE)));

                    Boolean pastMissionsConditionDefault = Boolean.FALSE;
                    if (DcpRankType.S1.getCode().equals(mDcpQualification.getDcpRank())
                            || DcpRankType.S2.getCode().equals(mDcpQualification.getDcpRank())
                            || DcpRankType.S3.getCode().equals(mDcpQualification.getDcpRank())) {
                        pastMissionsConditionDefault = Boolean.TRUE;
                        conditionsScoreDefault++;
                    }

                    // すべての条件が問題無い場合はDCP選定情報（ICT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    // 時間帯区分:早朝
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.EARLY.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(),
                            pastMissionsConditionDefault, 
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:AM
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.MORNING.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(),
                            pastMissionsConditionDefault,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:PM
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.AFTERNOON.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(),
                            pastMissionsConditionDefault,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:夜間
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.EVENING.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            Boolean.FALSE, conditionsScoreDefault,
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(),
                            languageConditionDefault,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(),
                            pastMissionsConditionDefault,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);

                    // 候補者毎の処理ここまで
                    continue dco_candidate_loop;
                }

                // 以下の条件で更に有力候補対象を絞る
                // 1.検査期間中の参加割合
                // 2.該当ミッションの外国語に該当している
                // 3.用務地とDCP在住地との距離
                // 4.DCPランクがA1,A2の場合は該当ミッションの競技に過去に参加したことがある
                // 5.直前に別のミッションが割当されている場合はそのミッションがICTではない
                // DCPMG#144 start
                // 6.検査日に備考の入力が無い
                // DCPMG#144 end

                // 参加可能なDCOをDCP選定情報（ICT）から取得する
                List<User> candidateDcoList = b030514Repository.getIctParticipation(
                        missionKey, testingDate, DcpRoleType.DCO.getCode());

                // DCPMG#151 add start
                // ミッション、日付、役割が同じ仮確定情報を取得
                List<TDcpProvisional> checkListBco =
                        b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                DcpRoleType.DCO.getCode());
                // DCPMG#151 add end

                // 有力候補者毎に処理
                strong_candidate_loop: for (User user : candidateDcoList) {

                    Integer userId = user.getUserId();

                    // 有力候補条件合致点数
                    int condScore = 0;
                    // 該当ミッションの外国語に該当している（その他言語は確認不要）
                    Boolean is_met_condition1 = Boolean.FALSE;
                    // 用務地とDCP在住地との距離関係が適正
                    Boolean is_met_condition2 = Boolean.FALSE;
                    // 該当ミッションの競技に過去に参加したことがある
                    Boolean is_met_condition3 = Boolean.FALSE;
                    // DCPMG#144 start
                    // 該当ミッション日に備考の入力がない
                    Boolean is_met_condition4 = Boolean.FALSE;
                    // DCPMG#144 end

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
                            is_met_condition1 = Boolean.TRUE;
                            condScore++;
                        }

                    } else if (LanguageType.CHINESE.getCode().equals(languageType)) {

                        // 該当ミッションの外国語区分が中国語
                        if (can_speak_chinese) {
                            // ユーザ付加情報の使用可能言語（中国語）がtrueの場合
                            is_met_condition1 = Boolean.TRUE;
                            condScore++;
                        }

                    } else if (LanguageType.BOTH.getCode().equals(languageType)) {

                        // 該当ミッションの外国語区分が両方
                        if ((can_speak_english) && (can_speak_chinese)) {
                            // ユーザ付加情報の使用可能言語（英語）、使用可能言語（中国語）共にtrueの場合
                            is_met_condition1 = Boolean.TRUE;
                            condScore++;
                        }
                    } else if (LanguageType.NONE.getCode().equals(languageType)) {

                        // 外国語区分が無しの場合は必ず条件をオンにする
                        is_met_condition1 = Boolean.TRUE;
                        condScore++;
                    }

                    // 用務地とDCP在住地との距離確認
                    List<User> regionalRelationshipsList =
                            b030514Repository.getRegionalRelationships(missionKey, userId);

                    if (!regionalRelationshipsList.isEmpty()) {

                        // データが取得出来た場合（=在住地優先順位マスタに該当がある）
                        is_met_condition2 = Boolean.TRUE;
                        condScore++;
                    }

                    // 過去の競技経験
                    MDcpQualification tmpQualification =
                            b030514Repository.getQualificationByUserId(userId);
                    Optional<MDcpQualification> optMDcpQualification =
                            Optional.ofNullable(tmpQualification);
                    MDcpQualification mDcpQualification = optMDcpQualification
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_QUALIFICATION_DCPRANK_NOTSET,
                                    new String[] {Integer.toString(userId)}, Locale.JAPANESE)));

                    if (DcpRankType.A1.getCode().equals(mDcpQualification.getDcpRank())
                            || DcpRankType.A2.getCode().equals(mDcpQualification.getDcpRank())) {

                        // DCPランクがA1,A2の場合のみ過去の競技経験が必要

                        // 該当ミッションの競技に過去に参加したことがある
                        List<ProcessDate> previousExperienceList =
                                b030514Repository.getPreviousExperienceDco(userId, disciplineId);

                        if (!previousExperienceList.isEmpty()) {

                            // データが取得出来た
                            is_met_condition3 = Boolean.TRUE;
                        }
                        // 競技経験がなくても有力候補からは除外しない
                        condScore++;
                    }

                    if (DcpRankType.S1.getCode().equals(mDcpQualification.getDcpRank())
                            || DcpRankType.S2.getCode().equals(mDcpQualification.getDcpRank())
                            || DcpRankType.S3.getCode().equals(mDcpQualification.getDcpRank())) {

                        // DCPランクがS1～S3なら競技区分の経験数不問
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

                    // 競技者が男性のみの場合、有力候補者も男性のみとする
                    if ((maleCompetitors > 0) && (femaleCompetitors == 0)) {
                        if (!GenderType.MALE.getCode().equals(tmpDcpInfo.getGenderType())) {
                            // 男性以外の場合は有力候補フラグ = falseに設定
                            isStrongCandidate = Boolean.FALSE;
                        }
                    }

                    // 競技者が女性のみの場合、有力候補者も女性のみとする
                    if ((maleCompetitors == 0) && (femaleCompetitors > 0)) {
                        if (!GenderType.FEMALE.getCode().equals(tmpDcpInfo.getGenderType())) {
                            // 女性以外の場合は有力候補フラグ = falseに設定
                            isStrongCandidate = Boolean.FALSE;
                        }
                    }

                    // DCPMG#151 add start
                    // 仮確定されている場合は強制的に有力候補になる
                    if (!isStrongCandidate) {
                        provisional_loop: for(TDcpProvisional provisional : checkListBco) {
                            if (provisional.getUserId().equals(userId)) {
                                isStrongCandidate = Boolean.TRUE;
                                break provisional_loop;
                            }
                        }
                    }
                    // DCPMG#151 add end

                    // DCP選定情報（ICT）の有力候補者情報を更新
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.EARLY.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition1,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition2,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.MORNING.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition1,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition2,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.AFTERNOON.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition1,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition2,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end
                    b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                            TimeSlotType.EVENING.getCode(), missionKey, DcpRoleType.DCO.getCode(),
                            isStrongCandidate, new Integer(condScore),
                            StrongCandidateType.FOREIGN_LANGUAGE.getValue(), is_met_condition1,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition2,
                            StrongCandidateType.PAST_MISSIONS_DCO.getValue(), is_met_condition3,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition4);
                            // DCPMG#144 end

                    // 有力候補者毎の処理ここまで
                    continue strong_candidate_loop;
                }

                // DCPMG#161-2 add start 確定したミッションは仮確定しない
                TAssignHeader tmpHeader = b030514Repository.getAssignHeaderByMissionkey(missionKey);
                Optional<TAssignHeader> optTAssignHeader = Optional.ofNullable(tmpHeader);
                if (optTAssignHeader.isPresent()) {
                    TAssignHeader tAssignHeader = optTAssignHeader.get();
                    // アサイン確定していれば仮確定しない
                    if (tAssignHeader.getIsAssignFixed()) {
                        continue date_loop;
                    }
                }
                // DCPMG#161-2 add end

                // ミッション、日付、役割が同じ仮確定情報があるか
                // DCPMG#151 delete 仮確定情報取得処理を移動
                if (!checkListBco.isEmpty()) {
                    continue date_loop;
                }

                // DCPMG#154 add start
                // DCO/BCOが仮確定されている場合もDCOの役割が仮確定済みとみなす
                List<TDcpProvisional> listDcoBco =
                        b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                DcpRoleType.DCO_BCO.getCode());
                if (!listDcoBco.isEmpty()) {
                    continue date_loop;
                }
                // DCPMG#154 add end

                // 手動割当でミッション、日付、役割が同じ仮確定情報があれば仮確定しない
                List<String> roleList = new ArrayList<String>();
                roleList.add(DcpRoleType.DCO.getCode());
                roleList.add(DcpRoleType.DCO_BCO.getCode());
                List<TDcpManualAssign> manualAssignList =
                        b030514Repository.getDcpManualProvisionalInfo(testingDate,
                                TimeSlotType.EARLY.getCode(), missionKey, roleList);
                if (!manualAssignList.isEmpty()) {
                    continue date_loop;
                }

                // DCO仮確定候補の情報をDCP選定情報（ICT）から取得
                List<DcpSelectInfoDto> isStrongCandidateList = null;
                if (ListLocationType.FIRST.getCode().equals(listLocationDco)) {
                    // 昇順のリスト
                    isStrongCandidateList =
                            subExtractDcoRepository.getDcoIctIsStrongCandidateAsc(testingDate, missionKey,
                                    DcpRoleType.DCO.getCode());
                } else if (ListLocationType.LAST.getCode().equals(listLocationDco)) {
                    // 降順のリスト
                    isStrongCandidateList =
                            subExtractDcoRepository.getDcoIctIsStrongCandidateDesc(testingDate, missionKey,
                                    DcpRoleType.DCO.getCode());
                }

                // 競技区分を取得
                String discipline =
                        subExtractDcoRepository.getMissionDisciplineType(missionKey);
                Optional<String> optDisciplineType =
                        Optional.ofNullable(discipline);
                String disciplineType = optDisciplineType.orElseThrow(
                        () -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_DISCIPLINETYPE_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));

                if ((DisciplineType.PERSONAL_RECORD.getCode().equals(disciplineType))
                        || (DisciplineType.PERSONAL_SCORE.getCode().equals(disciplineType))) {

                    // リスクが高い競技
                    List<ComponentRatioHighDto> highDtoList = subExtractDcoRepository
                            .getDcoComponentRatiohigh(requiredDco, disciplineType);
                    Optional<ComponentRatioHighDto> optHighDtoList =
                            Optional.ofNullable(highDtoList.get(0));
                    ComponentRatioHighDto componentRatioHigh = optHighDtoList
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_DISCIPLINETYPE_DATA_NOTFOUND,
                                    new String[] {missionKey.toString(), testingDate.toString()},
                                    Locale.JAPANESE)));

                    Map<String, Object> parameter = new HashMap<>();
                    parameter.put(B030514Const.ASSIGNED_DATE, testingDate);
                    parameter.put(B030514Const.MISSION_KEY, missionKey);
                    parameter.put(B030514Const.DCP_ROLE_TYPE, DcpRoleType.DCO.getCode());

                    highRiskCompetitionAssignment(componentRatioHigh, isStrongCandidateList,
                            requiredMaleDco, requiredFemaleDco, parameter, continuousDaysLimit,
                            requiredDays);

                } else if ((DisciplineType.TOURNAMENT.getCode().equals(disciplineType))
                        || (DisciplineType.TEAM.getCode().equals(disciplineType))) {

                    // リスクが低い競技
                    List<ComponentRatioLowDto> lowDtoList = subExtractDcoRepository
                            .getDcoComponentRatioLow(requiredDco, disciplineType);
                    Optional<ComponentRatioLowDto> optLowDtoList =
                            Optional.ofNullable(lowDtoList.get(0));
                    ComponentRatioLowDto componentRatioLow = optLowDtoList
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_DISCIPLINETYPE_DATA_NOTFOUND,
                                    new String[] {missionKey.toString(), testingDate.toString()},
                                    Locale.JAPANESE)));

                    Map<String, Object> parameter = new HashMap<>();
                    parameter.put(B030514Const.ASSIGNED_DATE, testingDate);
                    parameter.put(B030514Const.MISSION_KEY, missionKey);
                    parameter.put(B030514Const.DCP_ROLE_TYPE, DcpRoleType.DCO.getCode());

                    lowRiskCompetitionAssignment(componentRatioLow, isStrongCandidateList,
                            requiredMaleDco, requiredFemaleDco, parameter, continuousDaysLimit,
                            requiredDays);
                }

                // 検査日毎の処理ここまで
                continue date_loop;
            }

            // 処理ミッション
            targetMissions.add(missionKey);
            // 取得位置を切り替え
            updateListLocation();

            // ミッション毎の処理ここまで
            continue mission_loop;
        }

        // ICT側の処理ここまで
        return targetMissions;
    }

    /**
     * DCO仮確定処理（DCO）.<br>
     * 高リスク競技用のDCOを仮確定する処理を記述する。
     * 
     * @param componentRatioHigh DCPランク構成比（ハイリスク用）
     * @param candidateList 候補リスト
     * @param requiredMaleDco 必要アサイン人数（男性）
     * @param requiredFemaleDco 必要アサイン人数（女性）
     * @param parameter パラメータ
     * @param continuousDaysLimit 最大勤務可能日数
     * @param requiredDays 必要参加日数
     * @throws Exception 処理中に例外が発生した場合
     */
    private void highRiskCompetitionAssignment(ComponentRatioHighDto componentRatioHigh,
            List<DcpSelectInfoDto> candidateList, int requiredMaleDco, int requiredFemaleDco,
            Map<String, Object> parameter, int continuousDaysLimit, Integer requiredDays)
            throws Exception {

        // ランク毎のアサイン必要人数
        int requiredA1 = componentRatioHigh.getAsssigned_a1().intValue();
        int requiredS3 = componentRatioHigh.getAsssigned_s3().intValue();
        int requiredS2 = componentRatioHigh.getAsssigned_s2().intValue();
        int requiredS1 = componentRatioHigh.getAsssigned_s1().intValue();
        int requiredDco = requiredA1 + requiredS3 + requiredS2 + requiredS1;

        // 繰越要員数用
        int carryOver = 0;
        // 各ランクアサイン済数
        int assignedA1 = 0;
        int assignedS3 = 0;
        int assignedS2 = 0;
        int assignedS1 = 0;
        int assignedA2 = 0;
        // 性別アサイン済数
        assignedMaleDco = 0;
        assignedFemaleDco = 0;

        // ICTは全時間帯を一括コピーする為、個別の時間帯区分は指定しない
        Date assignedDate = (Date) parameter.get(B030514Const.ASSIGNED_DATE);
        Integer missionKey = (Integer) parameter.get(B030514Const.MISSION_KEY);
        String dcpRoleType = (String) parameter.get(B030514Const.DCP_ROLE_TYPE);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Integer createdBy = B030514Const.BATCH_CREATED_BY;

        // DCPランク：A1 要員
        while (assignedA1 < requiredA1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.A1.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(),
                    assignedDate, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：A1アサイン数
            assignedA1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // A1での不足分を加算
        carryOver = requiredA1 - assignedA1;
        requiredS3 = requiredS3 + carryOver;
        carryOver = 0;
        // DCPランク：S3 要員
        while (assignedS3 < requiredS3) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S3.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(),
                    assignedDate, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S3アサイン数
            assignedS3++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }        
        }

        // S3での不足分を加算
        carryOver = requiredS3 - assignedS3;
        requiredS2 = requiredS2 + carryOver;
        carryOver = 0;
        // DCPランク：S2要員
        while (assignedS2 < requiredS2) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S2.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(),
                    assignedDate, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S2アサイン数
            assignedS2++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // S2での不足分を加算
        carryOver = requiredS2 - assignedS2;
        requiredS1 = requiredS1 + carryOver;
        carryOver = 0;
        // DCPランク：S1要員
        while (assignedS1 < requiredS1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S1.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(), assignedDate,
                    missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1アサイン数
            assignedS1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // S1 ～ S3、A1で充足しない場合はA2をアサイン
        carryOver = requiredS1 - assignedS1;
        // DCPランク：A2要員
        while (assignedA2 < carryOver) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.A2.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(),
                    assignedDate, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：A2アサイン数
            assignedA2++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // リードDCOの処理で作成したDCO割当情報を更新
        Timestamp systemTime = new Timestamp(System.currentTimeMillis());
        b030514Repository.updateDcpAssignStatusDco(missionKey, assignedDate, requiredDco,
                requiredMaleDco, requiredFemaleDco, systemTime, B030514Const.BATCH_UPDATED_BY);
    }

    /**
     * DCO仮確定処理（DCO）.<br>
     * 低リスク競技用のDCOを仮確定する処理を記述する。
     * 
     * @param componentRatioHigh DCPランク構成比（ローリスク用）
     * @param candidateList 候補リスト
     * @param requiredMaleDco 必要アサイン人数（男性）
     * @param requiredFemaleDco 必要アサイン人数（女性）
     * @param parameter パラメータ
     * @param continuousDaysLimit 最大勤務可能日数
     * @param requiredDays 必要参加日数
     * @throws Exception 処理中に例外が発生した場合
     */
    private void lowRiskCompetitionAssignment(ComponentRatioLowDto componentRatioLow,
            List<DcpSelectInfoDto> candidateList, int requiredMaleDco, int requiredFemaleDco,
            Map<String, Object> parameter, int continuousDaysLimit, Integer requiredDays)
            throws Exception {

        // ランク毎のアサイン必要人数
        int requiredA2 = componentRatioLow.getAsssigned_a2().intValue();
        int requiredA1 = componentRatioLow.getAsssigned_a1().intValue();
        int requiredS1S3 = componentRatioLow.getAsssigned_s1s3().intValue();
        int requiredDco = requiredA2 + requiredA1 + requiredS1S3;
        // 繰越要員数用
        int carryOver = 0;
        // 各ランクアサイン済数
        int assignedA2 = 0;
        int assignedA1 = 0;
        int assignedS1S3 = 0;
        // 性別アサイン済数
        assignedMaleDco = 0;
        assignedFemaleDco = 0;

        // ICTは全時間帯を一括コピーする為、個別の時間帯区分は指定しない
        Date assignedDate = (Date) parameter.get(B030514Const.ASSIGNED_DATE);
        Integer missionKey = (Integer) parameter.get(B030514Const.MISSION_KEY);
        String dcpRoleType = (String) parameter.get(B030514Const.DCP_ROLE_TYPE);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Integer createdBy = B030514Const.BATCH_CREATED_BY;

        // DCPランク：A2要員
        while (assignedA2 < requiredA2) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.A2.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(), assignedDate,
                    missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：A2アサイン数
            assignedA2++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // A2での不足分を加算
        carryOver = requiredA2 - assignedA2;
        requiredA1 = requiredA1 + carryOver;
        carryOver = 0;
        // DCPランク：A1要員
        while (assignedA1 < requiredA1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.A1.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(), assignedDate,
                    missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：A1アサイン数
            assignedA1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // A1での不足分を加算
        carryOver = requiredA1 - assignedA1;
        requiredS1S3 = requiredS1S3 + carryOver;
        carryOver = 0;
        // DCPランク：S3 要員
        while (assignedS1S3 < requiredS1S3) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S3.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(), assignedDate,
                    missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3アサイン数
            assignedS1S3++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // DCPランク：S2要員
        while (assignedS1S3 < requiredS1S3) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S2.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(),
                    assignedDate, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3アサイン数
            assignedS1S3++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // DCPランク：S1要員
        while (assignedS1S3 < requiredS1S3) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S1.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoIct(missionKey, assignedDate, user.getUserId(), dcpRoleType,
                    continuousDaysLimit, requiredDays))
                continue;

            // DCP選定情報（ICT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectIct(user.getUserId(),
                    assignedDate, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3アサイン数
            assignedS1S3++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // リードDCOの処理で作成したDCO割当情報を更新
        Timestamp systemTime = new Timestamp(System.currentTimeMillis());
        b030514Repository.updateDcpAssignStatusDco(missionKey, assignedDate, requiredDco,
                requiredMaleDco, requiredFemaleDco, systemTime, B030514Const.BATCH_UPDATED_BY);
    }

    /**
     * OOCT側のDCO選出処理.<br>
     * DCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editOoctDcoProc() throws Exception {

        // 処理ミッション
        Set<Integer> targetMissions = new TreeSet<>();

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
        // 連続アサイン可能日数
        int continuousDaysLimit = tSystemDefaults.getContinuousDaysLimit();

        // DCP割当対象ミッションの抽出
        List<TMissionSortOoct> tMissionSortOoctList =
                subExtractDcoRepository.getDcoAssignmentMissionsOoct();

        mission_loop: for (TMissionSortOoct tMissionSortOoct : tMissionSortOoctList) {

            Integer missionKey = tMissionSortOoct.getMissionKey();

            // DCPMG#161 delete 確定済であっても選出する

            // ミッション基本情報を取得
            TMissionBase tmpBase = b030514Repository.getMissionBaseByMissionkey(missionKey);
            Optional<TMissionBase> optTMissionBase = Optional.ofNullable(tmpBase);
            TMissionBase tMissionBase = optTMissionBase.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_MISSIOBASE_DATA_NOTFOUND,
                            new String[] {missionKey.toString()}, Locale.JAPANESE)));

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

                // DCO仮確定人数算出の為、尿検体数を取得
                List<UrineSampleCountDto> urineCountDtoList =
                        subExtractDcoRepository.getOoctUrineSampleCount(missionKey);

                // 尿検体採取が無い場合、選出しない
                if (urineCountDtoList.isEmpty())
                    continue date_loop;

                UrineSampleCountDto urineCountDto = urineCountDtoList.get(0);
                Optional<Integer> optMaleCompetitors =
                        Optional.ofNullable(urineCountDto.getMale_competitors());
                Optional<Integer> optFemaleCompetitors =
                        Optional.ofNullable(urineCountDto.getFemale_competitors());
                Optional<Integer> optMaleUrineSample =
                        Optional.ofNullable(urineCountDto.getMale_urine_sample());
                Optional<Integer> optFemaleUrineSample =
                        Optional.ofNullable(urineCountDto.getFemale_urine_sample());

                int maleCompetitors = optMaleCompetitors
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), targetDate.toString()},
                                Locale.JAPANESE)));
                int femaleCompetitors = optFemaleCompetitors
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), targetDate.toString()},
                                Locale.JAPANESE)));
                int maleUrineSample = optMaleUrineSample
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), targetDate.toString()},
                                Locale.JAPANESE)));
                int femaleUrineSample = optFemaleUrineSample
                        .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                B030514Const.ERROR_URINECOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), targetDate.toString()},
                                Locale.JAPANESE)));

                // DCO人数算出
                int urineSampleCount = maleUrineSample + femaleUrineSample;

                // 尿検体採取が無い場合、選出しない
                if (urineSampleCount == 0)
                    continue date_loop;

                // 必要DCO人数
                int requiredDco = urineSampleCount;
                int competitors = maleCompetitors + femaleCompetitors;
                int male = (int) Math.ceil(
                        ((double) maleCompetitors / (double) competitors) * (double) requiredDco);
                int female = (int) Math.ceil(
                        ((double) femaleCompetitors / (double) competitors) * (double) requiredDco);

                // 切り上げで整数値化する為、全体人数から少ない性別の人数を除算して数を合わせる
                // 必要男性DCO人数
                requiredMaleDco = (male > female) ? (int) (requiredDco - female) : (int) male;
                // 必要女性DCO人数
                requiredFemaleDco = (female > male) ? (int) (requiredDco - male) : (int) female;

                // リードDCOの処理で作成したDCO割当情報を更新
                Timestamp assignStatusSystemTime = new Timestamp(System.currentTimeMillis());
                b030514Repository.updateDcpAssignStatusDco(missionKey, targetDate, requiredDco,
                        requiredMaleDco, requiredFemaleDco, assignStatusSystemTime,
                        B030514Const.BATCH_UPDATED_BY);

                // 以下の条件でDCO候補対象を絞る
                // ・DCPカレンダー情報に当該日かつ該当時間帯で参加可能で登録がされている
                // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<User> dcoList = new LinkedList<>();

                if (TimeSlotType.EARLY.getCode().equals(timeSlotType)) {

                    // 早朝の場合
                    dcoList = subExtractDcoRepository.getParticipantsDcoListEarlyOoct(missionKey,
                            targetDate);

                } else if (TimeSlotType.MORNING.getCode().equals(timeSlotType)) {

                    // AMの場合
                    dcoList = subExtractDcoRepository.getParticipantsDcoListMorningOoct(missionKey,
                            targetDate);

                } else if (TimeSlotType.AFTERNOON.getCode().equals(timeSlotType)) {

                    // PMの場合
                    dcoList = subExtractDcoRepository
                            .getParticipantsDcoListAfternoonOoct(missionKey, targetDate);

                } else if (TimeSlotType.EVENING.getCode().equals(timeSlotType)) {

                    // 夜間の場合
                    dcoList = subExtractDcoRepository.getParticipantsDcoListEveningOoct(missionKey,
                            targetDate);
                }

                // 候補者毎に追加で条件確認
                dco_candidate_loop: for (User user : dcoList) {

                    Integer userId = user.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                            .getDcpProvisionalInfo(userId, targetDate, timeSlotType,
                                    missionKey, DcpRoleType.DCO.getCode());
                    if (0 < tempConfirmedDcpList.size()) {
                        continue dco_candidate_loop;
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
                                continue dco_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue dco_candidate_loop;
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
                                continue dco_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue dco_candidate_loop;
                        }
                    }

                    // すべての条件が問題無い場合はDCP選定情報（OOCT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    b030514Repository.inserteDcpSelectOoct(userId, targetDate, timeSlotType,
                            missionKey, DcpRoleType.DCO.getCode(), Boolean.FALSE,
                            B030514Const.CONDITIONS_SCORE_ZERO,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE, 
                            // DCPMG#144 end
                            B030514Const.NULL_STRING,Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);

                    // 候補者毎の処理ここまで
                    continue dco_candidate_loop;
                }

                // DCOの有力候補対象を絞る
                // 1.用務地とDCP在住地との距離
                // DCPMG#144 start
                // 2.対象日に備考の入力が無い
                // DCPMG#144 end

                // DCP選定情報（OOCT）からユーザリストを取得する
                List<User> strongCandidateDcoList = b030514Repository.getOoctParticipation(
                        missionKey, targetDate, timeSlotType, DcpRoleType.DCO.getCode());

                // DCPMG#151 add start
                // ミッション、日付、時間帯、役割が同じ仮確定情報を取得
                List<TDcpProvisional> checkListBco = b030514Repository.getOoctDcpProvisionalInfo(
                        targetDate, timeSlotType, missionKey, DcpRoleType.DCO.getCode());
                // DCPMG#151 add end

                // ユーザ毎に処理
                dco_strong_candidate_loop: for (User user : strongCandidateDcoList) {

                    Integer userId = user.getUserId();

                    // 有力候補条件合致点数
                    int condScore = 0;
                    // 用務地とDCP在住地との距離関係が適正
                    Boolean is_met_condition1 = Boolean.FALSE;
                    // DCPMG#144 start
                    // 対象日に備考の入力が無い
                    Boolean is_met_condition2 = Boolean.FALSE;
                    // DCPMG#144 end

                    // 用務地とDCP在住地との距離確認
                    List<User> regionalRelationshipsList =
                            b030514Repository.getRegionalRelationships(missionKey, userId);

                    if (!regionalRelationshipsList.isEmpty()) {

                        // データが取得出来た場合（=在住地優先順位マスタに該当がある）
                        is_met_condition1 = Boolean.TRUE;
                        condScore++;
                    }
                    
                    // DCPMG#144 start 
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

                    // DCPMG#144  条件が増えたためスコアを増加 start
                    // 満点の場合
                    Boolean isStrongCandidate = condScore == 2 ? Boolean.TRUE : Boolean.FALSE;
                    // DCPMG#144 end

                    MDcpInformation tmpDcpInfo =
                            b030514Repository.getAdditionalInformationByUserId(userId);
                    Optional<MDcpInformation> optMDcpInformation =
                            Optional.ofNullable(tmpDcpInfo);
                    MDcpInformation mDcpInformation = optMDcpInformation
                            .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_INFORMATION_GENDERTYPE_NOTSET,
                                    new String[] {Integer.toString(userId)}, Locale.JAPANESE)));

                    // 競技者が男性のみの場合、有力候補者も男性のみとする
                    if ((maleCompetitors > 0) && (femaleCompetitors == 0)) {
                        if (!GenderType.MALE.getCode().equals(mDcpInformation.getGenderType())) {
                            // 男性以外の場合は有力候補フラグ = falseに設定
                            isStrongCandidate = Boolean.FALSE;
                        }
                    }

                    // 競技者が女性のみの場合、有力候補者も女性のみとする
                    if ((maleCompetitors == 0) && (femaleCompetitors > 0)) {
                        if (!GenderType.FEMALE.getCode().equals(mDcpInformation.getGenderType())) {
                            // 女性以外の場合は有力候補フラグ = falseに設定
                            isStrongCandidate = Boolean.FALSE;
                        }
                    }

                    // DCPMG#151 add start
                    // 仮確定されている場合は強制的に有力候補になる
                    if (!isStrongCandidate) {
                        provisional_loop: for(TDcpProvisional provisional : checkListBco) {
                            if (provisional.getUserId().equals(userId)) {
                                isStrongCandidate = Boolean.TRUE;
                                break provisional_loop;
                            }
                        }
                    }
                    // DCPMG#151 add end

                    // DCP選定情報（OOCT）の有力候補者情報を更新
                    b030514Repository.updateDcpOoctStrongCandidate(userId, targetDate, timeSlotType,
                            missionKey, DcpRoleType.DCO.getCode(), isStrongCandidate,
                            new Integer(condScore),
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE);

                    // 有力候補者毎の処理ここまで
                    continue dco_strong_candidate_loop;
                }

                // DCPMG#161-2 add start 確定したミッションは仮確定しない
                TAssignHeader tmpHeader = b030514Repository.getAssignHeaderByMissionkey(missionKey);
                Optional<TAssignHeader> optTAssignHeader = Optional.ofNullable(tmpHeader);
                if (optTAssignHeader.isPresent()) {
                    TAssignHeader tAssignHeader = optTAssignHeader.get();
                    // アサイン確定していれば仮確定しない
                    if (tAssignHeader.getIsAssignFixed()) {
                        continue date_loop;
                    }
                }
                // DCPMG#161-2 add end

                // ミッション、日付、時間帯、役割が同じ仮確定情報があるか
                // DCPMG#151 delete 仮確定情報取得処理を移動
                if (!checkListBco.isEmpty()) {
                    continue date_loop;
                }

                // DCPMG#154 add start
                // DCO/BCOが仮確定されている場合もDCOの役割が仮確定済みとみなす
                List<TDcpProvisional> listDcoBco =
                        b030514Repository.getOoctDcpProvisionalInfo(targetDate, timeSlotType,
                                missionKey, DcpRoleType.DCO_BCO.getCode());
                if (!listDcoBco.isEmpty()) {
                    continue date_loop;
                }
                // DCPMG#154 add end

                // 手動割当でミッション、日付、役割が同じ仮確定情報があれば仮確定しない
                List<String> roleList = new ArrayList<String>();
                roleList.add(DcpRoleType.DCO.getCode());
                roleList.add(DcpRoleType.DCO_BCO.getCode());
                List<TDcpManualAssign> manualAssignList =
                        b030514Repository.getDcpManualProvisionalInfo(targetDate, timeSlotType,
                                missionKey, roleList);
                if (!manualAssignList.isEmpty()) {
                    continue date_loop;
                }

                // DCO仮確定候補の情報をDCP選定情報（OOCT）から取得
                List<DcpSelectInfoDto> candidateList = null;
                if (ListLocationType.FIRST.getCode().equals(listLocationDco)) {
                    // 昇順のリスト
                    candidateList = subExtractDcoRepository.getDcoOoctIsStrongCandidateAsc(targetDate,
                            timeSlotType, missionKey, DcpRoleType.DCO.getCode());
                } else if (ListLocationType.LAST.getCode().equals(listLocationDco)) {
                    // 降順のリスト
                    candidateList = subExtractDcoRepository.getDcoOoctIsStrongCandidateDesc(targetDate,
                            timeSlotType, missionKey, DcpRoleType.DCO.getCode());
                }

                // OOCT用（競技区分で区別はしない）
                List<ComponentRatioOoctDto> ooctDtoList =
                        subExtractDcoRepository.getDcoComponentRatioOoct(requiredDco);
                Optional<ComponentRatioOoctDto> optOoctDtoList =
                        Optional.ofNullable(ooctDtoList.get(0));
                ComponentRatioOoctDto componentRatioOoct =
                        optOoctDtoList.orElseThrow(() -> new DataNotFoundException(messageSource
                                .getMessage(B030514Const.ERROR_DISCIPLINETYPE_DATA_NOTFOUND,
                                        new String[] {missionKey.toString(), targetDate.toString()},
                                        Locale.JAPANESE)));

                Map<String, Object> parameter = new HashMap<>();
                parameter.put(B030514Const.ASSIGNED_DATE, targetDate);
                parameter.put(B030514Const.MISSION_KEY, missionKey);
                parameter.put(B030514Const.DCP_ROLE_TYPE, DcpRoleType.DCO.getCode());
                parameter.put(B030514Const.TIME_SLOT_TYPE, timeSlotType);

                ooctCompetitionAssignment(componentRatioOoct, candidateList, requiredMaleDco,
                        requiredFemaleDco, parameter, continuousDaysLimit);

                // 検査日毎の処理ここまで
                continue date_loop;
            }

            // 処理ミッション
            targetMissions.add(missionKey);
            // 取得位置を切り替え
            updateListLocation();

            // ミッション毎の処理ここまで
            continue mission_loop;
        }

        // ICT側の処理ここまで
        return targetMissions;
    }

    /**
     * DCO仮確定処理（DCO）.<br>
     * OOCT用のDCOを仮確定する処理を記述する。
     * 
     * @param componentRatioOoctDto DCPランク構成比（OOCT用）
     * @param candidateList 候補リスト
     * @param requiredMaleDco 必要アサイン人数（男性）
     * @param requiredFemaleDco 必要アサイン人数（女性）
     * @param parameter パラメータ
     * @param continuousDaysLimit 最大勤務可能日数
     * @throws Exception 処理中に例外が発生した場合
     */
    private void ooctCompetitionAssignment(ComponentRatioOoctDto componentRatioOoctDto,
            List<DcpSelectInfoDto> candidateList, int requiredMaleDco, int requiredFemaleDco,
            Map<String, Object> parameter, int continuousDaysLimit) throws Exception {

        // ランク毎のアサイン必要人数
        int requiredS1S2S3A1 = componentRatioOoctDto.getAsssigned_upper().intValue();
        int requiredA2 = componentRatioOoctDto.getAsssigned_a2().intValue();
        // 繰越要員数用
        int carryOver = 0;
        // 各ランクアサイン済数
        int assignedS1S2S3A1 = 0;
        int assignedA2 = 0;
        // 性別アサイン済数
        assignedMaleDco = 0;
        assignedFemaleDco = 0;

        // ICTは全時間帯を一括コピーする為、個別の時間帯区分は指定しない
        Date assignedDate = (Date) parameter.get(B030514Const.ASSIGNED_DATE);
        String timeSlotType = (String) parameter.get(B030514Const.TIME_SLOT_TYPE);
        Integer missionKey = (Integer) parameter.get(B030514Const.MISSION_KEY);
        String dcpRoleType = (String) parameter.get(B030514Const.DCP_ROLE_TYPE);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Integer createdBy = B030514Const.BATCH_CREATED_BY;

        // DCPランク：A1 要員
        while (assignedS1S2S3A1 < requiredS1S2S3A1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.A1.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoOoct(missionKey, assignedDate, user.getUserId(), timeSlotType,
                    dcpRoleType, continuousDaysLimit))
                continue;

            // DCP選定情報（OOCT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectOoct(user.getUserId(),
                    assignedDate, timeSlotType, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3、A1アサイン数
            assignedS1S2S3A1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // DCPランク：S3 要員
        while (assignedS1S2S3A1 < requiredS1S2S3A1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S3.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoOoct(missionKey, assignedDate, user.getUserId(), timeSlotType,
                    dcpRoleType, continuousDaysLimit))
                continue;

            // DCP選定情報（OOCT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectOoct(user.getUserId(),
                    assignedDate, timeSlotType, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3、A1アサイン数
            assignedS1S2S3A1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // DCPランク：S2 要員
        while (assignedS1S2S3A1 < requiredS1S2S3A1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S2.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoOoct(missionKey, assignedDate, user.getUserId(), timeSlotType, dcpRoleType,
                    continuousDaysLimit))
                continue;

            // DCP選定情報（OOCT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectOoct(user.getUserId(), assignedDate,
                    timeSlotType, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3、A1アサイン数
            assignedS1S2S3A1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // DCPランク：S1要員
        while (assignedS1S2S3A1 < requiredS1S2S3A1) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.S1.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoOoct(missionKey, assignedDate, user.getUserId(), timeSlotType, dcpRoleType,
                    continuousDaysLimit))
                continue;

            // DCP選定情報（OOCT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectOoct(user.getUserId(), assignedDate,
                    timeSlotType, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：S1～S3、A1アサイン数
            assignedS1S2S3A1++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }

        // A1～S1で満たない場合はA2から追加で選出
        carryOver = requiredS1S2S3A1 - assignedS1S2S3A1;
        requiredA2 = requiredA2 + carryOver;
        // DCPランク：A2要員
        while (assignedA2 < requiredA2) {
            // ユーザを取得
            DcpSelectInfoDto user = getUserInfo(DcpRankType.A2.getCode(), candidateList);

            // ユーザが無くなった
            if (user == null)
                break;

            // ユーザチェックでfalseの場合は次のユーザ
            if (!this.gchkUserInfoOoct(missionKey, assignedDate, user.getUserId(), timeSlotType, dcpRoleType,
                    continuousDaysLimit))
                continue;

            // DCP選定情報（OOCT） --> DCP仮確定情報
            subExtractDcoRepository.insertDcpProvisionalFromSelectOoct(user.getUserId(), assignedDate,
                    timeSlotType, missionKey, dcpRoleType, createdAt, createdBy);

            // DCPランク：A2アサイン数
            assignedA2++;

            if (GenderType.MALE.getCode().equals(user.getGenderType())) {
                // 男性アサイン済人数
                assignedMaleDco++;
            } else if (GenderType.FEMALE.getCode().equals(user.getGenderType())) {
                // 女性アサイン済人数
                assignedFemaleDco++;
            }
        }
    }

    /**
     * ユーザ取得処理.<br>
     * 指定条件でユーザを取得する処理を記述する。
     * 
     * @param dcpRank DCPランク
     * @param userList ユーザ情報
     * @throws Exception 処理中に例外が発生した場合
     */
    private DcpSelectInfoDto getUserInfo(String dcpRank, List<DcpSelectInfoDto> userList) throws Exception {

        // 対象ランクのユーザを取り出す
        List<DcpSelectInfoDto> targetList = new ArrayList<>();
 
        if ((requiredMaleDco > assignedMaleDco) && (requiredFemaleDco > assignedFemaleDco)) {
            // 男女混合リスト
            targetList = userList.stream().filter(e -> dcpRank.equals(e.getDcpRank()))
                    .collect(Collectors.toList());
        } else if ((requiredMaleDco > assignedMaleDco) && (requiredFemaleDco <= assignedFemaleDco)) {
            // 男性リスト
            targetList = userList.stream().filter(e -> dcpRank.equals(e.getDcpRank()))
                    .filter(e -> GenderType.MALE.getCode().equals(e.getGenderType()))
                    .collect(Collectors.toList());
        } else if ((requiredMaleDco <= assignedMaleDco) && (requiredFemaleDco > assignedFemaleDco)) {
            // 女性リスト
            targetList = userList.stream().filter(e -> dcpRank.equals(e.getDcpRank()))
                    .filter(e -> GenderType.FEMALE.getCode().equals(e.getGenderType()))
                    .collect(Collectors.toList());
        }

        // 対象ランクのユーザが無かった場合
        if (targetList.isEmpty()) {
            return null;
        }

        // 返すユーザ
        int position = 0;
        DcpSelectInfoDto userInfo = null;

        if (ListLocationType.FIRST.getCode().equals(listLocationDco)) {
            // 最初
            position = 0;
            userInfo = targetList.get(position);
            // 仮確定情報に出力した内容は消去
            if (userInfo != null) {
                userList.remove(userInfo);
            }
// 一時的に中間は使用しない
//        } else if (ListLocationType.MIDDLE.getCode().equals(listLocationDco)) {
//            // 中間から取得
//            position = (int) Math.floor(targetList.size() / 2);
//            userInfo = targetList.get(position);
//            // 仮確定情報に出力した内容は消去
//            if (userInfo != null) {
//                userList.remove(userInfo);
//            }
        } else if (ListLocationType.LAST.getCode().equals(listLocationDco)) {
            // 最後
            position = targetList.size() > 0 ? targetList.size() - 1 : 0;
            userInfo = targetList.get(position);
            // 仮確定情報に出力した内容は消去
            if (userInfo != null) {
                userList.remove(userInfo);
            }
        } else {
            // 不正な値が設定されている
            throw new BatchRunTimeException(
                    messageSource.getMessage(B030514Const.ERROR_INTERNALVARIABLE_VALUE_INVALID,
                            new String[] {B030514Const.LIST_LOCATION_DCO, listLocationDco},
                            Locale.JAPANESE));
        }

        return userInfo;
    }

    /**
     * ICTユーザチェック処理.<br>
     * 以下の条件をすべて満たしているかチェックする。<br>
     * ・検査日に他ミッションにおいて仮確定されていない<br>
     * ・連続勤務日数が超過しない<br>
     * ・直前に別のミッションが割当されていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param userId ユーザID
     * @param dcpRoleType 役割区分
     * @param continuousDaysLimit 最大勤務可能日数
     * @throws Exception 処理中に例外が発生した場合
     */
    private boolean gchkUserInfoIct(Integer missionKey, Date testingDate, Integer userId,
            String dcpRoleType,int continuousDaysLimit, Integer requiredDays) throws Exception {

        // 検査日に他ミッションにおいて仮確定されていないか確認
        List<TDcpProvisional> provisionalList =
                b030514Repository.getIctProvisionalInfomation(userId, testingDate);

        if (!provisionalList.isEmpty())
            return false;

        // 連続勤務情報
        Integer maxContinuousDay =
                b030514Repository.findContinuousDutyInformation(userId, testingDate, continuousDaysLimit);

        if (maxContinuousDay >= continuousDaysLimit)
            return false;

        // 直前に別のミッションが割当されているか
        List<Mission> missionKeyList =
                b030514Repository.getConsecutiveMissionsIct(missionKey, userId, testingDate);

        if (!missionKeyList.isEmpty())
            return false;

        return true;
    }

    /**
     * OOCTユーザチェック処理.<br>
     * 以下の条件をすべて満たしているかチェックする。<br>
     * ・検査日に他ミッションにおいて仮確定されていない<br>
     * ・連続勤務日数が超過しない<br>
     * ・直前に別のミッションが割当されていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 検査日
     * @param userId ユーザID
     * @param timeSlotType 時間帯区分
     * @param dcpRoleType 役割区分
     * @param continuousDaysLimit 最大勤務可能日数
     * @throws Exception 処理中に例外が発生した場合
     */
    private boolean gchkUserInfoOoct(Integer missionKey, Date testingDate, Integer userId,
            String timeSlotType, String dcpRoleType,int continuousDaysLimit) throws Exception {

        // 検査日に他ミッションにおいて仮確定されていないか確認
        List<TDcpProvisional> provisionalList =
                b030514Repository.getOoctProvisionalInfomation(userId, testingDate, timeSlotType);

        if (!provisionalList.isEmpty())
            return false;

        // 連続勤務情報
        Integer maxContinuousDay =
                b030514Repository.findContinuousDutyInformation(userId, testingDate, continuousDaysLimit);

        if (maxContinuousDay >= continuousDaysLimit)
            return false;

        // 直前に別のミッションが割当されているか
        List<Mission> missionKeyList =
                b030514Repository.getConsecutiveMissionsOoct(missionKey, userId, testingDate, timeSlotType);

        if (!missionKeyList.isEmpty())
            return false;

        return true;
    }

    /**
     * 仮確定順番変更処理.<br>
     * ミッション切り替え時に取得位置を変更する。
     * 
     * @throws Exception 処理中に例外が発生した場合
     */
    private void updateListLocation() throws Exception {

       if (ListLocationType.FIRST.getCode().equals(listLocationDco)) {
            // 次回は最後から
            listLocationDco = ListLocationType.LAST.getCode();
        } else if (ListLocationType.LAST.getCode().equals(listLocationDco)) {
            // 次回は中間から
            listLocationDco = ListLocationType.FIRST.getCode();
// 一時的に中間は使用しない
//            listLocationDco = ListLocationType.MIDDLE.getCode();
//        } else if (ListLocationType.MIDDLE.getCode().equals(listLocationDco)) {
//            // 次回は最初から
//            listLocationDco = ListLocationType.FIRST.getCode();
        } else {
            // 不正な値が設定されている
            throw new BatchRunTimeException(
                    messageSource.getMessage(B030514Const.ERROR_INTERNALVARIABLE_VALUE_INVALID,
                            new String[] {B030514Const.LIST_LOCATION_DCO, listLocationDco},
                            Locale.JAPANESE));
        }
    }
}
