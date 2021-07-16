package com.example.appengine.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Captcha {
    private String captchaCode;
    private String captchaImage;
}
