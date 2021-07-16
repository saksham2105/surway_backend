package com.example.appengine.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
@Data
@Document(collection = "Purchase_Transaction")
public class PurchaseTransaction {
    @Id
    private String transactionId;
    private String userMail;
    private String purchaseType;
    private String purchaseTypeId;
    private String status; //refer locked or unlocked
    private Integer huCoinsUsed;
    private String timestamp;
    private String purchaseName;
    public PurchaseTransaction()
    {
        this.transactionId="";
        this.userMail="";
        this.timestamp="";
        this.purchaseName=null;
        this.purchaseType="";
        this.purchaseTypeId="";
        this.status="";
        this.huCoinsUsed=0;
    }
}
