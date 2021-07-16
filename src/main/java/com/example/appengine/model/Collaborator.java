package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Collaborator {
    private String email;
    private String id;
    private boolean verified;

    public Collaborator() {
        this.email = "";
        this.id = "";
        this.verified = false;
    }
}
