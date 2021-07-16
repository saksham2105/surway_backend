package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseTime {
    private Integer index;
    private Double  time;


    public ResponseTime(){
        this.index=0;
        this.time=0.0;
    }
}
