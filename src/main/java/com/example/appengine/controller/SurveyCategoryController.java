package com.example.appengine.controller;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.SurveyCategory;
import com.example.appengine.services.SurveyCategoryService;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/surveyCategory")

public class SurveyCategoryController {

    @Autowired
    private SurveyCategoryService surveyCategoryService;

    @GetMapping("/get")
    public ResponseWrapper getSurveyCategories() {
        return this.surveyCategoryService.getCategories();
    }

    @PostMapping("/add")
    public ResponseWrapper addCategory(@RequestBody SurveyCategory surveyCategory) throws SurwayException {
        if (surveyCategory == null) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(204);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Survey Category is Null !");
            return responseWrapper;
        }
        return this.surveyCategoryService.addSurveyCategory(surveyCategory);
    }
}
