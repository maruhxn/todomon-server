package com.maruhxn.todomon.infra.mail.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SendNotificationBatchDTO {

    private Long id;
    private LocalDateTime startAt;
    private String content;
    private String username;
    private String email;

    @Builder
    public SendNotificationBatchDTO(Long id, LocalDateTime startAt, String content, String username, String email) {
        this.id = id;
        this.startAt = startAt;
        this.content = content;
        this.username = username;
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
