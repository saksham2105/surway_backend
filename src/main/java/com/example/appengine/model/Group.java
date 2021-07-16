package com.example.appengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@ToString
@Document(collection = "Groups")
public class Group {
    @Id
    private String id;
    private String userMail;
    private String name;
    private String timestamp;
    private List<String> members;

    public Group() {
        this.id = "";
        this.userMail = "";
        this.name = "";
        this.timestamp = "";
        this.members = null;
    }
}
