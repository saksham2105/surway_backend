package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@ToString
public class AssignedSurveys {
    private String fromUser;
    private String surveyId;
    private List<String> toUsers;

    public AssignedSurveys() {
        this.fromUser = "";
        this.surveyId = "";
        this.toUsers = null;
    }
}
