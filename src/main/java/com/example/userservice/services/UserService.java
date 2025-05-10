package com.example.userservice.services;

import com.example.userservice.configs.KafkaProducerClient;
import com.example.userservice.dto.SendEmailDto;
import com.example.userservice.exception.InvalidPasswordExcpetion;
import com.example.userservice.exception.InvalidTokenExcpetion;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenRepository tokenRepository;
    private final KafkaProducerClient kafkaProducerClient;
    private final ObjectMapper objectMapper;

    UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, TokenRepository tokenRepository,
                KafkaProducerClient kafkaProducerClient, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepository = tokenRepository;
        this.kafkaProducerClient = kafkaProducerClient;
        this.objectMapper = objectMapper;
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
        //Once the signup is completed, send kafka event
        SendEmailDto sendEmailDto = new SendEmailDto();
        sendEmailDto.setTo(savedUser.getEmail());
        sendEmailDto.setSubject("New User");
        sendEmailDto.setBody("Thanks for joining the course");
        sendEmailDto.setFrom("sagarsocrates22@gmail.com");
        try{
            kafkaProducerClient.sendMessage("sendEmail",objectMapper.writeValueAsString(sendEmailDto));
        } catch (JsonProcessingException e) {
            System.out.println("Something went wrong while sending mail" + e.getMessage());
        }
        return savedUser;
    }

    public Token login(String email, String password) throws InvalidPasswordExcpetion {
        /*
         1) Check if user exists of not with email
         2) if not, throw exception
         3) if exists, compare incoming password, with password stored in db
         4) If password matches, then login successul, return token
         */
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()){
            return null;
        }
        User user = optionalUser.get();
        if(!bCryptPasswordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidPasswordExcpetion("Please enter correct password");
        }

        //Login successful, generate a new token
        Token token = generateToken(user);

        return tokenRepository.save(token);

    }
    private Token generateToken(User user){
        LocalDate curentTime = LocalDate.now();
        LocalDate thirtyDaysFromCurrentTime = LocalDate.now().plusDays(30);
        Date expiryDate = Date.from(thirtyDaysFromCurrentTime.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Token token = new Token();
        token.setExpiryAt(expiryDate);

        // Token value is a randomly generated string of 128 characters
        token.setValue(RandomStringUtils.randomAlphanumeric(128));
        token.setUser(user);
        return token;
    }

    public void logOut(String token) throws InvalidTokenExcpetion {
        //Validate if the given token is present in the DB as well as is_deleted is false
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeleted(token, false);
        if(optionalToken.isEmpty()){
            throw new InvalidTokenExcpetion("Invalid Token Passed");
        }
        Token deletedToken = optionalToken.get();
        deletedToken.setDeleted(true);
        tokenRepository.save(deletedToken);
    }

    public User validateToken(String tokenValue) throws InvalidTokenExcpetion {
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeleted(tokenValue, false);
        if (optionalToken.isEmpty()){
            throw new InvalidTokenExcpetion("Invalid Token");
        }
        return optionalToken.get().getUser();
    }
}
