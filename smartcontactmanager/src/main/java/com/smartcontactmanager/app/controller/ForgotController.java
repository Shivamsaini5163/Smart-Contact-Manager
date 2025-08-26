package com.smartcontactmanager.app.controller;

import com.smartcontactmanager.app.dao.UserRepository;
import com.smartcontactmanager.app.entities.User;
import com.smartcontactmanager.app.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
public class ForgotController {
    Random random = new Random(1000);
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    //email id form open handler
    @RequestMapping("/forgot")
    public String openEmailForm(Model model){
        model.addAttribute("title", "Login - Smart Contact Manager");
        return "forgot_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, Model model, HttpSession session){
        model.addAttribute("title", "Login - Smart Contact Manager");
        int otp=random.nextInt(9999999);
        System.out.println("OTP: "+otp);
        //write code for send otp to email...
        String subject="OTP From SCM";
        String body="<div style='border:1px solid blue; padding :20px;'>"
                +"<h1>"
                +"OTP is "
                +"<b>"+otp
                +"</n>"
                +"</h1>"
                +"</div>";
        String to=email;
        boolean flag=this.emailService.sendEmail(to,subject,body);
        if (flag){
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        }else{
            session.setAttribute("message", "Check your email id!!");
            return "forgot_email_form";
        }
    }
    //verify otp
    @PostMapping("verify-otp")
    public String verifyOTP(@RequestParam("otp") int otp,HttpSession session){
        int myOtp=(int) session.getAttribute("myotp");
        String email=(String)session.getAttribute("email");
        if (myOtp==otp){
            //password change form
            User user=this.userRepository.getUserByUserName(email);
            if (user==null){
                //send error message
                session.setAttribute("message", "User does not exists with this email id !!");
                return "forgot_email_form";
            }else{
                //send change password form
                return "password_change_form";
            }
        }else{
            session.setAttribute("message","You have entered wrong otp!!");
            return "verify_otp";
        }
    }
    //change password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session){
        String email=(String)session.getAttribute("email");
        User user=this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
        this.userRepository.save(user);
        return "redirect:/signin?change=password changed successfully..";

    }
}
