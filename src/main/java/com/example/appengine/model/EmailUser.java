package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailUser {
    private String to;
    private String otp;
    private String subject;
    private String message;
}
