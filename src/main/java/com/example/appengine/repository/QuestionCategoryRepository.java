package com.example.appengine.repository;

import com.example.appengine.model.QuestionCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionCategoryRepository extends MongoRepository<QuestionCategory, String> {
}
