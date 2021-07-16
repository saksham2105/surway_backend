package com.example.appengine.utility;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SurveyCategoryResponse
{
private String surveyId;
private String surveyCategory;
private Integer views;
private Integer responses;
public SurveyCategoryResponse()
{
 this.surveyId="";
 this.surveyCategory="";
 this.views=0;
 this.responses=0;
}
}
