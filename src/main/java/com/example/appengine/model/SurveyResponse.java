package com.example.appengine.model;

import com.example.appengine.utility.SurveyAnswer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class SurveyResponse {
//    private List<Question> questions;
    private String userMail;
    List<SurveyAnswer> surveyAnswers;
    private String actualTimeTaken;
    private String timestamp;

    public SurveyResponse() {
  //      this.questions = null;
//        this.actualAnswers = null;
        this.surveyAnswers=null;
        this.actualTimeTaken = "";
        this.userMail = "";
        this.timestamp="";
    }
}
