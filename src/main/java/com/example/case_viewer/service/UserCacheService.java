package com.example.case_viewer.service;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.case_viewer.entity.User;
import com.example.case_viewer.repository.UserRepository;
import com.example.case_viewer.cache.CachedUser;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cache.annotation.CachePut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserCacheService {

    private static final Logger log =
    LoggerFactory.getLogger(UserCacheService.class);


   

    private final UserRepository userRepository;

    private final AtomicInteger dbHits = new AtomicInteger();

    public UserCacheService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users", key = "#username")
    public CachedUser getUser(String username) {

      
        Integer count = dbHits.incrementAndGet();
        log.info("CACHE MISS / DB HIT - username={}, dbHits={}",
            username, count);

            log.info("CACHE MISS / DATABASE HIT - username={}", username);    

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username));
        
                                       
                                        

        return new CachedUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isEnabled()
        );
    }


    @CachePut(value = "users", key = "#user.username")
    public CachedUser cacheUser(User user) {


        log.info("CACHE PUT - username={}", user.getUsername());

        return new CachedUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isEnabled()
        );
    }
}