package com.example.appengine.utility;

import com.example.appengine.model.Question;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TemplateDummyPojo {
    private String id;
    private String color;
    private String surveyCategory;
    private List<Question> questions;
    private Boolean status;
    public TemplateDummyPojo(){
        this.id="";
        this.color="";
        this.surveyCategory="";
        this.questions=null;
        this.status=false;
    }

}
