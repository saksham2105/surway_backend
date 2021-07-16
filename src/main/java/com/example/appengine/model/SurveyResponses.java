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
@Document(collection = "Survey_Responses")
public class SurveyResponses {
    @Id
    private String surveyId;
    private String lastResponseTime;
    private List<SurveyResponse> surveyResponseList;

    public SurveyResponses() {
        this.surveyId = "";
        this.surveyResponseList = null;
    }
}
