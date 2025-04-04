package org.scoula.chat.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ChatResponse {

    private final String content;

    private ChatResponse(String content){
        this.content = content;
    }

    public static ChatResponse of(String content){
        return new ChatResponse(content);
    }

}