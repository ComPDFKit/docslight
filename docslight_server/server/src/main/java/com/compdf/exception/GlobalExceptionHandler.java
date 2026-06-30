package com.compdf.exception;

import com.compdf.config.base.R;
import com.compdf.enums.ErrorInfoEnum;
import com.compdfkit.auth.LicenseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wph 2022/7/5
 */
@RestControllerAdvice(basePackages = "com.compdf")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public R<Void> handlerThrowable(Throwable t) {
        log.info("ErrorClass: {}", t.getClass());
        log.error(t.getMessage(), t);
        return R.error();
    }

    @ExceptionHandler(ComPDFKitException.class)
    public R<Void> handlerThrowable(ComPDFKitException e) {
        log.error(e.getMessage(), e);
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(LicenseAuthException.class)
    public R<Void> handlerThrowable(LicenseAuthException e) {
        log.error(e.getMessage(), e);
        return R.error(e.getCode(), e.getMessage());
    }


    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public R<Map<String, String>> handlerBindException(Exception e) {
        log.error("parameter verification failed", e);
        BindingResult bindingResult = e instanceof BindException ? ((BindException) e).getBindingResult() : ((MethodArgumentNotValidException) e).getBindingResult();
        Map<String, String> errorData = bindingResult.getFieldErrors().stream()
                .filter(f -> f.getDefaultMessage() != null)
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (k1, k2) -> k1));
        return R.error(ErrorInfoEnum.PARAM_VALIDATE_ERROR, errorData);
    }
}
