package com.mustafa_mert.backend.common.exception;

import lombok.Getter;

@Getter
public enum MessageType {

    // Define various message types with unique codes, messages, and HTTP status codes

    NOT_FOUND("1001", "Resource not found", 404),
    BAD_REQUEST("1002", "Bad request", 400),
    INTERNAL_SERVER_ERROR("1003", "Internal server error", 500),
    UNAUTHORIZED("1004", "Unauthorized access", 401),
    FORBIDDEN("1005", "Forbidden access", 403),
    EMAIL_ALREADY_EXIST("1006", "Email already exists", 400),
    EMAIL_NOT_FOUND("1007", "Email not found", 404),
    INVALID_CREDENTIALS("1008", "Invalid credentials", 401),
    PASSWORD_NOT_MATCH("1009", "Password does not match", 400),
    USER_NOT_FOUND("1010", "User not found", 404),
    INVALID_CURRENT_PASSWORD("1011", "Invalid current password", 400),
    ONLY_FOR_USER("1012", "This action is only allowed for users", 403),
    ONLY_FOR_ADMIN("1013", "This action is only allowed for admins", 403),
    EVENT_NOT_FOUND("1014", "Event not found", 404),
    CANNOT_DELETE_EVENT_WITH_PURCHASES("1015", "Cannot delete event with existing purchases", 400),
    NOT_ENOUGH_TICKETS("1016", "Not enough tickets available", 400),
    TICKET_PURCHASE_NOT_FOUND("1017", "Ticket purchase not found", 404),
    CANNOT_CANCEL_BEFORE_EVENT("1018", "Cannot cancel ticket purchase before the event date", 400),
    GENERAL_EXCEPTION("9999", "An unexpected error occurred", 500),;

    private final String code;
    private final String message;
    private final int status;

    MessageType(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
