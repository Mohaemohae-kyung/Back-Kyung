package kyung.kung_backend.domain.integrity.service;

import your.package.integrity.domain.RiskLevel;
import your.package.integrity.dto.AppIntegrityReportRequest;
import your.package.integrity.dto.AppIntegrityReportResponse;
import your.package.integrity.dto.RootSignals;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AppIntegrityService {

    private final AppIntegrityAllowlist allowlist;

    public AppIntegrityService(AppIntegrityAllowlist allowlist) {
        this.allowlist = allowlist;
    }

    public AppIntegrityReportResponse evaluate(AppIntegrityReportRequest request) {
        Set<String> receivedSignatures = new HashSet<>(request.signatureSha256List());

        boolean validSignature = allowlist.isAllowedSignature(
                request.packageName(),
                request.buildType(),
                receivedSignatures
        );

        boolean validDex = allowlist.isAllowedDex(
                request.packageName(),
                request.versionCode(),
                request.buildType(),
                request.classesDexSha256()
        );

        RiskLevel riskLevel = calculateRiskLevel(
                validSignature,
                validDex,
                request.rootSignals(),
                request.fridaDetected()
        );

        String reason = buildReason(
                validSignature,
                validDex,
                request.rootSignals(),
                request.fridaDetected()
        );

        return new AppIntegrityReportResponse(
                validSignature,
                validDex,
                riskLevel.name(),
                reason
        );
    }

    private RiskLevel calculateRiskLevel(
            boolean validSignature,
            boolean validDex,
            RootSignals rootSignals,
            boolean fridaDetected
    ) {
        if (!validSignature) {
            return RiskLevel.BLOCK;
        }

        if (!validDex) {
            return RiskLevel.HIGH;
        }

        if (fridaDetected) {
            return RiskLevel.HIGH;
        }

        if (rootSignals.magiskDetected() || rootSignals.rootShellExecutable()) {
            return RiskLevel.HIGH;
        }

        if (rootSignals.suBinaryDetected()
                || rootSignals.systemPartitionWritable()
                || rootSignals.rootManagementAppDetected()
                || rootSignals.suspiciousSystemPathDetected()) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    private String buildReason(
            boolean validSignature,
            boolean validDex,
            RootSignals rootSignals,
            boolean fridaDetected
    ) {
        if (!validSignature) return "SIGNATURE_MISMATCH";
        if (!validDex) return "DEX_HASH_MISMATCH";
        if (fridaDetected) return "FRIDA_DETECTED";
        if (rootSignals.magiskDetected()) return "MAGISK_DETECTED";
        if (rootSignals.rootShellExecutable()) return "ROOT_SHELL_EXECUTABLE";
        if (rootSignals.suBinaryDetected()) return "SU_BINARY_DETECTED";
        if (rootSignals.systemPartitionWritable()) return "SYSTEM_PARTITION_WRITABLE";
        if (rootSignals.rootManagementAppDetected()) return "ROOT_MANAGEMENT_APP_DETECTED";
        if (rootSignals.suspiciousSystemPathDetected()) return "SUSPICIOUS_SYSTEM_PATH_DETECTED";

        return "OK";
    }
}