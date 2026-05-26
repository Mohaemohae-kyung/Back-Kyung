package kyung.kung_backend.domain.file.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LocalStorageProvider implements StorageProvider {

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Override
    public String store(MultipartFile file, String storedName) {
        try {
            // 파일 업로드 취약점 경로가 들어가면 순순히 해당 경로로 업로드됨
            // 이걸 막는다면 stored id 는 db 그대로 들어가되, 실제는 저장이 uploads에 됨
            // 아래 두줄은 절대 경로 구하고 표준화
            Path rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetPath = Paths.get(uploadDir + storedName).toAbsolutePath().normalize();

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            file.transferTo(targetPath.toFile());


            return "/api/files/download/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public Resource loadAsResource(String storedName) {
        try {
            // 💡 [경로 탐색 취약점 증명 테스트 주석]
            // 원인: 슬래시(/)와 역슬래시(\)가 혼용된 상태에서 .toUri()를 호출하면, 
            // JVM이 역슬래시(\)를 %5C로 URL 인코딩하여 경로 구분자가 아닌 일반 파일명 특수문자로 취급해 공격이 차단됩니다.
            // 해결: .normalize()를 먼저 호출하면 URI 인코딩이 일어나기 전에 메모리 레벨에서 경로 탐색 문자(..)와 
            // 슬래시/역슬래시 연산을 선제적으로 모두 상쇄하여 깔끔한 절대 경로로 표준화합니다.
            // ⚠️ 보안 경고: 이를 방어하기 위해 실제 서비스에서는 완성된 절대 경로가 허용된 디렉토리(uploadDir)의 하위 경로인지 검증해야 합니다.
            Path filePath = Paths.get(uploadDir + storedName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 읽을 수 없습니다.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 잘못되었습니다.", e);
        }
    }

    @Override
    public void delete(String storedName) {
        File file = new File(uploadDir + storedName);
        if (file.exists()) {
            file.delete();
        }
    }
}