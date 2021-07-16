# Backend for Surway(A Survey Tool)

It is a Backend for Survey Tool Written in Springboot Java to handle Api calls for Surway FrontEnd

## Features
* It Supports JWT so that no unauthorized User can send request to our Backend API's
* It Supports CORS So user has to first authorized themselves to Use This Tool
* All the functionalities are covered By Java 11 Features
* Captcha has been developed from scratch using AWT and java graphics programming
* It Uses an API for validating Payment Transactions through RazorPay Java API
* It supports image encryption as Base64 String
* It uses AES encyption algorithm to encrypt password in the database
* It Uses MongoJpa(To connect with mongodb to store and fetch data in Mongodb Cluster)

## How to Use it
* Clone this repo run the SprinbootApplication File contains main functions
* Customized your port in application.properties File and in your browser First send request to authenticate api to verify userful it will generate a token it will be valid for 10 minutes then use this token in headers to hit any API end point
* (localhost:8080/authenticate send username & password to this api it will be of type POST)
* (locahost:8090/surway/user/register POST send username(email) password image as base64 String and the data in request BODY)

Link for it's Front End Part [Surway_Frontend](https://github.com/saksham2105/surway_frontend)
