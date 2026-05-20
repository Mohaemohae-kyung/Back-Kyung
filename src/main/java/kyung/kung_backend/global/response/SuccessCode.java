package kyung.kung_backend.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseCode {

    OK(HttpStatus.OK, "COMMON_200", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "COMMON_201", "요청이 성공적으로 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON_204", "요청에 성공했으며 반환할 데이터가 없습니다."),

    // STORE PRODUCT
    STORE_PRODUCT_CREATE_SUCCESS(HttpStatus.CREATED, "STORE_PRODUCT_201_1", "마켓 상품이 등록되었습니다."),
    STORE_PRODUCT_LIST_GET_SUCCESS(HttpStatus.OK, "STORE_PRODUCT_200_1", "마켓 상품 목록을 조회했습니다."),
    STORE_PRODUCT_GET_SUCCESS(HttpStatus.OK, "STORE_PRODUCT_200_2", "마켓 상품을 조회했습니다."),
    STORE_PRODUCT_MY_LIST_GET_SUCCESS(HttpStatus.OK, "STORE_PRODUCT_200_3", "내 마켓 상품 목록을 조회했습니다."),
    STORE_PRODUCT_UPDATE_SUCCESS(HttpStatus.OK, "STORE_PRODUCT_200_4", "마켓 상품이 수정되었습니다."),
    STORE_PRODUCT_DELETE_SUCCESS(HttpStatus.OK, "STORE_PRODUCT_200_5", "마켓 상품이 삭제되었습니다."),

    // FAVORITE
    FAVORITE_EXPERT_TOGGLE_SUCCESS(HttpStatus.OK, "FAVORITE_200_1", "고수 찜 상태를 변경했습니다."),

    // MYPAGE
    MYPAGE_FAVORITE_EXPERTS_GET_SUCCESS(HttpStatus.OK, "MYPAGE_200_1", "찜한 고수 목록을 조회했습니다."),
    MYPAGE_SUMMARY_GET_SUCCESS(HttpStatus.OK, "MYPAGE_200_2", "마이페이지 요약 정보를 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}