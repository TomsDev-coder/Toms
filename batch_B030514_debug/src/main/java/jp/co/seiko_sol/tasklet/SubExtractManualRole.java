//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubExtractManualRole.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.tasklet;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
import jp.co.seiko_sol.domain.MSystemDefaults;
import jp.co.seiko_sol.domain.TDcpCalendar;
import jp.co.seiko_sol.domain.TDcpProvisional;
import jp.co.seiko_sol.domain.TMissionBase;
import jp.co.seiko_sol.domain.TMissionSortIct;
import jp.co.seiko_sol.domain.TMissionSortOoct;
import jp.co.seiko_sol.domain.TTestingDate;
import jp.co.seiko_sol.dto.ManualRoleUserDto;
import jp.co.seiko_sol.enumeration.DcpRoleType;
import jp.co.seiko_sol.enumeration.StrongCandidateType;
import jp.co.seiko_sol.enumeration.TimeSlotType;
import jp.co.seiko_sol.exception.DataNotFoundException;
import jp.co.seiko_sol.repository.B030514Repository;
import jp.co.seiko_sol.repository.MSystemDefaultsRepository;
import jp.co.seiko_sol.repository.SubExtractManualRoleRepository;

/**
 * 手動役割選出処理クラス.<br>
 * 以下の役割を選出する処理を記述する。<br>
 * ・研修DCO<br>
 * ・指導DCO<br>
 * ・DCO/BCO<br>
 * ・BCO/SCO<br>
 * ・メンター<br>
 * ・SCO
 * 
 * @author IIM
 * @version 1.0
 */
@Component
public class SubExtractManualRole {

    /** ロガーを保持するインスタンス */
    private static final Logger log = LoggerFactory.getLogger(SubExtractManualRole.class);

    /** 処理名 */
    private static final String PROC_NAME = "手動役割選出処理";

    /** メッセージリソース */
    @Autowired
    private MessageSource messageSource;

    /** システム設定マスタアクセス用Repository */
    @Autowired
    private MSystemDefaultsRepository tSystemDefaultsRepository;

    /** 手動役割選出処理アクセス用Repository */
    @Autowired
    private SubExtractManualRoleRepository subExtractManualRoleRepository;

    /** 自動アサインバッチ処理（ID：B030514）アクセスRepository */
    @Autowired
    private B030514Repository b030514Repository;

    /**
     * 手動役割選出処理.<br>
     * 以下の手動役割を条件によって選出する処理を記述する。<br>
     * ・研修DCO<br>
     * ・指導DCO<br>
     * ・DCO/BCO<br>
     * ・BCO/SCO<br>
     * ・メンター<br>
     * ・SCO
     * 
     * @throws Exception 処理中に例外が発生した場合
     */
    public Set<Integer> process() throws Exception {

        // 開始メッセージをログ出力
        String startMessage = messageSource.getMessage(B030514Const.INFOMATION_START_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(startMessage);

        Set<Integer> manualMissions = new TreeSet<>();

        // ICT側の編集処理
        // 研修DCOのみ
        Set<Integer> ictTraineeMissions = editTraineeIctProc();
        manualMissions.addAll(ictTraineeMissions);
        // 指導DCO、DCO/BCO、BCO/SCO、メンター、SCO
        Set<Integer> ictOtherMissions = editOtherDcoIctProc();
        manualMissions.addAll(ictOtherMissions);

        // OOCT側の編集処理
        // 研修DCOのみ
        Set<Integer> ooctTraineeMissions = editTraineeOoctProc();
        manualMissions.addAll(ooctTraineeMissions);
        // 指導DCO、DCO/BCO、BCO/SCO、メンター、SCO
        Set<Integer> ooctOtherMissions = editOtherDcoOoctProc();
        manualMissions.addAll(ooctOtherMissions);

        // 終了メッセージをログ出力
        String endMessage = messageSource.getMessage(B030514Const.INFOMATION_START_GENERAL,
                new String[] {PROC_NAME}, Locale.JAPANESE);

        log.info(endMessage);

        return manualMissions;
    }

    /**
     * ICT側の手動役割(研修DCOのみ)選出処理.<br>
     * 以下の手動役割を条件によって選出する処理を記述する。<br>
     * ・研修DCO<br>
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editTraineeIctProc() throws Exception {

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

        // DCP割当対象ミッションの抽出
        List<TMissionSortIct> tMissionSortIctList =
                subExtractManualRoleRepository.getManualRoleTraineeMissionsIct();

        mission_loop: for (TMissionSortIct tMissionSortIct : tMissionSortIctList) {

            Integer missionKey = tMissionSortIct.getMissionKey();

            // DCPMG#161 delete 確定済であっても選出する

            // 検査日別情報を取得
            List<TTestingDate> tTestingDateList =
                    b030514Repository.getTestingDateByMissionKey(missionKey);

            date_loop: for (TTestingDate tTestingDate : tTestingDateList) {

                // 検査日毎に候補を選定
                Date testingDate = tTestingDate.getTestingDate();

                // 日付、ミッション基本情報KEY、役割区分に該当するDCP選定情報（ICT）を削除
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.DCO_TRAINEE.getCode());

                // 以下の条件で研修DCO候補対象を絞る
                // ・DCPカレンダー情報に当該日に終日参加可能で登録がされている
                // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<ManualRoleUserDto> dtoList = subExtractManualRoleRepository
                        .getManualRoleParticipantsAllday(missionKey, testingDate);

                // 対象DCP種別：研修DCOのユーザを取り出す
                List<ManualRoleUserDto> traineeDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDcoTrainee()))
                                .collect(Collectors.toList());

                // DCPMG#151 add start
                // ミッション、日付、役割が同じ仮確定情報を取得
                List<TDcpProvisional> provisionalList =
                        b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                                DcpRoleType.DCO_TRAINEE.getCode());
                // DCPMG#151 add end

                // 候補者毎に追加で条件確認
                trainee_candidate_loop: for (ManualRoleUserDto traineeDto : traineeDtoList) {

                    // ユーザID
                    Integer userId = traineeDto.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                            .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                                    missionKey, DcpRoleType.DCO_TRAINEE.getCode());
                    if (0 < tempConfirmedDcpList.size()) {
                        continue trainee_candidate_loop;
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
                                continue trainee_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue trainee_candidate_loop;
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
                                continue trainee_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue trainee_candidate_loop;
                        }
                    }
                    
                    // DCPMG#144 start
                    // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
                    Boolean is_met_condition1 = Boolean.FALSE;
                    TDcpCalendar todayInfo =
                            b030514Repository.getTodayDcpCalendar(userId, testingDate);
                    if ((todayInfo != null) && (todayInfo.getIsRemarksWritten() != null)) {
                        if (!todayInfo.getIsRemarksWritten()) {
                            // 備考入力が無い
                            is_met_condition1 = Boolean.TRUE;
                        }
                    }
                    // DCPMG#144 end

                    // DCPMG#151 add start
                    // 仮確定されている場合は強制的に有力候補になる
                    Boolean isStrongCandidate = Boolean.FALSE;
                    provisional_loop: for(TDcpProvisional provisional : provisionalList) {
                        if (provisional.getUserId().equals(userId)) {
                            isStrongCandidate = Boolean.TRUE;
                            break provisional_loop;
                        }
                    }
                    // DCPMG#151 add end

                    // すべての条件が問題無い場合はDCP選定情報（ICT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    // DCPMG#151 change start 有力候補フラグを設定する
                    // 時間帯区分:早朝
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.EARLY.getCode(), missionKey,
                            DcpRoleType.DCO_TRAINEE.getCode(), isStrongCandidate, 0,
                            // DCPMG#144 start 
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:AM
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.MORNING.getCode(), missionKey,
                            DcpRoleType.DCO_TRAINEE.getCode(), isStrongCandidate, 0,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:PM
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.AFTERNOON.getCode(), missionKey,
                            DcpRoleType.DCO_TRAINEE.getCode(), isStrongCandidate, 0,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // 時間帯区分:夜間
                    b030514Repository.inserteDcpSelectIct(userId, testingDate,
                            TimeSlotType.EVENING.getCode(), missionKey,
                            DcpRoleType.DCO_TRAINEE.getCode(), isStrongCandidate, 0,
                            // DCPMG#144 start
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);
                    // DCPMG#151 change end

                    // 候補者毎の処理ここまで
                    continue trainee_candidate_loop;
                }

                // 検査日毎の処理ここまで
                continue date_loop;
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
     * ICT側の手動役割（研修DCO以外）選出処理.<br>
     * 以下の手動役割を条件によって選出する処理を記述する。
     * ・指導DCO<br>
     * ・DCO/BCO<br>
     * ・BCO/SCO<br>
     * ・メンター<br>
     * ・SCO
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editOtherDcoIctProc() throws Exception {

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

        // DCP割当対象ミッションの抽出
        List<TMissionSortIct> tMissionSortIctList =
                subExtractManualRoleRepository.getManualRoleOtherMissionsIct();

        mission_loop: for (TMissionSortIct tMissionSortIct : tMissionSortIctList) {

            Integer missionKey = tMissionSortIct.getMissionKey();

            // DCPMG#161 delete 確定済であっても選出する

            // 検査日別情報を取得
            List<TTestingDate> tTestingDateList =
                    b030514Repository.getTestingDateByMissionKey(missionKey);

            date_loop: for (TTestingDate tTestingDate : tTestingDateList) {

                // 検査日毎に候補を選定
                Date testingDate = tTestingDate.getTestingDate();

                // 日付、ミッション基本情報KEY、役割区分に該当するDCP選定情報（ICT）を削除
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.DCO_INSTRUCT.getCode());
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.DCO_BCO.getCode());
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.BCO_SCO.getCode());
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.MENTOR.getCode());
                b030514Repository.deleteDcpSelectIct(testingDate, missionKey,
                        DcpRoleType.SCO.getCode());

                // 以下の条件で研修DCO候補対象を絞る
                // ・DCPカレンダー情報に当該日に終日参加可能で登録がされている
                // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<ManualRoleUserDto> dtoList = subExtractManualRoleRepository
                        .getManualRoleParticipantsAllday(missionKey, testingDate);

                // 指導DCO
                // 対象DCP種別：シニアDCOのユーザを取り出す
                List<ManualRoleUserDto> seniorDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDcoSenior()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoIct(seniorDtoList, missionKey, testingDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.DCO_INSTRUCT.getCode());

                // DCO/BCO
                // 対象DCP種別：シニアDCOかつBCOのユーザを取り出す
                List<ManualRoleUserDto> dcobcoDtoList1 =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDcoSenior()))
                                .filter(e -> Boolean.TRUE.equals(e.getIsBco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoIct(dcobcoDtoList1, missionKey, testingDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.DCO_BCO.getCode());
                // 対象DCP種別：DCOかつBCOのユーザを取り出す
                List<ManualRoleUserDto> dcobcoDtoList2 =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDco()))
                                .filter(e -> Boolean.TRUE.equals(e.getIsBco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoIct(dcobcoDtoList2, missionKey, testingDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.DCO_BCO.getCode());

                // BCO/SCO
                // 対象DCP種別：BCOかつSCOのユーザを取り出す
                List<ManualRoleUserDto> bcoscoDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsBco()))
                                .filter(e -> Boolean.TRUE.equals(e.getIsSco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoIct(bcoscoDtoList, missionKey, testingDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.BCO_SCO.getCode());

                // メンター
                // 対象DCP種別：メンターのユーザを取り出す
                List<ManualRoleUserDto> mentorDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsMentor()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoIct(mentorDtoList, missionKey, testingDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.MENTOR.getCode());

                // SCO
                // 対象DCP種別：SCOのユーザを取り出す
                List<ManualRoleUserDto> scoDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsSco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoIct(scoDtoList, missionKey, testingDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.SCO.getCode());

                // 検査日毎の処理ここまで
                continue date_loop;
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
     * 手動役割選出処理(ICT側).<br>
     * 選出する為の前後泊のチェックを行い、問題無ければDBに登録する。
     * 
     * @param userDtoList 手動役割用ユーザDTOリスト
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 指定日
     * @param isAbleStayBefore 前泊必要フラグ（true:必要、false：不要）
     * @param isAbleStayNight 後泊必要フラグ（true:必要、false：不要）
     * DCPMG#151 delete 指定日仮確定確認フラグを消去
     * @param dcpRoleType 役割区分
     * @throws Exception 処理中に例外が発生した場合
     */
    private void extractManualDcoIct(List<ManualRoleUserDto> userDtoList, Integer missionKey,
            Date testingDate, boolean isAbleStayBefore, boolean isAbleStayNight,
            String dcpRoleType) throws Exception {

        // DCPMG#151 add start
        // ミッション、日付、役割が同じ仮確定情報を取得
        List<TDcpProvisional> provisionalList =
                b030514Repository.getIctDcpProvisionalInfo(testingDate, missionKey,
                        dcpRoleType);
        // DCPMG#151 add end

        user_loop: for (ManualRoleUserDto userDto : userDtoList) {

            // ユーザID
            Integer userId = userDto.getUserId();

            // DCPMG#154-2 add start 仮確定されていればスキップ
            List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                    .getDcpProvisionalInfo(userId, testingDate, TimeSlotType.EARLY.getCode(),
                            missionKey, dcpRoleType);
            if (0 < tempConfirmedDcpList.size()) {
                continue user_loop;
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
                        continue user_loop;
                    }
                } else {
                    // 取得出来ない場合は宿泊出来ない扱い
                    continue user_loop;
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
                        continue user_loop;
                    }
                } else {
                    // 取得出来ない場合は宿泊出来ない扱い
                    continue user_loop;
                }
            }

            // DCPMG#151 delete 検査日に他ミッションにおいて仮確定されていないか確認する処理を削除

            // DCPMG#144 start
            // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
            Boolean is_met_condition1 = Boolean.FALSE;
            TDcpCalendar todayInfo =
                    b030514Repository.getTodayDcpCalendar(userId, testingDate);
            if ((todayInfo != null) && (todayInfo.getIsRemarksWritten() != null)) {
                if (!todayInfo.getIsRemarksWritten()) {
                    // 備考入力が無い
                    is_met_condition1 = Boolean.TRUE;
                }
            }
            // DCPMG#144 end

            // DCPMG#151 add start
            // 仮確定されている場合は強制的に有力候補になる
            Boolean isStrongCandidate = Boolean.FALSE;
            provisional_loop: for(TDcpProvisional provisional : provisionalList) {
                if (provisional.getUserId().equals(userId)) {
                    isStrongCandidate = Boolean.TRUE;
                    break provisional_loop;
                }
            }
            // DCPMG#151 add end

            // すべての条件が問題無い場合はDCP選定情報（ICT）へ出力
            Timestamp systemTime = new Timestamp(System.currentTimeMillis());
            // DCPMG#151 change start 有力候補フラグを設定する
            // 時間帯区分:早朝
            b030514Repository.inserteDcpSelectIct(userId, testingDate, TimeSlotType.EARLY.getCode(),
                    missionKey, dcpRoleType, isStrongCandidate, 0,
                    // DCPMG#144 start
                    StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                    // DCPMG#144 end
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY, systemTime,
                    B030514Const.BATCH_CREATED_BY);
            // 時間帯区分:AM
            b030514Repository.inserteDcpSelectIct(userId, testingDate,
                    TimeSlotType.MORNING.getCode(), missionKey, dcpRoleType, isStrongCandidate, 0,
                    // DCPMG#144 start
                    StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                    // DCPMG#144 end
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY, systemTime,
                    B030514Const.BATCH_CREATED_BY);
            // 時間帯区分:PM
            b030514Repository.inserteDcpSelectIct(userId, testingDate,
                    TimeSlotType.AFTERNOON.getCode(), missionKey, dcpRoleType, isStrongCandidate, 0,
                    // DCPMG#144 start
                    StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                    // DCPMG#144 end
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY, systemTime,
                    B030514Const.BATCH_CREATED_BY);
            // 時間帯区分:夜間
            b030514Repository.inserteDcpSelectIct(userId, testingDate,
                    TimeSlotType.EVENING.getCode(), missionKey, dcpRoleType, isStrongCandidate, 0,
                    // DCPMG#144 start
                    StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                    // DCPMG#144 end
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY, systemTime,
                    B030514Const.BATCH_CREATED_BY);
            // DCPMG#151 change end
        }
    }

    /**
     * OOCT側の手動役割(研修DCOのみ)選出処理.<br>
     * 以下の手動役割を条件によって選出する処理を記述する。<br>
     * ・研修DCO<br>
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editTraineeOoctProc() throws Exception {

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
        // DCP割当対象ミッションの抽出
        List<TMissionSortOoct> tMissionSortOoctList =
                subExtractManualRoleRepository.getManualRoleTraineeMissionsOoct();

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


                // 以下の条件でDCO候補対象を絞る
                // ・DCPカレンダー情報に当該日かつ該当時間帯で参加可能で登録がされている
                // DCPMG#144 DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<ManualRoleUserDto> dtoList = new LinkedList<>();

                if (TimeSlotType.EARLY.getCode().equals(timeSlotType)) {

                    // 早朝の場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsEarly(missionKey, targetDate);

                } else if (TimeSlotType.MORNING.getCode().equals(timeSlotType)) {

                    // AMの場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsMorning(missionKey, targetDate);

                } else if (TimeSlotType.AFTERNOON.getCode().equals(timeSlotType)) {

                    // PMの場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsAfternoon(missionKey, targetDate);

                } else if (TimeSlotType.EVENING.getCode().equals(timeSlotType)) {

                    // 夜間の場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsEvening(missionKey, targetDate);
                }

                // 対象DCP種別：研修DCOのユーザを取り出す
                List<ManualRoleUserDto> traineeDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDcoTrainee()))
                                .collect(Collectors.toList());

                // DCPMG#151 add start
                // ミッション、日付、役割が同じ仮確定情報を取得
                List<TDcpProvisional> provisionalList =
                        b030514Repository.getIctDcpProvisionalInfo(targetDate, missionKey,
                                DcpRoleType.DCO_TRAINEE.getCode());
                // DCPMG#151 add end

                // 候補者毎に追加で条件確認
                trainee_candidate_loop: for (ManualRoleUserDto traineeDto : traineeDtoList) {

                    // ユーザID
                    Integer userId = traineeDto.getUserId();

                    // DCPMG#154-2 add start 仮確定されていればスキップ
                    List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                            .getDcpProvisionalInfo(userId, targetDate, timeSlotType,
                                    missionKey, DcpRoleType.DCO_TRAINEE.getCode());
                    if (0 < tempConfirmedDcpList.size()) {
                        continue trainee_candidate_loop;
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
                                continue trainee_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue trainee_candidate_loop;
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
                                continue trainee_candidate_loop;
                            }
                        } else {
                            // 取得出来ない場合は宿泊出来ない扱い
                            continue trainee_candidate_loop;
                        }
                    }
                    
                    // DCPMG#144 start
                    // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
                    Boolean is_met_condition1 = Boolean.FALSE;
                    TDcpCalendar todayInfo =
                            b030514Repository.getTodayDcpCalendar(userId, targetDate);
                    if ((todayInfo != null) && (todayInfo.getIsRemarksWritten() != null)) {
                        if (!todayInfo.getIsRemarksWritten()) {
                            // 備考入力が無い
                            is_met_condition1 = Boolean.TRUE;
                        }
                    }
                    // DCPMG#144 end

                    // DCPMG#151 add start
                    // 仮確定されている場合は強制的に有力候補になる
                    Boolean isStrongCandidate = Boolean.FALSE;
                    provisional_loop: for(TDcpProvisional provisional : provisionalList) {
                        if (provisional.getUserId().equals(userId)) {
                            isStrongCandidate = Boolean.TRUE;
                            break provisional_loop;
                        }
                    }
                    // DCPMG#151 add end

                    // すべての条件が問題無い場合はDCP選定情報（OOCT）へ出力
                    Timestamp systemTime = new Timestamp(System.currentTimeMillis());
                    // DCPMG#151 change 有力候補フラグを設定する
                    b030514Repository.inserteDcpSelectOoct(userId, targetDate, timeSlotType,
                            missionKey, DcpRoleType.DCO_TRAINEE.getCode(), isStrongCandidate, 0,
                            // DCPMG#144 start 
                            StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                            // DCPMG#144 end
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                            Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY,
                            systemTime, B030514Const.BATCH_CREATED_BY);

                    // 候補者毎の処理ここまで
                    continue trainee_candidate_loop;
                }

                // 検査日毎の処理ここまで
                continue date_loop;
            }

            // 処理ミッション
            targetMissions.add(missionKey);

            // ミッション毎の処理ここまで
            continue mission_loop;
        }

        // OOCT側の処理ここまで
        return targetMissions;
    }

    /**
     * OOCT側の手動役割(研修DCO以外)選出処理.<br>
     * 以下の手動役割を条件によって選出する処理を記述する。
     * ・指導DCO<br>
     * ・DCO/BCO<br>
     * ・BCO/SCO<br>
     * ・メンター<br>
     * ・SCO
     * 
     * @return 処理されたミッション基本情報KEY
     * @throws Exception 処理中に例外が発生した場合
     */
    private Set<Integer> editOtherDcoOoctProc() throws Exception {

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

        // DCP割当対象ミッションの抽出
        List<TMissionSortOoct> tMissionSortOoctList =
                subExtractManualRoleRepository.getManualRoleOtherMissionsOoct();

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


                // 以下の条件でDCO候補対象を絞る
                // ・DCPカレンダー情報に当該日かつ該当時間帯で参加可能で登録がされている
                // DCPMG#144  DCPカレンダー情報にの備考欄に入力が無い条件の削除
                // ・DCP利害関係マスタに当該ユーザで登録がされていない
                List<ManualRoleUserDto> dtoList = new LinkedList<>();

                if (TimeSlotType.EARLY.getCode().equals(timeSlotType)) {

                    // 早朝の場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsEarly(missionKey, targetDate);

                } else if (TimeSlotType.MORNING.getCode().equals(timeSlotType)) {

                    // AMの場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsMorning(missionKey, targetDate);

                } else if (TimeSlotType.AFTERNOON.getCode().equals(timeSlotType)) {

                    // PMの場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsAfternoon(missionKey, targetDate);

                } else if (TimeSlotType.EVENING.getCode().equals(timeSlotType)) {

                    // 夜間の場合
                    dtoList = subExtractManualRoleRepository
                            .getManualRoleParticipantsEvening(missionKey, targetDate);
                }

                // 指導DCO
                // 対象DCP種別：シニアDCOのユーザを取り出す
                List<ManualRoleUserDto> seniorDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDcoSenior()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoOoct(seniorDtoList, missionKey, targetDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.DCO_INSTRUCT.getCode(), timeSlotType);

                // DCO/BCO
                // 対象DCP種別：シニアDCOかつBCOのユーザを取り出す
                List<ManualRoleUserDto> dcobcoDtoList1 =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDcoSenior()))
                                .filter(e -> Boolean.TRUE.equals(e.getIsBco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoOoct(dcobcoDtoList1, missionKey, targetDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.DCO_BCO.getCode(), timeSlotType);
                // 対象DCP種別：DCOかつBCOのユーザを取り出す
                List<ManualRoleUserDto> dcobcoDtoList2 =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsDco()))
                                .filter(e -> Boolean.TRUE.equals(e.getIsBco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoOoct(dcobcoDtoList2, missionKey, targetDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.DCO_BCO.getCode(), timeSlotType);

                // BCO/SCO
                // 対象DCP種別：BCOかつSCOのユーザを取り出す
                List<ManualRoleUserDto> bcoscoDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsBco()))
                                .filter(e -> Boolean.TRUE.equals(e.getIsSco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoOoct(bcoscoDtoList, missionKey, targetDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.BCO_SCO.getCode(), timeSlotType);

                // メンター
                // 対象DCP種別：メンターのユーザを取り出す
                List<ManualRoleUserDto> mentorDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsMentor()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoOoct(mentorDtoList, missionKey, targetDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.MENTOR.getCode(), timeSlotType);

                // SCO
                // 対象DCP種別：BCOかつSCOのユーザを取り出す
                List<ManualRoleUserDto> scoDtoList =
                        dtoList.stream().filter(e -> Boolean.TRUE.equals(e.getIsSco()))
                                .collect(Collectors.toList());
                // 条件確認及び登録
                // DCPMG#151 change 仮確定確認フラグを消去
                extractManualDcoOoct(scoDtoList, missionKey, targetDate, isAbleStayBefore,
                        isAbleStayNight, DcpRoleType.SCO.getCode(), timeSlotType);

                // 検査日毎の処理ここまで
                continue date_loop;
            }

            // 処理ミッション
            targetMissions.add(missionKey);

            // ミッション毎の処理ここまで
            continue mission_loop;
        }

        // OOCT側の処理ここまで
        return targetMissions;
    }

    /**
     * 手動役割選出処理(OOCT側).<br>
     * 選出する為の前後泊のチェックを行い、問題無ければDBに登録する。
     * 
     * @param userDtoList 手動役割用ユーザDTOリスト
     * @param missionKey ミッション基本情報KEY
     * @param testingDate 指定日
     * @param isAbleStayBefore 前泊必要フラグ（true:必要、false：不要）
     * @param isAbleStayNight 後泊必要フラグ（true:必要、false：不要）
     * DCPMG#151 delete 指定日仮確定確認フラグを消去
     * @param dcpRoleType 役割区分
     * @param timeSlotType 時間帯区分
     * @throws Exception 処理中に例外が発生した場合
     */
    private void extractManualDcoOoct(List<ManualRoleUserDto> userDtoList, Integer missionKey,
            Date testingDate, boolean isAbleStayBefore, boolean isAbleStayNight,
            String dcpRoleType,String timeSlotType) throws Exception {

        // DCPMG#151 add start
        // ミッション、日付、時間帯、役割が同じ仮確定情報を取得
        List<TDcpProvisional> provisionalList = b030514Repository.getOoctDcpProvisionalInfo(
                testingDate, timeSlotType, missionKey, dcpRoleType);
        // DCPMG#151 add end

        user_loop: for (ManualRoleUserDto userDto : userDtoList) {

            // ユーザID
            Integer userId = userDto.getUserId();

            // DCPMG#154-2 add start 仮確定されていればスキップ
            List<TDcpProvisional> tempConfirmedDcpList = b030514Repository
                    .getDcpProvisionalInfo(userId, testingDate, timeSlotType,
                            missionKey, dcpRoleType);
            if (0 < tempConfirmedDcpList.size()) {
                continue user_loop;
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
                        continue user_loop;
                    }
                } else {
                    // 取得出来ない場合は宿泊出来ない扱い
                    continue user_loop;
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
                        continue user_loop;
                    }
                } else {
                    // 取得出来ない場合は宿泊出来ない扱い
                    continue user_loop;
                }
            }

            // DCPMG#151 delete 検査日において既に仮確定しているかどうかの確認を削除

            // DCPMG#144 start
            // DCPカレンダー情報の検査日に備考入力が無い場合は条件をオンにする
            Boolean is_met_condition1 = Boolean.FALSE;
            TDcpCalendar todayInfo =
                    b030514Repository.getTodayDcpCalendar(userId, testingDate);
            if ((todayInfo != null) && (todayInfo.getIsRemarksWritten() != null)) {
                if (!todayInfo.getIsRemarksWritten()) {
                    // 備考入力が無い
                    is_met_condition1 = Boolean.TRUE;
                }
            }
            // DCPMG#144 end

            // DCPMG#151 add start
            // 仮確定されている場合は強制的に有力候補になる
            Boolean isStrongCandidate = Boolean.FALSE;
            provisional_loop: for(TDcpProvisional provisional : provisionalList) {
                if (provisional.getUserId().equals(userId)) {
                    isStrongCandidate = Boolean.TRUE;
                    break provisional_loop;
                }
            }
            // DCPMG#151 add end

            // すべての条件が問題無い場合はDCP選定情報（OOCT）へ出力
            Timestamp systemTime = new Timestamp(System.currentTimeMillis());
            // DCPMG#151 change 有力候補フラグを設定する
            b030514Repository.inserteDcpSelectOoct(userId, testingDate, timeSlotType,
                    missionKey, dcpRoleType, isStrongCandidate, 0,
                    // DCPMG#144 start 
                    StrongCandidateType.REMARKS_WRITTEN.getValue(), is_met_condition1,
                    // DCPMG#144 end
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    B030514Const.NULL_STRING, Boolean.FALSE, B030514Const.NULL_STRING, Boolean.FALSE,
                    Boolean.FALSE, systemTime, B030514Const.BATCH_UPDATED_BY, systemTime,
                    B030514Const.BATCH_CREATED_BY);
        }
    }
}
