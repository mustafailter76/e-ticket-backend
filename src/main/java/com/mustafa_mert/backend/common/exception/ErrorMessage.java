package com.mustafa_mert.backend.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorMessage {

    // This class encapsulates the error message details, including the message type and an optional detailed message.

    private MessageType messageType;
    private String detailMessage;

    public ErrorMessage(MessageType messageType, String detailMessage) {
        this.messageType = messageType;
        this.detailMessage = detailMessage;
    }

    public ErrorMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public String prepareErrorMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(messageType.getCode())
                .append(" - ")
                .append(messageType.getMessage());
        if (detailMessage != null && !detailMessage.isEmpty()) {
            sb.append(": ").append(detailMessage);
        }
        return sb.toString();
    }
}
