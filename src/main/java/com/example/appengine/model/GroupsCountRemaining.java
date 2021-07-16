package com.example.appengine.model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@ToString
@Setter
@Getter
@Document(collection = "Groups_Count_Remaining")
public class GroupsCountRemaining {
    @Id
    private String userMail;
    private Integer groupsRemaining;
    public GroupsCountRemaining()
    {
      this.userMail="";
      this.groupsRemaining=0;
    }
}
