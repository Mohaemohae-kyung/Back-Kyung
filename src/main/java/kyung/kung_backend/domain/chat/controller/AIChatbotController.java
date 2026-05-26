package kyung.kung_backend.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kyung.kung_backend.domain.chat.dto.AIChatbotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Tag(name = "AI 챗봇 프록시 API", description = "Vercel 프론트엔드와 Tailscale LLM 서버 간의 통신을 중계하는 API")
@RestController
@RequiredArgsConstructor
public class AIChatbotController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${LLM_API_URL:http://localhost:8000/chat}")
    private String llmApiUrl;

    @Operation(summary = "AI 챗봇 메시지 전송 (프록시)", description = "프론트엔드로부터의 요청을 받아 Tailscale 망 내의 LLM 서버로 중계합니다.")
    @PostMapping("/api/chat/llm")
    public ResponseEntity<AIChatbotDto.Response> chatWithLLM(
            @RequestBody AIChatbotDto.Request request
    ) {
        // 백엔드 측에서도 강제로 항상 취약한 기본 모드('vulnerable')로 고정 송신
        request.setMode("vulnerable");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AIChatbotDto.Request> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AIChatbotDto.Response> response = restTemplate.postForEntity(
                    llmApiUrl,
                    entity,
                    AIChatbotDto.Response.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            AIChatbotDto.Response errorResponse = new AIChatbotDto.Response();
            errorResponse.setReply("⚠️ AI 서버 연결에 실패했습니다. 로컬 LLM 서버(Tailscale)가 정상 작동 중인지 확인해주세요.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
