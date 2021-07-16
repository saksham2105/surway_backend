package com.example.appengine.services;

import com.example.appengine.model.Question;
import com.example.appengine.repository.QuestionRepository;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    public ResponseWrapper addQuestion(Question question) {
        ResponseWrapper responseWrapper = new ResponseWrapper();

        try {

            if (question == null) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("Question is null");

            } else {
                this.questionRepository.save(question);

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(201);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("Question added successfully");

            }

        } catch (Exception e) {

            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(e.getMessage());

        }
        return responseWrapper;
    }

}
