package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResetPassword {
    String email;
    String password;
}


