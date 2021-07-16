package com.example.appengine.repository;

import com.example.appengine.model.PurchaseTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuchasedTransactionRepository extends MongoRepository<PurchaseTransaction,String> {
}
