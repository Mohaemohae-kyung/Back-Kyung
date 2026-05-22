package kyung.kung_backend.global;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "health", description = "health check 관련 API입니다.")
@RestController
public class RootController {

    @Operation(
            summary = "CI/CD health check",
            description = "서버가 정상적으로 실행 중인지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Success"
            )
    })
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}