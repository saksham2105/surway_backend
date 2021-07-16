package com.example.appengine.repository;

import com.example.appengine.model.Survey;
import com.example.appengine.model.SurveyResponses;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyResponsesRepository extends MongoRepository<SurveyResponses, String> {
}
