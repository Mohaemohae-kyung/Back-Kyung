package kyung.kung_backend.domain.request.enums;

public enum RequestStatus {
    // 사용자가 견적 요청을 생성했고, 아직 고수가 응답하지 않은 상태
    PENDING,

    // 고수가 견적 요청을 승인했고, 채팅방이 생성되어 협의 중인 상태
    CHATTING,

    // 고수가 견적 요청을 거절한 상태
    REJECTED,

    // 채팅/결제/거래가 최종 완료된 상태
    COMPLETED,

    // 사용자가 요청을 취소한 상태
    CANCELLED,

    // 관리 또는 소프트 삭제 처리된 상태
    DELETED
}