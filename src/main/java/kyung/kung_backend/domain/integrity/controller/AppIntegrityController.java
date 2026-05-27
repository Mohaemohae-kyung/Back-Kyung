package kyung.kung_backend.domain.integrity.controller;

import kyung.kung_backend.domain.integrity.dto.AppIntegrityReportRequest;
import kyung.kung_backend.domain.integrity.dto.AppIntegrityReportResponse;
import kyung.kung_backend.domain.integrity.service.AppIntegrityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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