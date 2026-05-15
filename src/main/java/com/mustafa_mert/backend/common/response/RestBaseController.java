package com.mustafa_mert.backend.common.response;

import org.springframework.http.ResponseEntity;

public abstract class RestBaseController {

    // Helper method to create a successful response with a payload wrapped in RootEntity

    protected <T> ResponseEntity<RootEntity<T>> ok(T payload) {
        return ResponseEntity.ok(RootEntity.ok(payload));
    }
}
