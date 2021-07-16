package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.*;
import com.example.appengine.repository.*;
import com.example.appengine.utility.*;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SurveyService {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurveysCreatedRepository surveysCreatedRepository;
    @Autowired
    private SurveyRepository surveyRepository;
    @Autowired
    private ViewRepository viewRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private SurveyResponsesService surveyResponsesService;
    @Autowired
    private SurveyResponsesRepository surveyResponsesRepository;
    @Autowired
    private GroupsCountRemainingRepository groupsCountRemainingRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Autowired
    private AssignedSurveysRepository assignedSurveysRepository;
    private int huCoinsPerSurvey = 2;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private PuchasedTransactionRepository puchasedTransactionRepository;
    @Transactional
    public Survey getSurveyById(String surveyId) throws SurwayException {
        Survey survey = null;
        for (Survey s : this.surveyRepository.findAll()) {
            if (s.getId().equals(surveyId)) {
                survey = s;
                break;
            }
        }
        if (survey != null && survey.getHasPassword() && survey.getPassword() != null && survey.getPassword().length() > 0) {
            survey.setPassword(PasswordUtility.decrypt(survey.getPassword(), survey.getPasswordKey()));
            survey.setPasswordKey("");
        }
        return survey;
    }
    public ResponseWrapper isSurveyEnabled(String surveyId)
    {
       ResponseWrapper responseWrapper=new ResponseWrapper();
       if(this.surveyRepository.findAll()!=null)
       {
         boolean isSurveyExist=false;
        for(Survey s : this.surveyRepository.findAll())
        {
            if(s.getId().equals(surveyId))
            {
              isSurveyExist=true;
              break;
            }
        }
        if(!isSurveyExist)
        {
          responseWrapper.setSuccess(false);
          responseWrapper.setResponseCode(404);
          responseWrapper.setMessage("Invalid Survey Id");
          responseWrapper.setHasException(false);
          responseWrapper.setHasError(true);
          return responseWrapper;
        }
       }
       boolean isSurveyActive=false;
       for(Survey s: this.surveyRepository.findAll())
       {
         if(s.getId().equals(surveyId))
         {
            isSurveyActive=s.getActive();
         }
       }
       if(!isSurveyActive)
       {
           responseWrapper.setSuccess(false);
           responseWrapper.setResponseCode(200);
           responseWrapper.setHasError(false);
           responseWrapper.setHasException(false);
           responseWrapper.setMessage("This survey is not active");
           return responseWrapper;
       }
       else
       {
           responseWrapper.setSuccess(true);
           responseWrapper.setResponseCode(200);
           responseWrapper.setHasError(false);
           responseWrapper.setHasException(false);
           responseWrapper.setMessage("This survey is active");
           return responseWrapper;
       }
    }
    public boolean removeSurveyViews(String surveyId) throws SurwayException {
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0 || this.surveysCreatedRepository.findAll() == null || this.surveysCreatedRepository.findAll().size() == 0) {
            return false;
        }
        User user = null;
        Survey survey = this.getSurveyById(surveyId);
        if (survey == null) {
            return false;
        }
        if (this.viewRepository.findAll() == null || this.viewRepository.findAll().size() == 0) {
            return false;
        }
        View viewToDelete = null;
        for (View view : this.viewRepository.findAll()) {
            if (view.getSurveyId().equals(surveyId)) {
                viewToDelete = view;
                break;
            }
        }
        if (viewToDelete == null) return false;
        this.viewRepository.delete(viewToDelete);
        return true;
    }

    public boolean decrementSurveyCount(String surveyId) throws SurwayException {
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0 || this.surveysCreatedRepository.findAll() == null || this.surveysCreatedRepository.findAll().size() == 0) {
            return false;
        }
        User user = null;
        Survey survey = this.getSurveyById(surveyId);
        if (survey == null) {
            return false;
        }
        int surveyCount = 0;
        if (this.surveysCreatedRepository.findAll() == null || this.surveysCreatedRepository.findAll().size() == 0) {
            return false;
        }
        for (SurveysCreated sc : this.surveysCreatedRepository.findAll()) {
            if (sc.getUserMail().equals(survey.getUserEmail())) {
                surveyCount = sc.getCountOfSurveys();
            }
        }
        SurveysCreated surveysCreated = new SurveysCreated();
        surveysCreated.setUserMail(survey.getUserEmail());
        if (surveyCount == 0) surveysCreated.setCountOfSurveys(surveyCount);
        else {
            surveysCreated.setCountOfSurveys(surveyCount - 1);
        }
        this.surveysCreatedRepository.save(surveysCreated);
        return true;
    }

    @Transactional
    public ResponseWrapper deleteSurvey(String surveyId) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Survey exist in the database");
            return responseWrapper;
        }
        Survey survey = this.getSurveyById(surveyId);
        if (survey == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid Survey Id");
            return responseWrapper;
        }
        this.surveyRepository.delete(survey);
        this.decrementSurveyCount(surveyId);
        this.removeSurveyViews(surveyId);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage("Survey Removed Successfully");
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            History history=new History();
            history.setUserMail(survey.getUserEmail());
            List<Tracking> trackings=null;
            if(this.historyRepository.findAll()!=null)
            {
                for(History history1 : this.historyRepository.findAll())
                {
                    if(history1.getUserMail().equals(survey.getUserEmail()))
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
                tracking.setActivity("You have deleted a survey ");
                trackings.add(tracking);
                history.setTrackingList(trackings);
                this.historyRepository.save(history);
            }
            else
            {
                Tracking tracking=new Tracking();
                tracking.setTimestamp(timestamp.toString());
                tracking.setActivity("You have deleted a survey ");
                history.setTrackingList(Arrays.asList(tracking));
                this.historyRepository.save(history);
            }
        return responseWrapper;
    }

    public ResponseWrapper addSurvey(Survey survey) throws SurwayException {
        //do nothing,right now
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {

            List<User> allUsers = this.userService.getAllUsers();
            User user = null;
            if (allUsers.size() > 0) {
                for (User u : allUsers) {
                    if (survey.getUserEmail().equals(u.getEmail())) {
                        user = u;
                        break;
                    }
                }
            }
            if (user == null) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("User doesn't exist");
                return responseWrapper;

            }
            //get surveys Remaining of user;
            int surveyRemaining = 0;
            if (this.surveysCountRemainingRepository.findAll() != null) {
                for (SurveysCountRemaining surveysCountRemaining : this.surveysCountRemainingRepository.findAll()) {
                    if (surveysCountRemaining.getUserMail().equals(user.getEmail())) {
                        surveyRemaining = surveysCountRemaining.getSurveysRemaining();
                        break;
                    }
                }
            }

            //check if your has no surveys remaining
            if (surveyRemaining == 0) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("No surveys has left in your account Spend Hu coins to get surveys");
                return responseWrapper;
            }

            //if surveys remainining is greater than zero
            if (survey.getPassword() == null || survey.getPassword().trim().length() == 0) {
                survey.setPassword("");
                survey.setPasswordKey("");
            } else {
                String passwordKey = UUID.randomUUID().toString();
                passwordKey = passwordKey.replaceAll("-", "");
                survey.setPasswordKey(passwordKey);
                survey.setPassword(PasswordUtility.encrypt(survey.getPassword(), passwordKey));
            }
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            survey.setTimestamp(timestamp.toString());
            String surveyId = "Surway_by_" + survey.getUserEmail() + UUID.randomUUID().toString().replaceAll("-", "").substring(5);
            survey.setId(surveyId);
            survey.setActive(false);
            this.surveyRepository.save(survey);

            //increase survey count of user by 1
            int surveysCount = 0;
            SurveysCreated surveysCreated = new SurveysCreated();
            List<SurveysCreated> surveysCreatedList1 = this.surveysCreatedRepository.findAll();

            if (surveysCreatedList1.size() > 0) {
                for (SurveysCreated x : surveysCreatedList1) {
                    if (x.getUserMail().length() != 0 && x.getUserMail().equals(survey.getUserEmail())) {
                        surveysCount = x.getCountOfSurveys();
                        break;
                    }
                }
            }

            surveysCount += 1;
            surveysCreated.setUserMail(user.getEmail());
            surveysCreated.setCountOfSurveys(surveysCount);
            this.surveysCreatedRepository.save(surveysCreated);
            //decrement surveys remaining and hu coins if any
            SurveysCountRemaining scr = null;
            for (SurveysCountRemaining surveysCountRemaining : this.surveysCountRemainingRepository.findAll()) {
                if (surveysCountRemaining.getUserMail().equals(user.getEmail())) {
                    scr = surveysCountRemaining;
                    break;
                }
            }
            if (scr != null) {
                scr.setUserMail(scr.getUserMail());
                scr.setSurveysRemaining(scr.getSurveysRemaining() - 1);
                this.surveysCountRemainingRepository.save(scr);
            }
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(201);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(survey);
            return responseWrapper;

        } catch (Exception e) {
            System.out.println("Exception Occured");
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(e.getMessage());
            return responseWrapper;
        }


    }

    public ResponseWrapper getViewsOfSurvey(View view) throws SurwayException {
        //check survey id
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Survey Exist in the database");
            return responseWrapper;
        }
        if (this.viewRepository.findAll() == null || this.viewRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No views exist");
            return responseWrapper;
        }
        int viewCount = 0;
        for (View v : this.viewRepository.findAll()) {
            if (v.getSurveyId().equals(view.getSurveyId())) {
                viewCount = v.getViewCount();
                break;
            }
        }
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        View v = new View();
        v.setViewCount(viewCount);
        responseWrapper.setMessage(v);
        return responseWrapper;
    }

    public ResponseWrapper editSurvey(Survey survey) throws SurwayException {
        //check if survey id is invalid
        Survey s = null;
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Survey surveyPresentInDatabase=null;
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("No Survey Exist in the database");
            return responseWrapper;
        }
        if (survey == null || survey.getId() == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Survey Id Can't be null");
            return responseWrapper;
        }
        boolean surveyIdExists = false;
        boolean validUserMail = false;
        for (Survey x : this.surveyRepository.findAll()) {
            if (x.getId().equals(survey.getId())) {
                s = x;
                surveyIdExists = true;
                if(x.getUserEmail().equals(survey.getUserEmail()))  validUserMail = true;
                break;
            }

        }
        if (!surveyIdExists || s == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Invalid Survey Id");
            return responseWrapper;
        }
        if (!validUserMail) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Invalid User mail");
            return responseWrapper;
        }
        s.setId(survey.getId());
        if(survey.getQuestions()!=null && survey.getQuestions().size()>0) s.setQuestions(survey.getQuestions());
        if(survey.getActive()!=null) s.setActive(survey.getActive());
        if(survey.getName()!=null && survey.getName().trim().length()!=0) s.setName(survey.getName());
        if(survey.getSurveyCategory()!=null && survey.getSurveyCategory().trim().length()!=0) s.setSurveyCategory(survey.getSurveyCategory());
        if(survey.getHasPassword()!=null) s.setHasPassword(survey.getHasPassword());
        if(survey.getColorCode()!=null && survey.getColorCode().trim().length()!=0) s.setColorCode(survey.getColorCode());
        //generating new pass word key
        if (survey.getHasPassword()!=null) {
            s.setHasPassword(survey.getHasPassword());
            if(survey.getHasPassword()==false){
                s.setPassword("");
                s.setPasswordKey("");
            }
            else
            {
                if(survey.getPassword()!=null && survey.getPassword().trim().length()>0){
                    String passwordKey = UUID.randomUUID().toString();
                    passwordKey = passwordKey.replaceAll("-", "");
                    s.setPasswordKey(passwordKey);
                    s.setPassword(PasswordUtility.encrypt(survey.getPassword(), passwordKey));
                }
                else{
                    s.setHasPassword(false);
                }
            }
        }
        //check if responses on survey is >0 then they can't edit it
        if(this.surveyResponsesRepository.findAll()!=null)
        {
           for(SurveyResponses surveyResponses : this.surveyResponsesRepository.findAll())
           {
             if(surveyResponses.getSurveyId().equals(survey.getId()))
             {
               if(surveyResponses.getSurveyResponseList()!=null && surveyResponses.getSurveyResponseList().size()>0)
               {
                   responseWrapper.setSuccess(false);
                   responseWrapper.setResponseCode(404);
                   responseWrapper.setHasException(false);
                   responseWrapper.setHasError(true);
                   responseWrapper.setMessage("User can't edit survey as some response/responses has already recorded on this survey");
                   return responseWrapper;
               }
             }
           }
        }
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        s.setTimestamp(timestamp.toString());
        this.surveyRepository.save(s);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage("Survey Edited Successfully");
        date = new Date();
        timestamp = new Timestamp(date.getTime());
        History history=new History();
        history.setUserMail(survey.getUserEmail());
        List<Tracking> trackings=null;
        if(this.historyRepository.findAll()!=null)
        {
            for(History history1 : this.historyRepository.findAll())
            {
                if(history1.getUserMail().equals(survey.getUserEmail()))
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
            tracking.setActivity("You have modified a survey");
            trackings.add(tracking);
            history.setTrackingList(trackings);
            this.historyRepository.save(history);
        }
        else
        {
            Tracking tracking=new Tracking();
            tracking.setTimestamp(timestamp.toString());
            tracking.setActivity("You have modified a survey");
            history.setTrackingList(Arrays.asList(tracking));
            this.historyRepository.save(history);
        }
        return responseWrapper;
    }

    public Integer getSurveyViews(View view) {
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            return 0;
        }
        if (this.viewRepository.findAll() == null || this.viewRepository.findAll().size() == 0) {
            return 0;
        }
        int viewCount = 0;
        for (View v : this.viewRepository.findAll()) {
            if (v.getSurveyId().equals(view.getSurveyId())) {
                viewCount = v.getViewCount();
                break;
            }
        }
        return viewCount;

    }

    public ResponseWrapper getUserSurvey(User user) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {

            List<Survey> surveyList = this.surveyRepository.findAll();
            if (surveyList == null || surveyList.size() == 0) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("No Survey is Present in DB Please Add one.");

            } else {
                List<Survey> userSurvey = new ArrayList<>();
                List<SurveyPojo> surveyPojoList = new ArrayList<>();
                SurveyPojo surveyPojo = new SurveyPojo();
                for (Survey s : surveyList) {
                    if (s != null && s.getUserEmail().equals(user.getEmail())) {
                        View view = new View();
                        view.setSurveyId(s.getId());
                        surveyPojo = new SurveyPojo();
                        surveyPojo.setId(s.getId());
                        surveyPojo.setName(s.getName());
                        surveyPojo.setSurveyCategory(s.getSurveyCategory());
                        surveyPojo.setActive(s.getActive());
                        if (s.getHasPassword() && s.getPassword() != null && s.getPassword().length() > 0) {
                            surveyPojo.setPassword(PasswordUtility.decrypt(s.getPassword(), s.getPasswordKey()));
                            surveyPojo.setPasswordKey("");
                        } else {
                            surveyPojo.setPassword(s.getPassword());
                            surveyPojo.setPasswordKey(s.getPasswordKey());
                        }
                        surveyPojo.setTimestamp(s.getTimestamp());
                        surveyPojo.setColorCode(s.getColorCode());
                        surveyPojo.setUserEmail(s.getUserEmail());
                        surveyPojo.setHasPassword(s.getHasPassword());
                        surveyPojo.setQuestions(s.getQuestions());
                        surveyPojo.setAllowedUsers(s.getAllowedUsers());
                        surveyPojo.setViews(getSurveyViews(view));
                        SurveyResponses surveyResponses = new SurveyResponses();
                        surveyResponses.setSurveyId(s.getId());
                        surveyPojo.setSurveyResponses(this.surveyResponsesService.getSurveyResponses(surveyResponses));
                        surveyPojoList.add(surveyPojo);
                        userSurvey.add(s);
                    }
                }
                if (userSurvey == null || userSurvey.size() == 0) {
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage("No Survey is Present in DB For This User");

                } else {
                    responseWrapper.setSuccess(true);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(false);
                    //responseWrapper.setMessage(userSurvey);
                    responseWrapper.setMessage(surveyPojoList);
                }
            }
        } catch (Exception e) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(e.getMessage());
        }
        return responseWrapper;
    }

    public boolean enableSurvey(String surveyId) throws SurwayException {
        Survey survey = this.getSurveyById(surveyId);
        if (survey == null) return false;
        if (survey.getActive() == true) {
            return false;
        }
        survey.setActive(true);
        this.surveyRepository.save(survey);
        return true;
    }

    public boolean disableSurvey(String surveyId) throws SurwayException {
        Survey survey = this.getSurveyById(surveyId);
        if (survey == null) return false;
        if (survey.getActive() == false) {
            return false;
        }
        survey.setActive(false);
        this.surveyRepository.save(survey);
        return true;
    }


    public ResponseWrapper addToAssignedSurveys(AssignedSurveys assignedSurveys) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            List<String> allowedUsers = new ArrayList<>();
            if (assignedSurveys == null || assignedSurveys.getToUsers() == null || assignedSurveys.getToUsers().size() == 0) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("Some Problem");
                return responseWrapper;
            } else {
                List<User> users = this.userService.getAllUsers();
                User user = null;
                if (users == null || users.size() == 0) {

                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasError(true);
                    responseWrapper.setHasException(false);
                    responseWrapper.setMessage("No user present in the database");
                    return responseWrapper;
                } else {

                    for (User u : users) {
                        if (assignedSurveys.getFromUser().equals(u.getEmail())) {
                            user = u;
                            break;
                        }
                    }
                    if (user == null) {

                        responseWrapper.setSuccess(false);
                        responseWrapper.setResponseCode(404);
                        responseWrapper.setHasError(true);
                        responseWrapper.setHasException(false);
                        responseWrapper.setMessage("User doesn't exist");
                        return responseWrapper;

                    } else {
                        //check for invalid Survey Id;
                        Survey survey = null;
                        for (Survey s : this.surveyRepository.findAll()) {
                            if (s.getId().equals(assignedSurveys.getSurveyId()) && s.getUserEmail().equals(user.getEmail())) {
                                survey = s;
                                break;
                            }
                        }
                        if (survey == null) {
                            responseWrapper.setSuccess(false);
                            responseWrapper.setResponseCode(404);
                            responseWrapper.setHasError(true);
                            responseWrapper.setHasException(false);
                            responseWrapper.setMessage("Survey with given id either doesn't exist or either it is not mapped to user");
                            return responseWrapper;
                        }
                        if (user.isSubscribed() == false) {
                            responseWrapper.setSuccess(false);
                            responseWrapper.setResponseCode(404);
                            responseWrapper.setHasError(true);
                            responseWrapper.setHasException(false);
                            responseWrapper.setMessage("Can't assign survey to different users as you are not premium user");
                            return responseWrapper;
                        }

                        //check if user is assigning survey to self
                        //if any then remove it
                        int index = 0;
                        for (String s : assignedSurveys.getToUsers()) {
                            if (s.equals(assignedSurveys.getFromUser())) {
                                assignedSurveys.getToUsers().remove(index);
                            }
                            index++;
                        }
//                        if (this.surveyRepository.findAll().size() > 0) {
//                            for (Survey s : this.surveyRepository.findAll()) {
//                                if (s.getUserEmail().equals(assignedSurveys.getFromUser())) {
//                                    allowedUsers = s.getAllowedUsers();
//                                    break;
//                                }
//                            }
//                        }
//                        //no previous user exist
//                        if (allowedUsers == null || allowedUsers.size() == 0)
//                            allowedUsers = assignedSurveys.getToUsers();
//                        else {
//                            boolean matched = false;
//                            for (String x : assignedSurveys.getToUsers()) {
//                                matched = false;
//                                //if we found new mail id then add it
//                                for (String y : allowedUsers) {
//                                    if (x.equals(y)) matched = true;
//                                }
//                                if (!matched) allowedUsers.add(x);
//                            }
//                        }
                        //check if he is assigning survey to user
                        //yaha pe unn users ko mail send karna hai jinko pehle send na kiya ho
//                        List<String> assignedSurveysList = assignedSurveys.getToUsers();
                        allowedUsers=assignedSurveys.getToUsers();
                        //this.assignedSurveysRepository.save(assignedSurveys);
                        Survey survey1 = survey;
                        //survey1.setAllowedUsers(assignedSurveys.getToUsers());
                        if (allowedUsers.size() > 10) {
                            responseWrapper.setSuccess(false);
                            responseWrapper.setResponseCode(404);
                            responseWrapper.setHasError(true);
                            responseWrapper.setHasException(false);
                            responseWrapper.setMessage("Can't assign survey to more than 10 users");
                            return responseWrapper;
                        }
                        survey1.setAllowedUsers(allowedUsers);
                        if (sendMail(allowedUsers, assignedSurveys.getSurveyId())) {
                            this.surveyRepository.save(survey1);
                            responseWrapper.setSuccess(true);
                            responseWrapper.setResponseCode(200);
                            responseWrapper.setHasError(false);
                            responseWrapper.setHasException(false);
                            responseWrapper.setMessage("Survey assigned to All the mentioned users by survey creator successfully");
                        } else {

                            responseWrapper.setSuccess(false);
                            responseWrapper.setResponseCode(400);
                            responseWrapper.setHasError(true);
                            responseWrapper.setHasException(true);
                            responseWrapper.setMessage("Was not able to assign survey to Users");
                        }


                        return responseWrapper;
                    }
                }

            }

        } catch (Exception e) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(e.getMessage());
            return responseWrapper;
        }
    }
    @Transactional
    public ResponseWrapper assignSurveyUsingGroup(String groupId,String surveyId) throws SurwayException
    {
      ResponseWrapper  responseWrapper=new ResponseWrapper();
      boolean groupExists=false;
      List<String> toUsers=new ArrayList<>();
      String userMail="";
      Group group=null;
      //if group id not exist then return
       if(this.groupRepository.findAll()!=null)
       {
         for(Group g : this.groupRepository.findAll())
         {
           if(g.getId().equals(groupId))
           {
               group=g;
               userMail=g.getUserMail();
               toUsers=g.getMembers();
               groupExists=true;
               break;
           }
         }
       }
       if(!groupExists)
       {
         responseWrapper.setSuccess(false);
         responseWrapper.setResponseCode(404);
         responseWrapper.setHasError(true);
         responseWrapper.setHasException(false);
         responseWrapper.setMessage("Invalid Group Id");
         return responseWrapper;
       }
       AssignedSurveys assignedSurveys=new AssignedSurveys();
       assignedSurveys.setSurveyId(surveyId);
       assignedSurveys.setToUsers(toUsers);
       assignedSurveys.setFromUser(userMail);
       if(this.surveyRepository.findAll()!=null)
       {
           for(Survey x : this.surveyRepository.findAll())
           {
             if(x.getId().equals(surveyId))
             {
                 x.setAllowedUsers(assignedSurveys.getToUsers());
                 //logic to check if user is subscribed or not if they aren;t then return negative response
                 User u=new User();
                 u.setEmail(x.getUserEmail());
                 User user=userService.getUserByMail(u);
                 if(user.isSubscribed()==false)
                 {
                     responseWrapper.setSuccess(false);
                     responseWrapper.setResponseCode(404);
                     responseWrapper.setHasError(true);
                     responseWrapper.setHasException(false);
                     responseWrapper.setMessage("Can't assign survey to different users as you are not premium user");
                     return responseWrapper;
                 }
                 this.surveyRepository.save(x);
                 responseWrapper.setSuccess(true);
                 responseWrapper.setResponseCode(200);
                 responseWrapper.setHasError(false);
                 responseWrapper.setHasException(false);
                 responseWrapper.setMessage("Survey Assigned to user successfully");


                     Date date = new Date();
                     Timestamp timestamp = new Timestamp(date.getTime());
                     History history=new History();
                     history.setUserMail(assignedSurveys.getFromUser());
                     List<Tracking> trackings=null;
                     if(this.historyRepository.findAll()!=null)
                     {
                         for(History history1 : this.historyRepository.findAll())
                         {
                             if(history1.getUserMail().equals(assignedSurveys.getFromUser()))
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
                         tracking.setActivity("You have assigned survey to a group "+group.getName());
                         trackings.add(tracking);
                         history.setTrackingList(trackings);
                         this.historyRepository.save(history);
                     }
                     else
                     {
                         Tracking tracking=new Tracking();
                         tracking.setTimestamp(timestamp.toString());
                         tracking.setActivity("You have assigned survey to a group "+group.getName());
                         history.setTrackingList(Arrays.asList(tracking));
                         this.historyRepository.save(history);
                     }

                 return responseWrapper;
             }
           }
       }
        responseWrapper.setSuccess(false);
        responseWrapper.setResponseCode(404);
        responseWrapper.setHasError(true);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage("Invalid Group Id");
        return responseWrapper;
    }
    @Transactional
    public ResponseWrapper deassignSurvey(AssignedSurveys assignedSurveys) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        List<String> allowedUsers = new ArrayList<>();
        Survey survey = null;
        if (this.surveyRepository.findAll() != null && this.surveyRepository.findAll().size() > 0) {
            for (Survey s : this.surveyRepository.findAll()) {
                if (s.getId().equals(assignedSurveys.getSurveyId())) {
                    survey = s;
                    allowedUsers = s.getAllowedUsers();
                }
            }
        }
        if (allowedUsers == null || allowedUsers.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Can't deassign as no user exist");
            return responseWrapper;
        }
        boolean matched = false;
        boolean hasDeletedAllUsers = true;
        List<String> usersToRemove = new ArrayList<>();
        for (String x : assignedSurveys.getToUsers()) {
            matched = false;
            for (String y : allowedUsers) {
                if (x.equals(y)) matched = true;
            }
            if (matched) {
                usersToRemove.add(x);
            } else {
                hasDeletedAllUsers = false;
            }
        }
        allowedUsers.removeAll(usersToRemove);
        survey.setAllowedUsers(allowedUsers);
        this.surveyRepository.save(survey);
        if (hasDeletedAllUsers) {
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(300);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Couldn't deassigned all the users since some are either mismatched or not assigned previously");
            return responseWrapper;
        } else {
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Deassigned all the users successfully");
            return responseWrapper;
        }
    }

    public List<AssignedSurveys> getAllAssignedSurveys() {
        List<Survey> surveys = this.surveyRepository.findAll();
        List<AssignedSurveys> assignedSurveysList = new ArrayList<>();
        if (surveys == null || surveys.size() == 0) return assignedSurveysList;
        for (Survey s : surveys) {
            AssignedSurveys assignedSurveys = new AssignedSurveys();
            assignedSurveys.setFromUser(s.getUserEmail());
            assignedSurveys.setSurveyId(s.getId());
            assignedSurveys.setToUsers(s.getAllowedUsers());
            assignedSurveysList.add(assignedSurveys);
        }
        return assignedSurveysList;
    }

    @Transactional
    public ResponseWrapper incrementSurveyViewCount(View view) throws SurwayException {
        Survey survey = this.getSurveyById(view.getSurveyId());
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (survey == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid Survey Id");
            return responseWrapper;
        }
        //code to get previous count
        int viewCount = 0;
        for (View v : this.viewRepository.findAll()) {
            if (v != null && v.getSurveyId().equals(view.getSurveyId())) {
                viewCount = v.getViewCount();
                break;
            }
        }
        viewCount += 1;
        view.setViewCount(viewCount);
        this.viewRepository.save(view);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage("Count of view increased successfully");
        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper addToGroup(Group group) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        //check for legit user;
        User user = null;
        User u = new User();
        u.setEmail(group.getUserMail());
        user = this.userService.getUserByMail(u);
        if (user == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid User mail");
            return responseWrapper;
        }
        int groupCountRemaining=0;
        if(this.groupsCountRemainingRepository.findAll()!=null)
        {
           for(GroupsCountRemaining gcr : this.groupsCountRemainingRepository.findAll())
           {
             if(gcr.getUserMail().equals(user.getEmail()))
             {
               groupCountRemaining=gcr.getGroupsRemaining();
               break;
             }
           }
        }
        if (groupCountRemaining==0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("No groups has left in your account Spend Hu coins to make groups");
            return responseWrapper;
        }
        String groupId = UUID.randomUUID().toString().replaceAll("-", "");
        groupId.replaceAll("_", "");
        group.setId("Group_By_" + group.getUserMail() + groupId);
        int index = 0;
        for (String mail : group.getMembers()) {
            if (mail.equals(group.getUserMail())) {
                group.getMembers().remove(index);
                group.setMembers(group.getMembers());
                break;
            }
            index += 1;
        }
        if (group.getMembers().size() > 10) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Can't create group of more than 10 members");
            return responseWrapper;

        }
        //check for invalid members
        if (group.getMembers() == null || group.getMembers().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Group Member can't be null or it's size can't be zero");
            return responseWrapper;
        }
        //decrease group count by one
        if(this.groupsCountRemainingRepository.findAll()!=null)
        {
            GroupsCountRemaining gcr1=null;
            for(GroupsCountRemaining gcr : this.groupsCountRemainingRepository.findAll())
            {
              if(gcr.getUserMail().equals(user.getEmail()))
              {
                gcr1=gcr;
              }
            }
            if(gcr1!=null)
            {
               GroupsCountRemaining groupsCountRemainingToReturn=new GroupsCountRemaining();
               groupsCountRemainingToReturn.setUserMail(gcr1.getUserMail());
               groupsCountRemainingToReturn.setGroupsRemaining(gcr1.getGroupsRemaining()-1);
               this.groupsCountRemainingRepository.save(groupsCountRemainingToReturn);
            }
        }
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        group.setTimestamp(timestamp.toString());
        this.groupRepository.save(group);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage("Group is successfully created against group id : " + groupId);
        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper editGroup(Group group) throws SurwayException {
        boolean groupIdExists = false;
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (this.groupRepository.findAll() == null || this.groupRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Group Exists");
            return responseWrapper;
        }
        for (Group g : this.groupRepository.findAll()) {
            if (g.getId().equals(group.getId())) {
                groupIdExists = true;
                break;
            }
        }
        if (!groupIdExists) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid Group Id");
            return responseWrapper;
        }
        User u = new User();
        u.setEmail(group.getUserMail());
        User user = this.userService.getUserByMail(u);
        if (user == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No user with given mail exist");
            return responseWrapper;
        }
        boolean mailIdExists = false;
        for (Group x : this.groupRepository.findAll()) {
            if (x.getId().equals(group.getId())) {
                if (x.getUserMail().equals(group.getUserMail())) {
                    mailIdExists = true;
                    break;
                }
            }
        }
        if (!mailIdExists) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid user mail");
            return responseWrapper;
        }
        //mail id exist and also group id
        //check is members list is greater than 10
        for (String s : group.getMembers()) {
            if (s.equals(group.getUserMail())) {
                group.getMembers().remove(s);
            }
        }
        if (group.getMembers().size() > 10) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Can't create group of more than 10 members");
            return responseWrapper;
        }
        //check for invalid members
        if (group.getMembers() == null || group.getMembers().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Group Member can't be null or it's size can't be zero");
            return responseWrapper;
        }
        Group groupToUpdate = new Group();
        groupToUpdate.setId(group.getId());
        groupToUpdate.setName(group.getName());
        groupToUpdate.setUserMail(group.getUserMail());
        groupToUpdate.setMembers(group.getMembers());
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        groupToUpdate.setTimestamp(timestamp.toString());
        this.groupRepository.save(groupToUpdate);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage("Group details updated successfully");

        Date date1 = new Date();
        Timestamp timestamp1 = new Timestamp(date1.getTime());
            History history=new History();
            history.setUserMail(group.getUserMail());
            List<Tracking> trackings=null;
            if(this.historyRepository.findAll()!=null)
            {
                for(History history1 : this.historyRepository.findAll())
                {
                    if(history1.getUserMail().equals(group.getUserMail()))
                    {
                        trackings=history1.getTrackingList();
                        break;
                    }
                }
            }
            if(trackings!=null)
            {
                Tracking tracking=new Tracking();
                tracking.setTimestamp(timestamp1.toString());
                tracking.setActivity("You have modified a group ");
                trackings.add(tracking);
                history.setTrackingList(trackings);
                this.historyRepository.save(history);
            }
            else
            {
                Tracking tracking=new Tracking();
                tracking.setTimestamp(timestamp1.toString());
                tracking.setActivity("You have modified a group ");
                history.setTrackingList(Arrays.asList(tracking));
                this.historyRepository.save(history);
            }

        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper getMyGroups(User user) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (user == null || this.userService.getUserByMail(user) == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid User mail");
            return responseWrapper;

        }
        if (this.groupRepository.findAll() == null || this.groupRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Group Exist");
            return responseWrapper;
        }
        //code to find groups created by this user
        List<Group> groups = new ArrayList<>();
        boolean userExist = false;
        for (Group g : this.groupRepository.findAll()) {
            if (g.getUserMail().equals(user.getEmail())) {
                //user found
                userExist = true;
                groups.add(g);
            }
        }
        if (!userExist) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No group has been created by this user");
            return responseWrapper;
        }
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage(groups);
        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper deleteGroup(Group group) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        User user = null;
        User u = new User();
        u.setEmail(group.getUserMail());
        user = this.userService.getUserByMail(u);
        if (user == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid User mail");
            return responseWrapper;

        }
        if (this.groupRepository.findAll() == null || this.groupRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Can't delete since No Group Exist");
            return responseWrapper;
        }
        //check if group id is valid or not
        boolean groupExists = false;
        Group groupToDelete = null;
        for (Group g : this.groupRepository.findAll()) {
            if (g.getId().equalsIgnoreCase(group.getId())) {
                groupToDelete = g;
                groupExists = true;
                break;
            }
        }
        if (groupExists) {
            this.groupRepository.delete(groupToDelete);
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage("Group removed Successfully");
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            History history=new History();
            history.setUserMail(group.getUserMail());
            List<Tracking> trackings=null;
            if(this.historyRepository.findAll()!=null)
            {
                for(History history1 : this.historyRepository.findAll())
                {
                    if(history1.getUserMail().equals(group.getUserMail()))
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
                tracking.setActivity("You have removed a group ");
                trackings.add(tracking);
                history.setTrackingList(trackings);
                this.historyRepository.save(history);
            }
            else
            {
                Tracking tracking=new Tracking();
                tracking.setTimestamp(timestamp.toString());
                tracking.setActivity("You have removed a group ");
                history.setTrackingList(Arrays.asList(tracking));
                this.historyRepository.save(history);
            }
            return responseWrapper;
        } else {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Can't remove group as group id is Invalid");
            return responseWrapper;
        }
    }

    //payload {groupId , usermail}
    @Transactional
    public ResponseWrapper getAllMembersInMyGroup(Group group) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        User user = null;
        User u = new User();
        u.setEmail(group.getUserMail());
        user = this.userService.getUserByMail(u);
        if (user == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid User mail");
            return responseWrapper;

        }
        if (this.groupRepository.findAll() == null || this.groupRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Group Exist");
            return responseWrapper;
        }
        List<String> membersInThisGroup = new ArrayList<>();
        Group searchedGroup = null;
        for (Group g : this.groupRepository.findAll()) {
            if (g.getId().equalsIgnoreCase(group.getId())) {
                searchedGroup = g;
            }
        }
        if (searchedGroup == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid Group Id");
            return responseWrapper;
        }
        for (String x : searchedGroup.getMembers()) {
            if (x.equals(group.getUserMail())) continue;
            membersInThisGroup.add(x);
        }
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage(membersInThisGroup);
        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper getAllGroupsAssignedToMe(User user) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (user == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid User mail");
            return responseWrapper;

        }
        if (this.groupRepository.findAll() == null || this.groupRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Group Exist");
            return responseWrapper;
        }
        List<Group> groupsAssignedToMe = new ArrayList<>();
        for (Group group : this.groupRepository.findAll()) {
            for (String mail : group.getMembers()) {
                if (mail.equals(user.getEmail())) {
                    groupsAssignedToMe.add(group);
                }
            }
        }
        if (groupsAssignedToMe == null || groupsAssignedToMe.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Groups has assigned to this user");
            return responseWrapper;
        }
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage(groupsAssignedToMe);
        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper getAllPublicSurveys(String mail) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Surveys available in the database");
            return responseWrapper;
        }
        List<Survey> publicSurveys = new ArrayList<>();
        for (Survey x : this.surveyRepository.findAll()) {
            if (x.getHasPassword() == false && (x.getAllowedUsers() == null || x.getAllowedUsers().size() == 0)) {
                publicSurveys.add(x);
            }
        }
        if (publicSurveys == null || publicSurveys.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Public Surveys available");
            return responseWrapper;
        }
        //get public surveys which is not attempted by user
        List<Survey> publicSurveysToReturn = new ArrayList<>();
        List<SurveyResponse> surveyResponsesList = null;
        boolean isUserAttemptedTheSurvey = false;
        for (Survey s : publicSurveys) {
            isUserAttemptedTheSurvey = false;
            surveyResponsesList=null;
            for (SurveyResponses surveyResponses : this.surveyResponsesRepository.findAll()) {
                if (s.getId().equals(surveyResponses.getSurveyId())) {
                    surveyResponsesList = surveyResponses.getSurveyResponseList();
                    break;
                }
            }
            if (surveyResponsesList != null) {
                for (SurveyResponse sr : surveyResponsesList) {
                    if (sr.getUserMail().equals(mail)) {
                        isUserAttemptedTheSurvey = true;
                        break;
                    }
                }
            }
            if (!isUserAttemptedTheSurvey) {
                publicSurveysToReturn.add(s);
            }

        }
        //code to check if it is launched publicly then add it
        List<Survey> publicLaunchSurveysToReturn=new ArrayList<>();
         if(this.puchasedTransactionRepository.findAll()!=null)
         {
          for(Survey survey : publicSurveysToReturn)
          {
              for(PurchaseTransaction purchaseTransaction : this.puchasedTransactionRepository.findAll())
              {
                 if(purchaseTransaction.getPurchaseType().equals("public_launch") && purchaseTransaction.getPurchaseTypeId().equals(survey.getId()))
                 {
                   publicLaunchSurveysToReturn.add(survey);
                 }
              }
          }
         }
        if (publicLaunchSurveysToReturn == null || publicLaunchSurveysToReturn.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Public Surveys available");
            return responseWrapper;
        }
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage(publicLaunchSurveysToReturn);
        return responseWrapper;
    }

    @Transactional
    public ResponseWrapper getAllPrivateSurveys(String mail) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Surveys available in the database");
            return responseWrapper;
        }
        List<Survey> privateSurveys = new ArrayList<>();
        Survey s;
        for (Survey x : this.surveyRepository.findAll()) {
            if (x.getHasPassword() == true) {
                s = x;
                s.setPassword(PasswordUtility.decrypt(x.getPassword(), x.getPasswordKey()));
                s.setPasswordKey("");
                privateSurveys.add(s);
            }
        }
        if (privateSurveys == null || privateSurveys.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Private Surveys available");
            return responseWrapper;
        }
        List<Survey> privateSurveysToReturn = new ArrayList<>();
        List<SurveyResponse> surveyResponsesList = null;
        boolean isUserAttemptedTheSurvey = false;
        for (Survey survey : privateSurveys) {
            isUserAttemptedTheSurvey = false;
            for (SurveyResponses surveyResponses : this.surveyResponsesRepository.findAll()) {
                if (survey.getId().equals(surveyResponses.getSurveyId())) {
                    surveyResponsesList = surveyResponses.getSurveyResponseList();
                    break;
                }
            }
            if (surveyResponsesList != null) {
                for (SurveyResponse sr : surveyResponsesList) {
                    if (sr.getUserMail().equals(mail)) {
                        isUserAttemptedTheSurvey = true;
                        break;
                    }
                }
            }
            if (!isUserAttemptedTheSurvey) {
                privateSurveysToReturn.add(survey);
            }

        }
        if (privateSurveysToReturn == null || privateSurveysToReturn.size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Private Surveys available");
            return responseWrapper;
        }

        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        responseWrapper.setMessage(privateSurveysToReturn);
        return responseWrapper;

    }

    @Transactional
    public ResponseWrapper getDashboardDetailsOfUser(String userMail) throws Exception {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Dashboard dashboard = new Dashboard();
        User u = new User();
        u.setEmail(userMail);
        User user = this.userService.getUserByMail(u);
        //invalid user mail
        if (user == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Invalid user mail");
            return responseWrapper;
        }
        if (this.surveyRepository.findAll() == null || this.surveyRepository.findAll().size() == 0) {
            dashboard.setSurveysCount(0);
            dashboard.setSurveyResponses(null);
            dashboard.setAverageTimeTaken(null);
            dashboard.setSurveys(null);
            dashboard.setSurveyEngagementRates(null);
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(dashboard);
            return responseWrapper;
        }
        List<Survey> surveysCreatedByUser = new ArrayList<>();
        int countOfSurveysCreated = 0;
        List<SurveyResponses> surveyResponsesList = new ArrayList<>();
        List<SurveyStatistics> averageTimeTaken = new ArrayList<>();
        List<SurveyEngagementRate> surveyEngagementRates = new ArrayList<>();
        //iterate on each survey and check which one matched with user's mail
        for (Survey s : this.surveyRepository.findAll()) {
            if (s.getUserEmail().equals(userMail)) {
                surveysCreatedByUser.add(s);
            }
        }
        if (surveysCreatedByUser == null || surveysCreatedByUser.size() == 0) {
            dashboard.setSurveysCount(0);
            dashboard.setSurveyResponses(null);
            dashboard.setAverageTimeTaken(null);
            dashboard.setSurveys(null);
            dashboard.setSurveyEngagementRates(null);
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(dashboard);
            return responseWrapper;
        }
        countOfSurveysCreated = surveysCreatedByUser.size();
        //if no record of response exist
        if (this.surveyResponsesRepository.findAll() == null || this.surveyResponsesRepository.findAll().size() == 0) {
            surveyResponsesList.add(null);
            averageTimeTaken.add(null);
            dashboard.setSurveysCount(countOfSurveysCreated);
            dashboard.setSurveys(surveysCreatedByUser);
            dashboard.setSurveyResponses(null);
            dashboard.setAverageTimeTaken(null);
            dashboard.setSurveyEngagementRates(null);
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(dashboard);
            return responseWrapper;
        } else {
            for (Survey x : surveysCreatedByUser) {
                for (SurveyResponses sr : this.surveyResponsesRepository.findAll()) {
                    if (x.getId().equals(sr.getSurveyId())) {
                        surveyResponsesList.add(sr);
                    }
                }
            }
            //surveys responses can be null
            if (surveyResponsesList == null && surveyResponsesList.size() == 0) {
                dashboard.setSurveysCount(countOfSurveysCreated);
                dashboard.setSurveys(surveysCreatedByUser);
                dashboard.setSurveyResponses(null);
                dashboard.setAverageTimeTaken(null);
                dashboard.setSurveyEngagementRates(null);
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(dashboard);
                return responseWrapper;
            }
            double averageTimeTakenBySurvey;
            int size;
            int sum;
            double engagementRate;
            int views = 0;
            SurveyEngagementRate surveyEngagementRate = new SurveyEngagementRate();
            //if survey responses exist
            for (SurveyResponses srs : surveyResponsesList) {
                averageTimeTakenBySurvey = 0;
                size = 0;
                sum = 0;
                views = 0;
                if (srs.getSurveyResponseList() != null) {
                    for (SurveyResponse sr : srs.getSurveyResponseList()) {
                        String[] splittedString = sr.getActualTimeTaken().split(" ");
                        sum += Integer.parseInt(splittedString[0]);
                        size += 1;
                    }
                    surveyEngagementRate = new SurveyEngagementRate();
                    surveyEngagementRate.setSurveyId(srs.getSurveyId());
                    //get views of particular survey
                    if (this.viewRepository.findAll() == null || this.viewRepository.findAll().size() == 0) {
                        views = 0;
                    } else {
                        for (View v : this.viewRepository.findAll()) {
                            if (v != null && v.getSurveyId().equals(srs.getSurveyId())) {
                                views = v.getViewCount();
                                break;
                            }
                        }
                    }
                    if (views == 0) engagementRate = 0;
                    else {
                        engagementRate = (double) size / views;
                        engagementRate = engagementRate * 100;
                        engagementRate = Math.round(engagementRate);
                        engagementRate = engagementRate / 100;
                    }
                    surveyEngagementRate.setEngagementRate(engagementRate);
                }
                surveyEngagementRates.add(surveyEngagementRate);
                averageTimeTakenBySurvey = (double) sum / size;
                averageTimeTakenBySurvey = averageTimeTakenBySurvey * 100;
                averageTimeTakenBySurvey = Math.round(averageTimeTakenBySurvey);
                averageTimeTakenBySurvey = averageTimeTakenBySurvey / 100;
                SurveyStatistics surveyStatistics = new SurveyStatistics();
                surveyStatistics.setSurveyId(srs.getSurveyId());
                surveyStatistics.setAverageTimeTaken(averageTimeTakenBySurvey);
                averageTimeTaken.add(surveyStatistics);
            }
            for (Survey x : surveysCreatedByUser) {
                boolean surveyResponseExist = false;
                for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                    if (srs.getSurveyId().equals(x.getId())) {
                        surveyResponseExist = true;
                        break;
                    }
                }
                if (!surveyResponseExist) {
                    surveyEngagementRate = new SurveyEngagementRate();
                    surveyEngagementRate.setEngagementRate(0);
                    surveyEngagementRate.setSurveyId(x.getId());
                    surveyEngagementRates.add(surveyEngagementRate);
                    SurveyStatistics surveyStatistics = new SurveyStatistics();
                    surveyStatistics.setSurveyId(x.getId());
                    surveyStatistics.setAverageTimeTaken(0.0);
                    averageTimeTaken.add(surveyStatistics);
                }
            }
            int huCoins = user.getHuCoins();
            int overallViews = 0;
            int groupsCount = 0;
            List<SurveyCategoryResponse> surveyCategoryResponses = new ArrayList<>();
            //logic to get views
            if (!(this.viewRepository.findAll() == null && this.viewRepository.findAll().size() == 0)) {
                for (Survey s : surveysCreatedByUser) {
                    for (View v : this.viewRepository.findAll()) {
                        if (s != null && v != null && s.getId() != null && v.getSurveyId() != null && s.getId().equals(v.getSurveyId())) {
                            overallViews += v.getViewCount();
                        }
                    }
                }
            }
            //logic to get groups count
            if (!(this.groupRepository.findAll() == null && this.groupRepository.findAll().size() == 0)) {
                for (Group g : this.groupRepository.findAll()) {
                    if (g != null && g.getUserMail() != null && g.getUserMail().equals(userMail)) {
                        groupsCount += 1;
                    }
                }
            }
            SurveyCategoryResponse surveyCategoryResponse = new SurveyCategoryResponse();
            for (Survey s : surveysCreatedByUser) {
                surveyCategoryResponse = new SurveyCategoryResponse();
                surveyCategoryResponse.setSurveyCategory(s.getSurveyCategory());
                surveyCategoryResponse.setSurveyId(s.getId());
                if (this.surveyResponsesRepository != null) {
                    for (SurveyResponses surveyResponses : this.surveyResponsesRepository.findAll()) {
                        if (surveyResponses != null && surveyResponses.getSurveyId().equals(s.getId())) {
                            if (surveyResponses.getSurveyResponseList() == null) surveyCategoryResponse.setResponses(0);
                            else surveyCategoryResponse.setResponses(surveyResponses.getSurveyResponseList().size());
                        }
                    }
                }
                //get views on that survey
                int surveyViewCount = 0;
                surveyCategoryResponse.setViews(surveyViewCount);
                if (this.viewRepository.findAll() != null) {
                    for (View v : this.viewRepository.findAll()) {
                        if (v != null && v.getSurveyId() != null && v.getSurveyId().equals(s.getId())) {
                            surveyViewCount = v.getViewCount();
                            break;
                        }
                    }
                    surveyCategoryResponse.setViews(surveyViewCount);
                }
                surveyCategoryResponses.add(surveyCategoryResponse);
            }
            //logic to get top 5 recent surveys
            List<Survey> sortedSurveysByDate;
            if (surveysCreatedByUser == null || surveysCreatedByUser.size() == 0) {
                dashboard.setTopRecentSurveys(null);
            } else {
                //comparison by timestamp
                sortedSurveysByDate = surveysCreatedByUser.stream().
                        sorted((s1, s2) -> Timestamp.valueOf(s2.getTimestamp()).
                                compareTo(Timestamp.valueOf(s1.getTimestamp()))).collect(Collectors.toList());
                if (sortedSurveysByDate.size() > 5) {
                    sortedSurveysByDate = sortedSurveysByDate.stream().limit(5).collect(Collectors.toList());
                }
                dashboard.setTopRecentSurveys(sortedSurveysByDate);
            }
            List<CategoryWiseSurvey> categoryWiseSurveys = new ArrayList<>();
            //logic to get category wise survey
            HashMap<String, Integer> categorySurveyMap = new HashMap<>();
            for (Survey s : surveysCreatedByUser) {
                if (categorySurveyMap.get(s.getSurveyCategory()) == null && categorySurveyMap.containsKey(s.getSurveyCategory()) == false) {
                    categorySurveyMap.put(s.getSurveyCategory(), 1);
                } else {
                    categorySurveyMap.put(s.getSurveyCategory(), categorySurveyMap.get(s.getSurveyCategory()) + 1);
                }
            }
            CategoryWiseSurvey categoryWiseSurvey;
            for (Map.Entry<String, Integer> entry : categorySurveyMap.entrySet()) {
                categoryWiseSurvey = new CategoryWiseSurvey();
                categoryWiseSurvey.setCategory(entry.getKey());
                categoryWiseSurvey.setSurveyCount(entry.getValue());
                categoryWiseSurveys.add(categoryWiseSurvey);
            }
            if (categoryWiseSurveys != null && categoryWiseSurveys.size() != 0)
                dashboard.setCategoryWiseSurveys(categoryWiseSurveys);
            else dashboard.setCategoryWiseSurveys(null);
            //category wise responses and views logic goes here
            List<CategoryWiseSurveyViewsAndResponses> categoryWiseSurveyViewsAndResponsesList = new ArrayList<>();
            CategoryWiseSurveyViewsAndResponses categoryWiseSurveyViewsAndResponses = new CategoryWiseSurveyViewsAndResponses();
            // --- modifying code starts
            HashMap<String, Integer> vMap = new HashMap<>();
            HashMap<String, Integer> rMap = new HashMap<>();
            HashMap<String, Boolean> cMap = new HashMap<>();
            for (Survey s : surveysCreatedByUser) {
                int surveyViews = 0;
                int res = 0;
                if (cMap.get(s.getSurveyCategory()) == null) {
                    if (vMap.get(s.getSurveyCategory()) == null) {
                        if (this.viewRepository.findAll() != null) {
                            for (View v : this.viewRepository.findAll()) {
                                if (v.getSurveyId().equals(s.getId())) {
                                    surveyViews = v.getViewCount();
                                    break;
                                }
                            }
                        }
                        vMap.put(s.getSurveyCategory(), surveyViews);
                    } else {
                        if (this.viewRepository.findAll() != null) {
                            for (View v : this.viewRepository.findAll()) {
                                if (v.getSurveyId().equals(s.getId())) {
                                    surveyViews = v.getViewCount();
                                    break;
                                }
                            }
                        }
                        vMap.put(s.getSurveyCategory(), vMap.get(s.getSurveyCategory()) + surveyViews);

                    }
                    if (rMap.get(s.getSurveyCategory()) == null) {
                        if (this.surveyResponsesRepository.findAll() != null) {
                            for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                                if (srs.getSurveyId().equals(s.getId())) {
                                    if (srs.getSurveyResponseList() == null) res = 0;
                                    else res = srs.getSurveyResponseList().size();
                                }
                            }
                        }
                        rMap.put(s.getSurveyCategory(), res);
                    } else {
                        if (this.surveyResponsesRepository.findAll() != null) {
                            for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                                if (srs.getSurveyId().equals(s.getId())) {
                                    if (srs.getSurveyResponseList() == null) res = 0;
                                    else res = srs.getSurveyResponseList().size();
                                }
                            }
                        }
                        rMap.put(s.getSurveyCategory(), rMap.get(s.getSurveyCategory()) + res);

                    }
                    cMap.put(s.getSurveyCategory(), true);
                } else {
                    if (vMap.get(s.getSurveyCategory()) == null) {
                        if (this.viewRepository.findAll() != null) {
                            for (View v : this.viewRepository.findAll()) {
                                if (v.getSurveyId().equals(s.getId())) {
                                    surveyViews = v.getViewCount();
                                    break;
                                }
                            }
                        }
                        vMap.put(s.getSurveyCategory(), surveyViews);
                    } else {
                        if (this.viewRepository.findAll() != null) {
                            for (View v : this.viewRepository.findAll()) {
                                if (v.getSurveyId().equals(s.getId())) {
                                    surveyViews = v.getViewCount();
                                    break;
                                }
                            }
                        }
                        vMap.put(s.getSurveyCategory(), vMap.get(s.getSurveyCategory()) + surveyViews);

                    }
                    if (rMap.get(s.getSurveyCategory()) == null) {
                        if (this.surveyResponsesRepository.findAll() != null) {
                            for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                                if (srs.getSurveyId().equals(s.getId())) {
                                    if (srs.getSurveyResponseList() == null) res = 0;
                                    else res = srs.getSurveyResponseList().size();
                                }
                            }
                        }
                        rMap.put(s.getSurveyCategory(), res);
                    } else {
                        if (this.surveyResponsesRepository.findAll() != null) {
                            for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                                if (srs.getSurveyId().equals(s.getId())) {
                                    if (srs.getSurveyResponseList() == null) res = 0;
                                    else res = srs.getSurveyResponseList().size();
                                }
                            }
                        }
                        rMap.put(s.getSurveyCategory(), rMap.get(s.getSurveyCategory()) + res);

                    }

                }
            }
            for (Map.Entry<String, Integer> entry : vMap.entrySet()) {
                categoryWiseSurveyViewsAndResponses = new CategoryWiseSurveyViewsAndResponses();
                categoryWiseSurveyViewsAndResponses.setSurveyCategory(entry.getKey());
                categoryWiseSurveyViewsAndResponses.setViews(entry.getValue());
                categoryWiseSurveyViewsAndResponses.setNumberOfResponses(rMap.get(entry.getKey()));
                categoryWiseSurveyViewsAndResponsesList.add(categoryWiseSurveyViewsAndResponses);
            }

            // --- modifying code ends

            if (categoryWiseSurveyViewsAndResponsesList == null || categoryWiseSurveyViewsAndResponsesList.size() == 0) {
                dashboard.setCategoryWiseSurveyViewsAndResponsesList(null);
            } else {
                List<String> surveyCategories = new ArrayList<>(Arrays.asList("education", "customer", "events", "hr", "research"));
                List<CategoryWiseSurveyViewsAndResponses> categoryWiseList = new ArrayList<>();
                categoryWiseList = categoryWiseSurveyViewsAndResponsesList;
                boolean flag = false;
                for (String surveyCategory : surveyCategories) {
                    flag = false;
                    for (CategoryWiseSurveyViewsAndResponses cw : categoryWiseSurveyViewsAndResponsesList) {
                        if (cw.getSurveyCategory().equals(surveyCategory)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        CategoryWiseSurveyViewsAndResponses c = new CategoryWiseSurveyViewsAndResponses();
                        c.setSurveyCategory(surveyCategory);
                        c.setViews(0);
                        c.setNumberOfResponses(0);
                        categoryWiseList.add(c);
                    }
                }
                dashboard.setCategoryWiseSurveyViewsAndResponsesList(categoryWiseList);
            }
            //logic to implement month wise survey views and responses
            HashMap<String, String> monthsMap = new HashMap<>();
            monthsMap.put("01", "January");
            monthsMap.put("02", "February");
            monthsMap.put("03", "March");
            monthsMap.put("04", "April");
            monthsMap.put("05", "May");
            monthsMap.put("06", "June");
            monthsMap.put("07", "July");
            monthsMap.put("08", "August");
            monthsMap.put("09", "September");
            monthsMap.put("10", "October");
            monthsMap.put("11", "November");
            monthsMap.put("12", "December");
            String thisYear = new SimpleDateFormat("yyyy").format(new Date());
            List<MonthBasedResponse> monthBasedResponseList = new ArrayList<>();
            HashMap<String, Boolean> monthWiseHashMap = new HashMap<>();
            HashMap<String, Integer> monthWiseViewsMap = new HashMap<>();
            HashMap<String, Integer> monthWiseResponseHashMap = new HashMap<>();
            MonthBasedResponse monthBasedResponse;
            for (Survey s : surveysCreatedByUser) {
                String dateOfCreation = s.getTimestamp().split(" ")[0];
                String monthByInteger = dateOfCreation.substring(5, 7);
                if (dateOfCreation.substring(0, 4).equals(thisYear)) {
                    if (monthWiseHashMap.get(monthsMap.get(monthByInteger)) == null) {
                        monthWiseHashMap.put(monthsMap.get(monthByInteger), true);
                    }
                }
            }
            String m;
            if (monthWiseHashMap.size() > 0) {
                for (Map.Entry<String, Boolean> entry : monthWiseHashMap.entrySet()) {
                    int viewsForMonth = 0;
                    int responsesForMonth = 0;
                    for (Survey s : surveysCreatedByUser) {
                        m = monthsMap.get(s.getTimestamp().split(" ")[0].substring(5, 7));
                        if (m.equals(entry.getKey())) {
                            //logic to get views for month
                            monthBasedResponse = new MonthBasedResponse();
                            if (monthWiseHashMap.get(m) == null) {
                                if (this.viewRepository.findAll() != null) {
                                    for (View v : this.viewRepository.findAll()) {
                                        if (v.getSurveyId().equals(s.getId())) {
                                            viewsForMonth = v.getViewCount();
                                            break;
                                        }
                                    }
                                }
                                if (monthWiseViewsMap.get(m) == null) monthWiseViewsMap.put(m, viewsForMonth);
                                else monthWiseViewsMap.put(m, monthWiseViewsMap.get(m) + viewsForMonth);
                                //code to get responses for month
                                if (this.surveyResponsesRepository.findAll() != null) {
                                    for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                                        if (srs.getSurveyId().equals(s.getId())) {
                                            if (srs.getSurveyResponseList() != null) {
                                                responsesForMonth = srs.getSurveyResponseList().size();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (monthWiseViewsMap.get(m) == null) monthWiseViewsMap.put(m, viewsForMonth);
                                else monthWiseViewsMap.put(m, monthWiseViewsMap.get(m) + viewsForMonth);
                                monthWiseHashMap.put(m, true);
                            }//hashmap if ends
                            else {

                                if (this.viewRepository.findAll() != null) {
                                    for (View v : this.viewRepository.findAll()) {
                                        if (v.getSurveyId().equals(s.getId())) {
                                            viewsForMonth = v.getViewCount();
                                            break;
                                        }
                                    }
                                }
                                if (monthWiseViewsMap.get(m) == null) monthWiseViewsMap.put(m, viewsForMonth);
                                else monthWiseViewsMap.put(m, monthWiseViewsMap.get(m) + viewsForMonth);

                                //code to get responses for month
                                if (this.surveyResponsesRepository.findAll() != null) {
                                    for (SurveyResponses srs : this.surveyResponsesRepository.findAll()) {
                                        if (srs.getSurveyId().equals(s.getId())) {
                                            if (srs.getSurveyResponseList() != null) {
                                                responsesForMonth = srs.getSurveyResponseList().size();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (monthWiseResponseHashMap.get(m) != null)
                                    monthWiseResponseHashMap.put(m, monthWiseResponseHashMap.get(m) + responsesForMonth);
                                else monthWiseResponseHashMap.put(m, responsesForMonth);
                                monthWiseHashMap.put(m, true);

                            }//hashmap else ends
                        }
                    }
                }
                for (Map.Entry<String, Integer> entry1 : monthWiseViewsMap.entrySet()) {
                    monthBasedResponse = new MonthBasedResponse();
                    for (Map.Entry<String, Integer> entry2 : monthWiseResponseHashMap.entrySet()) {
                        if (entry2.getKey().equals(entry1.getKey())) {
                            monthBasedResponse.setMonth(entry1.getKey());
                            monthBasedResponse.setResponsesCount(entry2.getValue());
                            monthBasedResponse.setViews(entry1.getValue());
                            monthBasedResponseList.add(monthBasedResponse);
                        }
                    }
                }
                for (Map.Entry<String, String> entry1 : monthsMap.entrySet()) {
                    boolean monthFound = false;
                    for (Map.Entry<String, Boolean> entry2 : monthWiseHashMap.entrySet()) {
                        if (entry2.getKey().equals(entry1.getValue())) {
                            monthFound = true;
                            break;
                        }
                    }
                    if (!monthFound) {
                        monthBasedResponse = new MonthBasedResponse();
                        monthBasedResponse.setMonth(entry1.getValue());
                        monthBasedResponse.setViews(0);
                        monthBasedResponse.setResponsesCount(0);
                        monthBasedResponseList.add(monthBasedResponse);
                    }
                }
            } else {
                for (Map.Entry<String, String> entry : monthsMap.entrySet()) {
                    monthBasedResponse = new MonthBasedResponse();
                    monthBasedResponse.setMonth(entry.getValue());
                    monthBasedResponse.setViews(0);
                    monthBasedResponse.setResponsesCount(0);
                    monthBasedResponseList.add(monthBasedResponse);
                }
            }
            List<MonthBasedResponse> sortedMonthBasedResponseList = new ArrayList<>();
            List<String> months = new ArrayList<>(Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"));
            for (String month : months) {
                for (MonthBasedResponse mbr : monthBasedResponseList) {
                    if (month.equals(mbr.getMonth())) {
                        sortedMonthBasedResponseList.add(mbr);
                        break;
                    }
                }
            }
            dashboard.setMonthBasedResponseList(sortedMonthBasedResponseList);
            dashboard.setGroupsCount(groupsCount);
            dashboard.setSurveyCategoryResponses(surveyCategoryResponses);
            dashboard.setHuCoins(huCoins);
            dashboard.setOverAllViews(overallViews);
            List<Survey> surveys = new ArrayList<>();
            for (Survey s : surveysCreatedByUser) {
                Survey x = new Survey();
                x.setId(s.getId());
                x.setTimestamp(s.getTimestamp());
                List<Question> questions = new ArrayList<>();
                for (Question q : s.getQuestions()) {
                    Question q1 = new Question();
                    q1.setQuestionCategory(q.getQuestionCategory());
                    questions.add(q1);
                }
                x.setQuestions(questions);
                surveys.add(x);
            }
            dashboard.setSurveys(surveys);
//            dashboard.setSurveys(surveysCreatedByUser);
            dashboard.setSurveysCount(countOfSurveysCreated);
            dashboard.setSurveyResponses(surveyResponsesList);
            dashboard.setAverageTimeTaken(averageTimeTaken);
            dashboard.setSurveyEngagementRates(surveyEngagementRates);
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(dashboard);
            return responseWrapper;
        }
    }

    public boolean sendMail(List<String> userEmail, String surveyId) {
        try {
            int number = ThreadLocalRandom.current().nextInt(100000, 1000000);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            StringBuilder stringBuilder = new StringBuilder();

            StringBuilder message = new StringBuilder();
            message.append("You have been assigned to survey. You can fill out this survey by clicking on this link\n");

            String surveyLink = "https://survey-tool-frontend-dot-hu18-groupa-java.et.r.appspot.com/appearSurvey/" + surveyId;
            Survey survey = null;
            for (Survey s : this.surveyRepository.findAll()) {
                if (s.getId().equals(surveyId)) {
                    survey = s;
                    break;
                }
            }
            if(survey==null) return false;
            String password = "";
            String passwordMessage = "";
            if (survey.getHasPassword() && survey.getPassword().trim().length()>0) {
                passwordMessage = "This survey is protected with password.\n So please use this password while attempting the survey- ";
                password = PasswordUtility.decrypt(survey.getPassword(), survey.getPasswordKey());
            }


            stringBuilder.append("" +
                    "    <style type=\"text/css\">\n" +
                    "        @media screen {\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: normal;\n" +
                    "                font-weight: 400;\n" +
                    "                src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v11/qIIYRU-oROkIk8vfvxw6QvesZW2xOQ-xsNqO47m55DA.woff) format('woff');\n" +
                    "            }\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: normal;\n" +
                    "                font-weight: 700;\n" +
                    "                src: local('Lato Bold'), local('Lato-Bold'), url(https://fonts.gstatic.com/s/lato/v11/qdgUG4U09HnJwhYI-uK18wLUuEpTyoUstqEm5AMlJo4.woff) format('woff');\n" +
                    "            }\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: italic;\n" +
                    "                font-weight: 400;\n" +
                    "                src: local('Lato Italic'), local('Lato-Italic'), url(https://fonts.gstatic.com/s/lato/v11/RYyZNoeFgb0l7W3Vu1aSWOvvDin1pK8aKteLpeZ5c0A.woff) format('woff');\n" +
                    "            }\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: italic;\n" +
                    "                font-weight: 700;\n" +
                    "                src: local('Lato Bold Italic'), local('Lato-BoldItalic'), url(https://fonts.gstatic.com/s/lato/v11/HkF_qI1x_noxlxhrhMQYELO3LdcAZYWl9Si6vvxL-qU.woff) format('woff');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        body,\n" +
                    "        table,\n" +
                    "        td,\n" +
                    "        a {\n" +
                    "            -webkit-text-size-adjust: 100%;\n" +
                    "            -ms-text-size-adjust: 100%;\n" +
                    "        }\n" +
                    "        table,\n" +
                    "        td {\n" +
                    "            mso-table-lspace: 0pt;\n" +
                    "            mso-table-rspace: 0pt;\n" +
                    "        }\n" +
                    "        img {\n" +
                    "            -ms-interpolation-mode: bicubic;\n" +
                    "        }\n" +
                    "        img {\n" +
                    "            border: 0;\n" +
                    "            height: auto;\n" +
                    "            line-height: 100%;\n" +
                    "            outline: none;\n" +
                    "            text-decoration: none;\n" +
                    "        }\n" +
                    "        table {\n" +
                    "            border-collapse: collapse !important;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            height: 100% !important;\n" +
                    "            margin: 0 !important;\n" +
                    "            padding: 0 !important;\n" +
                    "            width: 100% !important;\n" +
                    "        }\n" +
                    "        a[x-apple-data-detectors] {\n" +
                    "            color: inherit !important;\n" +
                    "            text-decoration: none !important;\n" +
                    "            font-size: inherit !important;\n" +
                    "            font-family: inherit !important;\n" +
                    "            font-weight: inherit !important;\n" +
                    "            line-height: inherit !important;\n" +
                    "        }\n" +
                    "        @media screen and (max-width:600px) {\n" +
                    "            h1 {\n" +
                    "                font-size: 32px !important;\n" +
                    "                line-height: 32px !important;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        div[style*=\"margin: 16px 0;\"] {\n" +
                    "            margin: 0 !important;\n" +
                    "        }\n" +
                    "    </style></head>\n");
            stringBuilder.append("" +
                    "<body style=\"background-color: #f4f4f4; margin: 0 !important; padding: 0 !important;\">\n" +
                    "    <!-- HIDDEN PREHEADER TEXT -->\n" +
                    "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                    "        <tr>\n" +
                    "            <td bgcolor=\"#FFA73B\" align=\"center\">\n" +
                    "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                    "                    <tr>\n" +
                    "                        <td align=\"center\" valign=\"top\" style=\"padding: 40px 10px 40px 10px;\"> </td>\n" +
                    "                    </tr>\n" +
                    "                </table>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td bgcolor=\"#FFA73B\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                    "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"center\" valign=\"top\" style=\"padding: 40px 20px 20px 20px; border-radius: 4px 4px 0px 0px; color: #111111; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 48px; font-weight: 400; letter-spacing: 4px; line-height: 48px;\">\n" +
                    "                            <h1 style=\"font-size: 48px; font-weight: 400; margin: 2;\">Welcome!</h1>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                </table>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td bgcolor=\"#f4f4f4\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                    "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                    "                            <p style=\"margin: 0;\">" + message.toString() + "</p>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                    "                            <p style=\"margin: 0;\">" + "<a href="+surveyLink+">Attempt Survey</a>" + "</p>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                    "                            <p style=\"margin: 0;\">" + passwordMessage + "<b>" + password + "</b></p>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                    <tr>\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 0px 30px 40px 30px; border-radius: 0px 0px 4px 4px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                    "                            <p style=\"margin: 0;\">Regards,<br>Team SurWay</p>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                </table>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "    </table>\n" +
                    "</body>");
            if(userEmail!=null)
            {
                for (String email : userEmail) {

                    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
                    mimeMessageHelper.setTo(email);
                    mimeMessageHelper.setFrom("surwaytool@gmail.com");
                    mimeMessageHelper.setSubject("You have new Survey to attempt");
                    mimeMessageHelper.setText(stringBuilder.toString(), true);
                    javaMailSender.send(mimeMessage);

                }
            }


        } catch (Exception exception) {
            return false;
        }
        return true;
    }
    
    @Transactional
    public ResponseWrapper getAllowedUsersForSurvey(String surveyId) throws Exception
    {
       ResponseWrapper responseWrapper=new ResponseWrapper();
       boolean surveyExists=false;
       Survey survey=null;
       if(this.surveyRepository.findAll()!=null)
       {
          for(Survey x: this.surveyRepository.findAll())
          {
              if(x.getId().equals(surveyId))
              {
                surveyExists=true;
                survey=x;
                break;
              }
          }
       }
       if(!surveyExists)
       {
         responseWrapper.setSuccess(false);
         responseWrapper.setResponseCode(404);
         responseWrapper.setHasError(true);
         responseWrapper.setHasException(false);
         responseWrapper.setMessage("Invalid Survey Id");
         return responseWrapper;
       }
       else{
           if(survey==null)
           {
               responseWrapper.setSuccess(false);
               responseWrapper.setResponseCode(404);
               responseWrapper.setHasError(true);
               responseWrapper.setHasException(false);
               responseWrapper.setMessage("Survey is null");
               return responseWrapper;
           }
           else if(survey.getAllowedUsers()==null || survey.getAllowedUsers().size()==0)
           {
               responseWrapper.setSuccess(false);
               responseWrapper.setResponseCode(404);
               responseWrapper.setHasError(true);
               responseWrapper.setHasException(false);
               responseWrapper.setMessage("No Users has assigned to this survey");
               return responseWrapper;
           }
           else{
               responseWrapper.setSuccess(true);
               responseWrapper.setResponseCode(200);
               responseWrapper.setHasError(false);
               responseWrapper.setHasException(false);
               responseWrapper.setMessage(survey.getAllowedUsers());
               return responseWrapper;
           }
           //code to fetch allowed users
       }
    }
    //fetch user history
    @Transactional
    public ResponseWrapper getUserHistory(String userMail) throws Exception
    {
       ResponseWrapper responseWrapper=new ResponseWrapper();
       User u=new User();
       u.setEmail(userMail);
       if(this.userService.getUserByMail(u)==null)
       {
           responseWrapper.setSuccess(false);
           responseWrapper.setResponseCode(404);
           responseWrapper.setHasError(true);
           responseWrapper.setHasException(false);
           responseWrapper.setMessage("Invalid user mail");
           return responseWrapper;
       }
       if(this.historyRepository.findAll()==null)
       {
           responseWrapper.setSuccess(false);
           responseWrapper.setResponseCode(404);
           responseWrapper.setHasError(true);
           responseWrapper.setHasException(false);
           responseWrapper.setMessage("No History exist");
           return responseWrapper;
       }
       //logic to find user history by mail
        History historyToReturn=null;
        for(History history : this.historyRepository.findAll())
        {
           if(history.getUserMail().equals(userMail))
           {
             historyToReturn=history;
             break;
           }
        }
        if(historyToReturn==null)
        {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("No History exist");
            return responseWrapper;
        }
        else{
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(historyToReturn);
            return responseWrapper;
        }
    }
}
