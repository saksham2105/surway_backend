package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


//customer-satisfaction
//education
//events
//human-resource
//research

@Setter
@Getter
@ToString
@Document(collection = "Survey_Category")
public class SurveyCategory {
    @Id
    private String id;
    private String categoryName;


    public SurveyCategory() {
        this.id = "";
        this.categoryName = "";
    }
}
