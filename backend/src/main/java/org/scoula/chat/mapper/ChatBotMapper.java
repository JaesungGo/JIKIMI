package org.scoula.chat.mapper;

import org.scoula.chat.dto.ChatBotDTO;

import java.util.List;

public interface ChatBotMapper {
    ChatBotDTO getByQuestion(String question);
    void incrementCacheHit(Long id);

    void insertQuestion(ChatBotDTO chatBotDTO);

    void updateQuestion(ChatBotDTO chatBotDTO);

    List<ChatBotDTO> getPopularQuestions(int limit);

    int syncHitCount(String question, long hitCount);

}
