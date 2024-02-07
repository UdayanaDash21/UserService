package com.user.service.services;

import com.user.service.entities.User;

import java.util.List;

public interface UserService {

    //Create
    User saveUser(User user);

    //Get All
    List<User> getAllUser();

    //Get By Id
    User getUser(String userId);

    //Delete User

    //Update User

}
