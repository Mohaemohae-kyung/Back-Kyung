package kyung.kung_backend.domain.store.service;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.repository.FileUploadRepository;
import kyung.kung_backend.domain.store.dto.StoreProductCreateRequest;
import kyung.kung_backend.domain.store.dto.StoreProductResponse;
import kyung.kung_backend.domain.store.dto.StoreProductUpdateRequest;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.store.entity.enums.StoreProductStatus;
import kyung.kung_backend.domain.store.repository.StoreProductRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreProductService {

    private static final String STORE_PRODUCT_TARGET_TYPE = "STORE_PRODUCT";

    private final StoreProductRepository storeProductRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final FileUploadRepository fileUploadRepository;

    @Transactional
    public StoreProductResponse createStoreProduct(
            User user,
            StoreProductCreateRequest request
    ) {
        ExpertProfile expertProfile = getExpertProfileByUser(user);

        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.CATEGORY_NOT_FOUND));

        FileUpload thumbnailImage = getUsableThumbnailImage(user, request.getThumbnailImageFileId());

        StoreProduct storeProduct = StoreProduct.builder()
                .expertProfile(expertProfile)
                .category(category)
                .title(request.getTitle())
                .thumbnailImageUrl(thumbnailImage.getFileUrl())
                .description(request.getDescription())
                .price(request.getPrice())
                .serviceType(request.getServiceType())
                .serviceRegion(request.getServiceRegion())
                .status(StoreProductStatus.ACTIVE)
                .build();

        StoreProduct savedStoreProduct = storeProductRepository.save(storeProduct);

        thumbnailImage.updateTarget(
                STORE_PRODUCT_TARGET_TYPE,
                savedStoreProduct.getStoreProductId()
        );

        return StoreProductResponse.from(savedStoreProduct);
    }

    public List<StoreProductResponse> getStoreProducts(Long categoryId) {
        if (categoryId == null) {
            return storeProductRepository.findAllByStatus(StoreProductStatus.ACTIVE)
                    .stream()
                    .map(StoreProductResponse::from)
                    .toList();
        }

        return storeProductRepository.findAllByCategory_CategoryIdAndStatus(
                        categoryId,
                        StoreProductStatus.ACTIVE
                )
                .stream()
                .map(StoreProductResponse::from)
                .toList();
    }

    public StoreProductResponse getStoreProduct(Long storeProductId) {
        StoreProduct storeProduct = storeProductRepository.findByStoreProductIdAndStatus(
                        storeProductId,
                        StoreProductStatus.ACTIVE
                )
                .orElseThrow(() -> GeneralException.of(ErrorCode.STORE_PRODUCT_NOT_FOUND));

        return StoreProductResponse.from(storeProduct);
    }

    public List<StoreProductResponse> getMyStoreProducts(User user) {
        ExpertProfile expertProfile = getExpertProfileByUser(user);

        return storeProductRepository.findAllByExpertProfile_ExpertProfileIdAndStatus(
                        expertProfile.getExpertProfileId(),
                        StoreProductStatus.ACTIVE
                )
                .stream()
                .map(StoreProductResponse::from)
                .toList();
    }

    @Transactional
    public StoreProductResponse updateStoreProduct(
            User user,
            Long storeProductId,
            StoreProductUpdateRequest request
    ) {
        ExpertProfile expertProfile = getExpertProfileByUser(user);

        StoreProduct storeProduct = storeProductRepository.findByStoreProductIdAndStatus(
                        storeProductId,
                        StoreProductStatus.ACTIVE
                )
                .orElseThrow(() -> GeneralException.of(ErrorCode.STORE_PRODUCT_NOT_FOUND));

        validateOwner(storeProduct, expertProfile);

        ServiceCategory category = null;

        if (request.getCategoryId() != null) {
            category = serviceCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> GeneralException.of(ErrorCode.CATEGORY_NOT_FOUND));
        }

        String thumbnailImageUrl = null;

        if (request.getThumbnailImageFileId() != null) {
            FileUpload thumbnailImage = getUsableThumbnailImage(user, request.getThumbnailImageFileId());

            thumbnailImage.updateTarget(
                    STORE_PRODUCT_TARGET_TYPE,
                    storeProduct.getStoreProductId()
            );

            thumbnailImageUrl = thumbnailImage.getFileUrl();
        }

        storeProduct.update(
                category,
                request.getTitle(),
                thumbnailImageUrl,
                request.getDescription(),
                request.getPrice(),
                request.getServiceType(),
                request.getServiceRegion()
        );

        return StoreProductResponse.from(storeProduct);
    }

    @Transactional
    public void deleteStoreProduct(
            User user,
            Long storeProductId
    ) {
        ExpertProfile expertProfile = getExpertProfileByUser(user);

        StoreProduct storeProduct = storeProductRepository.findByStoreProductIdAndStatus(
                        storeProductId,
                        StoreProductStatus.ACTIVE
                )
                .orElseThrow(() -> GeneralException.of(ErrorCode.STORE_PRODUCT_NOT_FOUND));

        validateOwner(storeProduct, expertProfile);

        storeProduct.delete();
    }

    private FileUpload getUsableThumbnailImage(User user, Long fileId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("대표 이미지 파일을 찾을 수 없습니다."));

        if (!fileUpload.getUploader().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("본인이 업로드한 이미지만 대표 이미지로 설정할 수 있습니다.");
        }

        if ("DELETED".equals(fileUpload.getStatus())) {
            throw new IllegalArgumentException("삭제된 이미지는 대표 이미지로 설정할 수 없습니다.");
        }

        if (fileUpload.getTargetId() != null) {
            throw new IllegalArgumentException("이미 다른 대상에 연결된 이미지입니다.");
        }

        return fileUpload;
    }

    private ExpertProfile getExpertProfileByUser(User user) {
        if (user == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        return expertProfileRepository.findByUser(user)
                .orElseThrow(() -> GeneralException.of(ErrorCode.EXPERT_PROFILE_NOT_FOUND));
    }

    private void validateOwner(
            StoreProduct storeProduct,
            ExpertProfile expertProfile
    ) {
        if (!storeProduct.getExpertProfile().getExpertProfileId()
                .equals(expertProfile.getExpertProfileId())) {
            throw GeneralException.of(ErrorCode.STORE_PRODUCT_ACCESS_DENIED);
        }
    }
}