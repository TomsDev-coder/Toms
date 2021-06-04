//////////////////////////////////////////////////////////////////////////
// Project Name : TOMS
// File Name : DcpSelectInfoDto.java
// Encoding : UTF-8
// Creation Date : 2020-01-09
//
// Copyright © 2020 JADA. All rights reserved.
//////////////////////////////////////////////////////////////////////////
package jp.co.seiko_sol.dto;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 自動アサインバッチ処理（ID：B030514）用のDCP選定情報用DTOクラス（ICT/OOCT兼用）.<br>
 * DCO仮確定候補のユーザID、性別、DCPランクを保持する。
 * 
 * @author IIM
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DcpSelectInfoDto {

    /** ユーザID */
    @Column(name = "user_id")
    private Integer userId;

    /** 性別 */
    @Column(name = "gender_type")
    private String genderType;

    /** DCPランク */
    @Column(name = "dcp_rank")
    private String dcpRank;
}
