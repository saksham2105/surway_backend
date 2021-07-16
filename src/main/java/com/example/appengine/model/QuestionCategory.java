package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@ToString
@Document(collection = "Question_Category")
public class QuestionCategory {
    @Id
    private String id;
    private String categoryName;

    public QuestionCategory() {
        this.id = "";
        this.categoryName = "";
    }
}
