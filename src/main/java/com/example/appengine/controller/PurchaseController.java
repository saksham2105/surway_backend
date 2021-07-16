package com.example.appengine.controller;

import com.example.appengine.model.*;
import com.example.appengine.repository.*;
import com.example.appengine.services.UserService;
import com.example.appengine.utility.Tracking;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/purchase")
public class PurchaseController {
    @Autowired
    private PuchasedTransactionRepository puchasedTransactionRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Autowired
    private GroupsCountRemainingRepository groupsCountRemainingRepository;
    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @GetMapping("/addToPurchaseList/{userMail}/{type}/{genericId}/{purchaseName}")
    public ResponseWrapper addToPurchaseList(@PathVariable String userMail, @PathVariable String type, @PathVariable String genericId, @PathVariable String purchaseName) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        PurchaseTransaction purchaseTransaction = new PurchaseTransaction();
        try {
            //logic to decrease hu coins based in conditions
            if (type.equals("survey")) {
                User u = new User();
                u.setEmail(userMail);
                User user = this.userService.getUserByMail(u);
                if (user == null) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("Invalid User Mail");
                    return responseWrapper;
                }
                if (user.getHuCoins() < 2) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("You have insufficient HU coins left in your account to buy survey");
                    return responseWrapper;
                }
                purchaseTransaction.setPurchaseType(type);
                purchaseTransaction.setUserMail(userMail);
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                purchaseTransaction.setTimestamp(timestamp.toString());
                purchaseTransaction.setTransactionId(UUID.randomUUID().toString());
                purchaseTransaction.setPurchaseTypeId(genericId);
                purchaseTransaction.setHuCoinsUsed(2);
                purchaseTransaction.setStatus("unlocked");
                purchaseTransaction.setPurchaseName(purchaseName);
                this.puchasedTransactionRepository.save(purchaseTransaction);
                u = user;
                //logic to decrement hu coins
                u.setHuCoins(user.getHuCoins() - 2);
                this.userRepository.save(u);
                //logic to increment survey count remaining
                SurveysCountRemaining surveysCountRemaining = null;
                int previousSurveysRemaining = 0;
                if (this.surveysCountRemainingRepository.findAll() != null) {
                    for (SurveysCountRemaining scr : this.surveysCountRemainingRepository.findAll()) {
                        if (scr.getUserMail().equals(userMail)) {
                            previousSurveysRemaining = scr.getSurveysRemaining();
                            surveysCountRemaining = scr;
                            break;
                        }
                    }
                }
                if (surveysCountRemaining != null) {
                    surveysCountRemaining.setSurveysRemaining(previousSurveysRemaining + 1);
                    surveysCountRemaining.setUserMail(userMail);
                    this.surveysCountRemainingRepository.save(surveysCountRemaining);
                    responseWrapper.setSuccess(true);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage(purchaseTransaction);

                    return responseWrapper;
                }
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("Transaction couldn't perform since there is no survey remaining in your account");
                return responseWrapper;
                //2
            }

            if (type.equals("group")) {
                User u = new User();
                u.setEmail(userMail);
                User user = this.userService.getUserByMail(u);
                if (user == null) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("Invalid User Mail");
                    return responseWrapper;
                }
                if (user.getHuCoins() < 5) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("You have insufficient HU coins left in your account to buy survey");
                    return responseWrapper;
                }
                purchaseTransaction.setPurchaseType(type);
                purchaseTransaction.setUserMail(userMail);
                purchaseTransaction.setTransactionId(UUID.randomUUID().toString());
                purchaseTransaction.setPurchaseTypeId(genericId);
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                purchaseTransaction.setTimestamp(timestamp.toString());
                purchaseTransaction.setHuCoinsUsed(5);
                purchaseTransaction.setPurchaseName(purchaseName);
                purchaseTransaction.setStatus("unlocked");
                this.puchasedTransactionRepository.save(purchaseTransaction);
                u = user;
                //logic to decrement hu coins
                u.setHuCoins(user.getHuCoins() - 5);
                this.userRepository.save(u);
                GroupsCountRemaining groupsCountRemaining = null;
                int previousGroupsRemaining = 0;
                if (this.groupsCountRemainingRepository.findAll() != null) {
                    for (GroupsCountRemaining gcr : this.groupsCountRemainingRepository.findAll()) {
                        if (gcr.getUserMail().equals(userMail)) {
                            previousGroupsRemaining = gcr.getGroupsRemaining();
                            groupsCountRemaining = gcr;
                            break;
                        }
                    }
                }
                if (groupsCountRemaining != null) {
                    groupsCountRemaining.setGroupsRemaining(previousGroupsRemaining + 1);
                    groupsCountRemaining.setUserMail(userMail);
                    this.groupsCountRemainingRepository.save(groupsCountRemaining);
                    responseWrapper.setSuccess(true);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage(purchaseTransaction);
                    return responseWrapper;
                }
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("Transaction couldn't perform since there is no groups remaining in your account");
                return responseWrapper;
                //5
            }
            if (type.equals("template")) {
                User u = new User();
                u.setEmail(userMail);
                User user = this.userService.getUserByMail(u);
                if (user == null) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("Invalid User Mail");
                    return responseWrapper;
                }
                boolean transactionExist = false;
                if (this.puchasedTransactionRepository.findAll() != null) {
                    for (PurchaseTransaction pc : this.puchasedTransactionRepository.findAll()) {
                        if (pc.getUserMail().equals(userMail) && pc.getPurchaseType().equals("template") && pc.getPurchaseTypeId().equals(genericId)) //generic id will be survey id
                        {
                            transactionExist = true;
                            break;
                        }
                    }
                }
                if (transactionExist) {
                    responseWrapper.setSuccess(true);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage("This Transaction already exist against this user");
                    return responseWrapper;
                }
                if (user.getHuCoins() < 2) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("You have insufficient HU coins left in your account to buy template");
                    return responseWrapper;
                }
                purchaseTransaction.setPurchaseType(type);
                purchaseTransaction.setUserMail(userMail);
                purchaseTransaction.setTransactionId(UUID.randomUUID().toString());
                purchaseTransaction.setPurchaseTypeId(genericId);
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                purchaseTransaction.setTimestamp(timestamp.toString());
                purchaseTransaction.setHuCoinsUsed(2);
                purchaseTransaction.setStatus("unlocked");
                purchaseTransaction.setPurchaseName(purchaseName);
                this.puchasedTransactionRepository.save(purchaseTransaction);
                u = user;
                //logic to decrement hu coins
                u.setHuCoins(user.getHuCoins() - 2);
                this.userRepository.save(u);
                //logic to increment survey count remaining
                SurveysCountRemaining surveysCountRemaining = null;
                int previousSurveysRemaining = 0;
                if (this.surveysCountRemainingRepository.findAll() != null) {
                    for (SurveysCountRemaining scr : this.surveysCountRemainingRepository.findAll()) {
                        if (scr.getUserMail().equals(userMail)) {
                            previousSurveysRemaining = scr.getSurveysRemaining();
                            surveysCountRemaining = scr;
                            break;
                        }
                    }
                }
                if (surveysCountRemaining != null) {
                    surveysCountRemaining.setSurveysRemaining(previousSurveysRemaining + 1);
                    surveysCountRemaining.setUserMail(userMail);
                    this.surveysCountRemainingRepository.save(surveysCountRemaining);
                    responseWrapper.setSuccess(true);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage(purchaseTransaction);
                    return responseWrapper;
                }
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("Transaction couldn't perform since there is no survey remaining in your account");
                return responseWrapper;
                //2
            }
            if (type.equals("report")) {
                //check this transaction already exist
                User u = new User();
                u.setEmail(userMail);
                User user = this.userService.getUserByMail(u);
                if (user == null) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("Invalid User Mail");
                    return responseWrapper;
                }
                boolean transactionExist = false;
                if (this.puchasedTransactionRepository.findAll() != null) {
                    for (PurchaseTransaction pc : this.puchasedTransactionRepository.findAll()) {
                        if (pc.getUserMail().equals(userMail) && pc.getPurchaseType().equals("report") && pc.getPurchaseTypeId().equals(genericId)) //generic id will be survey id
                        {
                            transactionExist = true;
                            break;
                        }
                    }
                }
                if (transactionExist) {
                    responseWrapper.setSuccess(true);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage("This Transaction already exist against this user");
                    return responseWrapper;
                }
                if (user.getHuCoins() < 5) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("You have insufficient HU coins left in your account to buy reports");
                    return responseWrapper;
                }
                purchaseTransaction.setPurchaseType(type);
                purchaseTransaction.setUserMail(userMail);
                purchaseTransaction.setTransactionId(UUID.randomUUID().toString());
                purchaseTransaction.setPurchaseTypeId(genericId);
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                purchaseTransaction.setTimestamp(timestamp.toString());
                purchaseTransaction.setHuCoinsUsed(5);
                purchaseTransaction.setStatus("unlocked");
                purchaseTransaction.setPurchaseName(purchaseName);
                this.puchasedTransactionRepository.save(purchaseTransaction);
                //reduce hu coins by 5
                u = user;
                //logic to decrement hu coins
                u.setHuCoins(user.getHuCoins() - 5);
                this.userRepository.save(u);
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("Transaction performed successfully against transaction id : " + purchaseTransaction.getTransactionId());
                return responseWrapper;
                //5
            }
            if (type.equals("public_launch")) {
                User u = new User();
                u.setEmail(userMail);
                User user = this.userService.getUserByMail(u);
                if (user == null) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("Invalid User Mail");
                    return responseWrapper;
                }
//                boolean transactionExist = false;
//                if (this.puchasedTransactionRepository.findAll() != null) {
//                    for (PurchaseTransaction pc : this.puchasedTransactionRepository.findAll()) {
//                        if (pc.getUserMail().equals(userMail) && pc.getPurchaseType().equals("public_launch") && pc.getPurchaseTypeId().equals(genericId)) //generic id will be survey id
//                        {
//                            transactionExist = true;
//                            break;
//                        }
//                    }
//                }
//                if (transactionExist) {
//                    responseWrapper.setSuccess(true);
//                    responseWrapper.setResponseCode(200);
//                    responseWrapper.setHasException(false);
//                    responseWrapper.setHasError(false);
//                    responseWrapper.setMessage("This Transaction already exist against this user");
//                    return responseWrapper;
//                }
                if (user.getHuCoins() < 10) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("You have insufficient HU coins left in your account to launch public surveys");
                    return responseWrapper;
                }
                purchaseTransaction.setPurchaseType(type);
                purchaseTransaction.setUserMail(userMail);
                purchaseTransaction.setTransactionId(UUID.randomUUID().toString());
                purchaseTransaction.setPurchaseTypeId(genericId);
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                purchaseTransaction.setTimestamp(timestamp.toString());
                purchaseTransaction.setHuCoinsUsed(10);
                purchaseTransaction.setPurchaseName(purchaseName);
                purchaseTransaction.setStatus("unlocked");
                this.puchasedTransactionRepository.save(purchaseTransaction);
                //reduce hu coins by 10
                u = user;
                //logic to decrement hu coins
                u.setHuCoins(user.getHuCoins() - 10);
                this.userRepository.save(u);
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage(purchaseTransaction);
                return responseWrapper;
                //10
            }
        } catch (Exception exception) {
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
        }
        return null;
    }

    @GetMapping("/getTemplatesByUserMail/{userMail}")
    public ResponseWrapper getTemplatesByUserMail(@PathVariable String userMail) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (userMail == null) {
            responseWrapper.setResponseCode(400);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Please provide valid User E-mail");
            return responseWrapper;
        }

        List<PurchaseTransaction> purchaseTransactionList = new ArrayList<>();
        //here we are filtering our purchase list on basis of userMail and purchaseType
        for (PurchaseTransaction pt : this.puchasedTransactionRepository.findAll()) {
            if (pt.getUserMail().equals(userMail) && pt.getPurchaseType().equals("template")) {
                purchaseTransactionList.add(pt);
            }
        }

        String category[] = {"hr", "event", "education", "customer", "research"}; //our category names
        List<TemplateList> templateListList = new ArrayList<>();  //list of templateList object
        List<Template> templateList1 = this.templateRepository.findAll();
        for (int i = 0; i < category.length; i++) {

            List<TemplatePojo> templatePojoList = new ArrayList<>(); //list of templatePojo object

            for (PurchaseTransaction pt : purchaseTransactionList) {
                String id = pt.getPurchaseTypeId();
                //finding template using above Id
                Template template = new Template();
                for (Template t : templateList1) {
                    if (t.getId().equals(id)) {
                        template = t;
                        break;
                    }
                }
                template.setQuestions(null);


                // here we are checking if template category is same as category we are looking right now and
                // we have buyed this template before
                if (template.getSurveyCategory().equals(category[i]) && template.getId().equals(pt.getPurchaseTypeId())) {
                    TemplatePojo templatePojo = new TemplatePojo(); //creating templatePjo object
                    templatePojo.setTemplate(template);
                    templatePojo.setStatus(true); //if we
                    templatePojoList.add(templatePojo); //adding templatePjo object in list of templatePojo object.
                } else {
                    TemplatePojo templatePojo = new TemplatePojo(); //creating templatePjo object
                    templatePojo.setTemplate(template);
                    templatePojo.setStatus(false); //if we
                    templatePojoList.add(templatePojo);
                }
            }

            TemplateList templateList = new TemplateList(); //creating templateList object here
            templateList.setTemplatePojoList(templatePojoList); //adding templatePojo list in templatList object
            templateList.setCategoryName(category[i]);
            templateListList.add(templateList);
        }

        if (templateListList.size() == 0) {
            responseWrapper.setResponseCode(400);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("You have not buyed any template please buy one! 😉");
            return responseWrapper;

        }
        responseWrapper.setResponseCode(200);
        responseWrapper.setSuccess(true);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage(templateListList);
        return responseWrapper;

    }

    @GetMapping("/getHuCoinsTimeline/{mail}")
    public ResponseWrapper getHuCoinsTimeline(@PathVariable String mail) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        List<PurchaseTransaction> purchaseTransactionList = new ArrayList<>();
        for (PurchaseTransaction pt : this.puchasedTransactionRepository.findAll()) {
            if (pt.getUserMail().equals(mail)) {
                purchaseTransactionList.add(pt);
            }
        }
        if (purchaseTransactionList == null || purchaseTransactionList.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No transaction exist against this user");
            return responseWrapper;
        } else {
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(purchaseTransactionList);
            return responseWrapper;
        }
    }
}
