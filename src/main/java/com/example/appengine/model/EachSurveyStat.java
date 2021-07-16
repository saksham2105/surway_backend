package com.example.appengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class EachSurveyStat {

    private Integer views;
    private Integer responses;
    private String lastResponseTime;
    private Double completionRate;
    private Double avgSurveyTime;
    private String name;
    private String surveyCategory;
    private String surveyCreatedOn;
    private Boolean surveyStatus;
    private String reportStatus;
    private List<SurveyResponse> surveyResponses;

    public EachSurveyStat() {
        this.views = 0;
        this.responses = 0;
        this.name="";
        this.surveyCategory="";
        this.surveyCreatedOn="";
        this.surveyStatus=false;
        this.lastResponseTime = "";
        this.completionRate = 0.0;
        this.completionRate = 0.0;
        this.avgSurveyTime = 0.0;
        this.reportStatus="";
        this.surveyResponses = null;
    }

}

