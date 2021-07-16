package com.example.appengine.controller;

import com.example.appengine.services.StatisticsService;
import com.example.appengine.utility.SurveyStatistics;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/hello")
    public String getHello() {
        return "Hello World ! ";
    }

    @GetMapping("/{surveyId}")
    public ResponseWrapper SurveyStatsById(@PathVariable String surveyId) {
        try {
            return this.statisticsService.getSurweyStatsById(surveyId);
        }catch(Exception exception)
        {
           ResponseWrapper responseWrapper=new ResponseWrapper();
           responseWrapper.setSuccess(false);
           responseWrapper.setResponseCode(404);
           responseWrapper.setHasException(true);
           responseWrapper.setHasError(false);
           responseWrapper.setMessage(exception.getMessage());
           return responseWrapper;
        }
    }
}
