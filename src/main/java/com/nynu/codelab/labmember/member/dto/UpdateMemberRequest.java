package com.nynu.codelab.labmember.member.dto;

import com.nynu.codelab.labmember.common.validation.EnumValue;
import com.nynu.codelab.labmember.member.constant.Department;
import com.nynu.codelab.labmember.member.constant.Position;
import com.nynu.codelab.labmember.member.constant.ProfileStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMemberRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @NotBlank(message = "学号不能为空")
    @Size(max = 30, message = "学号长度不能超过30")
    private String studentNo;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别不合法")
    @Max(value = 2, message = "性别不合法")
    private Integer gender;

    @NotBlank(message = "年级不能为空")
    @Size(max = 20, message = "年级长度不能超过20")
    private String grade;

    @NotBlank(message = "专业不能为空")
    @Size(max = 100, message = "专业长度不能超过100")
    private String major;

    @Size(max = 20, message = "手机号长度不能超过20")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Size(max = 100, message = "邮箱长度不能超过100")
    @Email(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "邮箱格式错误")
    private String email;

    @NotBlank(message = "部门不能为空")
    @EnumValue(enumClass = Department.class, message = "部门不合法")
    private String department;

    @NotBlank(message = "职位不能为空")
    @EnumValue(enumClass = Position.class, message = "职位不合法")
    private String position;

    @NotBlank(message = "资料状态不能为空")
    @EnumValue(enumClass = ProfileStatus.class, message = "资料状态不合法")
    private String profileStatus;

    @NotNull(message = "成员状态不能为空")
    @Min(value = 0, message = "成员状态不合法")
    @Max(value = 1, message = "成员状态不合法")
    private Integer status;
}
