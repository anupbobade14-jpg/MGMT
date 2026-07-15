package com.society.management.security;

import com.society.management.entity.User;
import com.society.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if (!u.isActive()) throw new UsernameNotFoundException("User disabled");

        return new AppUserDetails(
                u.getId(),
                u.getEmail(),
                u.getPasswordHash() != null ? u.getPasswordHash() : "",
                u.getFullName(),
                List.of(new SimpleGrantedAuthority(u.getRole().authority())),
                u.isActive());
    }
}
