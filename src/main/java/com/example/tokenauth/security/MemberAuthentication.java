package com.example.tokenauth.security;

import com.example.tokenauth.domain.entity.Role;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MemberAuthentication extends AbstractAuthenticationToken {

    private final Long memberId;

    public MemberAuthentication(Long memberId, List<Role> roles) {
        super(authorities(roles));
        this.memberId = memberId;
    }

    private static List<? extends GrantedAuthority> authorities(List<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Long getPrincipal() {
        return memberId;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
