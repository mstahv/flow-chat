package org.vaadin.example.chat.backend;

import java.time.LocalDateTime;

/** Simple bean representing a single chat message. */
public class ChatMessage {

    private LocalDateTime time;
    private String from;
    private String message;

    public ChatMessage(String from, String message) {
        this.from = from;
        this.message = message;
        this.time = LocalDateTime.now();
    }

    public String getFrom() {
        return from;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

}
