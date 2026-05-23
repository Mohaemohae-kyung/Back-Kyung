package kyung.kung_backend.domain.integrity.controller;

import your.package.integrity.dto.AppIntegrityReportRequest;
import your.package.integrity.dto.AppIntegrityReportResponse;
import your.package.integrity.service.AppIntegrityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app-integrity")
public class AppIntegrityController {

    private final AppIntegrityService appIntegrityService;

    public AppIntegrityController(AppIntegrityService appIntegrityService) {
        this.appIntegrityService = appIntegrityService;
    }

    @PostMapping("/report")
    public ResponseEntity<AppIntegrityReportResponse> report(
            @RequestBody AppIntegrityReportRequest request
    ) {
        AppIntegrityReportResponse response = appIntegrityService.evaluate(request);
        return ResponseEntity.ok(response);
    }
}