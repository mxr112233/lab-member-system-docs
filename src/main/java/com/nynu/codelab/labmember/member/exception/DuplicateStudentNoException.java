package com.nynu.codelab.labmember.member.exception;

public class DuplicateStudentNoException extends RuntimeException {

    public DuplicateStudentNoException() {
        super("学号已存在");
    }
}