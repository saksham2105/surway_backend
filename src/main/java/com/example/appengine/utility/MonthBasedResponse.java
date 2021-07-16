package com.example.appengine.utility;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class MonthBasedResponse {
  private String month;
  private Integer views;
  private Integer responsesCount;
  public MonthBasedResponse()
  {
   this.month="";
   this.views=0;
   this.responsesCount=0;
  }
}
