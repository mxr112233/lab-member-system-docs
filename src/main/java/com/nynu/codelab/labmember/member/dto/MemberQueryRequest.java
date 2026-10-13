package com.nynu.codelab.labmember.member.dto;

import com.nynu.codelab.labmember.common.validation.EnumValue;
import com.nynu.codelab.labmember.member.constant.Department;
import com.nynu.codelab.labmember.member.constant.Position;
import com.nynu.codelab.labmember.member.constant.ProfileStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MemberQueryRequest {

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    private String name;

    private String studentNo;

    private String grade;

    @EnumValue(enumClass = Department.class, message = "部门不合法")
    private String department;

    @EnumValue(enumClass = Position.class, message = "职位不合法")
    private String position;

    @EnumValue(enumClass = ProfileStatus.class, message = "资料状态不合法")
    private String profileStatus;

    @Min(value = 0, message = "成员状态不合法")
    @Max(value = 1, message = "成员状态不合法")
    private Integer status;
}
