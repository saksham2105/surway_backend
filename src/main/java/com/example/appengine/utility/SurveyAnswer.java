package com.example.appengine.utility;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SurveyAnswer
{
 private int questionNumber;
 private String question;
 //private List<String> options;
 private double timeTaken;
 private List<String> answers; //it is list because one question can have multiple options
 public SurveyAnswer()
 {
  this.questionNumber=0;
  this.question="";
  this.timeTaken=0.0;
  this.answers=null;
 }
}
