//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubTaskExtractBco.java
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
import java.util.LinkedList;
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
import jp.co.seiko_sol.domain.MSystemDefaults;
import jp.co.seiko_sol.domain.TAssignHeader;
import jp.co.seiko_sol.domain.TDcpCalendar;
import jp.co.seiko_sol.domain.TDcpManualAssign;
import jp.co.seiko_sol.domain.TDcpProvisional;
import jp.co.seiko_sol.domain.TDcpSelectIct;
import jp.co.seiko_sol.domain.TDcpSelectOoct;
import jp.co.seiko_sol.domain.TMissionBase;
import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.domain.TTestingDate;
import jp.co.seiko_sol.dto.AvailableDaysDto;
import jp.co.seiko_sol.dto.Count;
import jp.co.seiko_sol.dto.Mission;
import jp.co.seiko_sol.dto.User;
import jp.co.seiko_sol.enumeration.DcpRoleType;
import jp.co.seiko_sol.enumeration.ListLocationType;
import jp.co.seiko_sol.enumeration.StrongCandidateType;
import jp.co.seiko_sol.enumeration.TestingType;
import jp.co.seiko_sol.enumeration.TimeSlotType;
import jp.co.seiko_sol.exception.BatchRunTimeException;
import jp.co.seiko_sol.exception.DataNotFoundException;
import jp.co.seiko_sol.repository.B030514Repository;
import jp.co.seiko_sol.repository.MSystemDefaultsRepository;
import jp.co.seiko_sol.repository.SubExtractBcoRepository;

/**
 * BCO選出処理クラス.<br>
 * BCOを条件によって選出する処理を記述する。
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class SubExtractBco {

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(SubExtractBco.class);

    /** 処理名 */
    private static final String PROC_NAME = "BCO選出処理";

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** システム設定マスタアクセス用Repository */
    @Autowired
    private MSystemDefaultsRepository tSystemDefaultsRepository;

    /** BCO選出処理アクセス用Repository */
    @Autowired
    private SubExtractBcoRepository subExtractBcoRepository;

    /** 自動アサインバッチ処理（ID：B030514）アクセスRepository */
    @Autowired
    private B030514Repository b030514Repository;

    /** 取得場所（"0"：最初、"1"：最後、"2"：中間） */
    private String listLocationBco;

    /**
     * BCO選出処理.<br>
     * BCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    public Set<Integer> process() throws Exception {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        Set<Integer> bcoMissions = new TreeSet<>();

        // 取得場所初期値を最初に設定
        listLocationBco = ListLocationType.FIRST.getCode();

        // ICT側の編集処理
        Set<Integer> ictMissions = editIctBcoProc();
        bcoMissions.addAll(ictMissions);

        // OOCT側の編集処理
        Set<Integer> ooctMissions = editOoctBcoProc();
        bcoMissions.addAll(ooctMissions);

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_END_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(endMessage);

        return bcoMissions;
    }

    /**
     * ICT側のBCO選出処理.<br>
     * BCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editIctBcoProc() throws Exception {

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
        // DCP閾値（BCO基準 ICT）
        int bcoBorderIct = tSystemDefaults.getBcoBorderIct();

        // DCP割当対象ミッションの抽出
        List<TMissionSortIct> tMissionSortIctList =
                subExtractBcoRepository.getBcoAssignmentMissionsIct();

        mission_loop: for (TMissionSortIct tMissionSortIct : tMissionSortIctList) {

            Integer missionKey = tMissionSortIct.getMissionKey();

            // DCPMG#161 delete 確定済であっても選出する

            // ミッション基本情報を取得
            TMissionBase tmpBase = b030514Repository.getMissionBaseByMissionkey(missionKey);
            Optional<TMissionBase> optTMissionBase = Optional.ofNullable(tmpBase);
            TMissionBase tMissionBase = optTMissionBase.orElseThrow(() -> new DataNotFoundException(
                    messageSource.getMessage(B030514Const.ERROR_MISSIOBASE_DATA_NOTFOUND,
                            new String[] {missionKey.toString()}, Locale.JAPANESE)));

            // 後方支援病院を利用する場合は管理者BCO/BCO共に対象外
            if( tMissionBase.getIsUsingSupportHospital() ) {
                // 次のミッションへ
                continue mission_loop;
            }

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

                // BCO人数確定の為、血液検体数を取得
                List<Count> bloodCountList =
                        subExtractBcoRepository.getBloodSampleCount(missionKey, testingDate);

                // 血液検体採取が無い場合、選出しない
                if (bloodCountList.isEmpty())
                    continue date_loop;

                Optional<Count> optBloodCountList = Optional.ofNullable(bloodCountList.get(0));
                Count tmpCount = optBloodCountList.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_BLOODCOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), testingDate.toString()},
                                Locale.JAPANESE)));

                int bloodCount = tmpCount.getCount();

                // 血液検体採取が無い場合、選出しない
                if (bloodCount == 0)
                    continue date_loop;

                // 日付、ミッション基本情報KEY、役割区分に該当するDCP選定情報（ICT）を削除
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.BCO_ADMIN.getCode());

                // 以下の条件で管理者BCO候補対象を絞る
                // ・DCPカレンダー情報に当該日に終日参加可能で登録がされている
                // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<User> selectBcoAdminList = subExtractBcoRepository
                        .getParticipantsBcoAdminListIct(missionKey, testingDate);

                // 必要管理者BCO人数
                int requiredBcoAdmin = 0;
                // 割当済管理者BCO人数
                int assignedBcoAdmin = 0;

                // 管理者BCO側の選出
                if (!TestingType.PRE_OOCT.getCode().equals(tMissionBase.getTestingType())) {

                    // Pre-OOCT以外の場合は選出
                    requiredBcoAdmin = 1;

                    // 候補者毎に追加で条件確認
                    admin_candidate_loop: for (User user : selectBcoAdminList) {

                        Integer userId = user.getUserId();

                        // DCPMG#154-2 add start 仮確定されていればスキップ
                        List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                                .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                                        missionKey, DcpRoleType.BCO_ADMIN.getCode());
                        if (0 < tempConfirmedDcpList.size()) {
                            continue admin_candidate_loop;
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
                                    continue admin_candidate_loop;
                                }
                            } else {
                                // 取得出来ない場合は宿泊出来ない扱い
                                continue admin_candidate_loop;
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
                                    continue admin_candidate_loop;
                                }
                            } else {
                                // 取得出来ない場合は宿泊出来ない扱い
                                continue admin_candidate_loop;
                            }
                        }

                        // すべての条件が問題無い場合はDCP選定情報（ICT）へ出力
                        Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                        // 時間帯区分:早朝
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.EARLY.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), Boolean.FALSE,
                                B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);
                        // 時間帯区分:AM
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.MORNING.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), Boolean.FALSE,
                                B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);
                        // 時間帯区分:PM
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.AFTERNOON.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), Boolean.FALSE,
                                B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);
                        // 時間帯区分:夜間
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.EVENING.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), Boolean.FALSE,
                                B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);

                        // ユーザ毎の処理ここまで
                        continue admin_candidate_loop;
                    }

                    // BCO管理者の有力候補対象を絞る
                    // ・用務地とDCP在住地との距離

                    // 管理者BCOをDCP選定情報（ICT）から取得する
                    List<User> candidateBcoAdminList = b030514Repository.getIctParticipation(
                            missionKey, testingDate, DcpRoleType.BCO_ADMIN.getCode());

                    // DCPMG#151 add start
                    // ミッション、日付、役割が同じ仮確定情報を取得
                    List<TDcpProvisional> checkListAdmin =
                            b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                    DcpRoleType.BCO_ADMIN.getCode());
                    // DCPMG#151 add end

                    // 候補者毎に処理
                    admin_strong_candidate_loop: for (User user : candidateBcoAdminList) {

                        Integer userId = user.getUserId();
                        // 有力候補条件合致点数
                        int condScore = 0;
                        // 用務地とDCP在住地との距離関係が適正
                        Boolean is_met_condition1 = Boolean.FALSE;
                        // DCPMG#144 start
                        // 該当ミッション日に備考の入力がない
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
                        // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
                        TDcpCalendar todayInfo =
                                b030514Repository.getTodayDcpCalendar(userId, testingDate);
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
                            provisional_loop: for(TDcpProvisional provisional : checkListAdmin) {
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
                                DcpRoleType.BCO_ADMIN.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);
                        b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                                TimeSlotType.MORNING.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);
                        b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                                TimeSlotType.AFTERNOON.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);
                        b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                                TimeSlotType.EVENING.getCode(), missionKey,
                                DcpRoleType.BCO_ADMIN.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);

                        // 候補者毎の処理ここまで
                        continue admin_strong_candidate_loop;
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
                    if (!checkListAdmin.isEmpty()) {
                        continue date_loop;
                    }

                    // 手動割当でミッション、日付、役割が同じ仮確定情報があれば仮確定しない
                    List<String>roleList = new ArrayList<String>();
                    roleList.add(DcpRoleType.BCO_ADMIN.getCode());
                    List<TDcpManualAssign> manualAssignList =
                            b030514Repository.getDcpManualProvisionalInfo(testingDate,
                                    TimeSlotType.EARLY.getCode(), missionKey, roleList);
                    if (!manualAssignList.isEmpty()) {
                        continue date_loop;
                    }

                    // DCP選定情報（ICT）から有力候補になっているユーザリストを取得する
                    List<User> isStrongCandidateList = null;
                    if (ListLocationType.FIRST.getCode().equals(listLocationBco)) {
                        // 昇順のリスト
                        isStrongCandidateList = b030514Repository.getBcoIctIsStrongCandidateAsc(
                                missionKey, testingDate, DcpRoleType.BCO_ADMIN.getCode());
                    } else if (ListLocationType.LAST.getCode().equals(listLocationBco)) {
                        // 降順のリスト
                        isStrongCandidateList = b030514Repository.getBcoIctIsStrongCandidateDesc(
                                missionKey, testingDate, DcpRoleType.BCO_ADMIN.getCode());
                    }

                    while (assignedBcoAdmin < requiredBcoAdmin) {

                        if (isStrongCandidateList.isEmpty())
                            break;

                        // 管理者BCOは1人をDCP仮確定情報に出力する
                        Integer userId = getAssignmentTarget(isStrongCandidateList);

                        // 取得出来ない場合は処理しない
                        if (userId != null) {

                            // 検査日に他ミッションにおいて仮確定されていないか確認
                            List<TDcpProvisional> provisionalList = b030514Repository
                                    .getIctProvisionalInfomation(userId, testingDate);

                            // 連続勤務情報
                            Integer maxContinuousDay = b030514Repository
                                    .findContinuousDutyInformation(userId, testingDate, continuousDaysLimit);

                            // 前日又は次の日に別のミッションが割当されているか
                            List<Mission> missionKeyList = b030514Repository
                                    .getConsecutiveMissionsIct(missionKey, userId, testingDate);

                            // 以下の条件が満たされる場合のみ仮確定
                            // ・検査日に他ミッションにおいて仮確定されていない
                            // ・連続勤務日数が超過しない
                            // ・前日又は次の日に別のミッションが割当されていない
                            // ・ミッション、日付、時間帯、役割が同じ仮確定情報が無い場合
                            if ((provisionalList.isEmpty())
                                    && (maxContinuousDay < continuousDaysLimit)
                                    && (missionKeyList.isEmpty())) {

                                // 必要人数に満たない場合のみ登録
                                if ((userId != null) && (requiredBcoAdmin > assignedBcoAdmin)) {

                                    // DCP選定情報（ICT）情報を取得
                                    List<TDcpSelectIct> tDcpSelectIctList =
                                            b030514Repository.selectDcpSelectIct(userId,
                                                    testingDate, TimeSlotType.EARLY.getCode(),
                                                    missionKey, DcpRoleType.BCO_ADMIN.getCode());
                                    if (tDcpSelectIctList.isEmpty()) {
                                        // 取得出来ない場合は例外
                                        throw new DataNotFoundException(messageSource.getMessage(
                                                B030514Const.ERROR_DCPSELECT_ICT_NOTFOUND,
                                                new String[] {userId.toString(),
                                                        testingDate.toString(),
                                                        TimeSlotType.EARLY.getCode(),
                                                        missionKey.toString(),
                                                        DcpRoleType.BCO_ADMIN.getCode()},
                                                Locale.JAPANESE));
                                    }

                                    TDcpSelectIct tDcpSelectIct = tDcpSelectIctList.get(0);
                                    Timestamp tDcpSelectIctTime =
                                            new Timestamp(System.currentTimeMillis());
                                    // DCP選定情報（ICT） --> DCP仮確定情報
                                    // 時間帯区分:早朝
                                    b030514Repository.inserteDcpProvisional(
                                            tDcpSelectIct.getUserId(),
                                            tDcpSelectIct.getAssignedDate(),
                                            TimeSlotType.EARLY.getCode(),
                                            tDcpSelectIct.getMissionKey(),
                                            tDcpSelectIct.getDcpRoleType(),
                                            tDcpSelectIct.getIsStrongCandidate(),
                                            tDcpSelectIct.getConditionsScore(),
                                            tDcpSelectIct.getAnyCondition1(),
                                            tDcpSelectIct.getIsMetCondition1(),
                                            tDcpSelectIct.getAnyCondition2(),
                                            tDcpSelectIct.getIsMetCondition2(),
                                            tDcpSelectIct.getAnyCondition3(),
                                            tDcpSelectIct.getIsMetCondition3(),
                                            tDcpSelectIct.getAnyCondition4(),
                                            tDcpSelectIct.getIsMetCondition4(),
                                            tDcpSelectIct.getAnyCondition5(),
                                            tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                            B030514Const.BATCH_CREATED_BY);
                                    // 時間帯区分:AM
                                    b030514Repository.inserteDcpProvisional(
                                            tDcpSelectIct.getUserId(),
                                            tDcpSelectIct.getAssignedDate(),
                                            TimeSlotType.MORNING.getCode(),
                                            tDcpSelectIct.getMissionKey(),
                                            tDcpSelectIct.getDcpRoleType(),
                                            tDcpSelectIct.getIsStrongCandidate(),
                                            tDcpSelectIct.getConditionsScore(),
                                            tDcpSelectIct.getAnyCondition1(),
                                            tDcpSelectIct.getIsMetCondition1(),
                                            tDcpSelectIct.getAnyCondition2(),
                                            tDcpSelectIct.getIsMetCondition2(),
                                            tDcpSelectIct.getAnyCondition3(),
                                            tDcpSelectIct.getIsMetCondition3(),
                                            tDcpSelectIct.getAnyCondition4(),
                                            tDcpSelectIct.getIsMetCondition4(),
                                            tDcpSelectIct.getAnyCondition5(),
                                            tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                            B030514Const.BATCH_CREATED_BY);
                                    // 時間帯区分:PM
                                    b030514Repository.inserteDcpProvisional(
                                            tDcpSelectIct.getUserId(),
                                            tDcpSelectIct.getAssignedDate(),
                                            TimeSlotType.AFTERNOON.getCode(),
                                            tDcpSelectIct.getMissionKey(),
                                            tDcpSelectIct.getDcpRoleType(),
                                            tDcpSelectIct.getIsStrongCandidate(),
                                            tDcpSelectIct.getConditionsScore(),
                                            tDcpSelectIct.getAnyCondition1(),
                                            tDcpSelectIct.getIsMetCondition1(),
                                            tDcpSelectIct.getAnyCondition2(),
                                            tDcpSelectIct.getIsMetCondition2(),
                                            tDcpSelectIct.getAnyCondition3(),
                                            tDcpSelectIct.getIsMetCondition3(),
                                            tDcpSelectIct.getAnyCondition4(),
                                            tDcpSelectIct.getIsMetCondition4(),
                                            tDcpSelectIct.getAnyCondition5(),
                                            tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                            B030514Const.BATCH_CREATED_BY);
                                    // 時間帯区分:夜間
                                    b030514Repository.inserteDcpProvisional(
                                            tDcpSelectIct.getUserId(),
                                            tDcpSelectIct.getAssignedDate(),
                                            TimeSlotType.EVENING.getCode(),
                                            tDcpSelectIct.getMissionKey(),
                                            tDcpSelectIct.getDcpRoleType(),
                                            tDcpSelectIct.getIsStrongCandidate(),
                                            tDcpSelectIct.getConditionsScore(),
                                            tDcpSelectIct.getAnyCondition1(),
                                            tDcpSelectIct.getIsMetCondition1(),
                                            tDcpSelectIct.getAnyCondition2(),
                                            tDcpSelectIct.getIsMetCondition2(),
                                            tDcpSelectIct.getAnyCondition3(),
                                            tDcpSelectIct.getIsMetCondition3(),
                                            tDcpSelectIct.getAnyCondition4(),
                                            tDcpSelectIct.getIsMetCondition4(),
                                            tDcpSelectIct.getAnyCondition5(),
                                            tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                            B030514Const.BATCH_CREATED_BY);

                                    assignedBcoAdmin++;
                                }
                            }
                        }
                    }
                }

                // BCO側の選出

                // 必要BCO人数
                int requiredBco = 0;
                // 割当済BCO人数
                int assignedBco = 0;

                if (TestingType.PRE_OOCT.getCode().equals(tMissionBase.getTestingType())) {
                    // PreOOCTの場合
                    requiredBco = bloodCount >= bcoBorderIct ? B030514Const.PERSON_TWO
                            : B030514Const.PERSON_ONE;
                } else {
                    // PreOOCT以外の場合
                    requiredBco = bloodCount >= bcoBorderIct ? B030514Const.PERSON_ONE
                            : B030514Const.PERSON_ZERO;
                }

                // 日付、ミッション基本情報KEY、役割区分に該当するDCP選定情報（ICT）を削除
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.BCO.getCode());

                if (requiredBco > B030514Const.PERSON_ZERO) {
                    // 選定を含め、1人以上の場合のみ処理

                    // 以下の条件でBCO候補対象を絞る
                    // ・DCPカレンダー情報に当該日に終日参加可能で登録がされている
                    // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除
                    // ・DCP利害関係マスタに当該ユーザで登録がされていない
                    List<User> bcoList = subExtractBcoRepository
                            .getParticipantsBcoListIct(missionKey, testingDate);

                    // 候補者毎に追加で条件確認
                    bco_candidate_loop: for (User user : bcoList) {

                        Integer userId = user.getUserId();

                        // DCPMG#154-2 add start 仮確定されていればスキップ
                        List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                                .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                                        missionKey, DcpRoleType.BCO.getCode());
                        if (0 < tempConfirmedDcpList.size()) {
                            continue bco_candidate_loop;
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
                                    continue bco_candidate_loop;
                                }
                            } else {
                                // 取得出来ない場合は宿泊出来ない扱い
                                continue bco_candidate_loop;
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
                                    continue bco_candidate_loop;
                                }
                            } else {
                                // 取得出来ない場合は宿泊出来ない扱い
                                continue bco_candidate_loop;
                            }
                        }

                        // すべての条件が問題無い場合はDCP選定情報（ICT）へ出力
                        Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                        // 時間帯区分:早朝
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.EARLY.getCode(), missionKey, DcpRoleType.BCO.getCode(),
                                Boolean.FALSE, B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);
                        // 時間帯区分:AM
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.MORNING.getCode(), missionKey,
                                DcpRoleType.BCO.getCode(), Boolean.FALSE,
                                B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);
                        // 時間帯区分:PM
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.AFTERNOON.getCode(), missionKey,
                                DcpRoleType.BCO.getCode(), Boolean.FALSE,
                                B030514Const.CONDITIONS_SCORE_ZERO,
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), Boolean.FALSE,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                                B030514Const.BATCH_UPDATED_BY, systemTime,
                                B030514Const.BATCH_CREATED_BY);
                        // 時間帯区分:夜間
                        b030514Repository.inserteDcpSelectIct(userId, testingDate,
                                TimeSlotType.EVENING.getCode(), missionKey,
                                DcpRoleType.BCO.getCode(), Boolean.FALSE,
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
                        continue bco_candidate_loop;
                    }

                    // BCOの有力候補対象を絞る
                    // ・用務地とDCP在住地との距離
                    // DCPMG#144 start
                    // ・検査日に備考の入力が無い
                    // DCPMG#144 end

                    // 取得した必要な参加日数以上参加可能なBCOをDCP選定情報（ICT）から取得する
                    List<User> strongCandidateBcoList = b030514Repository.getIctParticipation(
                            missionKey, testingDate, DcpRoleType.BCO.getCode());

                    // DCPMG#151 add start
                    // ミッション、日付、役割が同じ仮確定情報を取得
                    List<TDcpProvisional> checkListBco =
                            b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                    DcpRoleType.BCO.getCode());
                    // DCPMG#151 add end

                    // 有力候補者毎に処理
                    bco_strong_candidate_loop: for (User user : strongCandidateBcoList) {

                        Integer userId = user.getUserId();
                        // 有力候補条件合致点数
                        int condScore = 0;
                        // 用務地とDCP在住地との距離関係が適正
                        Boolean is_met_condition1 = Boolean.FALSE;
                        // DCPMG#144 start
                        // 検査日に備考の入力が無い
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
                        // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
                        TDcpCalendar todayInfo =
                                b030514Repository.getTodayDcpCalendar(userId, testingDate);
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
                                TimeSlotType.EARLY.getCode(), missionKey, DcpRoleType.BCO.getCode(),
                                isStrongCandidate, new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);
                        b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                                TimeSlotType.MORNING.getCode(), missionKey,
                                DcpRoleType.BCO.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);
                        b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                                TimeSlotType.AFTERNOON.getCode(), missionKey,
                                DcpRoleType.BCO.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);
                        b030514Repository.updateDcpIctStrongCandidate(userId, testingDate,
                                TimeSlotType.EVENING.getCode(), missionKey,
                                DcpRoleType.BCO.getCode(), isStrongCandidate,
                                new Integer(condScore),
                                StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                                // DCPMG#144 start
                                StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                                // DCPMG#144 end
                                B030514Const.NULL_STRING, Boolean.FALSE,
                                B030514Const.NULL_STRING, Boolean.FALSE);

                        // 有力候補者毎の処理ここまで
                        continue bco_strong_candidate_loop;
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
                    // DCO/BCOとBCO/SCOが仮確定されている場合もBCOの役割が仮確定済みとみなす
                    List<TDcpProvisional> listDcoBco =
                            b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                    DcpRoleType.DCO_BCO.getCode());
                    if (!listDcoBco.isEmpty()) {
                        continue date_loop;
                    }
                    List<TDcpProvisional> listBcoSco =
                            b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                    DcpRoleType.BCO_SCO.getCode());
                    if (!listBcoSco.isEmpty()) {
                        continue date_loop;
                    }
                    // DCPMG#154 add end

                    // 手動割当でミッション、日付、役割が同じ仮確定情報があれば仮確定しない
                    List<String> roleList = new ArrayList<String>();
                    roleList.add(DcpRoleType.BCO.getCode());
                    roleList.add(DcpRoleType.DCO_BCO.getCode());
                    roleList.add(DcpRoleType.BCO_SCO.getCode());
                    List<TDcpManualAssign> manualAssignList =
                            b030514Repository.getDcpManualProvisionalInfo(testingDate,
                                    TimeSlotType.EARLY.getCode(), missionKey, roleList);
                    if (!manualAssignList.isEmpty()) {
                        continue date_loop;
                    }

                    // DCP選定情報（ICT）から有力候補になっているユーザリストを取得する
                    List<User> isStrongCandidateList = null;
                    if (ListLocationType.FIRST.getCode().equals(listLocationBco)) {
                        // 昇順のリスト
                        isStrongCandidateList = b030514Repository.getBcoIctIsStrongCandidateAsc(
                                missionKey, testingDate, DcpRoleType.BCO.getCode());
                    } else if (ListLocationType.LAST.getCode().equals(listLocationBco)) {
                        // 降順のリスト
                        isStrongCandidateList = b030514Repository.getBcoIctIsStrongCandidateDesc(
                                missionKey, testingDate, DcpRoleType.BCO.getCode());
                    }

                    while (assignedBco < requiredBco) {

                        // BCOは0人～2人（条件による）をDCP仮確定情報に出力する
                        Integer userId = getAssignmentTarget(isStrongCandidateList);

                        // ユーザが無くなった
                        if (userId == null)
                            break;

                        // 検査日に他ミッションにおいて仮確定されていないか確認
                        List<TDcpProvisional> provisionalList =
                                b030514Repository.getIctProvisionalInfomation(userId, testingDate);

                        // 連続勤務情報
                        Integer maxContinuousDay = b030514Repository
                                .findContinuousDutyInformation(userId, testingDate, continuousDaysLimit);

                        // 前日又は次の日に別のミッションが割当されているか
                        List<Mission> missionKeyList = b030514Repository
                                .getConsecutiveMissionsIct(missionKey, userId, testingDate);

                        // 以下の条件が満たされる場合のみ仮確定
                        // ・検査日に他ミッションにおいて仮確定されていない
                        // ・参加可能日数が必要日数以上
                        // ・連続勤務日数が超過しない
                        // ・前日又は次の日に別のミッションが割当されていない
                        // ・ミッション、日付、時間帯、役割が同じ仮確定情報が無い場合
                        if ((provisionalList.isEmpty())
                                && (maxContinuousDay < continuousDaysLimit)
                                && (missionKeyList.isEmpty())) {

                            // DCP選定情報（ICT）情報を取得
                            List<TDcpSelectIct> tDcpSelectIctList =
                                    b030514Repository.selectDcpSelectIct(userId, testingDate,
                                            TimeSlotType.EARLY.getCode(), missionKey,
                                            DcpRoleType.BCO.getCode());
                            if (tDcpSelectIctList.isEmpty()) {
                                // 取得出来ない場合は例外
                                throw new DataNotFoundException(messageSource.getMessage(
                                        B030514Const.ERROR_DCPSELECT_ICT_NOTFOUND,
                                        new String[] {userId.toString(), testingDate.toString(),
                                                TimeSlotType.EARLY.getCode(),
                                                missionKey.toString(),
                                                DcpRoleType.BCO.getCode()},
                                        Locale.JAPANESE));
                            }

                            TDcpSelectIct tDcpSelectIct = tDcpSelectIctList.get(0);
                            Timestamp tDcpSelectIctTime =
                                    new Timestamp(System.currentTimeMillis());
                            // DCP選定情報（ICT） --> DCP仮確定情報
                            // 時間帯区分:早朝
                            b030514Repository.inserteDcpProvisional(tDcpSelectIct.getUserId(),
                                    tDcpSelectIct.getAssignedDate(),
                                    TimeSlotType.EARLY.getCode(), tDcpSelectIct.getMissionKey(),
                                    tDcpSelectIct.getDcpRoleType(),
                                    tDcpSelectIct.getIsStrongCandidate(),
                                    tDcpSelectIct.getConditionsScore(),
                                    tDcpSelectIct.getAnyCondition1(),
                                    tDcpSelectIct.getIsMetCondition1(),
                                    tDcpSelectIct.getAnyCondition2(),
                                    tDcpSelectIct.getIsMetCondition2(),
                                    tDcpSelectIct.getAnyCondition3(),
                                    tDcpSelectIct.getIsMetCondition3(),
                                    tDcpSelectIct.getAnyCondition4(),
                                    tDcpSelectIct.getIsMetCondition4(),
                                    tDcpSelectIct.getAnyCondition5(),
                                    tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                    B030514Const.BATCH_CREATED_BY);
                            // 時間帯区分:AM
                            b030514Repository.inserteDcpProvisional(tDcpSelectIct.getUserId(),
                                    tDcpSelectIct.getAssignedDate(),
                                    TimeSlotType.MORNING.getCode(),
                                    tDcpSelectIct.getMissionKey(),
                                    tDcpSelectIct.getDcpRoleType(),
                                    tDcpSelectIct.getIsStrongCandidate(),
                                    tDcpSelectIct.getConditionsScore(),
                                    tDcpSelectIct.getAnyCondition1(),
                                    tDcpSelectIct.getIsMetCondition1(),
                                    tDcpSelectIct.getAnyCondition2(),
                                    tDcpSelectIct.getIsMetCondition2(),
                                    tDcpSelectIct.getAnyCondition3(),
                                    tDcpSelectIct.getIsMetCondition3(),
                                    tDcpSelectIct.getAnyCondition4(),
                                    tDcpSelectIct.getIsMetCondition4(),
                                    tDcpSelectIct.getAnyCondition5(),
                                    tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                    B030514Const.BATCH_CREATED_BY);
                            // 時間帯区分:PM
                            b030514Repository.inserteDcpProvisional(tDcpSelectIct.getUserId(),
                                    tDcpSelectIct.getAssignedDate(),
                                    TimeSlotType.AFTERNOON.getCode(),
                                    tDcpSelectIct.getMissionKey(),
                                    tDcpSelectIct.getDcpRoleType(),
                                    tDcpSelectIct.getIsStrongCandidate(),
                                    tDcpSelectIct.getConditionsScore(),
                                    tDcpSelectIct.getAnyCondition1(),
                                    tDcpSelectIct.getIsMetCondition1(),
                                    tDcpSelectIct.getAnyCondition2(),
                                    tDcpSelectIct.getIsMetCondition2(),
                                    tDcpSelectIct.getAnyCondition3(),
                                    tDcpSelectIct.getIsMetCondition3(),
                                    tDcpSelectIct.getAnyCondition4(),
                                    tDcpSelectIct.getIsMetCondition4(),
                                    tDcpSelectIct.getAnyCondition5(),
                                    tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                    B030514Const.BATCH_CREATED_BY);
                            // 時間帯区分:夜間
                            b030514Repository.inserteDcpProvisional(tDcpSelectIct.getUserId(),
                                    tDcpSelectIct.getAssignedDate(),
                                    TimeSlotType.EVENING.getCode(),
                                    tDcpSelectIct.getMissionKey(),
                                    tDcpSelectIct.getDcpRoleType(),
                                    tDcpSelectIct.getIsStrongCandidate(),
                                    tDcpSelectIct.getConditionsScore(),
                                    tDcpSelectIct.getAnyCondition1(),
                                    tDcpSelectIct.getIsMetCondition1(),
                                    tDcpSelectIct.getAnyCondition2(),
                                    tDcpSelectIct.getIsMetCondition2(),
                                    tDcpSelectIct.getAnyCondition3(),
                                    tDcpSelectIct.getIsMetCondition3(),
                                    tDcpSelectIct.getAnyCondition4(),
                                    tDcpSelectIct.getIsMetCondition4(),
                                    tDcpSelectIct.getAnyCondition5(),
                                    tDcpSelectIct.getIsMetCondition5(), tDcpSelectIctTime,
                                    B030514Const.BATCH_CREATED_BY);

                            // BCOは0人～2人
                            assignedBco++;
                        }
                    }
                }

                // リードDCOの処理で作成したDCO割当情報を更新
                Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                b030514Repository.updateDcpAssignStatusBco(missionKey, testingDate,
                        requiredBcoAdmin, requiredBco, systemTime, B030514Const.BATCH_UPDATED_BY);

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
     * OOCT側のBCO選出処理.<br>
     * BCOを条件によって選出する処理を記述する。
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editOoctBcoProc() throws Exception {

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
        // DCP閾値（BCO基準 OOCT）
        int bcoBorderOoct = tSystemDefaults.getBcoBorderOoct();

        // DCP割当対象ミッションの抽出
        List<TMissionSortOoct> tMissionSortOoctList =
                subExtractBcoRepository.getBcoAssignmentMissionsOoct();

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

                // BCO人数確定の為、血液検体数を取得
                // OOCTは開始日のみに予定検体数がある
                List<Count> bloodCountList = subExtractBcoRepository.getBloodSampleCount(missionKey,
                        tMissionBase.getTestingDateFrom());

                // 血液検体採取が無い場合、選出しない
                if (bloodCountList.isEmpty())
                    continue date_loop;

                Optional<Count> optBloodCountList = Optional.ofNullable(bloodCountList.get(0));
                Count tmpCount = optBloodCountList.orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage(B030514Const.ERROR_BLOODCOUNT_DATA_NOTFOUND,
                                new String[] {missionKey.toString(), targetDate.toString()},
                                Locale.JAPANESE)));

                int bloodCount = tmpCount.getCount();

                // 血液検体採取が無い場合、選出しない
                if (bloodCount == 0)
                    continue date_loop;

                // 必要管理者BCO人数（OOCTは不要）
                int requiredBcoAdmin = 0;

                // BCO側の選出

                // 必要BCO人数
                int requiredBco = 0;
                // 割当済BCO人数
                int assignedBco = 0;

                requiredBco = bloodCount >= bcoBorderOoct ? B030514Const.PERSON_TWO
                        : B030514Const.PERSON_ONE;

                // 日付、時間帯区分、ミッション基本情報KEY、役割区分に該当するDCP選定情報（OOCT）を削除
                b030514Repository.deleteDcpSelectOoct(targetDate, timeSlotType, missionKey,
                        DcpRoleType.BCO.getCode());

                // 以下の条件でBCO候補対象を絞る
                // ・DCPカレンダー情報に当該日かつ該当時間帯で参加可能で登録がされている
                // DCPMG#144 　DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<User> bcoSelectList = new LinkedList<>();

                if (TimeSlotType.EARLY.getCode().equals(timeSlotType)) {

                    // 早朝の場合
                    bcoSelectList = subExtractBcoRepository
                            .getParticipantsBcoListEarlyOoct(missionKey, targetDate);

                } else if (TimeSlotType.MORNING.getCode().equals(timeSlotType)) {

                    // AMの場合
                    bcoSelectList = subExtractBcoRepository
                            .getParticipantsBcoListMorningOoct(missionKey, targetDate);

                } else if (TimeSlotType.AFTERNOON.getCode().equals(timeSlotType)) {

                    // PMの場合
                    bcoSelectList = subExtractBcoRepository
                            .getParticipantsBcoListAfternoonOoct(missionKey, targetDate);

                } else if (TimeSlotType.EVENING.getCode().equals(timeSlotType)) {

                    // 夜間の場合
                    bcoSelectList = subExtractBcoRepository
                            .getParticipantsBcoListEveningOoct(missionKey, targetDate);
                }

                // ユーザ毎に追加で条件確認
                bco_candidate_loop: for (User user : bcoSelectList) {

                    Integer userId = user.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                            .getDcpProvisionalInfo(userId, targetDate, timeSlotType,
                                    missionKey, DcpRoleType.BCO.getCode());
                    if (0 < tempConfirmedDcpList.size()) {
                        continue bco_candidate_loop;
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
                                continue bco_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue bco_candidate_loop;
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
                                continue bco_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue bco_candidate_loop;
                        }
                    }

                    // DCPMG#151 delete 検査日において既に仮確定しているかどうかの確認を削除

                    // 連続勤務情報
                    Integer maxContinuousDay = b030514Repository
                            .findContinuousDutyInformation(userId, targetDate, continuousDaysLimit);

                    if (maxContinuousDay >= continuousDaysLimit) {
                        // 連続アサイン可能日数を超える場合は割り当てない
                        continue bco_candidate_loop;
                    }

                    // すべての条件が問題無い場合はDCP選定情報（OOCT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    b030514Repository.inserteDcpSelectOoct(userId, targetDate, timeSlotType,
                            missionKey, DcpRoleType.BCO.getCode(), Boolean.FALSE,
                            B030514Const.CONDITIONS_SCORE_ZERO,
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING,
                            Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, Boolean.FALSE, systemTime,
                            B030514Const.BATCH_UPDATED_BY, systemTime,
                            B030514Const.BATCH_CREATED_BY);

                    // 候補者毎の処理ここまで
                    continue bco_candidate_loop;
                }

                // BCOの有力候補対象を絞る
                // 1.用務地とDCP在住地との距離
                // DCPMG#144 start
                // 2.対象日に備考の入力が無い
                // DCPMG#144 end

                // DCP選定情報（OOCT）からユーザリストを取得する
                List<User> bcoCandidateList = b030514Repository.getOoctParticipation(
                        missionKey, targetDate, timeSlotType, DcpRoleType.BCO.getCode());

                // DCPMG#151 add start
                // ミッション、日付、時間帯、役割が同じ仮確定情報を取得
                List<TDcpProvisional> checkListBco = b030514Repository.getOoctDcpProvisionalInfo(
                        targetDate, timeSlotType, missionKey, DcpRoleType.BCO.getCode());
                // DCPMG#151 add end

                // ユーザ毎に処理
                bco_strong_candidate_loop: for (User user : bcoCandidateList) {

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
                    // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
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
                            missionKey, DcpRoleType.BCO.getCode(), isStrongCandidate,
                            new Integer(condScore),
                            StrongCandidateType.DISTANCE_SUITABILITY.getValue(), is_met_condition1,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition2,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE);

                    // 有力候補者毎の処理ここまで
                    continue bco_strong_candidate_loop;
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
                // DCO/BCOとBCO/SCOが仮確定されている場合もBCOの役割が仮確定済みとみなす
                List<TDcpProvisional> listDcoBco =
                        b030514Repository.getOoctDcpProvisionalInfo(targetDate, timeSlotType,
                                missionKey, DcpRoleType.DCO_BCO.getCode());
                if (!listDcoBco.isEmpty()) {
                    continue date_loop;
                }
                List<TDcpProvisional> listBcoSco =
                        b030514Repository.getOoctDcpProvisionalInfo(targetDate, timeSlotType,
                                missionKey, DcpRoleType.BCO_SCO.getCode());
                if (!listBcoSco.isEmpty()) {
                    continue date_loop;
                }
                // DCPMG#154 add end

                // 手動割当でミッション、日付、役割が同じ仮確定情報があれば仮確定しない
                List<String> roleList = new ArrayList<String>();
                roleList.add(DcpRoleType.BCO.getCode());
                roleList.add(DcpRoleType.BCO_SCO.getCode());
                roleList.add(DcpRoleType.DCO_BCO.getCode());
                List<TDcpManualAssign> manualAssignList =
                        b030514Repository.getDcpManualProvisionalInfo(targetDate, timeSlotType,
                                missionKey, roleList);
                if (!manualAssignList.isEmpty()) {
                    continue date_loop;
                }

                // DCP選定情報（OOCT）から有力候補になっているユーザリストを取得する
                List<User> isStrongCandidateList = null;
                if (ListLocationType.FIRST.getCode().equals(listLocationBco)) {
                    // 昇順のリスト
                    isStrongCandidateList = b030514Repository.getBcoOoctIsStrongCandidateAsc(
                            missionKey, targetDate, timeSlotType, DcpRoleType.BCO.getCode());
                } else if (ListLocationType.LAST.getCode().equals(listLocationBco)) {
                    // 降順のリスト
                    isStrongCandidateList = b030514Repository.getBcoOoctIsStrongCandidateDesc(
                            missionKey, targetDate, timeSlotType, DcpRoleType.BCO.getCode());
                }

                while (assignedBco < requiredBco) {

                    // BCOは0人～2人（条件による）をDCP仮確定情報に出力する
                    Integer userId = getAssignmentTarget(isStrongCandidateList);

                    // ユーザが無くなった
                    if (userId == null)
                        break;

                    // 検査日に他ミッションにおいて仮確定されていないか確認
                    List<TDcpProvisional> provisionalList = b030514Repository
                            .getOoctProvisionalInfomation(userId, targetDate, timeSlotType);

                    // 連続勤務情報
                    Integer maxContinuousDay = b030514Repository
                            .findContinuousDutyInformation(userId, targetDate, continuousDaysLimit);

                    // 直前に別のミッションが割当されており、そのミッションがOOCTの場合は割り当てない
                    List<Mission> missionKeyList = b030514Repository.getConsecutiveMissionsOoct(
                            missionKey, userId, targetDate, timeSlotType);

                    // 以下の条件が満たされる場合のみ仮確定
                    // ・検査日に他ミッションにおいて仮確定されていない
                    // ・連続勤務日数が超過しない
                    // ・直前に別のミッションが割当されていない
                    // ・ミッション、日付、時間帯、役割が同じ仮確定情報が無い場合
                    if ((provisionalList.isEmpty())
                            && (maxContinuousDay < continuousDaysLimit)
                            && (missionKeyList.isEmpty())) {

                        // DCP選定情報（OOCT）情報を取得
                        List<TDcpSelectOoct> tDcpSelectOoctList = b030514Repository
                                .selectDcpSelectOoct(userId, targetDate,
                                        timeSlotType, missionKey, DcpRoleType.BCO.getCode());
                        if (tDcpSelectOoctList.isEmpty()) {
                            // 取得出来ない場合は例外
                            throw new DataNotFoundException(messageSource.getMessage(
                                    B030514Const.ERROR_DCPSELECT_OOCT_NOTFOUND,
                                    new String[] {userId.toString(),
                                            targetDate.toString(), timeSlotType,
                                            missionKey.toString(), DcpRoleType.BCO.getCode()},
                                    Locale.JAPANESE));
                        }

                        // DCP選定情報（OOCT） --> DCP仮確定情報
                        TDcpSelectOoct tDcpSelectOoct = tDcpSelectOoctList.get(0);
                        Timestamp tDcpSelectOoctTime =
                                new Timestamp(System.currentTimeMillis());
                        b030514Repository.inserteDcpProvisional(tDcpSelectOoct.getUserId(),
                                tDcpSelectOoct.getAssignedDate(), timeSlotType,
                                tDcpSelectOoct.getMissionKey(), tDcpSelectOoct.getDcpRoleType(),
                                tDcpSelectOoct.getIsStrongCandidate(),
                                tDcpSelectOoct.getConditionsScore(),
                                tDcpSelectOoct.getAnyCondition1(),
                                tDcpSelectOoct.getIsMetCondition1(),
                                tDcpSelectOoct.getAnyCondition2(),
                                tDcpSelectOoct.getIsMetCondition2(),
                                tDcpSelectOoct.getAnyCondition3(),
                                tDcpSelectOoct.getIsMetCondition3(),
                                tDcpSelectOoct.getAnyCondition4(),
                                tDcpSelectOoct.getIsMetCondition4(),
                                tDcpSelectOoct.getAnyCondition5(),
                                tDcpSelectOoct.getIsMetCondition5(), tDcpSelectOoctTime,
                                B030514Const.BATCH_CREATED_BY);

                        // BCOは0人～2人
                        assignedBco++;
                    }
                }

                // リードDCOの処理で作成したDCO割当情報を更新
                Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                b030514Repository.updateDcpAssignStatusBco(missionKey, targetDate,
                        requiredBcoAdmin, requiredBco, systemTime, B030514Const.BATCH_UPDATED_BY);

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

        // OOCT側の処理ここまで
        return targetMissions;
    }

    /**
     * 仮確定情報出力情報取得処理（BCO）.<br>
     * BCOを条件によって選出する処理を記述する。
     * 
     * @param candidatesList 候補リスト
     * @return 対象ユーザID
     * @throws Exception 処理中に例外が発生した場合
     */
    private Integer getAssignmentTarget(List<User> candidatesList)
            throws Exception {

        // 対象ユーザID
        Integer targetUser = null;

        // リストが空の場合、nullを返却
        if (candidatesList.isEmpty())
            return null;

        if (ListLocationType.FIRST.getCode().equals(listLocationBco)) {
            // 最初から取得
            int position = 0;
            User user = candidatesList.get(position);
            if (user != null) {
                targetUser = user.getUserId();
            }
            // 仮確定情報に出力した内容は消去
            candidatesList.remove(position);
        } else if (ListLocationType.LAST.getCode().equals(listLocationBco)) {
            // 最後から取得
            int position = candidatesList.size() - 1;
            User user = candidatesList.get(position);
            if (user != null) {
                targetUser = user.getUserId();
            }
            // 仮確定情報に出力した内容は消去
            candidatesList.remove(position);
// 一時的に中間は使用しない
//        } else if (ListLocationType.MIDDLE.getCode().equals(listLocationBco)) {
//            // 中間から取得
//            int position = (int) Math.floor(candidatesList.size() / 2);
//            User user = candidatesList.get(position);
//            if (user != null) {
//                targetUser = user.getUserId();
//            }
//            // 仮確定情報に出力した内容は消去
//            candidatesList.remove(position);
        } else {
            // 不正な値が設定されている
            throw new BatchRunTimeException(
                    messageSource.getMessage(B030514Const.ERROR_INTERNALVARIABLE_VALUE_INVALID,
                            new String[] {B030514Const.LIST_LOCATION_BCO, listLocationBco},
                            Locale.JAPANESE));
        }

        return targetUser;
    }

    /**
     * 仮確定順番変更処理.<br>
     * ミッション切り替え時に取得位置を変更する。
     * 
     * @throws Exception 処理中に例外が発生した場合
     */
    private void updateListLocation() throws Exception {

       if (ListLocationType.FIRST.getCode().equals(listLocationBco)) {
            // 次回は最後から
            listLocationBco = ListLocationType.LAST.getCode();
        } else if (ListLocationType.LAST.getCode().equals(listLocationBco)) {
// 一時的に中間は使用しない
//            // 次回は中間から
//            listLocationBco = ListLocationType.MIDDLE.getCode();
//        } else if (ListLocationType.MIDDLE.getCode().equals(listLocationBco)) {
            // 次回は最初から
            listLocationBco = ListLocationType.FIRST.getCode();
        } else {
            // 不正な値が設定されている
            throw new BatchRunTimeException(
                    messageSource.getMessage(B030514Const.ERROR_INTERNALVARIABLE_VALUE_INVALID,
                            new String[] {B030514Const.LIST_LOCATION_BCO, listLocationBco},
                            Locale.JAPANESE));
        }
    }
}
