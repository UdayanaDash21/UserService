package com.user.service.controllers;


import com.user.service.entities.User;
import com.user.service.services.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//import static java.awt.event.InputEvent.logger;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    //create
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
      User user1 =  userService.saveUser(user);
      return ResponseEntity.status(HttpStatus.CREATED).body(user1);
    }

    int retryCount =1;

    //Get Single User
    @GetMapping("/{userId}")
//    @CircuitBreaker(name="ratingHotelBreaker",fallbackMethod = "ratingHotelFallback")
//    @Retry(name="ratingHotelBreaker",fallbackMethod = "ratingHotelFallback")
    @RateLimiter(name="userRateLimiter", fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getSingleUser(@PathVariable String userId){

        System.out.println(userId);
        // logger.info("RetryCount: {} ",retryCount);
        retryCount++;
        System.out.println(retryCount);
       User user2 = userService.getUser(userId);
        return ResponseEntity.ok(user2);
    }

    //Creating Fallback method for Circuit breaker

    public ResponseEntity<User> ratingHotelFallback(String userId,Exception ex){
//       logger.info("Fallback is executted because service is doen't work",ex.getMessage());
      User user = User.builder().email("dummy@email.com")
               .name("Dummy")
               .about("This user is created dummy because some service is down")
               .userId("12345")
               .build();
       return new ResponseEntity<>(user,HttpStatus.OK);
    }

    //Get All Users
    @GetMapping
    public ResponseEntity<List<User>> getAllUser(){
      List<User> allUser =   userService.getAllUser();

        return ResponseEntity.ok(allUser);
    }






}


