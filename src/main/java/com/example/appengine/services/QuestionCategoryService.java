package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.QuestionCategory;
import com.example.appengine.model.SurveyCategory;
import com.example.appengine.repository.QuestionCategoryRepository;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionCategoryService {
    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;


    public ResponseWrapper addQuestionCategory(QuestionCategory questionCategory) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();

        try {


            //check if category already exist in database
            List<QuestionCategory> questionCategoryList = this.questionCategoryRepository.findAll();

            if (questionCategoryList != null && questionCategoryList.size() > 0) {

                List<QuestionCategory> questionCategory1 = questionCategoryList.
                        stream().filter((x) -> x.getCategoryName()
                        .equalsIgnoreCase(questionCategory.getCategoryName())).
                        collect(Collectors.toList());
                if (questionCategory1.size() > 0) {
                    //a survey category name already exists

                    responseWrapper.setSuccess(false);
                    responseWrapper.setResponseCode(404);
                    responseWrapper.setHasError(true);
                    responseWrapper.setHasException(false);
                    responseWrapper.setMessage("This Question Category already exist");

                } else {
                    //survey category exist so add it
                    String id = UUID.randomUUID().toString();
                    questionCategory.setId(id);
                    this.questionCategoryRepository.save(questionCategory);

                    responseWrapper.setSuccess(true);
                    responseWrapper.setHasException(false);
                    responseWrapper.setResponseCode(200);
                    responseWrapper.setHasError(false);
                    responseWrapper.setMessage("Question Category added successfully");

                }

            } else {
                //this means no survey category is in the list and
                String id = UUID.randomUUID().toString();
                questionCategory.setId(id);
                this.questionCategoryRepository.save(questionCategory);

                responseWrapper.setSuccess(true);
                responseWrapper.setHasException(false);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("Question Category added successfully");

            }


        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(e.getMessage());

        }

        return responseWrapper;
    }

    public ResponseWrapper getAllCategories() {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {

            List<QuestionCategory> questionCategoryList = this.questionCategoryRepository.findAll();
            if (questionCategoryList.size() == 0) {

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No Question category exist. Please add one.");

            } else {

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(302);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(questionCategoryList);

            }

        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(e.getMessage());
        }

        return responseWrapper;
    }
}
