package com.nynu.codelab.labmember.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;

    private final LabMemberMapper labMemberMapper;

    public MemberIdVO add(AddMemberRequest request) {
        normalizeOptionalFields(request);
        ensureStudentNoUnique(request.getStudentNo(), null);
        LabMember member = new LabMember();
        BeanUtils.copyProperties(request, member);
        labMemberMapper.insert(member);
        return new MemberIdVO(member.getId());
    }

    public MemberVO get(Long id) {
        return toVO(getEntity(id));
    }

    public MemberPageVO page(MemberQueryRequest request) {
        long pageNumber = request.getPage() == null ? DEFAULT_PAGE : Math.max(request.getPage(), 1);
        long pageSize = request.getPageSize() == null
                ? DEFAULT_PAGE_SIZE
                : Math.min(Math.max(request.getPageSize(), 1), MAX_PAGE_SIZE);

        LambdaQueryWrapper<LabMember> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.hasText(request.getName()), LabMember::getName, request.getName())
                .eq(StringUtils.hasText(request.getStudentNo()), LabMember::getStudentNo, request.getStudentNo())
                .eq(StringUtils.hasText(request.getGrade()), LabMember::getGrade, request.getGrade())
                .eq(StringUtils.hasText(request.getDepartment()), LabMember::getDepartment, request.getDepartment())
                .eq(StringUtils.hasText(request.getPosition()), LabMember::getPosition, request.getPosition())
                .eq(StringUtils.hasText(request.getProfileStatus()), LabMember::getProfileStatus,
                        request.getProfileStatus())
                .eq(request.getStatus() != null, LabMember::getStatus, request.getStatus())
                .orderByAsc(LabMember::getId);

        Page<LabMember> result = labMemberMapper.selectPage(new Page<>(pageNumber, pageSize), wrapper);
        List<MemberVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new MemberPageVO(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public MemberVO update(Long id, UpdateMemberRequest request) {
        LabMember member = getEntity(id);
        normalizeOptionalFields(request);
        ensureStudentNoUnique(request.getStudentNo(), id);
        BeanUtils.copyProperties(request, member, "id", "createTime", "updateTime");
        member.setUpdateTime(LocalDateTime.now());
        labMemberMapper.updateById(member);
        return toVO(getEntity(id));
    }

    public void delete(Long id) {
        getEntity(id);
        labMemberMapper.deleteById(id);
    }

    private void ensureStudentNoUnique(String studentNo, Long excludedId) {
        LambdaQueryWrapper<LabMember> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(LabMember::getStudentNo, studentNo)
                .ne(excludedId != null, LabMember::getId, excludedId);
        if (labMemberMapper.selectCount(wrapper) > 0) {
            throw new DuplicateStudentNoException();
        }
    }

    private LabMember getEntity(Long id) {
        LabMember member = labMemberMapper.selectById(id);
        if (member == null) {
            throw new MemberNotFoundException(id);
        }
        return member;
    }

    private void normalizeOptionalFields(AddMemberRequest request) {
        request.setPhone(normalizeOptional(request.getPhone()));
        request.setEmail(normalizeOptional(request.getEmail()));
    }

    private void normalizeOptionalFields(UpdateMemberRequest request) {
        request.setPhone(normalizeOptional(request.getPhone()));
        request.setEmail(normalizeOptional(request.getEmail()));
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private MemberVO toVO(LabMember member) {
        MemberVO vo = new MemberVO();
        BeanUtils.copyProperties(member, vo);
        return vo;
    }
}
