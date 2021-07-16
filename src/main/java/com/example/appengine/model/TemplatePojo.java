package com.example.appengine.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TemplatePojo {
    private Template template;
    private Boolean status;

    public TemplatePojo(){
        this.template=null;
        this.status=false;
    }
}
