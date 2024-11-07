package com.example.userservice.security.models;

import com.example.userservice.models.Role;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.core.GrantedAuthority;

@JsonDeserialize
public class CustomGrantAuthority implements GrantedAuthority {
    private String authority;

    public CustomGrantAuthority() {

    }
    public CustomGrantAuthority(Role role) {
        this.authority = role.getName();
    }
    @Override
    public String getAuthority() {
        return authority;
    }
}
