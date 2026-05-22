package kyung.kung_backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.aws")
public class S3Properties {

    private Credentials credentials = new Credentials();

    private String region;

    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Credentials {

        private String accessKey;

        private String secretKey;
    }

    @Getter
    @Setter
    public static class S3 {

        private String bucket;

        private String uploadPath;
    }
}