package kyung.kung_backend.global.lab;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ExpertProfileUploadWebappResourceConfig {

    private static final String WEBAPP_MOUNT_PATH = "/uploads/expert-profile";

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> expertProfileUploadWebappMount(
            @Value("${expert-profile.image.upload-dir}")
            String uploadDir
    ) {
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();

        return factory -> factory.addContextCustomizers(context -> {
            WebResourceRoot resources = context.getResources();

            if (resources == null) {
                resources = new StandardRoot(context);
                context.setResources(resources);
            }

            try {
                Files.createDirectories(uploadRoot);
            } catch (Exception e) {
                throw new IllegalStateException("failed to prepare expert profile upload webapp mount", e);
            }

            resources.addPreResources(
                    new DirResourceSet(
                            resources,
                            WEBAPP_MOUNT_PATH,
                            uploadRoot.toString(),
                            "/"
                    )
            );
        });
    }
}
