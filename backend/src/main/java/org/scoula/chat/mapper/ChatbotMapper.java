package org.scoula.chat.mapper;

import org.scoula.chat.vo.ChatQVO;

import java.util.List;

public interface ChatbotMapper {
    List<ChatQVO> getAllFaqs();

    void insertFaq(ChatQVO faq);

    void incrementFrequency(Long faqNo);
}
