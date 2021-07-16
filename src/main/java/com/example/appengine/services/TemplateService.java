package com.example.appengine.services;

import com.example.appengine.model.PurchaseTransaction;
import com.example.appengine.model.Template;
import com.example.appengine.model.TemplatePojo;
import com.example.appengine.repository.PuchasedTransactionRepository;
import com.example.appengine.repository.TemplateRepository;
import com.example.appengine.utility.TemplateDummyPojo;
import com.example.appengine.wrapper.ResponseWrapper;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private PuchasedTransactionRepository puchasedTransactionRepository;
    public ResponseWrapper addTemplate(Template template) {
        ResponseWrapper responseWrapper = new ResponseWrapper();

        try {

            List<Template> templateList = this.templateRepository.findAll();

            if (templateList.size() > 0) {

                List<Template> templateList2 = templateList.stream()
                        .filter(x -> x.getColor().equals(template.getColor())
                                && x.getSurveyCategory().equals(template.getSurveyCategory()))
                        .collect(Collectors.toList());

                if (templateList2.size() > 0) {

                    responseWrapper.setSuccess(false);
                    responseWrapper.setHasError(true);
                    responseWrapper.setHasException(false);
                    responseWrapper.setResponseCode(302);
                    responseWrapper.setMessage("This template already exist");

                } else {

                    this.templateRepository.save(template);
                    responseWrapper.setSuccess(true);
                    responseWrapper.setHasError(false);
                    responseWrapper.setHasException(false);
                    responseWrapper.setResponseCode(201);
                    responseWrapper.setMessage("Template Created Successfully");

                }
            } else {

                this.templateRepository.save(template);
                responseWrapper.setSuccess(true);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setResponseCode(201);
                responseWrapper.setMessage("Template Created Successfully");

            }


        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setResponseCode(400);
            responseWrapper.setMessage(e.getMessage());

        }

        return responseWrapper;
    }

    public ResponseWrapper getTemplate(String category, String colorCode) {
        ResponseWrapper responseWrapper = new ResponseWrapper();

        try {

            List<Template> template = this.templateRepository.findAll();
            Template temp = null;
            for (Template t : template) {
                if (t.getSurveyCategory().equalsIgnoreCase(category) && t.getColor().equalsIgnoreCase(colorCode)) {
                    temp = t;
                }
            }

            if (temp == null) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No Template Found");

            } else {

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(302);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(temp);


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

    public ResponseWrapper getAll() {
        ResponseWrapper responseWrapper =  new ResponseWrapper();
        try{

            List<Template> templateList =  this.templateRepository.findAll();

            if(templateList.size()==0){

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("No template is present");
            }

            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(templateList);

        }catch(Exception e){

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(e.getMessage());

        }

        return responseWrapper;

    }
    public ResponseWrapper getAllTemplatesByMail(String mail)
    {
     //fetch all templates and send status along with it
        List<TemplateDummyPojo> dummyPojoList=new ArrayList<>();
        TemplateDummyPojo templatePojo=new TemplateDummyPojo();
        boolean isTemplatePurchased=false;
        for(Template template : this.templateRepository.findAll())
        {
          if(template.getColor().equals("twitter-light-gray"))
          {
            templatePojo=new TemplateDummyPojo();
              templatePojo.setId(template.getId());
              templatePojo.setColor(template.getColor());
              templatePojo.setQuestions(template.getQuestions());
              templatePojo.setSurveyCategory(template.getSurveyCategory());
              templatePojo.setStatus(true);
              dummyPojoList.add(templatePojo);
          }
        }
        for(Template template : this.templateRepository.findAll())
        {
          isTemplatePurchased=false;
         for(PurchaseTransaction purchaseTransaction : this.puchasedTransactionRepository.findAll())
         {
            if(purchaseTransaction.getPurchaseType().equals("template") && purchaseTransaction.getPurchaseTypeId().equals(template.getId()) && purchaseTransaction.getUserMail().equals(mail))
            {
             isTemplatePurchased=true;
             break;
            }

         }
         if(template.getColor().equals("twitter-light-gray")==false)
         {
             templatePojo=new TemplateDummyPojo();
             templatePojo.setId(template.getId());
             templatePojo.setColor(template.getColor());
             templatePojo.setQuestions(template.getQuestions());
             templatePojo.setSurveyCategory(template.getSurveyCategory());
             templatePojo.setStatus(isTemplatePurchased);
             dummyPojoList.add(templatePojo);
         }
        }
        ResponseWrapper responseWrapper=new ResponseWrapper();
        if(dummyPojoList==null || dummyPojoList.size()==0)
        {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage("Template list is empty");
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            return responseWrapper;
        }
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setMessage(dummyPojoList);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        return responseWrapper;
    }
}
