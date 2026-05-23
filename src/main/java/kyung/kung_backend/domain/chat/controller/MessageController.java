package kyung.kung_backend.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import kyung.kung_backend.domain.chat.dto.ChatDto;
import kyung.kung_backend.domain.chat.dto.ChatMessageRequest;
import kyung.kung_backend.domain.chat.entity.ChatRoom;
import kyung.kung_backend.domain.chat.service.ChatService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(
        name = "채팅 API",
        description =
                "실시간 WebSocket 메시지 브로커링 및 채팅 이력 조회 API"
)
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    private final SimpMessageSendingOperations
            messagingTemplate;

    // =========================
    // 내 채팅방 목록 조회
    // =========================
    @Operation(
            summary = "내 채팅방 목록 조회",
            description =
                    "현재 로그인한 사용자가 참여 중인 활성화된 채팅방 목록을 반환합니다."
    )
    @GetMapping("/api/chat/rooms")
    public ApiResponse<List<ChatDto.RoomResponse>>
    getMyRooms(

            @AuthenticationPrincipal
            User user
    ) {

        System.out.println(

                "현재 로그인 유저 = " +

                        user.getUserId()
        );

        List<ChatDto.RoomResponse>
                response =

                chatService.getMyRoomsAsUser(

                        user.getUserId()
                );

        return ApiResponse.onSuccess(

                SuccessCode.OK,

                response
        );
    }

    // =========================
    // 채팅 메시지 조회
    // =========================
    @Operation(
            summary = "채팅 내역 전체 조회",
            description =
                    "특정 채팅방의 식별 번호(chatRoomId)를 기준으로 이전 송수신 메시지 타임라인 내역을 조회합니다."
    )
    @GetMapping(
            "/api/chat/rooms/{chatRoomId}/messages"
    )
    public ApiResponse<List<ChatDto.MessageResponse>>
    getRoomMessages(

            @PathVariable
            Long chatRoomId
    ) {

        System.out.println(

                "메시지 조회 roomId = " +

                        chatRoomId
        );

        List<ChatDto.MessageResponse>
                response =

                chatService.getRoomMessages(
                        chatRoomId
                );

        return ApiResponse.onSuccess(

                SuccessCode.OK,

                response
        );
    }

    // =========================
    // 채팅방 읽음 처리
    // =========================
    @PatchMapping(
            "/api/chat/rooms/{roomId}/read"
    )
    public ResponseEntity<Void> readRoom(

            @PathVariable
            Long roomId,

            @AuthenticationPrincipal
            User user
    ) {

        chatService.readMessages(

                roomId,

                user.getUserId()
        );

        return ResponseEntity.ok().build();
    }

    // =========================
    // WebSocket 메시지 송수신
    // =========================
    @MessageMapping("/chat/message")
    public void message(

            @Payload
            ChatMessageRequest request
    ) {

        try {

            System.out.println(

                    "메시지 수신 = " +

                            request
            );

            // =========================
            // 입장 메시지 처리
            // =========================
            if (
                    "ENTER".equals(
                            request.getType()
                    )
            ) {

                System.out.println(
                        "채팅방 입장"
                );
            }

            // =========================
            // 메시지 저장
            // =========================
            ChatDto.MessageResponse
                    savedMessage =

                    chatService.saveMessage(
                            request
                    );

            System.out.println(
                    "메시지 저장 완료"
            );

            // =========================
            // room 조회
            // =========================
            ChatRoom room =

                    chatService.getRoom(

                            Long.valueOf(
                                    request.getRoomId()
                            )
                    );

            // =========================
            // 상대방 ID 계산
            // =========================
            Long receiverId;

            if (

                    room.getUser()
                            .getUserId()

                            .equals(

                                    Long.valueOf(
                                            request.getSenderId()
                                    )
                            )
            ) {

                receiverId =

                        room.getExpertProfile()
                                .getUser()
                                .getUserId();

            } else {

                receiverId =

                        room.getUser()
                                .getUserId();
            }

            // =========================
            // 현재 채팅방 메시지 전송
            // =========================
            messagingTemplate.convertAndSend(

                    "/sub/chat/room/" +

                            request.getRoomId(),

                    savedMessage
            );

            // =========================
            // 상대방 알림 전송
            // =========================
            messagingTemplate.convertAndSend(

                    "/sub/chat/notify/" +

                            receiverId,

                    Map.of(

                            "roomId",
                            request.getRoomId(),

                            "message",
                            savedMessage.getContent()
                    )
            );

            System.out.println(
                    "WebSocket 전송 완료"
            );

        } catch (Exception e) {

            System.err.println(
                    "[STOMP 전송 오류]"
            );

            e.printStackTrace();
        }
    }
}