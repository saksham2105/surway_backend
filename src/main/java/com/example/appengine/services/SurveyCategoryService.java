package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.SurveyCategory;
import com.example.appengine.repository.SurveyCategoryRepository;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SurveyCategoryService {
    @Autowired
    private SurveyCategoryRepository surveyCategoryRepository;

    public ResponseWrapper addSurveyCategory(SurveyCategory surveyCategory) {
        ResponseWrapper responseWrapper = new ResponseWrapper();

        try {


            //check if category already exist in database
            List<SurveyCategory> surveyCategoryList = this.surveyCategoryRepository.findAll();
            if (surveyCategoryList != null && surveyCategoryList.size() > 0) {


                List<SurveyCategory> surveyCategory1 = surveyCategoryList.
                        stream().filter((x) -> x.getCategoryName()
                        .equalsIgnoreCase(surveyCategory.getCategoryName())).
                        collect(Collectors.toList());
                if (surveyCategory1.size() > 0) {
                    //a survey category name already exists

                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(302);
                    responseWrapper.setHasError(true);
                    responseWrapper.setHasException(false);
                    responseWrapper.setMessage("This Survey Category already exist");

                } else {
                    //survey category exist so add it
                    String id = UUID.randomUUID().toString();
                    surveyCategory.setId(id);
                    this.surveyCategoryRepository.save(surveyCategory);

                    responseWrapper.setSuccess(true);
                    responseWrapper.setHasException(false);
                    responseWrapper.setResponseCode(201);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage("Survey Category added successfully");

                }
            } else {
                System.out.println(surveyCategory);
                //this means no survey category is in the list
                String id = UUID.randomUUID().toString();
                surveyCategory.setId(id);
                this.surveyCategoryRepository.save(surveyCategory);

                responseWrapper.setSuccess(true);
                responseWrapper.setHasException(false);
                responseWrapper.setResponseCode(201);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("Survey Category added successfully");

            }


        } catch (Exception e) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(e.getMessage());
        }

        return responseWrapper;
    }

    public ResponseWrapper getCategories() {
        ResponseWrapper responseWrapper = new ResponseWrapper();

        try {

            List<SurveyCategory> surveyCategoryList = this.surveyCategoryRepository.findAll();

            if (surveyCategoryList.size() == 0) {

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No Categories exist for survey please add one.");

            } else {

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(302);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(surveyCategoryList);

            }

        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(e.getMessage());

        }

        return responseWrapper;
    }
}
