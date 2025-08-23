package com.smartcontactmanager.app.controller;

import com.smartcontactmanager.app.dao.ContactRepository;
import com.smartcontactmanager.app.dao.UserRepository;
import com.smartcontactmanager.app.entities.Contact;
import com.smartcontactmanager.app.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;
    //search handler
    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal) {
        System.out.println(query);
        User user=this.userRepository.getUserByUserName(principal.getName());
        List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query,user);
        return ResponseEntity.ok(contacts);
    }
}
