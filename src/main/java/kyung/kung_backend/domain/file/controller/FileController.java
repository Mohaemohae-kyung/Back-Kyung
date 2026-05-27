package kyung.kung_backend.domain.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kyung.kung_backend.domain.file.dto.FileUploadResponse;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.service.FileService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Tag(name = "File", description = "파일 업로드 및 다운로드 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(
            summary = "파일 업로드",
            description = "로그인한 사용자가 파일을 업로드하고 파일 메타데이터를 저장합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file,
            @RequestParam("domain") String domain
    ) {
        FileUploadResponse response = fileService.uploadFile(user, file, domain);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "파일 다운로드",
            description = "저장된 파일명을 통해 업로드된 파일을 다운로드합니다."
    )
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFileTest(
            @RequestParam("storedName") String storedName
    ) {
        Resource resource = fileService.downloadFile(storedName);
        FileUpload fileInfo = fileService.getFileInfo(storedName);

        String encodedFileName = UriUtils.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .body(resource);
    }

    @Operation(
            summary = "파일 삭제",
            description = "로그인한 사용자가 업로드한 파일을 삭제합니다."
    )
    @DeleteMapping("/{storedName}")
    public ApiResponse<Void> deleteFile(
            @AuthenticationPrincipal User user,
            @PathVariable String storedName
    ) {
        fileService.deleteFile(user, storedName);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }
}