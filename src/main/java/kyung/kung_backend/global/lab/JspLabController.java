package kyung.kung_backend.global.lab;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lab/jsp")
public class JspLabController {

    @GetMapping("/status")
    public String status(Model model) {
        model.addAttribute("applicationName", "kyung-backend");
        model.addAttribute("packaging", "bootWar");
        model.addAttribute("purpose", "JSP/WAR lab runtime check");

        return "lab/status";
    }
}
