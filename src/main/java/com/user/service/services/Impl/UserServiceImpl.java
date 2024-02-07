package com.user.service.services.Impl;

import com.mysql.cj.log.LogFactory;
import com.user.service.entities.Hotel;
import com.user.service.entities.Rating;
import com.user.service.entities.User;
import com.user.service.exceptions.ResourceNotFoundException;
import com.user.service.externalService.HotelService;
import com.user.service.repositories.UserRepository;
import com.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelService hotelService;


    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User saveUser(User user) {
        //It will generate a Unique Userid

        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        //Implementing Ratings with all users with their ratings
          List<User> allUsers = userRepository.findAll();
          if(allUsers.isEmpty()){
              return allUsers;
          }
          allUsers.stream().forEach( user -> {
              String apiUrl = "http://localhost:8083/ratings/users/" + user.getUserId();
              ParameterizedTypeReference<List<Rating>> responseType = new ParameterizedTypeReference<List<Rating>>() {};
//                // Make the GET request
              ResponseEntity<List<Rating>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, responseType);
              List<Rating> ratingList = responseEntity.getBody().stream().map(rating -> {
                  //Api call to hotel service to get the hotel
                  //http://localhost:8082/hotels/04ad6219-fce5-45bf-b553-e587172b0e77
                  try {
                      ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://localhost:8082/hotels/" + rating.getHotelId(), Hotel.class);
                      Hotel hotel = forEntity.getBody();
                      // Hotel hotel = hotelService.getHotel(rating.getHotelId());
                      logger.info("response status code ", forEntity.getStatusCode());
                      // set the hotel to rating
                      rating.setHotel(hotel);
                      //return the rating
                      return rating;
                  } catch (Exception e) {
                      logger.error(e.getMessage());
                      return new Rating();
                  }
              }).collect(Collectors.toList());
              user.setRatings(ratingList);
          });
        return allUsers;
    }

    @Override
    public User getUser(String userId) {
        //Firstly We are using RestTemplate for getting Single User data here
        //get user from database with the help of user repository
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with given Id is not found on Server : " + userId));
//        System.out.println(user.getUserId()+" "+user.getEmail());
//        //fetch ratings of above user in RATING SERVICE
        //http://localhost:8083/ratings/users/1c0372fc-8680-4995-b97e-817cf8e11a03
        //RestTemplate
        try {
            String apiUrl = "http://localhost:8083/ratings/users/" + user.getUserId();
            ParameterizedTypeReference<List<Rating>> responseType = new ParameterizedTypeReference<List<Rating>>() {};
            // Make the GET request
                ResponseEntity<List<Rating>> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, null, responseType);
//           List<Rating> ratingsOfUser = restTemplate.getForObject("http://localhost:8083/ratings/users/" + user.getUserId(), ArrayList.class);
//           logger.info("{} ", ratingsOfUser);

           List<Rating> ratingList = responseEntity.getBody().stream().map(rating -> {
               //Api call to hotel service to get the hotel
               //http://localhost:8082/hotels/04ad6219-fce5-45bf-b553-e587172b0e77
               try {
                   ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://HOTEL_SERVICE/hotels/" + rating.getHotelId(), Hotel.class);
//                   Hotel hotel = forEntity.getBody();
                   logger.info("response status code ", forEntity.getStatusCode());


                   //Using Feign Client Here

                   Hotel hotel = hotelService.getHotel(rating.getHotelId());

                   // set the hotel to rating
                   rating.setHotel(hotel);
                   //return the rating
                   return rating;
               } catch (Exception e) {
                   logger.error(e.getMessage());
                   return new Rating();
               }
           }).collect(Collectors.toList());
            user.setRatings(ratingList);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return user;
    }
}
