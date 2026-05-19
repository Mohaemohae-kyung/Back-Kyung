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
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filePath = uploadDir + storedName;
            file.transferTo(new File(filePath));

            return "/api/files/download/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public Resource loadAsResource(String storedName) {
        try {
            Path filePath = Paths.get(uploadDir + storedName);
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