package com.example.appengine.controller;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.QuestionCategory;
import com.example.appengine.services.QuestionCategoryService;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/questionCategory")

public class QuestionCategoryController {

    @Autowired
    private QuestionCategoryService questionCategoryService;

    @GetMapping("/get")
    public ResponseWrapper getAllQuestionCategories() {
        return this.questionCategoryService.getAllCategories();
    }

    @PostMapping("/add")
    public ResponseWrapper addCategory(@RequestBody QuestionCategory questionCategory) throws SurwayException {
        if (questionCategory == null) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(204);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Question Category is Null !");
            return responseWrapper;
        }

        return this.questionCategoryService.addQuestionCategory(questionCategory);
    }
}
