package com.example.appengine.model;
import com.example.appengine.utility.*;
import lombok.Data;
import lombok.ToString;
import java.util.List;
@Data
@ToString
public class Dashboard
{
    private Integer surveysCount;
    private List<Survey> surveys;
    private List<SurveyResponses> surveyResponses;
    private List<SurveyStatistics> averageTimeTaken;
    private List<SurveyEngagementRate> surveyEngagementRates;
    private Integer huCoins;
    private Integer overAllViews;
    private Integer groupsCount;
    private List<SurveyCategoryResponse> surveyCategoryResponses;
    private List<Survey> topRecentSurveys;
    private List<CategoryWiseSurvey> categoryWiseSurveys;
    private List<CategoryWiseSurveyViewsAndResponses> categoryWiseSurveyViewsAndResponsesList;
    private List<MonthBasedResponse> monthBasedResponseList;
    public Dashboard()
    {
      this.surveysCount=0;
      this.surveys=null;
      this.surveyResponses=null;
      this.averageTimeTaken=null;
      this.surveyEngagementRates=null;
      this.huCoins=0;
      this.overAllViews=0;
      this.groupsCount=0;
      this.surveyCategoryResponses=null;
      this.topRecentSurveys=null;
      this.categoryWiseSurveys=null;
      this.categoryWiseSurveyViewsAndResponsesList=null;
      this.monthBasedResponseList=null;
    }
}

