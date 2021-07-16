package com.example.appengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@ToString
@NoArgsConstructor

@Document(collection = "Order")
public class Order {
    @Id
    private String id;
    private Integer amount;
    private String userMail;
    private String razorpayPaymentId;
    private String plan;
    private String razorpayOrderId;
    private String timestamp;
    private String razorpaySignature;

}

