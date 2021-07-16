package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.*;
import com.example.appengine.repository.GroupsCountRemainingRepository;
import com.example.appengine.repository.OrderRepository;
import com.example.appengine.repository.SurveysCountRemainingRepository;
import com.example.appengine.repository.UserRepository;
import com.example.appengine.utility.Signature;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Autowired
    private GroupsCountRemainingRepository groupsCountRemainingRepository;
    private int huCoinsPerSurvey=2;

    @Transactional
    public Order saveOrder(String razorpayOrderId, OrderRequest orderRequest) throws Exception {
        User user = new User();
        user.setEmail(orderRequest.getEmail());
        if (userService.getUserByMail(user) == null) {
            System.out.println("Order can't be created as user doesn't present in the database");
            return null;
        }
        //check if user has some previous order then delete that order..
        Order order1 = null;
        List<Order> orders = this.orderRepository.findAll();
        List<Order> ordersToDelete = new ArrayList<>();
        if (orders.size() > 0) {
            for (Order o : orders) {
                if (o.getUserMail().equals(orderRequest.getEmail())) {
                    order1 = o;
                    ordersToDelete.add(o);
                }
            }
            String razorPaySignature = "";
            for (Order o : this.orderRepository.findAll()) {
                if (o.getUserMail().equals(orderRequest.getEmail())) {
                    razorPaySignature = o.getRazorpaySignature();
                }
            }
            if (order1 != null && (razorPaySignature == null || razorPaySignature.length() == 0)) {
                //deleting if order is not null
                for (Order o : ordersToDelete) this.orderRepository.delete(order1);
            }
        }
        Order order = new Order();
        order.setRazorpayOrderId(razorpayOrderId);
        order.setUserMail(orderRequest.getEmail());
        order.setPlan(orderRequest.getPlan());
        order.setAmount(Integer.parseInt(orderRequest.getAmount()));
        return orderRepository.save(order);
    }

    @Transactional
    public String validateAndUpdateOrder(final String razorpayOrderId, final String razorpayPaymentId, final String razorpaySignature, final String secret) {
        String errorMsg = null;
        try {
            Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId);
            // Verify if the razorpay signature matches the generated one to
            // confirm the authenticity of the details returned
            String generatedSignature = Signature.calculateRFC2104HMAC(order.getRazorpayOrderId() + "|" + razorpayPaymentId, secret);
            if (generatedSignature.equals(razorpaySignature)) {
                order.setRazorpayOrderId(razorpayOrderId);
                order.setRazorpayPaymentId(razorpayPaymentId);
                order.setRazorpaySignature(razorpaySignature);
                order.setPlan(order.getPlan());
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                order.setTimestamp(timestamp.toString());
                orderRepository.save(order);

                //update user subscription status

                User u = new User();
                u.setEmail(order.getUserMail());

                User user = this.userService.getUserByMail(u);


                if (user != null) {

                    //set plan in user modal
                    if (order.getPlan().equalsIgnoreCase("standard")) {
                        user.setSubscriptionDetails(1);
                        user.setHuCoins(user.getHuCoins()+100);
                        int previousSurveysCountRemaining=0;
                        //add 7 surveys in their account
                        if(this.surveysCountRemainingRepository.findAll()!=null)
                        {
                         for(SurveysCountRemaining surveysCountRemaining : this.surveysCountRemainingRepository.findAll())
                         {
                           if(surveysCountRemaining.getUserMail().equals(user.getEmail()))
                           {
                             previousSurveysCountRemaining=surveysCountRemaining.getSurveysRemaining();
                           }
                         }
                        }
                        if(user.isSubscribed()==false)
                        {
                          SurveysCountRemaining scr=new SurveysCountRemaining();
                          scr.setUserMail(user.getEmail());
                          scr.setSurveysRemaining(7);
                          this.surveysCountRemainingRepository.save(scr);
                        }
                        if(user.isSubscribed())
                        {
                            SurveysCountRemaining scr=new SurveysCountRemaining();
                            scr.setUserMail(user.getEmail());
                            scr.setSurveysRemaining(7+previousSurveysCountRemaining);
                            this.surveysCountRemainingRepository.save(scr);
                        }
                        int previousGroupsCountRemaining=0;
                        if(this.groupsCountRemainingRepository.findAll()!=null)
                        {
                           for(GroupsCountRemaining groupsCountRemaining : this.groupsCountRemainingRepository.findAll())
                           {
                             if(groupsCountRemaining.getUserMail().equals(user.getEmail()))
                             {
                                previousGroupsCountRemaining=groupsCountRemaining.getGroupsRemaining();
                             }
                           }
                        }
                        if(user.isSubscribed()==false)
                        {
                           GroupsCountRemaining gcr=new GroupsCountRemaining();
                           gcr.setUserMail(user.getEmail());
                           gcr.setGroupsRemaining(5);
                           this.groupsCountRemainingRepository.save(gcr);
                        }
                        if(user.isSubscribed())
                        {
                            GroupsCountRemaining gcr=new GroupsCountRemaining();
                            gcr.setUserMail(user.getEmail());
                            gcr.setGroupsRemaining(5+previousGroupsCountRemaining);
                            this.groupsCountRemainingRepository.save(gcr);
                        }
                        //add survey remaining to his account
                        //logic to add groups to his account
                    } else if (order.getPlan().equalsIgnoreCase("mega")) {
                        //add survey remaining to his account
                        user.setSubscriptionDetails(2);
                        user.setHuCoins(1000+user.getHuCoins());
                        int previousSurveysCountRemaining=0;
                        //add 7 surveys in their account
                        if(this.surveysCountRemainingRepository.findAll()!=null)
                        {
                            for(SurveysCountRemaining surveysCountRemaining : this.surveysCountRemainingRepository.findAll())
                            {
                                if(surveysCountRemaining.getUserMail().equals(user.getEmail()))
                                {
                                    previousSurveysCountRemaining=surveysCountRemaining.getSurveysRemaining();
                                }
                            }
                        }
                        if(user.isSubscribed()==false)
                        {
                            SurveysCountRemaining scr=new SurveysCountRemaining();
                            scr.setUserMail(user.getEmail());
                            scr.setSurveysRemaining(20);
                            this.surveysCountRemainingRepository.save(scr);
                        }
                        if(user.isSubscribed())
                        {
                            SurveysCountRemaining scr=new SurveysCountRemaining();
                            scr.setUserMail(user.getEmail());
                            scr.setSurveysRemaining(20+previousSurveysCountRemaining);
                            this.surveysCountRemainingRepository.save(scr);
                        }
                        int previousGroupsCountRemaining=0;
                        if(this.groupsCountRemainingRepository.findAll()!=null)
                        {
                            for(GroupsCountRemaining groupsCountRemaining : this.groupsCountRemainingRepository.findAll())
                            {
                                if(groupsCountRemaining.getUserMail().equals(user.getEmail()))
                                {
                                    previousGroupsCountRemaining=groupsCountRemaining.getGroupsRemaining();
                                }
                            }
                        }
                        if(user.isSubscribed()==false)
                        {
                            GroupsCountRemaining gcr=new GroupsCountRemaining();
                            gcr.setUserMail(user.getEmail());
                            gcr.setGroupsRemaining(10);
                            this.groupsCountRemainingRepository.save(gcr);
                        }
                        if(user.isSubscribed())
                        {
                            GroupsCountRemaining gcr=new GroupsCountRemaining();
                            gcr.setUserMail(user.getEmail());
                            gcr.setGroupsRemaining(10+previousGroupsCountRemaining);
                            this.groupsCountRemainingRepository.save(gcr);
                        }
                    }
                    if(user.isSubscribed()==false) user.setSubscribed(true);
                    this.userRepository.save(user);
                }
            } else {
                errorMsg = "Payment validation failed: Signature doesn't match";
            }
        } catch (Exception e) {
            log.error("Payment validation failed", e);
            errorMsg = e.getMessage();
        }
        return errorMsg;
    }

    @Transactional
    public List<Order> getOrdersFromMail(User user) throws SurwayException {
        Order order1 = null;
        List<Order> orders = this.orderRepository.findAll();
        List<Order> ordersToDelete = new ArrayList<>();
        //code to delete redundant blank orders if any
        if (orders.size() > 0) {
            for (Order o : orders) {
                if (o.getUserMail().equals(user.getEmail())) {
                    order1 = o;
                    ordersToDelete.add(o);
                }
            }
            String razorPaySignature = "";
            for (Order o : this.orderRepository.findAll()) {
                if (o.getUserMail().equals(user.getEmail())) {
                    razorPaySignature = o.getRazorpaySignature();
                }
            }
            if (order1 != null && (razorPaySignature == null || razorPaySignature.length() == 0)) {
                //deleting if order is not null
                for (Order o : ordersToDelete) this.orderRepository.delete(order1);
            }
        }
        List<Order> ordersToReturn = new ArrayList<>();
        for (Order o : orders) {
            if (o.getUserMail().equals(user.getEmail())) {
                ordersToReturn.add(o);
            }
        }
        return ordersToReturn;
    }
}
