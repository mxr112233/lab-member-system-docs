package com.nynu.codelab.labmember.member.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("成员不存在: " + id);
    }
}
