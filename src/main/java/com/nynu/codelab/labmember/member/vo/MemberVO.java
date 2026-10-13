package com.nynu.codelab.labmember.member.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MemberVO {

    private Long id;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
