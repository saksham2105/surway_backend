package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString

public class Participant {

  private List<QuestionPojo> questionTime;

  public Participant(){
    this.questionTime=null;
  }
}
