package com.example.appengine.repository;

import com.example.appengine.model.SurveysCountRemaining;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveysCountRemainingRepository extends MongoRepository<SurveysCountRemaining,String>
{
}
