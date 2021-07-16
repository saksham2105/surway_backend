package com.example.appengine.repository;

import com.example.appengine.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    Order findByRazorpayOrderId(String orderId);

}
