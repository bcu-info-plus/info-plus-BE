package com.example.demo.service;

import com.example.demo.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    // 실제 User 엔티티 객체에 접근 가능
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getUserId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 리스트를 반환 (예: ROLE_USER, ROLE_ADMIN 등)
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
