package com.example.appengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Getter
@Setter
@ToString


@Document(collection = "Template")
public class Template {

    @Id
    private String id;
    private String color;
    private String surveyCategory;
    private List<Question> questions;

    public Template(){
        this.id="";
        this.color="";
        this.surveyCategory="";
        this.questions=null;
    }
}
