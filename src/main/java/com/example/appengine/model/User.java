package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
@Document(collection = "User")
//by specifying setters and getters annotation of lombok we don't need to create setters and getters
public class User {
    @Id
    private String id;
    private String firstName;
    private String secondName;
    private String email;
    private String password;
    private String passwordKey;
    private boolean verified;
    private Integer huCoins;
    private String contact;
    private boolean subscribed;
    private Integer subscriptionDetails;
    private String registeredDate;
    private String imageString;
    private List<Collaborator> collaborators;

    //default constructor
    public User() {
        this.id = "";
        this.firstName = "";
        this.secondName = "";
        this.email = "";
        this.passwordKey = "";
        this.huCoins = 0;
        this.contact = "";
        this.subscribed = false;
        this.subscriptionDetails = null;
        this.verified = false;
        this.collaborators = null;
        this.registeredDate = "";
    }
}
