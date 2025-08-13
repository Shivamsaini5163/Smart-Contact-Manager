package com.smartcontactmanager.app.controller;

import com.smartcontactmanager.app.dao.ContactRepository;
import com.smartcontactmanager.app.dao.UserRepository;
import com.smartcontactmanager.app.entities.Contact;
import com.smartcontactmanager.app.entities.User;
import com.smartcontactmanager.app.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    //method for adding common data to response
    @ModelAttribute
   public void addCommonData(Model model,Principal principal){
        //getting username(Email) using principle property of spring security
        String userName=principal.getName();
        System.out.println("USERNAME "+userName);
        User user=this.userRepository.getUserByUserName(userName);
        System.out.println(user);
        model.addAttribute("user",user);
   }
   // dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        //get the user using username(Email)
        return "normal/user_dashboard";
    }
    //open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }
    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
                                 Principal principal, HttpSession session){
        try{
            String name=principal.getName();
            User user=this.userRepository.getUserByUserName(name);

            //processing and uploading file
            if (file.isEmpty()){
                //if the file is empty then try our message
                System.out.println("File is empty");
            }else{
                //upload the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());
                File saveFile=new ClassPathResource("static/image").getFile();
                Path path=Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is Uploaded");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("DATA "+contact);
            System.out.println("Added to database");
            //message success...
            session.setAttribute("message", new Message("Your contact is added!! Add more...","success"));
        }catch(Exception e){
            System.out.println("ERROR "+e.getMessage());
            e.printStackTrace();
            //message error...
            session.setAttribute("message", new Message("Something went wrong!!! Try again...","danger"));
        }
        return "normal/add_contact_form";
    }
    //show contacts handler
    @GetMapping("/show-contacts/{page}")
    public String showContact(@PathVariable("page") Integer page, Model model, Principal principal){
        model.addAttribute("title", "View Contacts");
        //get contacts of those user which is login only
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);
        Pageable pageRequest=PageRequest.of(page, 5);
        Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageRequest);
        model.addAttribute("contacts",contacts);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show_contacts";
    }
}