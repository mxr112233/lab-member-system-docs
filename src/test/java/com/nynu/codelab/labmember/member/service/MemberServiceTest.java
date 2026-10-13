package com.nynu.codelab.labmember.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nynu.codelab.labmember.member.dto.AddMemberRequest;
import com.nynu.codelab.labmember.member.dto.MemberQueryRequest;
import com.nynu.codelab.labmember.member.dto.UpdateMemberRequest;
import com.nynu.codelab.labmember.member.entity.LabMember;
import com.nynu.codelab.labmember.member.exception.DuplicateStudentNoException;
import com.nynu.codelab.labmember.member.exception.MemberNotFoundException;
import com.nynu.codelab.labmember.member.mapper.LabMemberMapper;
import com.nynu.codelab.labmember.member.vo.MemberIdVO;
import com.nynu.codelab.labmember.member.vo.MemberPageVO;
import com.nynu.codelab.labmember.member.vo.MemberVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private LabMemberMapper labMemberMapper;

    @InjectMocks
    private MemberService memberService;

    @Test
    void addCreatesMemberAndReturnsId() {
        AddMemberRequest request = addRequest();
        when(labMemberMapper.selectCount(any())).thenReturn(0L);
        when(labMemberMapper.insert(any(LabMember.class))).thenAnswer(invocation -> {
            LabMember member = invocation.getArgument(0);
            member.setId(101L);
            return 1;
        });

        MemberIdVO result = memberService.add(request);

        assertThat(result.getId()).isEqualTo(101L);
        ArgumentCaptor<LabMember> captor = ArgumentCaptor.forClass(LabMember.class);
        verify(labMemberMapper).insert(captor.capture());
        LabMember inserted = captor.getValue();
        assertThat(inserted.getStudentNo()).isEqualTo("20260001");
        assertThat(inserted.getDepartment()).isEqualTo("SOFTWARE");
        assertThat(inserted.getPosition()).isEqualTo("BACKEND");
        assertThat(inserted.getPhone()).isNull();
        assertThat(inserted.getEmail()).isNull();
    }

    @Test
    void addRejectsDuplicateStudentNo() {
        when(labMemberMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> memberService.add(addRequest()))
                .isInstanceOf(DuplicateStudentNoException.class)
                .hasMessage("学号已存在");

        verify(labMemberMapper, never()).insert(any(LabMember.class));
    }

    @Test
    void getRejectsMissingMember() {
        when(labMemberMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> memberService.get(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void pageUsesMapperPaginationAndCombinedConditions() {
        MemberQueryRequest request = new MemberQueryRequest();
        request.setPage(2);
        request.setPageSize(5);
        request.setName("张");
        request.setDepartment("SOFTWARE");
        request.setPosition("BACKEND");
        request.setProfileStatus("COMPLETED");
        request.setStatus(1);

        Page<LabMember> mapperPage = new Page<>(2L, 5L, 11L);
        mapperPage.setRecords(List.of(member(1L, "20230001")));
        when(labMemberMapper.selectPage(any(Page.class), any())).thenReturn(mapperPage);

        MemberPageVO result = memberService.page(request);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(11L);
        assertThat(result.getPage()).isEqualTo(2L);
        assertThat(result.getPageSize()).isEqualTo(5L);
        verify(labMemberMapper).selectPage(any(Page.class), any());
    }

    @Test
    void updateRefreshesUpdateTimeAndReturnsPersistedMember() {
        LabMember existing = member(1L, "20230001");
        LocalDateTime oldUpdateTime = LocalDateTime.of(2026, 10, 13, 12, 0);
        existing.setUpdateTime(oldUpdateTime);
        when(labMemberMapper.selectById(1L)).thenReturn(existing);
        when(labMemberMapper.selectCount(any())).thenReturn(0L);

        UpdateMemberRequest request = updateRequest();
        MemberVO result = memberService.update(1L, request);

        assertThat(result.getName()).isEqualTo("李四");
        assertThat(result.getPhone()).isEqualTo("13800138000");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUpdateTime()).isAfter(oldUpdateTime);
        verify(labMemberMapper).updateById(existing);
    }

    @Test
    void updateRejectsMissingMember() {
        when(labMemberMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> memberService.update(99L, updateRequest()))
                .isInstanceOf(MemberNotFoundException.class);

        verify(labMemberMapper, never()).updateById(any(LabMember.class));
    }

    @Test
    void deleteRemovesExistingMember() {
        when(labMemberMapper.selectById(1L)).thenReturn(member(1L, "20230001"));

        memberService.delete(1L);

        verify(labMemberMapper).deleteById(1L);
    }

    @Test
    void deleteRejectsMissingMember() {
        when(labMemberMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> memberService.delete(99L))
                .isInstanceOf(MemberNotFoundException.class);

        verify(labMemberMapper, never()).deleteById(any(Long.class));
    }

    private AddMemberRequest addRequest() {
        AddMemberRequest request = new AddMemberRequest();
        request.setName("张三");
        request.setStudentNo("20260001");
        request.setGender(1);
        request.setGrade("2026");
        request.setMajor("软件工程");
        request.setPhone(" ");
        request.setEmail("");
        request.setDepartment("SOFTWARE");
        request.setPosition("BACKEND");
        request.setProfileStatus("PENDING");
        request.setStatus(1);
        return request;
    }

    private UpdateMemberRequest updateRequest() {
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setName("李四");
        request.setStudentNo("20230001");
        request.setGender(1);
        request.setGrade("2023");
        request.setMajor("软件工程");
        request.setPhone(" 13800138000 ");
        request.setEmail("");
        request.setDepartment("SOFTWARE");
        request.setPosition("BACKEND");
        request.setProfileStatus("COMPLETED");
        request.setStatus(1);
        return request;
    }

    private LabMember member(Long id, String studentNo) {
        LabMember member = new LabMember();
        member.setId(id);
        member.setName("张三");
        member.setStudentNo(studentNo);
        member.setGender(1);
        member.setGrade("2023");
        member.setMajor("软件工程");
        member.setDepartment("SOFTWARE");
        member.setPosition("BACKEND");
        member.setProfileStatus("COMPLETED");
        member.setStatus(1);
        member.setCreateTime(LocalDateTime.of(2026, 10, 13, 11, 0));
        member.setUpdateTime(LocalDateTime.of(2026, 10, 13, 11, 0));
        return member;
    }
}