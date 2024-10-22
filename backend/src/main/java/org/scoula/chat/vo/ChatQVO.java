package org.scoula.chat.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatQVO {
    private Long chatQNo;
    private String question;
    private String answer;
    private int frequency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
