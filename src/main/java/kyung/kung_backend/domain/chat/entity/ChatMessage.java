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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_MESSAGES_SEQ_GENERATOR")
    @Column(name = "CHAT_MESSAGE_ID", nullable = false)
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SENDER_ID", nullable = false)
    private User sender;

    @Column(name = "MESSAGE_TYPE", nullable = false, length = 20)
    private String messageType;

    @Lob
    @Column(name = "CONTENT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FILE_ID")
    private FileUpload file;

    @Column(name = "READ_YN", nullable = false, length = 1)
    private String readYn;

    public static ChatMessage create(
            ChatRoom chatRoom,
            User sender,
            String messageType,
            String content
    ) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.chatRoom = chatRoom;
        chatMessage.sender = sender;
        chatMessage.messageType = messageType;
        chatMessage.content = content;
        chatMessage.readYn = "N";
        return chatMessage;
    }
}