package org.scoula.chat.service.chatbot;

import lombok.extern.slf4j.Slf4j;
import org.scoula.chat.dto.ChatBotDTO;
import org.scoula.chat.dto.ChatRequestOptions;
import org.scoula.chat.mapper.ChatBotMapper;
import org.scoula.chat.service.ChatServiceImpl;
import org.scoula.chat.service.WebClientService;
import org.scoula.dictionary.domain.DictionaryVO;
import org.scoula.dictionary.mapper.DictionaryMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ChatBotService extends ChatServiceImpl {

    private final DictionaryMapper dictionaryMapper;
    private final ChatBotCacheService chatBotCacheService;
    private final ChatBotMapper chatBotMapper;

    public ChatBotService(WebClientService webClientService, DictionaryMapper dictionaryMapper, ChatBotCacheService chatBotCacheService, ChatBotMapper chatBotMapper) {
        super(webClientService);
        this.dictionaryMapper = dictionaryMapper;
        this.chatBotCacheService = chatBotCacheService;
        this.chatBotMapper = chatBotMapper;
    }

    @Override
    public Mono<String> getResponse(String prompt, List<String> selectedAnswers) {
        String responseWithCache = chatBotCacheService.getResponseWithCache(prompt);
        if (responseWithCache != null) {
            return Mono.just(responseWithCache)
                    .map(this::extractWordToLink);
        }
        return super.getResponse(prompt, selectedAnswers)
                .flatMap(response -> {
                    String resultResponse = extractWordToLink(response);

                    // DB에 저장 (비동기!!!!!!!!!)
                    Mono.fromRunnable(() -> {
                        try {
                            String normalizedQuestion = chatBotCacheService.normalizeQuestion(prompt);

                            ChatBotDTO existedData = chatBotMapper.getByQuestion(normalizedQuestion);

                            if(existedData != null){
                                existedData.setAnswer(resultResponse);
                                existedData.setUpdatedAt(LocalDateTime.now());
                                chatBotMapper.updateQuestion(existedData);
                            }else{
                                ChatBotDTO chatBotDTO = ChatBotDTO.builder()
                                        .question(prompt)
                                        .answer(resultResponse)
                                        .hitCount(1L)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                try{
                                    chatBotMapper.insertQuestion(chatBotDTO);
                                }catch (DuplicateKeyException e){
                                    log.error("중복 키 예외 발생(MySQL)");
                                    ChatBotDTO byQuestion = chatBotMapper.getByQuestion(normalizedQuestion);
                                    byQuestion.setAnswer(resultResponse);
                                    byQuestion.setUpdatedAt(LocalDateTime.now());
                                    chatBotMapper.updateQuestion(byQuestion);
                                }
                            }
                        } catch (Exception e) {
                            log.error("챗봇 응답 DB 저장 실패..:{}", e.getMessage());
                        }
                    }).subscribeOn(Schedulers.boundedElastic()).subscribe();
                    return Mono.just(resultResponse);
                });
    }

    private String extractWordToLink(String response) {
        List<DictionaryVO> dictionaries;
        try {
            dictionaries = dictionaryMapper.getList();
            dictionaries.sort((a, b) ->
                    b.getDictionaryTitle().length() - a.getDictionaryTitle().length());

            // 챗봇의 답변에 Dictionary 내용이 있는지 확인
            for (DictionaryVO dictionary : dictionaries) {
                String title = dictionary.getDictionaryTitle();
                String pattern = "(?<!\\w)" + Pattern.quote(title) + "(?!\\w)";
                String newResponse = response.replaceAll(pattern,
                        String.format("<a href='/study/dictionary/detail/%d' class='dictionary-link' style='color: green; font-weight: bold;'>%s</a>",
                                dictionary.getDictionaryNo(), title));

                if (!newResponse.equals(response)) {
                    response = newResponse;
                    break;
                }
            }
            return response;
        } catch (Exception e) {
            return response;
        }


    }

    @Override
    protected String getModelName() {
        return "gpt-4o-mini";
    }

    @Override
    protected String getAdditionalContext(List<String> selectedAnswers) {
        return "당신의 이름은 '부기봇'이고, 한국의 부동산 전문가입니다. 한국에 사는 사람에게 2줄 이내로 요약된 정보를 존댓말로 제공해주세요.투자와 관련된 정보는 제한해주세요.";
    }

    @Override
    protected ChatRequestOptions getRequestOptions() {
        return new ChatRequestOptions(0.7, 150, 0.0, 0.0, 1);
    }

}
