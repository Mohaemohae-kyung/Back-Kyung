package kyung.kung_backend.domain.file.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider {

    // 파일을 물리적 공간에 저장하고 접근 가능한 URL 또는 경로 반환
    String store(MultipartFile file, String storedName);

    // 파일명으로 실제 리소스 로드
    Resource loadAsResource(String storedName);

    // 물리적 파일 삭제
    void delete(String storedName);
}