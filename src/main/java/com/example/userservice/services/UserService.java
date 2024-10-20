package com.example.userservice.services;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User signUp(String email, String password, String name){
        Optional<User> existingUser = userRepository.findByEmail(email);
        if(existingUser.isPresent()){
            //User exists
            return existingUser.get();
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));

        User savedUser = userRepository.save(user);
        return savedUser;
    }

    public Token login(String email, String password){
        return null;
    }

    public void logOut(String token){
        return;
    }
}
