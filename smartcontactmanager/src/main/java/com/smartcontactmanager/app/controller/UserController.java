package com.smartcontactmanager.app.controller;

import com.smartcontactmanager.app.dao.UserRepository;
import com.smartcontactmanager.app.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        //getting username(Email) using principle property of spring security
        String userName=principal.getName();
        System.out.println("USERNAME "+userName);
        User user=this.userRepository.getUserByUserName(userName);
        System.out.println(user);
        model.addAttribute("user",user);
        //get the user using username(Email)
        return "normal/user_dashboard";
    }
}