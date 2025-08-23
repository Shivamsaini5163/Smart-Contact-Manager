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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
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
                contact.setImage("no_dp.jpg");
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
    //showing particular contact details.
    @RequestMapping("/contact/{cid}")
    public String showContactDetail(@PathVariable("cid") Integer cid, Model model,Principal principal){
        System.out.println("CID "+cid);
        Optional<Contact> contactOptional=this.contactRepository.findById(cid);
        Contact contact=contactOptional.get();
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);
        if(user.getId()==contact.getUser().getId()){
            model.addAttribute("contact",contact);
            model.addAttribute("title", "Contact- "+contact.getName());
        }
        return "normal/contact_detail";
    }
    //delete contact handler
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cid, Model model,Principal principal,HttpSession session){
        Optional<Contact> contactOptional=this.contactRepository.findById(cid);
        Contact contact=contactOptional.get();
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);
        if(user.getId()==contact.getUser().getId()) {
            contact.setUser(null);
            this.contactRepository.delete(contact);
            session.setAttribute("message", new Message("Your contact is deleted!!","success"));
        }
        return "redirect:/user/show-contacts/0";
    }
    //open update form handler
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model model){
        model.addAttribute("title", "Update Contact");
        Contact contact=this.contactRepository.findById(cid).get();
        model.addAttribute("contact",contact);
        return "normal/update_form";
    }
    //update contact handler
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
                                Model model,HttpSession session,Principal principal){
        try{
            //old contact details
            Contact oldContactDetails=this.contactRepository.findById(contact.getCid()).get();
            //image
            if(!file.isEmpty()){
                //delete old photo
                File deleteFile=new ClassPathResource("static/image").getFile();
                File file1=new File(deleteFile,oldContactDetails.getImage());
                file1.delete();
                //update new photo
                File saveFile=new ClassPathResource("static/image").getFile();
                Path path=Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
                System.out.println("New Image is Updated");
            }else {
                contact.setImage(oldContactDetails.getImage());
            }
            User user=this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your contact is updated!!","success"));
        }catch(Exception e){
            System.out.println("ERROR "+e.getMessage());
        }
        System.out.println("CONTACT NAME "+contact.getName());
        System.out.println("CONTACT ID "+contact.getCid());
        return "redirect:/user/contact/"+contact.getCid();
    }
    //your profile handler
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title", "Your Profile");
        return "normal/profile";
    }
    //open settings handler
    @GetMapping("/settings")
    public String openSettings(Model model){
        model.addAttribute("title", "Settings");
        return "normal/settings";
    }
    //change password... handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,
                                 Principal principal,HttpSession session){
        System.out.println("OLD PASSWORD "+oldPassword);
        System.out.println("NEW PASSWORD "+newPassword);
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);
        System.out.println("Password "+user.getPassword());
        if (this.passwordEncoder.matches(oldPassword,user.getPassword())){
            //change the password
            user.setPassword(this.passwordEncoder.encode(newPassword));
            this.userRepository.save(user);
            session.setAttribute("message", new Message("Your password is successfully changed!","success"));
        }else{
            //error...
            session.setAttribute("message", new Message("Please Enter Correct old password","danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }
}