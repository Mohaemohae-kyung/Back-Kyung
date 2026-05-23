package kyung.kung_backend.domain.integrity.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class AppIntegrityAllowlist {

    private static final String PACKAGE_NAME = "kyung.kung_android";

    private final Map<String, Set<String>> allowedSignaturesByBuildType = Map.of(
            "debug", Set.of(
                    "debug 서명 작성"
            ),
            "release", Set.of(
                    "release 서명 작성"
            )
    );

    private final Map<DexKey, String> allowedClassesDexHashes = Map.of(
            new DexKey(PACKAGE_NAME, 1L, "debug"),
            "여기에_VERSION_1_DEBUG_CLASSES_DEX_SHA256"
    );

    public boolean isAllowedSignature(
            String packageName,
            String buildType,
            Set<String> receivedSignatures
    ) {
        if (!PACKAGE_NAME.equals(packageName)) {
            return false;
        }

        Set<String> allowed = allowedSignaturesByBuildType.get(buildType);
        if (allowed == null || allowed.isEmpty()) {
            return false;
        }

        for (String received : receivedSignatures) {
            if (allowed.contains(received)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAllowedDex(
            String packageName,
            long versionCode,
            String buildType,
            String receivedDexHash
    ) {
        if (receivedDexHash == null || receivedDexHash.isBlank()) {
            return false;
        }

        String allowed = allowedClassesDexHashes.get(
                new DexKey(packageName, versionCode, buildType)
        );

        return receivedDexHash.equals(allowed);
    }

    private record DexKey(
            String packageName,
            long versionCode,
            String buildType
    ) {
    }
}