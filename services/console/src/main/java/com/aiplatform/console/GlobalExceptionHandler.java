package com.aiplatform.console;

import com.aiplatform.common.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleIllegalArgument(IllegalArgumentException e) {
        return R.fail(400, e.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        String msg = e.getMessage();
        if (msg.contains("duplicate key")) msg = "Record already exists";
        else if (msg.contains("violates not-null")) msg = "Required field is missing";
        else if (msg.length() > 200) msg = msg.substring(0, 200);
        return R.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleGeneral(Exception e) {
        log.error("Unhandled exception", e);
        return R.fail(500, "Internal server error: " + e.getMessage());
    }
}
