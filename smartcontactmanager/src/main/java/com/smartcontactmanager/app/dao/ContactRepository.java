package com.smartcontactmanager.app.dao;

import com.smartcontactmanager.app.entities.Contact;
import com.smartcontactmanager.app.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
    //pagination...
    @Query("from Contact as c where c.user.id=:userId")
    //currentPage-page
    //Contact per page - 5
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
    //search contacts in login user only
    public List<Contact> findByNameContainingAndUser(String keywords, User user);

    User user(User user);
}
