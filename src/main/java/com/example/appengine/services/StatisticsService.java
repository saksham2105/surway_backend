package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.*;
import com.example.appengine.repository.SurveyRepository;
import com.example.appengine.repository.PuchasedTransactionRepository;
import com.example.appengine.repository.SurveyResponsesRepository;
import com.example.appengine.repository.ViewRepository;
import com.example.appengine.utility.SurveyAnswer;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class StatisticsService {

    @Autowired
    private SurveyResponsesRepository surveyResponsesRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private PuchasedTransactionRepository puchasedTransactionRepository;

    public ResponseWrapper getSurweyStatsById(String surveyId) throws Exception {

        ResponseWrapper responseWrapper = new ResponseWrapper();
        EachSurveyStat surveyStat = new EachSurveyStat();
        String reportStatus ="lock";
        String name = "";
        String categoryname="";
        String time ="";
//        System.out.println("dnbdjhjhdg");

        try {
            //if survey with surveyId doesnot exist or invalid survey id
            if (!this.surveyRepository.existsById(surveyId) || surveyId==null || surveyId.length()==0) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("Survey with this Id: " + surveyId + " Does not exist");
                return responseWrapper;

            } else {

                List<Survey> list  =  this.surveyRepository.findAll();
                for(Survey s:list){
                    if(s.getId().equals(surveyId)){
                        name =s.getName();
                        categoryname =s.getSurveyCategory();
                        time=s.getTimestamp();
                    }
                }

                //getting survey view here
                if (this.viewRepository.existsById(surveyId)) {
                    for (View v : this.viewRepository.findAll()) {
                        if (v.getSurveyId().equals(surveyId)) {
                            surveyStat.setViews(v.getViewCount());
                            break;
                        }
                    }
                    //logic to add participants
                    List<SurveyResponse> surveyResponseList=new ArrayList<>();; //survey Response list
                    List<SurveyResponses> surveyResponsesList = this.surveyResponsesRepository.findAll(); //various survey response list
                    if(this.surveyRepository.findAll()!=null)
                    {
                     for(Survey s : this.surveyRepository.findAll())
                     {
                         if(s.getId().equals(surveyId))
                         {
                           surveyStat.setName(s.getName());
                           surveyStat.setSurveyCategory(s.getSurveyCategory());
                           surveyStat.setSurveyCreatedOn(s.getTimestamp());
                           surveyStat.setSurveyStatus(s.getActive());
                           break;
                         }
                     }
                    }
                    if (this.surveyResponsesRepository.existsById(surveyId)) {
                        for (SurveyResponses surveyResponses : surveyResponsesList) {
                            if (surveyResponses.getSurveyId().equals(surveyId)) {
                                surveyStat.setLastResponseTime(surveyResponses.getLastResponseTime());
                                surveyStat.setResponses(surveyResponses.getSurveyResponseList().size());
                                surveyResponseList.addAll(surveyResponses.getSurveyResponseList()); //adding all reponses of particular survey
                                break;
                            }
                        }
//                        System.out.println(surveyResponseList.size());
                        surveyStat.setSurveyResponses(surveyResponseList); //survey response list
                        Double cr = (double) surveyStat.getResponses() / surveyStat.getViews(); //complettion rate
                        surveyStat.setCompletionRate(cr);
//                        System.out.println(cr);
                        Integer totalTime = 0;
                        for(SurveyResponse s:surveyResponseList){
                            String[] splittedString = s.getActualTimeTaken().split(" ");
                            totalTime += Integer.parseInt(splittedString[0]);
                        }
                        Double avgTime=0.0;
                        if(surveyResponseList.size()!=0) avgTime=(double)totalTime/surveyResponseList.size();
                        surveyStat.setAvgSurveyTime(avgTime);
//

                    } else {
                        if(this.puchasedTransactionRepository.findAll().size()>0){
                            for(PurchaseTransaction pt:this.puchasedTransactionRepository.findAll()){
                                if(pt.getPurchaseType().equals("report") && pt.getPurchaseTypeId().equals(surveyId)){
                                    reportStatus="unlock";
                                    break;
                                }
                            }
                        }


                        surveyStat.setReportStatus(reportStatus);
                        surveyStat.setLastResponseTime("");
                        surveyStat.setName(name);
                        surveyStat.setSurveyCategory(categoryname);
                        surveyStat.setSurveyCreatedOn(time);
                        surveyStat.setResponses(0);
                        surveyStat.setAvgSurveyTime((double) 0);
                    }
                } else {
                    if(this.puchasedTransactionRepository.findAll().size()>0){
                        for(PurchaseTransaction pt:this.puchasedTransactionRepository.findAll()){
                            if(pt.getPurchaseType().equals("report") && pt.getPurchaseTypeId().equals(surveyId)){
                                reportStatus="unlock";
                                break;
                            }
                        }
                    }


                    surveyStat.setReportStatus(reportStatus);
                    surveyStat.setViews(0);
                    surveyStat.setName(name);
                    surveyStat.setSurveyCategory(categoryname);
                    surveyStat.setSurveyCreatedOn(time);
                    surveyStat.setLastResponseTime("");
                    surveyStat.setResponses(0);
                }

                //adding report status here
                // 0 means lock
                if(this.puchasedTransactionRepository.findAll().size()>0){
                    for(PurchaseTransaction pt:this.puchasedTransactionRepository.findAll()){
                        if(pt.getPurchaseType().equals("report") && pt.getPurchaseTypeId().equals(surveyId)){
                            reportStatus="unlock";
                            break;
                        }
                    }
               }


                surveyStat.setReportStatus(reportStatus);
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(surveyStat);


            }


        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(e.getMessage());

        }

        return responseWrapper;
    }

}
