package com.nynu.codelab.labmember.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("lab_member")
public class LabMember {

    @TableId(value = "id", type = IdType.AUTO)
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
