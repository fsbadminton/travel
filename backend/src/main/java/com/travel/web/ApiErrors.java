package com.travel.web;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException error) {
    return ResponseEntity.status(error.getStatusCode()).body(Map.of("message", error.getReason() == null ? "请求失败" : error.getReason()));
  }
  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ResponseEntity<?> invalid(Exception error) { return ResponseEntity.badRequest().body(Map.of("message", "输入格式无效，请检查必填字段、用户名和密码长度")); }
  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(DataIntegrityViolationException error) { return ResponseEntity.status(409).body(Map.of("message", "数据冲突，请检查用户名或关联内容")); }
}
