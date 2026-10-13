package com.nynu.codelab.labmember.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nynu.codelab.labmember.common.exception.GlobalExceptionHandler;
import com.nynu.codelab.labmember.member.dto.AddMemberRequest;
import com.nynu.codelab.labmember.member.dto.MemberQueryRequest;
import com.nynu.codelab.labmember.member.dto.UpdateMemberRequest;
import com.nynu.codelab.labmember.member.exception.MemberNotFoundException;
import com.nynu.codelab.labmember.member.service.MemberService;
import com.nynu.codelab.labmember.member.vo.MemberIdVO;
import com.nynu.codelab.labmember.member.vo.MemberPageVO;
import com.nynu.codelab.labmember.member.vo.MemberVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class MemberControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(memberService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void addReturnsCreatedMemberId() throws Exception {
        when(memberService.add(any(AddMemberRequest.class))).thenReturn(new MemberIdVO(12L));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(12));

        verify(memberService).add(any(AddMemberRequest.class));
    }

    @Test
    void addReturnsBadRequestWhenValidationFails() throws Exception {
        AddMemberRequest request = addRequest();
        request.setName("");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数错误"));

        verifyNoInteractions(memberService);
    }

    @Test
    void getReturnsMemberDetails() throws Exception {
        when(memberService.get(2L)).thenReturn(memberVO(2L, "李四"));

        mockMvc.perform(get("/api/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("李四"));
    }

    @Test
    void getReturnsNotFoundWhenMemberDoesNotExist() throws Exception {
        when(memberService.get(99L)).thenThrow(new MemberNotFoundException(99L));

        mockMvc.perform(get("/api/members/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("成员不存在"));
    }

    @Test
    void pageReturnsPaginationResult() throws Exception {
        when(memberService.page(any(MemberQueryRequest.class)))
                .thenReturn(new MemberPageVO(List.of(memberVO(1L, "张三")), 1L, 1L, 10L));

        mockMvc.perform(get("/api/members")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "张")
                        .param("department", "SOFTWARE")
                        .param("position", "BACKEND")
                        .param("profileStatus", "COMPLETED")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].name").value("张三"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    void pageReturnsBadRequestForInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/members").param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数错误"));

        verifyNoInteractions(memberService);
    }

    @Test
    void updateReturnsUpdatedMember() throws Exception {
        when(memberService.update(any(Long.class), any(UpdateMemberRequest.class)))
                .thenReturn(memberVO(2L, "李四修改"));

        mockMvc.perform(put("/api/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("李四修改"));

        verify(memberService).update(any(Long.class), any(UpdateMemberRequest.class));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(memberService).delete(2L);
    }

    private AddMemberRequest addRequest() {
        AddMemberRequest request = new AddMemberRequest();
        request.setName("张三");
        request.setStudentNo("20260001");
        request.setGender(1);
        request.setGrade("2026");
        request.setMajor("软件工程");
        request.setPhone("13800138000");
        request.setEmail("zhangsan@example.com");
        request.setDepartment("SOFTWARE");
        request.setPosition("BACKEND");
        request.setProfileStatus("COMPLETED");
        request.setStatus(1);
        return request;
    }

    private UpdateMemberRequest updateRequest() {
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setName("李四");
        request.setStudentNo("20230001");
        request.setGender(2);
        request.setGrade("2023");
        request.setMajor("人工智能");
        request.setPhone("13800138001");
        request.setEmail("lisi@example.com");
        request.setDepartment("ACHIEVEMENT");
        request.setPosition("PRODUCT");
        request.setProfileStatus("COMPLETED");
        request.setStatus(1);
        return request;
    }

    private MemberVO memberVO(Long id, String name) {
        MemberVO vo = new MemberVO();
        vo.setId(id);
        vo.setName(name);
        vo.setStudentNo("20230001");
        vo.setGender(1);
        vo.setGrade("2023");
        vo.setMajor("软件工程");
        vo.setPhone("13800138001");
        vo.setEmail("member@example.com");
        vo.setDepartment("SOFTWARE");
        vo.setPosition("BACKEND");
        vo.setProfileStatus("COMPLETED");
        vo.setStatus(1);
        vo.setCreateTime(LocalDateTime.of(2026, 10, 13, 12, 0));
        vo.setUpdateTime(LocalDateTime.of(2026, 10, 13, 12, 0));
        return vo;
    }
}