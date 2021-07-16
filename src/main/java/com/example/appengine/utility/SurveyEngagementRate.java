package com.example.appengine.utility;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SurveyEngagementRate {
    private String surveyId;
    private double engagementRate;
}