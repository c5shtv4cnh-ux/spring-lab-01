package kz.iitu.spring_lab_01.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/** Individual variant 3: renders the 403 template (any HTTP method, because a denied POST is forwarded too). */
@Controller
public class AccessDeniedController {

    @RequestMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
