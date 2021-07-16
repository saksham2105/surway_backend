package com.example.appengine.repository;
import com.example.appengine.model.History;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface HistoryRepository extends MongoRepository<History,String> {
}
