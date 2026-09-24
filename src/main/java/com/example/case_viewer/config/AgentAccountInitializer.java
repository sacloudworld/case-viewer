package com.example.case_viewer.config;

import com.example.case_viewer.entity.User;
import com.example.case_viewer.repository.UserRepository;
import com.example.case_viewer.service.UserCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates an agent account (role AGENT) on startup when app.agent.username/password are set.
 * Public registration always creates customers (role USER), so agents are provisioned here.
 * An existing user with that name is left untouched.
 */
@Component
@ConditionalOnProperty(name = "app.agent.username")
public class AgentAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;
    private final String username;
    private final String password;

    public AgentAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserCacheService userCacheService,
            @Value("${app.agent.username}") String username,
            @Value("${app.agent.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCacheService = userCacheService;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUsername(username).ifPresentOrElse(
                existing -> {
                    if (!"AGENT".equals(existing.getRole())) {
                        log.warn("User {} exists with role {}; not converting it to an agent",
                                username, existing.getRole());
                    }
                },
                () -> {
                    User agent = new User();
                    agent.setUsername(username);
                    agent.setPassword(passwordEncoder.encode(password));
                    agent.setRole("AGENT");
                    agent.setEnabled(true);
                    userCacheService.cacheUser(userRepository.save(agent));
                    log.info("Created agent account {}", username);
                });
    }
}
