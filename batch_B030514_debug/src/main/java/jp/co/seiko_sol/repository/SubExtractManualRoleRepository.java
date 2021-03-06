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
import jp.co.seiko_sol.dto.ManualRoleUserDto;

/**
 * BCO選出処理アクセス用Repositoryインターフェースクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
public interface SubExtractManualRoleRepository {

    /**
     * 研修DCO選出対象ミッションリスト（ICT)取得.<br>
     * 研修DCO選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return 研修DCO選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getManualRoleTraineeMissionsIct();

    /**
     * 手動役割（その他）選出対象ミッションリスト（ICT)取得.<br>
     * 手動役割（その他）選出対象ミッションリスト（ICT)を作成して返却する。
     * 
     * @return 手動役割（その他）選出対象ミッションリスト（ICT)
     */
    public List<TMissionSortIct> getManualRoleOtherMissionsIct();

    /**
     * 研修DCO選出対象ミッションリスト（OOCT)取得.<br>
     * 研修DCO選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return 研修DCO選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getManualRoleTraineeMissionsOoct();

    /**
     * 手動役割（その他）選出対象ミッションリスト（OOCT)取得.<br>
     * 手動役割（その他）選出対象ミッションリスト（OOCT)を作成して返却する。
     * 
     * @return 手動役割（その他）選出対象ミッションリスト（OOCT)
     */
    public List<TMissionSortOoct> getManualRoleOtherMissionsOoct();

    /**
     * 手動役割参加可能者（終日）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に終日参加可能で登録がされている<br>
     *  DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsAllday(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * 手動役割参加可能者（早朝）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に早朝参加可能で登録がされている<br>
     * DCPMG#144  ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsEarly(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * 手動役割参加可能者（AM）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にAM参加可能で登録がされている<br>
     * DCPMG#144 ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsMorning(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * 手動役割参加可能者（PM）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日にPM参加可能で登録がされている<br>
     * DCPMG#144 ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsAfternoon(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);

    /**
     * 手動役割参加可能者（夜間）取得処理.<br>
     * 以下の条件で参加可能対象者を抽出し返却する。<br>
     * ①DCPカレンダー情報に当該日に夜間参加可能で登録がされている<br>
     * DCPMG#144 ②DCPカレンダー情報にの備考欄に入力が無い条件の削除<br>
     * ③DCP利害関係マスタに当該ユーザで登録がされていない<br>
     * 
     * @param missionKey ミッション基本情報KEY
     * @param assignedDate アサイン日
     * @return 手動役割用ユーザDTOリスト
     */
    public List<ManualRoleUserDto> getManualRoleParticipantsEvening(
            @Param("missionKey") Integer missionKey, @Param("assignedDate") Date assignedDate);
}
