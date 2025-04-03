package com.tripfriend.global.exception;

import com.tripfriend.global.dto.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
        MethodArgumentNotValidException 예외 처리
        -> 요청 데이터 검증 할 때, 유효성 검사 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        // 검증 실패한 필드의 오류 메시지를 정리하여 하나의 문자열 반환
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + " : " + fe.getCode() + " : "  + fe.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("\n"));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                .body(
                        new RsData<>(
                                "400-1",
                                message
                        )
                );
    }

    /*
        서비스 계층에서 발생하는 예외 처리
        예외 내부에서 정의된 HTTP 상태 코드와 코드 및 메시지를 포함한 응답 반환
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> ServiceExceptionHandle(ServiceException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(
                        new RsData<>(
                                ex.getCode(),
                                ex.getMsg()
                        )
                );
    }
}
