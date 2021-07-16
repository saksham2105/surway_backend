package com.example.appengine.wrapper;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
//by specifying setter and getter annotation of lombok we don't need to create setters and getters
public class ResponseWrapper
{
private boolean success;
private Object message;
public int responseCode;
public boolean hasError;
public boolean hasException;
}
