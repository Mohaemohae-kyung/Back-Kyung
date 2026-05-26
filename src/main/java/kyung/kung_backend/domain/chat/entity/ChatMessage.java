package kyung.kung_backend.domain.chat.entity;

import jakarta.persistence.*;

import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseCreatedEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "CHAT_MESSAGES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "CHAT_MESSAGES_SEQ_GENERATOR",
        sequenceName = "CHAT_MESSAGES_SEQ",
        allocationSize = 1
)
public class ChatMessage extends BaseCreatedEntity {

    // =========================
    // 채팅 메시지 ID
    // =========================
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "CHAT_MESSAGES_SEQ_GENERATOR"
    )
    @Column(
            name = "CHAT_MESSAGE_ID",
            nullable = false
    )
    private Long chatMessageId;

    // =========================
    // 채팅방
    // =========================
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "CHAT_ROOM_ID",
            nullable = false
    )
    private ChatRoom chatRoom;

    // =========================
    // 보낸 사람
    // SYSTEM 메시지는 null 가능
    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID")
    private User sender;

    // =========================
    // 메시지 타입
    // TEXT / IMAGE / FILE
    // PAYMENT_REQUEST / SYSTEM
    // =========================
    @Column(
            name = "MESSAGE_TYPE",
            nullable = false,
            length = 30
    )
    private String messageType;

    // =========================
    // 메시지 내용
    // =========================
    @Lob
    @Column(name = "CONTENT")
    private String content;

    // =========================
    // 결제 요청 ID
    // PAYMENT_REQUEST 타입일 때 사용
    // =========================
    @Column(name = "PAYMENT_ID")
    private Long paymentId;

    // =========================
    // 첨부 파일
    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FILE_ID")
    private FileUpload file;

    // =========================
    // 읽음 여부
    // Y : 읽음
    // N : 읽지 않음
    // =========================
    @Column(
            name = "READ_YN",
            nullable = false,
            length = 1
    )
    private String readYn;

    // =========================
    // 일반 메시지 생성
    // =========================
    public static ChatMessage create(
            ChatRoom chatRoom,
            User sender,
            String messageType,
            String content
    ) {

        ChatMessage chatMessage =
                new ChatMessage();

        chatMessage.chatRoom =
                chatRoom;

        chatMessage.sender =
                sender;

        chatMessage.messageType =
                messageType;

        chatMessage.content =
                content;

        // 최초 생성 시 읽지 않음
        chatMessage.readYn =
                "N";

        return chatMessage;
    }

    // =========================
    // 결제 요청 메시지 생성
    // =========================
    public static ChatMessage createPaymentMessage(
            ChatRoom chatRoom,
            User sender,
            String content,
            Long paymentId
    ) {

        ChatMessage chatMessage =
                new ChatMessage();

        chatMessage.chatRoom =
                chatRoom;

        chatMessage.sender =
                sender;

        chatMessage.messageType =
                "PAYMENT_REQUEST";

        chatMessage.content =
                content;

        chatMessage.paymentId = paymentId;

        chatMessage.readYn =
                "N";

        return chatMessage;
    }

    // =========================
    // 시스템 메시지 생성
    // =========================
    public static ChatMessage createSystemMessage(
            ChatRoom chatRoom,
            String content
    ) {

        ChatMessage chatMessage =
                new ChatMessage();

        chatMessage.chatRoom =
                chatRoom;

        // 시스템 메시지는 sender 없음
        chatMessage.sender =
                null;

        chatMessage.messageType =
                "SYSTEM";

        chatMessage.content =
                content;

        // 시스템 메시지는 읽음 처리
        chatMessage.readYn =
                "Y";

        return chatMessage;
    }

    // =========================
    // 읽음 처리
    // =========================
    public void read() {

        this.readYn = "Y";
    }

    // =========================
    // 읽음 여부 확인
    // =========================
    public boolean isRead() {

        return "Y".equals(this.readYn);
    }
}