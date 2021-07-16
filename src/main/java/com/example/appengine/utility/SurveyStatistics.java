package com.example.appengine.utility;

import com.example.appengine.wrapper.ResponseWrapper;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SurveyStatistics {
    private String surveyId;
    private Double averageTimeTaken;



}
