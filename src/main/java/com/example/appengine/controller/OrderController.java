package com.example.appengine.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.appengine.config.RazorPayClientConfig;
import com.example.appengine.model.*;
import com.example.appengine.repository.HistoryRepository;
import com.example.appengine.services.OrderService;
import com.example.appengine.utility.Tracking;
import com.example.appengine.wrapper.ResponseWrapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/api")

public class OrderController {

    private RazorpayClient client;

    private RazorPayClientConfig razorPayClientConfig;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private OrderService orderService;

    @Autowired
    public OrderController(RazorPayClientConfig razorpayClientConfig) throws RazorpayException {
        this.razorPayClientConfig = razorpayClientConfig;
        this.client = new RazorpayClient(razorpayClientConfig.getKey(), razorpayClientConfig.getSecret());
    }

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse razorPay = null;
        try {
            System.out.println(orderRequest.getPlan());
            // The transaction amount is expressed in the currency subunit, such
            // as paise (in case of INR)
            String amountInPaise = convertRupeeToPaise(orderRequest.getAmount());
            // Create an order in RazorPay and get the order id
            Order order = createRazorPayOrder(amountInPaise);
            razorPay = getOrderResponse((String) order.get("id"), amountInPaise);
            // Save order in the database
            if (orderService.saveOrder(razorPay.getRazorpayOrderId(), orderRequest) == null) {
                return new ResponseEntity<>(new ApiResponse(false, "User doesn't exist in the database"), HttpStatus.EXPECTATION_FAILED);
            }
            orderService.saveOrder(razorPay.getRazorpayOrderId(), orderRequest);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(new ApiResponse(false, "Error while create payment order: " + e.getMessage()), HttpStatus.EXPECTATION_FAILED);
        }
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        History history=new History();
        history.setUserMail(orderRequest.getEmail());
        List<Tracking> trackings=null;
        if(this.historyRepository.findAll()!=null)
        {
            for(History history1 : this.historyRepository.findAll())
            {
                if(history1.getUserMail().equals(orderRequest.getEmail()))
                {
                    trackings=history1.getTrackingList();
                    break;
                }
            }
        }
        if(trackings!=null)
        {
            Tracking tracking=new Tracking();
            tracking.setTimestamp(timestamp.toString());
            tracking.setActivity("User has placed order");
            trackings.add(tracking);
            history.setTrackingList(trackings);
            this.historyRepository.save(history);
        }
        else
        {
            Tracking tracking=new Tracking();
            tracking.setTimestamp(timestamp.toString());
            tracking.setActivity("User has placed order");
            history.setTrackingList(Arrays.asList(tracking));
            this.historyRepository.save(history);
        }
        return ResponseEntity.ok(razorPay);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody PaymentResponse paymentResponse) {
        String errorMsg = orderService.validateAndUpdateOrder(paymentResponse.getRazorpayOrderId(), paymentResponse.getRazorpayPaymentId(), paymentResponse.getRazorpaySignature(),
                razorPayClientConfig.getSecret());
        if (errorMsg != null) {
            return new ResponseEntity<>(new ApiResponse(false, errorMsg), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(new ApiResponse(true, paymentResponse.getRazorpayPaymentId()));
    }

    @PostMapping("/getOrdersFromMail")
    public ResponseWrapper getOrdersFromMail(@RequestBody User user) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            if (this.orderService.getOrdersFromMail(user) != null && this.orderService.getOrdersFromMail(user).size() > 0) {
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage(this.orderService.getOrdersFromMail(user));
                return responseWrapper;
            } else {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("No Order has been place by this user");
                return responseWrapper;
            }
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    private OrderResponse getOrderResponse(String orderId, String amountInPaise) {
        OrderResponse razorPay = new OrderResponse();
        razorPay.setApplicationFee(amountInPaise);
        razorPay.setRazorpayOrderId(orderId);
        razorPay.setSecretKey(razorPayClientConfig.getKey());
        return razorPay;
    }

    private Order createRazorPayOrder(String amount) throws RazorpayException {
        JSONObject options = new JSONObject();
        options.put("amount", amount);
        options.put("currency", "INR");
        options.put("receipt", "txn_123456");
        return client.Orders.create(options);
    }

    private String convertRupeeToPaise(String paise) {
        BigDecimal b = new BigDecimal(paise);
        BigDecimal value = b.multiply(new BigDecimal("100"));
        return value.setScale(0, RoundingMode.UP).toString();
    }
}