package com.example.appengine.utility;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class CategoryWiseSurveyViewsAndResponses {
    private String surveyCategory;
    private Integer views;
    private Integer numberOfResponses;
    public CategoryWiseSurveyViewsAndResponses()
    {
      this.surveyCategory="";
      this.views=0;
      this.numberOfResponses=0;
    }
}
