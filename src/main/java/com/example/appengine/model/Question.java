package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class Question {
    private QuestionCategory questionCategory;
    private String title;
    private List<String> expectedAnswers;
    private String expectedTime;
    private Boolean isMandatory;
    private Integer sliderMinValue;
    private Integer sliderMaxValue;
    private List<String> options;

    public Question() {
        this.questionCategory = null;
        this.title = "";
        this.sliderMaxValue = 0;
        this.sliderMinValue = 0;
        this.expectedAnswers = null;
        this.expectedTime = "";
        this.isMandatory = false;
        this.options = null;
    }
}
