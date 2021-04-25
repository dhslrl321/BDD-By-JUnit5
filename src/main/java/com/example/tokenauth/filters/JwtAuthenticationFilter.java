package com.example.tokenauth.filters;

import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.security.MemberAuthentication;
import com.example.tokenauth.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final AuthenticationService authenticationService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   AuthenticationService authenticationService) {
        super(authenticationManager);
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authorization = request.getHeader("Authorization");

        if(authorization != null) {
            String accessToken = authorization.substring("Bearer ".length());

            Long memberId = authenticationService.parseToken(accessToken);
            List<Role> roles = authenticationService.roles(memberId);

            Authentication memberAuthentication = new MemberAuthentication(memberId, roles);

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(memberAuthentication);
        }

        chain.doFilter(request, response);
    }
}
