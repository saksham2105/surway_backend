package com.example.appengine.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@ToString

@Document(collection = "Survey")
public class Survey {
    @Id
    private String id;
    private String name;
    private String surveyCategory;
    private String userEmail;
    private String password;
    private String passwordKey;
    private Boolean hasPassword;
    private String timestamp;
    private Boolean active;
    private String colorCode;
    private List<Question> questions;
    private List<String> allowedUsers;

    public Survey() {
        this.allowedUsers = null;
        this.questions = null;
        this.id = "";
        this.name = "";
        this.active = false;
        this.colorCode="";
        this.hasPassword = false;
        this.userEmail = "";
        this.password = "";
        //this.user=null;
        this.surveyCategory = "";
        this.passwordKey = "";
    }

    @Override
    public String toString() {
        return this.name + "," + this.surveyCategory + "," + this.userEmail + "," + this.password + "," +
                this.hasPassword + ",";
    }
}


