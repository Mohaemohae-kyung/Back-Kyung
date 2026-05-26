package kyung.kung_backend.domain.integrity.dto;

public record AppIntegrityReportResponse(
        boolean validSignature,
        boolean validDex,
        String riskLevel,
        String reason
) {
}