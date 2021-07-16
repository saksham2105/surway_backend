package com.example.appengine.repository;

import com.example.appengine.model.SurveysCreated;
import com.example.appengine.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveysCreatedRepository extends MongoRepository<SurveysCreated, User> {
}
