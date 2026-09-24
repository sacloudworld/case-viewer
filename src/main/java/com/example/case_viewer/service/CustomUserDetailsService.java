package com.example.case_viewer.service;



import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.case_viewer.cache.CachedUser;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

        private static final Logger log =
                LoggerFactory.getLogger(UserCacheService.class);

    private final UserCacheService userCacheService;

   

    public CustomUserDetailsService(
            UserCacheService userCacheService) {
        this.userCacheService = userCacheService;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        CachedUser user =
                userCacheService.getUser(username);
        
                log.info(
                        "USER RETRIEVED - username={}, role={}, enabled={}",
                        user.getUsername(),
                        user.getRole(),
                        user.isEnabled()
                );        
                    

               

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .disabled(!user.isEnabled())
                .build();
    }
}
