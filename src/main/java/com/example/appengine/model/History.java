package com.example.appengine.model;

import com.example.appengine.utility.Tracking;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "History")
@ToString
@Data
public class History
{
  @Id
 private String userMail;
 private List<Tracking> trackingList;
}
