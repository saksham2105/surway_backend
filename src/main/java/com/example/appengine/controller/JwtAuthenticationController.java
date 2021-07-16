package com.example.appengine.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.io.File;

import com.example.appengine.model.User;
import com.example.appengine.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.appengine.services.JwtUserDetailsService;
import com.example.appengine.config.JwtTokenUtil;
import com.example.appengine.model.JwtRequest;
import com.example.appengine.model.JwtResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

//    public byte[] contentOf(String filename) throws Exception {
//        Path path = Paths.get("./" + filename);
//        byte[] data = Files.readAllBytes(path);
//        return data;
//    }
//
//
//    @RequestMapping(path = "/download/{filename}", method = RequestMethod.GET)
//    public String downloadFile(@PathVariable String filename, HttpServletResponse response) throws Exception {
//        String fileName = filename;
//        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
//        response.getOutputStream().write(contentOf(fileName));
//        return "File Downloaded";
//    }

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportToPdf(HttpServletResponse response) throws IOException {

        List<User> users = this.userService.getAllUsers();
        ByteArrayInputStream bais = this.userService.exportPdf(users);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "attachment; filename=users.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bais));
    }

    @GetMapping("/getAllUsers")
    public List<User> getAllusers() {
        return this.userService.getAllUsers();
    }


    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @GetMapping("/_ah/start")
    public String start() {
        return "Everything working";
    }


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}