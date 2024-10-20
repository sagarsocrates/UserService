package com.example.userservice.services;

import com.example.userservice.exception.InvalidPasswordExcpetion;
import com.example.userservice.exception.InvalidTokenExcpetion;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TokenRepository tokenRepository;

    UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepository = tokenRepository;
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
        return;
    }

    public User validateToken(String tokenValue) throws InvalidTokenExcpetion {
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeleted(tokenValue, false);
        if (optionalToken.isEmpty()){
            throw new InvalidTokenExcpetion("Invalid Token");
        }
        return optionalToken.get().getUser();
    }
}
