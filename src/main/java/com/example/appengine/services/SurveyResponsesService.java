package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.*;
import com.example.appengine.repository.*;
import com.example.appengine.utility.SurveyAnswer;
import com.example.appengine.utility.Tracking;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class SurveyResponsesService {
    @Autowired
    private SurveyResponsesRepository surveyResponsesRepository;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private SurveyRepository surveyRepository;
    private int huCoinsPerSurvey = 2;

    public void incrementHuCoinsWithSurveysRemaining(String mail) {
//     System.out.println("Increment HU Coins");
        if (userRepository.findAll() != null) {
            for (User user : this.userRepository.findAll()) {
                if (user.getEmail().equals(mail)) {
                    //increment hu coins by 2
                    User u = user;
                    u.setHuCoins(user.getHuCoins() + 2);
//             System.out.println(u.getHuCoins());
                    this.userRepository.save(u);
                    break;
                }
            }
        }
    }

    @Transactional
    public ResponseWrapper addSurveyResponse(SurveyResponses surveyResponses) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        boolean matched = false;
        if (this.surveyService.getSurveyById(surveyResponses.getSurveyId()) == null) {
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Invalid Survey Id");
            return responseWrapper;
        }
        if (surveyResponses.getSurveyResponseList() == null || surveyResponses.getSurveyResponseList().size() == 0) {
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Can't add empty response list in the database");
            return responseWrapper;

        }
        if (surveyResponses.getSurveyResponseList().size() != 1) {
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Can't add more than one response in a list at a time");
            return responseWrapper;
        }
        //check for user has already sent the response
        SurveyResponses macthedSurveyResponses = null;
        for (SurveyResponses x : this.surveyResponsesRepository.findAll()) {
            if (x.getSurveyId().equals(surveyResponses.getSurveyId())) {
                macthedSurveyResponses = x;
                break;
            }
        }
        if (macthedSurveyResponses != null) {
            for (SurveyResponse y : surveyResponses.getSurveyResponseList()) {
                matched = false;
                for (SurveyResponse x : macthedSurveyResponses.getSurveyResponseList()) {
                    if (x.getUserMail().equals(y.getUserMail())) {
                        matched = true;
                    }
                }
                if (matched) {
                    break;
                }
            }
        }
        if (matched) {
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("This User has already responded to this survey");
            return responseWrapper;
        }

        //everything is fine
        //get Previous response list if any
        List<SurveyResponse> previousSurveyResponses = new ArrayList<>();
        if (this.surveyResponsesRepository.findAll() != null && this.surveyResponsesRepository.findAll().size() > 0) {
            for (SurveyResponses x : this.surveyResponsesRepository.findAll()) {
                if (x.getSurveyId().equals(surveyResponses.getSurveyId())) {
                    previousSurveyResponses = x.getSurveyResponseList();
                }
            }
        }
        if (previousSurveyResponses.size() == 0) {
            previousSurveyResponses = surveyResponses.getSurveyResponseList();
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            surveyResponses.getSurveyResponseList().get(0).setTimestamp(timestamp.toString());
//            previousSurveyResponses.add(surveyResponses.getSurveyResponseList().get(0));
        } else {
            //if previous responses is not null
            double sumOfTimes = 0;
            if (surveyResponses.getSurveyResponseList().get(0).getActualTimeTaken() == null ||
                    surveyResponses.getSurveyResponseList().get(0).getActualTimeTaken().trim().length() == 0) {
                if (surveyResponses.getSurveyResponseList().get(0).getSurveyAnswers() == null) {
                    sumOfTimes = 0;
                } else {
                    for (SurveyAnswer sa : surveyResponses.getSurveyResponseList().get(0).getSurveyAnswers()) {
                        sumOfTimes += sa.getTimeTaken();
                    }
                }
                int a = (int) sumOfTimes;
                surveyResponses.getSurveyResponseList().get(0).setActualTimeTaken(String.valueOf(a));
            }

            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            surveyResponses.getSurveyResponseList().get(0).setTimestamp(timestamp.toString());
            previousSurveyResponses.add(surveyResponses.getSurveyResponseList().get(0));
        }
        //check if user is replying to it's own survey
        surveyResponses.setSurveyResponseList(previousSurveyResponses);
        Survey survey = this.surveyService.getSurveyById(surveyResponses.getSurveyId());
        if (surveyResponses.getSurveyResponseList() != null && surveyResponses.getSurveyResponseList().size() == 1) {
            if (survey.getUserEmail().equals(surveyResponses.getSurveyResponseList().get(0).getUserMail())) {
                responseWrapper.setResponseCode(404);
                responseWrapper.setSuccess(false);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("User can't respond on it's own survey");
                return responseWrapper;
            }
        }
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        surveyResponses.setLastResponseTime(timestamp.toString());
        //if survey is public then increment surveys remaining and hu coins as well
        if ((survey.getAllowedUsers() == null || survey.getAllowedUsers().size() == 0) && survey.getHasPassword() == false) {
            this.incrementHuCoinsWithSurveysRemaining(surveyResponses.getSurveyResponseList().get(0).getUserMail());
        }
        double sumOfTimes = 0;
        if (surveyResponses.getSurveyResponseList().get(0).getActualTimeTaken() == null ||
                surveyResponses.getSurveyResponseList().get(0).getActualTimeTaken().trim().length() == 0) {
            if (surveyResponses.getSurveyResponseList().get(0).getSurveyAnswers() == null) {
                sumOfTimes = 0;
            } else {
                for (SurveyAnswer sa : surveyResponses.getSurveyResponseList().get(0).getSurveyAnswers()) {
                    sumOfTimes += sa.getTimeTaken();
                }
            }
            int a = (int) sumOfTimes;

            surveyResponses.getSurveyResponseList().get(0).setTimestamp(timestamp.toString());
            surveyResponses.getSurveyResponseList().get(0).setActualTimeTaken(String.valueOf(a));
        }

        date = new Date();
        timestamp = new Timestamp(date.getTime());
        History history = new History();
        history.setUserMail(survey.getUserEmail());
        List<Tracking> trackings = null;
        if (this.historyRepository.findAll() != null) {
            for (History history1 : this.historyRepository.findAll()) {
                if (history1.getUserMail().equals(survey.getUserEmail())) {
                    trackings = history1.getTrackingList();
                    break;
                }
            }
        }
        if (trackings != null) {
            Tracking tracking = new Tracking();
            tracking.setTimestamp(timestamp.toString());
            Survey s = this.surveyService.getSurveyById(surveyResponses.getSurveyId());
            tracking.setActivity("You have responded to survey " + s.getName());
            trackings.add(tracking);
            history.setTrackingList(trackings);
            this.historyRepository.save(history);
        } else {
            Tracking tracking = new Tracking();
            tracking.setTimestamp(timestamp.toString());
            Survey s = this.surveyService.getSurveyById(surveyResponses.getSurveyId());
            tracking.setActivity("You have responded to survey " + s.getName());
            history.setTrackingList(Arrays.asList(tracking));
            this.historyRepository.save(history);
        }
        //logic to check if a survey is responded publicly or not
        boolean isSurveyPublic = false;
        if (this.surveyRepository.findAll() != null) {
               for(Survey s : this.surveyRepository.findAll())
               {
                  if(s.getId().equals(surveyResponses.getSurveyId()))
                  {
                    //logic to check if survey is public or not
                    if(s.getHasPassword()==false && (s.getAllowedUsers()==null || s.getAllowedUsers().size()==0))
                    {
                      isSurveyPublic=true;
                      break;
                    }
                  }
               }
        }
        this.surveyResponsesRepository.save(surveyResponses);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasError(false);
        if(isSurveyPublic) responseWrapper.setMessage("Your response has been recorded on public survey");
        else
        {
            responseWrapper.setMessage("Response has been Recorded successfully on non public survey");
        }
        return responseWrapper;
    }

    @Transactional
    public SurveyResponses getSurveyResponses(SurveyResponses surveyResponses) throws SurwayException {
        if (this.surveyResponsesRepository.findAll() == null || this.surveyResponsesRepository.findAll().size() == 0) {
            return null;
        }
        //check for valid survey id
        boolean validSurveyId = false;
        SurveyResponses surveyResponsesToReturn = null;
        for (SurveyResponses x : this.surveyResponsesRepository.findAll()) {
            if (surveyResponses.getSurveyId().equals(x.getSurveyId())) {
                surveyResponsesToReturn = x;
                validSurveyId = true;
                break;
            }
        }
        if (!validSurveyId) return null;
        return surveyResponsesToReturn;
    }
}
