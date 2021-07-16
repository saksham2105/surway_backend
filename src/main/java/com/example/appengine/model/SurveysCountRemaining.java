package com.example.appengine.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
@Data
@Document(collection = "Surveys_Count_Remaining")
public class SurveysCountRemaining {
    @Id
   private String userMail;
   private Integer surveysRemaining;
   public SurveysCountRemaining()
   {
     this.userMail="";
     this.surveysRemaining=0;
   }
}
