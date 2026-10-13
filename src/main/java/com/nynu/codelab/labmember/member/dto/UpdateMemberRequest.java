package com.nynu.codelab.labmember.member.dto;

import lombok.Data;

@Data
public class UpdateMemberRequest {

    private String name;

    private String studentNo;

    private Integer gender;

    private String grade;

    private String major;

    private String phone;

    private String email;

    private String department;

    private String position;

    private String profileStatus;

    private Integer status;
}
