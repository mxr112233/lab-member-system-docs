package com.nynu.codelab.labmember.member.controller;

import com.nynu.codelab.labmember.common.api.ApiResponse;
import com.nynu.codelab.labmember.member.dto.AddMemberRequest;
import com.nynu.codelab.labmember.member.dto.MemberQueryRequest;
import com.nynu.codelab.labmember.member.dto.UpdateMemberRequest;
import com.nynu.codelab.labmember.member.service.MemberService;
import com.nynu.codelab.labmember.member.vo.MemberIdVO;
import com.nynu.codelab.labmember.member.vo.MemberPageVO;
import com.nynu.codelab.labmember.member.vo.MemberVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "成员管理", description = "实验室成员 CRUD 接口")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "新增成员")
    public ApiResponse<MemberIdVO> add(@Valid @RequestBody AddMemberRequest request) {
        return ApiResponse.success(memberService.add(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询成员详情")
    public ApiResponse<MemberVO> get(@PathVariable Long id) {
        return ApiResponse.success(memberService.get(id));
    }

    @GetMapping
    @Operation(summary = "分页条件查询成员")
    public ApiResponse<MemberPageVO> page(@Valid @ParameterObject MemberQueryRequest request) {
        return ApiResponse.success(memberService.page(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改成员")
    public ApiResponse<MemberVO> update(@PathVariable Long id, @Valid @RequestBody UpdateMemberRequest request) {
        return ApiResponse.success(memberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除成员")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
