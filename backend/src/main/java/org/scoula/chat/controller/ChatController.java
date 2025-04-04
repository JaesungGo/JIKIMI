package org.scoula.chat.controller;

import org.scoula.chat.dto.ChatRequest;
import org.scoula.chat.dto.ChatResponse;
import org.scoula.chat.service.ChatService;
import org.scoula.chat.service.chatbot.ChatBotService;
import org.scoula.chat.service.scenario.ScenarioService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatBotService chatBotService;
    private final ScenarioService scenarioService;

    public ChatController(ChatBotService chatBotService, ScenarioService scenarioService) {
        this.chatBotService = chatBotService;
        this.scenarioService = scenarioService;
    }

    @PostMapping(value="/chatbot",produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    public Mono<ChatResponse> chat(@RequestBody ChatRequest request) {
        return chatBotService.getResponse(request.getPrompt(), (List<String>) null)
                .map(ChatResponse::of);
    }
    @PostMapping(value = "/scenario", produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Mono<ChatResponse>> getScenarioResponse(@RequestBody ChatRequest request) {
        try {
            Mono<ChatResponse> scenarioResponse = scenarioService.getResponse(request.getPrompt(), (List<String>) null)
                    .map(ChatResponse::of);
            return ResponseEntity.ok().body(scenarioResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Mono.just(ChatResponse.of(e.getMessage())));
        }
    }
}
