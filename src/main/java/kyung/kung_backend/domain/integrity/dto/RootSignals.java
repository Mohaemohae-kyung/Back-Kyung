package kyung.kung_backend.domain.integrity.dto;

public record RootSignals(
        boolean suBinaryDetected,
        boolean magiskDetected,
        boolean systemPartitionWritable,
        boolean rootManagementAppDetected,
        boolean suspiciousSystemPathDetected,
        boolean rootShellExecutable
) {
}