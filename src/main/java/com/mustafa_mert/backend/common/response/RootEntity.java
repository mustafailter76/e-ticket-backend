package com.mustafa_mert.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RootEntity<T> {

    // Indicates whether the operation was successful or not

    private boolean success;
    private Integer status;
    private T payload;
    private String errorMessage;
    private Map<String, List<String>> errors;
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> RootEntity<T> ok(T payload) {
        RootEntity<T> rootEntity = new RootEntity<>();
        rootEntity.setSuccess(true);
        rootEntity.setStatus(200);
        rootEntity.setPayload(payload);
        return rootEntity;
    }

    public static <T> RootEntity<T> ok() {
        RootEntity<T> rootEntity = new RootEntity<>();
        rootEntity.setSuccess(true);
        rootEntity.setStatus(200);
        return rootEntity;
    }

    public static <T> RootEntity<T> error(String errorMessage, int status) {
        RootEntity<T> rootEntity = new RootEntity<>();
        rootEntity.setSuccess(false);
        rootEntity.setStatus(status);
        rootEntity.setErrorMessage(errorMessage);
        return rootEntity;
    }

    public static <T> RootEntity<T> validationError(Map<String, List<String>> errors) {
        RootEntity<T> rootEntity = new RootEntity<>();
        rootEntity.setSuccess(false);
        rootEntity.setStatus(400);
        rootEntity.setErrors(errors);
        return rootEntity;
    }
}

