package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TemplateList {

    private List<TemplatePojo> templatePojoList;
    private String categoryName;

    public  TemplateList(){
        this.templatePojoList = null;
        this.categoryName="";
    }

}
