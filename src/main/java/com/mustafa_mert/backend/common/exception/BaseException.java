package com.mustafa_mert.backend.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    // This class serves as the base exception for the application, encapsulating an ErrorMessage object that contains details about the error.

    private final ErrorMessage errorMessage;

    public BaseException(ErrorMessage errorMessage) {
        super(errorMessage.prepareErrorMessage());
        this.errorMessage = errorMessage;
    }
}
