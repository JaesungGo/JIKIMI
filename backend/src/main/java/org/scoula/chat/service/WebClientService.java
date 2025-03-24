package org.scoula.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WebClientService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public WebClientService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, @Value("${openai.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/chat/completions").build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }


    /**
     * OpenAI에 요청 <-> 응답 처리 메서드
     * @param prompt 요청에 포함할 프롬프트
     * @param modelName 사용할 모델명 ex.gpt4o, gpt-4o-mini
     * @param additionalContext 추가적으로 같이 전송할 프롬프트
     * @param options 요청에 추가할 모델의 옵션
     * @return 응답에서 추출한 내용을 Mono로 반환
     */
    public Mono<String> sendRequestToAPI(String prompt, String modelName, String additionalContext, Map<String, Object> options) {
        String fullPrompt;
        if(prompt!= null) {
            fullPrompt = additionalContext + " " + prompt ;
        } else {
            fullPrompt = additionalContext;
        }

        String requestBody = createRequestBody(fullPrompt, modelName, options);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractContentFromResponse);
    }


    private String createRequestBody(String fullPrompt, String modelName, Map<String, Object> options) {
        Map<String, Object> message = Map.of("role", "user", "content", fullPrompt);
        Map<String, Object> requestBodyMap = new HashMap<>(options);
        requestBodyMap.put("model", modelName);
        requestBodyMap.put("messages", List.of(message));

        try {
            return objectMapper.writeValueAsString(requestBodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating JSON request", e);
        }
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("API 파싱 에러", e);
        }
    }
}


