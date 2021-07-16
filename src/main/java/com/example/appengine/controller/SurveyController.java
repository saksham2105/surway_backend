package com.example.appengine.controller;

import com.example.appengine.model.*;
import com.example.appengine.repository.HistoryRepository;
import com.example.appengine.repository.SurveyResponsesRepository;
import com.example.appengine.services.SurveyResponsesService;
import com.example.appengine.services.SurveyService;
import com.example.appengine.utility.Tracking;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/survey")

public class SurveyController {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    SurveyResponsesService surveyResponsesService;
    @Autowired
    SurveyResponsesRepository surveyResponsesRepository;
    @Autowired
    private HistoryRepository historyRepository;

    @PostMapping("/add")
    public ResponseWrapper addSurvey(@RequestBody Survey survey) {
        try {
            //return new ResponseWrapper();
            ResponseWrapper responseWrapper = this.surveyService.addSurvey(survey);
            if (responseWrapper.isSuccess()) {
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
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
                    tracking.setActivity("You have created a new survey");
                    trackings.add(tracking);
                    history.setTrackingList(trackings);
                    this.historyRepository.save(history);
                } else {
                    Tracking tracking = new Tracking();
                    tracking.setTimestamp(timestamp.toString());
                    tracking.setActivity("You have created a new survey ");
                    history.setTrackingList(Arrays.asList(tracking));
                    this.historyRepository.save(history);
                }
            }
            return responseWrapper;

        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/isSurveyEnabled/{surveyId}")
    public ResponseWrapper isSurveyEnabled(@PathVariable String surveyId) {
        try {
            return this.surveyService.isSurveyEnabled(surveyId);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/assignSurveyUsingGroup/{groupId}/{surveyId}")
    public ResponseWrapper assignSurveyUsingGroup(@PathVariable String groupId, @PathVariable String surveyId) {
        try {
            ResponseWrapper responseWrapper = this.surveyService.assignSurveyUsingGroup(groupId, surveyId);
            return responseWrapper;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/getSurveyById/{surveyId}")
    public ResponseWrapper getSurveyById(@PathVariable String surveyId) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            Survey survey = null;
            survey = this.surveyService.getSurveyById(surveyId);
            if (survey == null) {
                responseWrapper.setResponseCode(404);
                responseWrapper.setSuccess(false);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("Invalid Survey Id");
                return responseWrapper;
            } else {
                responseWrapper.setResponseCode(200);
                responseWrapper.setSuccess(true);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage(survey);
                return responseWrapper;
            }
        } catch (Exception exception) {
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/editSurvey")
    public ResponseWrapper editSurvey(@RequestBody Survey survey) {
        try {
            //return new ResponseWrapper();
            ResponseWrapper responseWrapper = this.surveyService.editSurvey(survey);
            return responseWrapper;
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/enableSurvey/{surveyId}")
    public ResponseWrapper enableSurvey(@PathVariable String surveyId) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            if (!this.surveyService.enableSurvey(surveyId)) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("Survey is already enabled of either survey id doesn'e exist");
                return responseWrapper;
            } else {
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("Survey enabled successfully");
                return responseWrapper;
            }
            //yet to implement

        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/disableSurvey/{surveyId}")
    public ResponseWrapper disableSurvey(@PathVariable String surveyId) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            if (!this.surveyService.disableSurvey(surveyId)) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("Survey is already disabled of either survey id doesn'e exist");
                return responseWrapper;
            } else {
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("Survey disabled successfully");
                return responseWrapper;
            }
            //yet to implement

        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/getAssignedSurveyToUser")
    public ResponseWrapper getAssignedSurveyToUser(@RequestBody User user) {
        //yet to implement
        try {
            List<Survey> surveys = new ArrayList<>();
            List<AssignedSurveys> assignedSurveysList = this.surveyService.getAllAssignedSurveys();
            if (assignedSurveysList == null || assignedSurveysList.size() == 0) {
                ResponseWrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No Surveys are available in the database");
                return responseWrapper;

            }
            for (AssignedSurveys assignedSurveys : assignedSurveysList) {
                if (assignedSurveys.getToUsers() != null) {
                    for (String mail : assignedSurveys.getToUsers()) {
                        if (mail.equals(user.getEmail())) {
                            if (this.surveyService.getSurveyById(assignedSurveys.getSurveyId()) != null) {
                                surveys.add(this.surveyService.getSurveyById(assignedSurveys.getSurveyId()));
                            }
                        }
                    }
                }
            }
            if (surveys != null && surveys.size() != 0) {
                //check if survey is already attempted by user
                //logic to check user has already attempted that survey starts
                List<Survey> assignedSurveysToReturn = new ArrayList<>();
                List<SurveyResponse> surveyResponsesList = null;
                boolean isUserAttemptedTheSurvey = false;
                for (Survey survey : surveys) {
                    isUserAttemptedTheSurvey = false;
                    surveyResponsesList=null;
                    for (SurveyResponses surveyResponses : this.surveyResponsesRepository.findAll()) {
                        if (survey.getId().equals(surveyResponses.getSurveyId())) {
                            surveyResponsesList = surveyResponses.getSurveyResponseList();
                            break;
                        }
                    }
                    if (surveyResponsesList != null) {
                        for (SurveyResponse sr : surveyResponsesList) {
                            if (sr.getUserMail().equals(user.getEmail())) {
                                isUserAttemptedTheSurvey = true;
                                break;
                            }
                        }
                    }
                    if (!isUserAttemptedTheSurvey) {
                        assignedSurveysToReturn.add(survey);
                    }

                }
                //logic to check user has already attempted that survey ends
                if (assignedSurveysToReturn == null || assignedSurveysToReturn.size() == 0) {
                    ResponseWrapper responseWrapper = new ResponseWrapper();
                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasException(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setMessage("No Assigned Surveys available");
                    return responseWrapper;
                }

                ResponseWrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(assignedSurveysToReturn);
                return responseWrapper;
            } else {
                ResponseWrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No Surveys has assigned to this user");
                return responseWrapper;
            }

        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/getSurveyByMail")
    public ResponseWrapper getSurveyByMail(@RequestBody User user) {
        try {
            return this.surveyService.getUserSurvey(user);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/assignSurvey")
    public ResponseWrapper assignSurvey(@RequestBody AssignedSurveys assignedSurveys) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            responseWrapper = surveyService.addToAssignedSurveys(assignedSurveys);
            if (responseWrapper.isSuccess()) {
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                History history = new History();
                history.setUserMail(assignedSurveys.getFromUser());
                List<Tracking> trackings = null;
                if (this.historyRepository.findAll() != null) {
                    for (History history1 : this.historyRepository.findAll()) {
                        if (history1.getUserMail().equals(assignedSurveys.getFromUser())) {
                            trackings = history1.getTrackingList();
                            break;
                        }
                    }
                }
                if (trackings != null) {
                    Tracking tracking = new Tracking();
                    tracking.setTimestamp(timestamp.toString());
                    tracking.setActivity("You have assigned survey to users");
                    trackings.add(tracking);
                    history.setTrackingList(trackings);
                    this.historyRepository.save(history);
                } else {
                    Tracking tracking = new Tracking();
                    tracking.setTimestamp(timestamp.toString());
                    tracking.setActivity("You have assigned survey to users");
                    history.setTrackingList(Arrays.asList(tracking));
                    this.historyRepository.save(history);
                }
            }

            return responseWrapper;
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage(exception.getMessage());
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            return responseWrapper;
        }
    }

    @PostMapping("/deassignSurvey")
    public ResponseWrapper deassignSurvey(@RequestBody AssignedSurveys assignedSurveys) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            return surveyService.deassignSurvey(assignedSurveys);
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage(exception.getMessage());
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            return responseWrapper;
        }
    }

    //one response will come at a time in array
    @PostMapping("/saveResponse")
    public ResponseWrapper saveResponse(@RequestBody SurveyResponses surveyResponses) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            responseWrapper = this.surveyResponsesService.addSurveyResponse(surveyResponses);
            return responseWrapper;
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage(exception.getMessage());
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            return responseWrapper;
        }
    }

    @PostMapping("/getSurveyResponses")
    public ResponseWrapper getSurveyResponses(@RequestBody SurveyResponses surveyResponses) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            if (this.surveyResponsesService.getSurveyResponses(surveyResponses) == null || this.surveyResponsesService.getSurveyResponses(surveyResponses).getSurveyResponseList() == null) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No Survey Response exist against this survey");
                return responseWrapper;
            } else {
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(this.surveyResponsesService.getSurveyResponses(surveyResponses));
                return responseWrapper;
            }
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/incrementSurveyViewCount")
    public ResponseWrapper incrementSurveyViewCount(@RequestBody View view) {
        try {
            return this.surveyService.incrementSurveyViewCount(view);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/deleteSurvey/{surveyId}")
    public ResponseWrapper deleteSurvey(@PathVariable String surveyId) {
        try {
            ResponseWrapper responseWrapper = this.surveyService.deleteSurvey(surveyId);
            return responseWrapper;
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/getViewsOfSurvey")
    public ResponseWrapper getSurveyViews(@RequestBody View view) {
        try {
            return this.getSurveyViews(view);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/addToGroup")
    public ResponseWrapper addToGroup(@RequestBody Group group) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            responseWrapper = this.surveyService.addToGroup(group);
            if (responseWrapper.isSuccess()) {
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                History history = new History();
                history.setUserMail(group.getUserMail());
                List<Tracking> trackings = null;
                if (this.historyRepository.findAll() != null) {
                    for (History history1 : this.historyRepository.findAll()) {
                        if (history1.getUserMail().equals(group.getUserMail())) {
                            trackings = history1.getTrackingList();
                            break;
                        }
                    }
                }
                if (trackings != null) {
                    Tracking tracking = new Tracking();
                    tracking.setTimestamp(timestamp.toString());
                    tracking.setActivity("You have created a new group");
                    trackings.add(tracking);
                    history.setTrackingList(trackings);
                    this.historyRepository.save(history);
                } else {
                    Tracking tracking = new Tracking();
                    tracking.setTimestamp(timestamp.toString());
                    tracking.setActivity("You have created a new group");
                    history.setTrackingList(Arrays.asList(tracking));
                    this.historyRepository.save(history);
                }
            }

            return responseWrapper;
        } catch (Exception exception) {
            responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/editGroup")
    public ResponseWrapper editGroup(@RequestBody Group group) {
        try {
            ResponseWrapper responseWrapper = this.surveyService.editGroup(group);
            return responseWrapper;
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }

    }

    @PostMapping("/getMyGroups")
    public ResponseWrapper getMyGroups(@RequestBody User user) {
        try {
            return this.surveyService.getMyGroups(user);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/deleteGroup")
    public ResponseWrapper deleteGroup(@RequestBody Group group) {
        try {
            ResponseWrapper responseWrapper = this.surveyService.deleteGroup(group);
            return responseWrapper;
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/getAllMembersInMyGroup")
    public ResponseWrapper getAllMembersInMyGroup(@RequestBody Group group) {
        try {
            return this.surveyService.getAllMembersInMyGroup(group);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/getAllGroupsAssignedToMe")
    public ResponseWrapper getAllGroupsAssignedToMe(@RequestBody User user) {
        try {
            return this.surveyService.getAllGroupsAssignedToMe(user);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/getAllPublicSurveys/{mail}")
    public ResponseWrapper getAllPublicSurveys(@PathVariable String mail) {
        try {
            return this.surveyService.getAllPublicSurveys(mail);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }

    }

    @GetMapping("/getAllPrivateSurveys/{mail}")
    public ResponseWrapper getAllPrivateSurveys(@PathVariable String mail) {
        try {
            return this.surveyService.getAllPrivateSurveys(mail);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }

    }

    @GetMapping("/getDashboardDetailsOfUser/{mail}")
    public ResponseWrapper getDashboardDetailsOfUser(@PathVariable String mail) {
        try {
            return this.surveyService.getDashboardDetailsOfUser(mail);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/sendMail/{surveyId}")
    public ResponseWrapper sendMail(@PathVariable String surveyId, @RequestBody AssignedSurveys assignedSurveys) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            if (this.surveyService.sendMail(assignedSurveys.getToUsers(), surveyId)) {
                responseWrapper.setMessage("Mail sent successfully");
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setSuccess(true);
                return responseWrapper;
            } else {
                responseWrapper.setMessage("Mail couldn't sent");
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setSuccess(false);
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

    @GetMapping("/getAllowedUsersForSurvey/{surveyId}")
    public ResponseWrapper getAllowedUsersForSurvey(@PathVariable String surveyId) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            return this.surveyService.getAllowedUsersForSurvey(surveyId);
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @GetMapping("/getUserHistory/{mail}")
    public ResponseWrapper getUserHistory(@PathVariable String mail) throws Exception {
        try {
            return this.surveyService.getUserHistory(mail);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping("/addToHistory")
    public ResponseWrapper addToHistory(@RequestBody History history) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        List<Tracking> trackings = null;
        if (this.historyRepository.findAll() != null) {
            for (History history1 : this.historyRepository.findAll()) {
                if (history1.getUserMail().equals(history.getUserMail())) {
                    trackings = history1.getTrackingList();
                    break;
                }
            }
        }
        if (trackings != null) {
            Tracking tracking = history.getTrackingList().get(0);
            tracking.setTimestamp(timestamp.toString());
            trackings.add(tracking);
            history.setTrackingList(trackings);
            this.historyRepository.save(history);
        } else {
            Tracking tracking = history.getTrackingList().get(0);
            tracking.setTimestamp(timestamp.toString());
            history.setTrackingList(Arrays.asList(tracking));
            this.historyRepository.save(history);
        }
        responseWrapper.setResponseCode(200);
        responseWrapper.setSuccess(true);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage("Added in history successfully");
        return responseWrapper;
    }

    @GetMapping("/generateCaptcha")
    public ResponseWrapper generateCaptcha() {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            int width = 150;
            int height = 50;
            List arrayList = new ArrayList();
            String capcode = "abcdefghijklmnopqurstuvwxyzABCDEFGHIJKLMONOPQURSTUVWXYZ0123456789";
            for (int i = 1; i < capcode.length() - 1; i++) {
                arrayList.add(capcode.charAt(i));
            }
            Collections.shuffle(arrayList);
            Iterator itr = arrayList.iterator();
            String s = "";
            String s2 = "";
            Object obj;
            while (itr.hasNext()) {
                obj = itr.next();
                s = obj.toString();
                s2 = s2 + s;
            }
            String s1 = s2.substring(0, 6);
            char[] s3 = s1.toCharArray();
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            Font font = new Font("Georgia", Font.BOLD, 20);
            g2d.setFont(font);
            RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHints(rh);
            GradientPaint gp = new GradientPaint(0, 0, new Color(0,99,91), 0, height / 2, Color.black, true);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(new Color(255, 153, 0));
            Random r = new Random();
            int index = Math.abs(r.nextInt()) % 5;
            String captchaCode = String.copyValueOf(s3);
            int x = 0;
            int y = 0;
            for (int i = 0; i < s3.length; i++) {
                x += 10 + (Math.abs(r.nextInt()) % 15);
                y = 20 + Math.abs(r.nextInt()) % 20;
                g2d.drawChars(s3, i, 1, x, y);
            }
            g2d.dispose();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            String result = Base64.getEncoder().encodeToString(os.toByteArray());
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            Captcha captcha = new Captcha();
            captcha.setCaptchaCode(captchaCode);
            captcha.setCaptchaImage(result);
            responseWrapper.setMessage(captcha);
            return responseWrapper;
        } catch (Exception exception) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }
    }
}
