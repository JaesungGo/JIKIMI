package org.scoula.chat.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
public class ChatBotDTO {

    public ChatBotDTO(Long id, String question, String answer, Long hitCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.hitCount = hitCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private Long id;
    private String question;
    private String answer;
    private Long hitCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
