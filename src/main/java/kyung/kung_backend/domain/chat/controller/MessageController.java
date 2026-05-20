package kyung.kung_backend.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kyung.kung_backend.domain.chat.dto.ChatDto;
import kyung.kung_backend.domain.chat.dto.ChatMessageRequest;
import kyung.kung_backend.domain.chat.service.ChatService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "채팅 API", description = "실시간 WebSocket 메시지 브로커링 및 채팅 이력 조회 API")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    @Operation(summary = "내 채팅방 목록 조회", description = "유저 식별 번호를 기반으로 참여 중인 활성화된 채팅방 목록을 반환합니다.")
    @GetMapping("/api/chat/rooms")
    public ApiResponse<List<ChatDto.RoomResponse>> getMyRooms(@RequestParam Long userId) {
        List<ChatDto.RoomResponse> response = chatService.getMyRoomsAsUser(userId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(summary = "채팅 내역 전체 조회", description = "특정 채팅방의 식별 번호(chatRoomId)를 기준으로 이전 송수신 메시지 타임라인 내역을 조회합니다.")
    @GetMapping("/api/chat/rooms/{chatRoomId}/messages")
    public ApiResponse<List<ChatDto.MessageResponse>> getRoomMessages(@PathVariable Long chatRoomId) {
        List<ChatDto.MessageResponse> response = chatService.getRoomMessages(chatRoomId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @MessageMapping("/chat/message")
    public void message(@Payload ChatMessageRequest request) {
        try {
            if ("ENTER".equals(request.getType())) {
                // 입장 처리 로직
            }

            ChatDto.MessageResponse savedMessage = chatService.saveMessage(request);
            messagingTemplate.convertAndSend("/sub/chat/room/" + request.getRoomId(), savedMessage);

        } catch (Exception e) {
            System.err.println("[STOMP 전송 오류]: " + e.getMessage());
            e.printStackTrace();
        }
    }
}