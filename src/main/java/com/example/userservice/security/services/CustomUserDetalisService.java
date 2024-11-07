package com.example.userservice.security.services;

import com.example.userservice.models.User;
import com.example.userservice.repositories.UserRepository;
import com.example.userservice.security.models.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetalisService implements UserDetailsService {

    private UserRepository userRepository;

    public CustomUserDetalisService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User with username" + username + " not found");
        }

        User user = optionalUser.get();
        CustomUserDetails customerUserDetalis = new CustomUserDetails(user);
        return customerUserDetalis;
    }
}
