package com.nynu.codelab.labmember.common.exception;

import com.nynu.codelab.labmember.common.api.ApiResponse;
import com.nynu.codelab.labmember.member.controller.MemberController;
import com.nynu.codelab.labmember.member.exception.DuplicateStudentNoException;
import com.nynu.codelab.labmember.member.exception.MemberNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(basePackageClasses = MemberController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFound(MemberNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "成员不存在");
    }

    @ExceptionHandler({DuplicateStudentNoException.class, DuplicateKeyException.class})
    public ResponseEntity<ApiResponse<Void>> handleDuplicateStudentNo(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "学号已存在");
    }

    @ExceptionHandler({
            BindException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "参数错误");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected server error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "系统异常");
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), message));
    }
}