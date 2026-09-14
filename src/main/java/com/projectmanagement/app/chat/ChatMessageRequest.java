package com.projectmanagement.app.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotBlank
    @Size(max = 5000)
    private String content;
    @Positive
    private Long recipientId;
    @Positive
    private Long roomId;
}
