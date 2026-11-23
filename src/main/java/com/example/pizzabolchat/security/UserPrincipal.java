package com.example.pizzabolchat.security;

import java.security.Principal;

public record UserPrincipal(Long id, String email) implements Principal {

    @Override
    public String getName() {
        return id.toString();
    }
}
