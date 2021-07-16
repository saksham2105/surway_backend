package com.example.appengine.utility;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class CategoryWiseSurvey {
    private String category;
    private Integer surveyCount;
    public CategoryWiseSurvey()
    {
     this.category="";
     this.surveyCount=0;
    }
}
