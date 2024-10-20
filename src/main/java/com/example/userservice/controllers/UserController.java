package com.example.userservice.controllers;

import com.example.userservice.dto.LogOutRequestDto;
import com.example.userservice.dto.LoginRequestDto;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/signup")
    public UserDto signUp(@RequestBody SignUpRequestDto signUpRequestDto){
        User savedUser = userService.signUp(
                signUpRequestDto.getEmail(),
                signUpRequestDto.getPassword(),
                signUpRequestDto.getName()
        );
        //get UserDto from user;
        return UserDto.from(savedUser);
    }

    @PostMapping("/login")
    public Token login(@RequestBody LoginRequestDto loginRequestDto){
            return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(@RequestBody LogOutRequestDto logOutRequestDto){
        return null;
    }
}
