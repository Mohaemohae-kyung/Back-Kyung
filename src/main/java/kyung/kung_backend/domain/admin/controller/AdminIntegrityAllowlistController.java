package kyung.kung_backend.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kyung.kung_backend.domain.integrity.entity.AppIntegrityAllowlistEntry;
import kyung.kung_backend.domain.integrity.repository.AppIntegrityAllowlistRepository;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin/integrity/allowlist")
@RequiredArgsConstructor
public class AdminIntegrityAllowlistController {

    private final AppIntegrityAllowlistRepository allowlistRepository;

    @Operation(
            summary = "앱 무결성 해시 등록",
            description = "동일한 (packageName, versionCode, buildType, sha256) 조합이 이미 존재하면 기존 ID를 반환하는 멱등 동작입니다."
    )
    @PostMapping
    public ApiResponse<Long> register(@Valid @RequestBody AllowlistRegisterRequest request) {
        Long id = allowlistRepository
                .findByPackageNameAndVersionCodeAndBuildTypeAndSha256(
                        request.getPackageName(),
                        request.getVersionCode(),
                        request.getBuildType(),
                        request.getSha256()
                )
                .map(AppIntegrityAllowlistEntry::getId)
                .orElseGet(() -> allowlistRepository.save(
                        AppIntegrityAllowlistEntry.of(
                                request.getPackageName(),
                                request.getVersionCode(),
                                request.getBuildType(),
                                request.getSha256(),
                                request.getNote()
                        )
                ).getId());

        return ApiResponse.onSuccess(SuccessCode.CREATED, id);
    }

    @Getter
    @Setter
    public static class AllowlistRegisterRequest {

        @NotBlank
        private String packageName;

        @NotNull
        private Long versionCode;

        @NotBlank
        private String buildType;

        @NotBlank
        private String sha256;

        private String note;
    }
}
