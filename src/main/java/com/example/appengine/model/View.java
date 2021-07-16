package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@ToString
@Document(collection = "View")
public class View {
    @Id
    private String surveyId;
    private Integer viewCount;

    public View() {
        this.surveyId = "";
        this.viewCount = 0;
    }
}
