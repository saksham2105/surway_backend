package com.example.appengine.repository;

import com.example.appengine.model.GroupsCountRemaining;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsCountRemainingRepository extends MongoRepository<GroupsCountRemaining,String> {
}
