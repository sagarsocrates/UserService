package com.example.userservice.controllers;

import com.example.userservice.dto.*;
import com.example.userservice.exception.InvalidPasswordExcpetion;
import com.example.userservice.exception.InvalidTokenExcpetion;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import org.springframework.http.HttpStatus;
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
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) throws InvalidPasswordExcpetion {
        Token token = userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(token);
            return loginResponseDto;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(@RequestBody LogOutRequestDto logOutRequestDto) throws InvalidTokenExcpetion {
        ResponseEntity<Void> responseEntity = null;
        try{
            userService.logOut(logOutRequestDto.getToken());
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            System.out.println("Something went wrong"   );
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST    );
        }
        return responseEntity;
    }
}
