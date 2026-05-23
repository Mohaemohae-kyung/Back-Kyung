package kyung.kung_backend.domain.integrity.dto;

import java.util.List;

public record AppIntegrityReportRequest(
        String packageName,
        long versionCode,
        String versionName,
        String buildType,

        List<String> signatureSha256List,
        String classesDexSha256,

        RootSignals rootSignals,
        boolean fridaDetected
) {
}