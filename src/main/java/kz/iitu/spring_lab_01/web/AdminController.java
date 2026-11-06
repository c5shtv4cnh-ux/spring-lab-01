package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.user.AppUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AppUserRepository users;

    public AdminController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", users.findAllByOrderByIdAsc());
        return "admin/users";
    }
}
