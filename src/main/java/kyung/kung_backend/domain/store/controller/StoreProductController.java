package kyung.kung_backend.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.store.dto.StoreProductCreateRequest;
import kyung.kung_backend.domain.store.dto.StoreProductResponse;
import kyung.kung_backend.domain.store.dto.StoreProductUpdateRequest;
import kyung.kung_backend.domain.store.service.StoreProductService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store-products")
public class StoreProductController {

    private final StoreProductService storeProductService;

    @Operation(
            summary = "마켓 상품 등록",
            description = "로그인한 고수가 마켓에 판매할 상품을 등록합니다."
    )
    @PostMapping
    public ApiResponse<StoreProductResponse> createStoreProduct(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody StoreProductCreateRequest request
    ) {
        StoreProductResponse response = storeProductService.createStoreProduct(user, request);

        return ApiResponse.onSuccess(
                SuccessCode.STORE_PRODUCT_CREATE_SUCCESS,
                response
        );
    }

    @Operation(
            summary = "고수 본인 마켓 상품 목록 조회",
            description = "로그인한 고수가 본인이 등록한 마켓 상품 목록을 조회합니다."
    )
    @GetMapping("/my")
    public ApiResponse<List<StoreProductResponse>> getMyStoreProducts(
            @AuthenticationPrincipal User user
    ) {
        List<StoreProductResponse> response = storeProductService.getMyStoreProducts(user);

        return ApiResponse.onSuccess(
                SuccessCode.STORE_PRODUCT_MY_LIST_GET_SUCCESS,
                response
        );
    }

    @Operation(
            summary = "마켓 상품 목록 조회",
            description = "판매 중인 마켓 상품 목록을 조회합니다. categoryId를 전달하면 해당 카테고리의 상품만 조회합니다."
    )
    @GetMapping
    public ApiResponse<List<StoreProductResponse>> getStoreProducts(
            @RequestParam(required = false) Long categoryId
    ) {
        List<StoreProductResponse> response = storeProductService.getStoreProducts(categoryId);

        return ApiResponse.onSuccess(
                SuccessCode.STORE_PRODUCT_LIST_GET_SUCCESS,
                response
        );
    }

    @Operation(
            summary = "마켓 상품 상세 조회",
            description = "마켓 상품 ID를 기준으로 상품 상세 정보를 조회합니다."
    )
    @GetMapping("/{storeProductId}")
    public ApiResponse<StoreProductResponse> getStoreProduct(
            @PathVariable Long storeProductId
    ) {
        StoreProductResponse response = storeProductService.getStoreProduct(storeProductId);

        return ApiResponse.onSuccess(
                SuccessCode.STORE_PRODUCT_GET_SUCCESS,
                response
        );
    }

    @Operation(
            summary = "마켓 상품 수정",
            description = "로그인한 고수가 본인이 등록한 마켓 상품 정보를 수정합니다."
    )
    @PatchMapping("/{storeProductId}")
    public ApiResponse<StoreProductResponse> updateStoreProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long storeProductId,
            @RequestBody StoreProductUpdateRequest request
    ) {
        StoreProductResponse response = storeProductService.updateStoreProduct(
                user,
                storeProductId,
                request
        );

        return ApiResponse.onSuccess(
                SuccessCode.STORE_PRODUCT_UPDATE_SUCCESS,
                response
        );
    }

    @Operation(
            summary = "마켓 상품 삭제",
            description = "로그인한 고수가 본인이 등록한 마켓 상품을 삭제 처리합니다."
    )
    @DeleteMapping("/{storeProductId}")
    public ApiResponse<Void> deleteStoreProduct(
            @AuthenticationPrincipal User user,
            @PathVariable Long storeProductId
    ) {
        storeProductService.deleteStoreProduct(user, storeProductId);

        return ApiResponse.onSuccess(
                SuccessCode.STORE_PRODUCT_DELETE_SUCCESS,
                null
        );
    }
}