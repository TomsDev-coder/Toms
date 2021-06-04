//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : SubTaskMissionSortRepositoryImpl.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.co.seiko_sol.dto.MissionSortDto;

/**
 * ミッションソート（ICT系、OCT系）処理アクセス用のRepositoryクラス.<br>
 * 複数テーブルをJOINしてアクセスを行う場合に利用。
 * 
 * @author IIM
 * @version 1.0
 */
@Repository
public class SubMissionSortRepositoryImpl implements SubMissionSortRepository {

    /** JDBC設定 */
    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /** ミッションソート（ICT)取得SQL定義 */
    private static final String MISSION_SORT_ICT_QUERY =
            "SELECT "                                                                             +
            "    ROW_NUMBER() OVER(ORDER BY SUBQUERY) as sortId, "                                +
            "    SUBQUERY.mission_key as missionKey, "                                            +
            "    CURRENT_TIMESTAMP as createdAt, "                                                +
            "    110000 as createdBy "                                                            +
            "FROM "                                                                               +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            A.mission_key, "                                                         +
            "            A.testing_date_from, "                                                   +
            "            A.testing_days, "                                                        +
            "            C.language_priority, "                                                   +
            "            B.discipline_type "                                                      +
            "        FROM "                                                                       +
            "            t_mission_base A "                                                       +
            "            INNER JOIN m_discipline B ON "                                           +
            "                B.discipline_id = A.discipline_id AND "                              +
            "                B.is_deleted = false "                                               +
            "            INNER JOIN m_language_priority C ON "                                    +
            "                C.language_type = A.language_type "                                  +
            "            INNER JOIN m_association D ON "                                          +
            "                D.association_id = A.collect_samples_id AND "                        +
            "                D.association_name_short = 'JADA' AND "                              +
            "                D.is_deleted = false "                                               +
            "        WHERE "                                                                      +
            "            A.test_execute_type <> '1' AND "                                         +
            "            A.mission_status <> '11' AND "                                           +
            "            A.mission_status <> '25' AND "                                           +
            "            A.testing_type IN ( '11','12', '13' ) AND "                              +
            "            A.is_deleted = false AND "                                               +
            "            ( "                                                                      +
            "                A.samples_urine > 0 OR "                                             +
            "                ( A.samples_blood_y + A.samples_blood_p ) > 0 "                      +
            "            ) "                                                                      +
            "    ) SUBQUERY "                                                                     +
            "ORDER BY "                                                                           +
            "    SUBQUERY.testing_date_from, "                                                    +
            "    SUBQUERY.testing_days DESC, "                                                    +
            "    SUBQUERY.language_priority, "                                                    +
            "    SUBQUERY.discipline_type";

    /** ミッションソート（OOCT)取得SQL定義 */
    private static final String MISSION_SORT_OOCT_QUERY =
            "SELECT "                                                                             +
            "    ROW_NUMBER() OVER(ORDER BY SUBQUERY) as sortId, "                                +
            "    SUBQUERY.mission_key as missionKey, "                                            +
            "    CURRENT_TIMESTAMP as createdAt, "                                                +
            "    110000 as createdBy "                                                            +
            "FROM "                                                                               +
            "    ( "                                                                              +
            "        SELECT "                                                                     +
            "            A.mission_key, "                                                         +
            "            ( A.testing_date_to - A.testing_date_from + 1 ) as testing_days, "       +
            "            A.testing_date_from, "                                                   +
            "            A.language_type "                                                        +
            "        FROM "                                                                       +
            "            t_mission_base A "                                                       +
            "            INNER JOIN m_association B ON "                                          +
            "                B.association_id = A.collect_samples_id AND "                        +
            "                B.association_name_short = 'JADA' AND "                              +
            "                B.is_deleted = false "                                               +
            "        WHERE "                                                                      +
            "            A.test_execute_type <> '1' AND "                                         +
            "            A.mission_status <> '11' AND "                                           +
            "            A.mission_status <> '25' AND "                                           +
            "            A.testing_type = '21' AND "                                              +
            "            A.is_deleted = false AND "                                               +
            "            ( "                                                                      +
            "                A.samples_urine > 0 OR "                                             +
            "                ( A.samples_blood_y + A.samples_blood_p ) > 0 "                      +
            "            ) "                                                                      +
            "    ) SUBQUERY "                                                                     +
            "ORDER BY "                                                                           +
            "    SUBQUERY.testing_days, "                                                         +
            "    SUBQUERY.testing_date_from ";

    /** ミッション情報ソート結果（ICT）登録SQL定義 */
    private static final String INSERT_MISSION_SORT_ICT_QUERY =
            "INSERT INTO t_mission_sort_ict "                                                     +
            "("                                                                                   +
            "    sort_id, "                                                                       +
            "    mission_key, "                                                                   +
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            ") VALUES "                                                                           +
            "( "                                                                                  +
            "    nextval('t_mission_sort_ict_sort_id_seq'), "                                     +
            "    :missionKey, "                                                                   +
            "    :createdAt, "                                                                    +
            "    :createdBy "                                                                     +
            ") ";

    /** ミッション情報ソート結果（OOCT）登録SQL定義 */
    private static final String INSERT_MISSION_SORT_OOCT_QUERY =
            "INSERT INTO t_mission_sort_ooct "                                                    +
            "("                                                                                   +
            "    sort_id, "                                                                       +
            "    mission_key, "                                                                   +
            "    created_at, "                                                                    +
            "    created_by "                                                                     +
            ") VALUES "                                                                           +
            "( "                                                                                  +
            "    nextval('t_mission_sort_ooct_sort_id_seq'), "                                    +
            "    :missionKey, "                                                                   +
            "    :createdAt, "                                                                    +
            "    :createdBy "                                                                     +
            ") ";

    /**
     * ミッションソート（ICT)情報リスト取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、ミッションソート（ICT)情報リストを作成して返却する。
     * 
     * @return ミッションソート（ICT)情報リスト
     */
    public List<MissionSortDto> getTMissionSortIctList() {

        // レコードマップを生成
        RowMapper<MissionSortDto> mapper = new BeanPropertyRowMapper<>(MissionSortDto.class);

        // 結果返却
        return namedJdbcTemplate.query(MISSION_SORT_ICT_QUERY, mapper);
    }

    /**
     * ミッションソート（OOCT)情報リスト取得.<br>
     * ミッション基本情報、予定検体・分析数より予め決められた条件でデータを抽出し、ミッションソート（OOCT)情報リストを作成して返却する。
     * 
     * @return ミッションソート（OOCT)情報リスト
     */
    public List<MissionSortDto> getTMissionSortOoctList() {

        // レコードマップを生成
        RowMapper<MissionSortDto> mapper = new BeanPropertyRowMapper<>(MissionSortDto.class);

        // 結果返却
        return namedJdbcTemplate.query(MISSION_SORT_OOCT_QUERY, mapper);
    }

    /**
     * ミッション情報ソート結果（ICT）登録.<br>
     * ミッション情報ソート結果（ICT）登録を登録する。ソートIDは自動取得。<br>
     * 
     * @return 処理件数
     */
    public int insertTMissionSortIct(@Param("missionKey") Integer missionKey,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);

        // 結果返却
        return namedJdbcTemplate.update(INSERT_MISSION_SORT_ICT_QUERY, parameter);
    }

    /**
     * ミッション情報ソート結果（OOCT）登録.<br>
     * ミッション情報ソート結果（OOCT）登録を登録する。ソートIDは自動取得。<br>
     * 
     * @return 処理件数
     */
    public int insertTMissionSortOoct(@Param("missionKey") Integer missionKey,
            @Param("createdAt") Timestamp createdAt, @Param("createdBy") Integer createdBy) {

        // SQLパラメータ
        Map<String, Object> parameter = new HashMap<String, Object>();
        // パラメータ設定
        parameter.put("missionKey", missionKey);
        parameter.put("createdAt", createdAt);
        parameter.put("createdBy", createdBy);

        // 結果返却
        return namedJdbcTemplate.update(INSERT_MISSION_SORT_OOCT_QUERY, parameter);
    }
}
