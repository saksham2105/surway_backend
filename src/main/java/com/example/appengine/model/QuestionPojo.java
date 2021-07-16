package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class QuestionPojo {
     private  String email;
     private List<ResponseTime> responseTimeList;

     public QuestionPojo(){
         this.email="";
         this.responseTimeList=null;
     }
}
