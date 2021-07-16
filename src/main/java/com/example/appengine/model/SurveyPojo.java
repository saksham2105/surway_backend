package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

@Setter
@Getter
@ToString
public class SurveyPojo {
    private String id;
    private String name;
    private String surveyCategory;
    private String userEmail;
    private String password;
    private String colorCode;
    private String passwordKey;
    private Boolean hasPassword;
    private String timestamp;
    private Boolean active;
    private List<Question> questions;
    private List<String> allowedUsers;
    private Integer views;
    private SurveyResponses surveyResponses;

    public SurveyPojo() {
        this.allowedUsers = null;
        this.questions = null;
        this.id = "";
        this.name = "";
        this.active = false;
        this.colorCode="";
        this.views = 0;
        this.surveyResponses = null;
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
