package com.example.userservice.dto;

import com.example.userservice.models.Role;
import java.util.List;

import com.example.userservice.models.User;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private String name;
    private String email;
    @ManyToMany
    private List<Role> roles;
    private boolean isEmailVerified;

    public static UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.name = user.getName();
        userDto.email = user.getEmail();
        userDto.isEmailVerified = user.isEmailVerified();
        userDto.roles = user.getRoles();
        return userDto;
    }
}


