package com.example.appengine.services;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("surwayToolApplication@907785".equals(username)) {
            //pass - SurWayToolDevelopers@82250
            return new User("surwayToolApplication@907785", "$2a$10$pwCwdBBsgVyCp2Hz4N19E.3xKCnHpGKmuls1kLWE7Q/FQpdtdJEY.",
                    new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}