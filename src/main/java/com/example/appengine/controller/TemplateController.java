package com.example.appengine.controller;

import com.example.appengine.model.Template;
import com.example.appengine.services.TemplateService;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping("/template/getAllTemplates")
    public ResponseWrapper getAllTemplate(){
        return this.templateService.getAll();
    }

    @GetMapping("/template/{category}/{colorCode}")
    public ResponseWrapper getTemplateById(@PathVariable String category,
                                           @PathVariable String colorCode) {

        return this.templateService.getTemplate(category,colorCode);
    }

    @PostMapping("/add")
    public ResponseWrapper addTemplate(@RequestBody Template template) {

        if (template == null) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(400);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("template is NULL !");
            return responseWrapper;
        }
        return this.templateService.addTemplate(template);
    }
    @GetMapping("/getAllTemplatesByMail/{mail}")
    public ResponseWrapper getAllTemplatesByMail(@PathVariable String mail)
    {
      return this.templateService.getAllTemplatesByMail(mail);
    }
}
