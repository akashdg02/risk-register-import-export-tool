package com.internship.tool.service;

import com.internship.tool.entity.UserAccount;
import com.internship.tool.repository.UserAccountRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(username.trim())
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return User.builder()
            .username(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
            .disabled(Boolean.FALSE.equals(user.getActive()))
            .build();
    }
}
