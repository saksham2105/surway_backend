package com.example.appengine.repository;

import com.example.appengine.model.AssignedSurveys;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignedSurveysRepository extends MongoRepository<AssignedSurveys, String> {
}
