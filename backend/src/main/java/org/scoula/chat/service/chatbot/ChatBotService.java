package org.scoula.chat.service.chatbot;

import org.scoula.chat.dto.ChatRequestOptions;
import org.scoula.chat.mapper.ChatbotMapper;
import org.scoula.chat.service.ChatServiceImpl;
import org.scoula.chat.service.WebClientService;
import org.scoula.chat.vo.ChatQVO;
import org.scoula.dictionary.domain.DictionaryVO;
import org.scoula.dictionary.mapper.DictionaryMapper;
import org.scoula.faq.domain.FaqVO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ChatBotService extends ChatServiceImpl {

    private final DictionaryMapper dictionaryMapper;
    private final ChatbotMapper chatbotMapper;

    // 단어 유사도 기준점
    private static final double SIMILARITY_STANDARD = 0.8;

    public ChatBotService(WebClientService webClientService, DictionaryMapper dictionaryMapper, ChatbotMapper chatbotMapper) {
        super(webClientService);
        this.dictionaryMapper = dictionaryMapper;
        this.chatbotMapper = chatbotMapper;
    }


    @Override
    public Mono<String> getResponse(String prompt, List<String> selectedAnswers) {
        ChatQVO similarChat = findSimilarChat(prompt);
        if(similarChat != null){
            chatbotMapper.incrementFrequency(similarChat.getChatQNo());
            return Mono.just(extractWordToLink(similarChat.getAnswer()));
        }
        return super.getResponse(prompt, selectedAnswers)
                .map(response -> {
                    saveChatMessage(prompt,response);
                    return extractWordToLink(response);
                });
    }

    private String extractWordToLink(String response) {
        System.out.println("response = " + response);
        List<DictionaryVO> dictionaries;
        try {
            dictionaries = dictionaryMapper.getList();
            dictionaries.sort((a, b) ->
                    b.getDictionaryTitle().length() - a.getDictionaryTitle().length());

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

    private ChatQVO findSimilarChat(String prompt) {
        List<ChatQVO> chats = chatbotMapper.getAllFaqs();
        double highSimilarity = 0;
        ChatQVO mostSimilarQ = null;

        for(ChatQVO chat : chats){
            double similarity = calculateSimilarity(prompt, chat.getQuestion());
            if(similarity > SIMILARITY_STANDARD && similarity > highSimilarity){
                highSimilarity = similarity;
                mostSimilarQ = chat;
            }
        }
        return mostSimilarQ;
    }

    /**
     * 코사인 유사도 계산 사용 A*B / (||A||*||B||)
     * @param prompt
     * @param question
     * @return
     */
    private double calculateSimilarity(String prompt, String question){

        // 공백을 기준으로 단어 배열로 변환
        String[] tokenPrompt = prompt.toLowerCase().split("\\s+");
        String[] tokenQuestion = question.toLowerCase().split("\\s+");

        Map<String, Integer> frequencyP = getFrequency(tokenPrompt);
        Map<String, Integer> frequencyQ = getFrequency(tokenQuestion);

        double result = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for(String word : frequencyP.keySet()){
            if(frequencyQ.containsKey(word)) {
                result += frequencyP.get(word) * frequencyQ.get(word);
            }
            norm1 += Math.pow(frequencyP.get(word),2);
        }

        for(Integer value : frequencyQ.values()){
            norm2 += Math.pow(value,2);
        }

        if( norm1 == 0 || norm2 == 0) return 0;

        return result / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> getFrequency(String[] tokens){
        Map<String, Integer> frequency = new HashMap<>();
        for(String token : tokens){
            frequency.merge(token, 1, Integer::sum);
        }
        return frequency;
    }

    private void saveChatMessage(String question, String answer){
        ChatQVO chatQ = new ChatQVO();
        chatQ.setQuestion(question);
        chatQ.setAnswer(answer);
        chatbotMapper.insertFaq(chatQ);
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
