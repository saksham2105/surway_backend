package com.example.appengine.model;

import lombok.Data;

@Data
public class OrderRequest {
    private String customerName;
    private String plan;
    private String email;
    private String phoneNumber;
    private String amount;
}
