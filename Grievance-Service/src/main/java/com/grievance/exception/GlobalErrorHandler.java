package com.grievance.exception;

import com.egov.common.exception.CommonErrorHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler extends CommonErrorHandler {
}
