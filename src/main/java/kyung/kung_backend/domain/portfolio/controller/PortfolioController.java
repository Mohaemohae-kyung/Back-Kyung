package kyung.kung_backend.domain.portfolio.controller;

import kyung.kung_backend.domain.portfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/api/portfolios/viewer")
    public ResponseEntity<byte[]> getPortfolioView(@RequestParam String url) {
        return portfolioService.fetchPortfolioWithSsrf(url);
    }
}