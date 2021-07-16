package com.example.appengine.repository;

import com.example.appengine.model.SurveyCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyCategoryRepository extends MongoRepository<SurveyCategory, String> {

}
