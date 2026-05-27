package kyung.kung_backend.domain.integrity.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class AppIntegrityAllowlist {

    private static final String PACKAGE_NAME = "kyung.kung_android";

    private final Map<String, Set<String>> allowedSignaturesByBuildType = Map.of(
            "debug", Set.of(
                    "87:F9:BE:5B:C6:D9:98:C4:E9:52:92:35:E2:F9:A5:D1:AA:EE:7A:93:B8:66:2A:85:68:98:F6:33:0B:F5:47:7C"
            ),
            "release", Set.of(
                    "04:DE:91:84:8B:A9:9C:5C:5D:D9:15:27:F7:91:95:68:DB:BA:74:F1:DB:5A:C8:10:EC:30:F6:E7:35:DD:E8:65"
            )
    );

    private final Map<DexKey, String> allowedClassesDexHashes = Map.of(
            new DexKey(PACKAGE_NAME, 1L, "debug"),
            "A4C4BCB3F38B43930525CC036DBCF43CAA29F9AE2ACB8FFBCB2D1075759F1AF3"
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
