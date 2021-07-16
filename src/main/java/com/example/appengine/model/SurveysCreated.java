package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@ToString
@Document(collection = "Surveys_Created")
public class SurveysCreated {
    @Id
    private String userMail;
    private Integer countOfSurveys;

    public SurveysCreated() {
        this.userMail = "";
        this.countOfSurveys = 0;
    }
}
