package main.controllers;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class DefaultController {
    @RequestMapping("/")
    public void main() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @RequestMapping("${web.path}")
    public String index(@NotNull Model model) {
        model.addAttribute("test", "test");
        return "index";
    }
}
