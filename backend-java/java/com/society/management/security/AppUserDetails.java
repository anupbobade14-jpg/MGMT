package com.society.management.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class AppUserDetails extends User {
    private final Long userId;
    private final String fullName;

    public AppUserDetails(Long userId, String username, String password, String fullName,
                          Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        super(username, password, enabled, true, true, true, authorities);
        this.userId = userId;
        this.fullName = fullName;
    }
}
