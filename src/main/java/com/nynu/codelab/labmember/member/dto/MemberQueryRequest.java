package com.nynu.codelab.labmember.member.dto;

import lombok.Data;

@Data
public class MemberQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String name;

    private String studentNo;

    private String grade;

    private String department;

    private String position;

    private String profileStatus;

    private Integer status;
}
