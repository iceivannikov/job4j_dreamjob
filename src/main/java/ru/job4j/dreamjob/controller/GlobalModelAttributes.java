package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.job4j.dreamjob.model.User;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("user")
    public User addUser(HttpSession session) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User();
            user.setName("Гость");
        }
        return user;
    }
}
