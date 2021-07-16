package com.example.appengine.controller;

import com.example.appengine.model.EmailUser;
import com.example.appengine.model.History;
import com.example.appengine.model.ResetPassword;
import com.example.appengine.model.User;
import com.example.appengine.repository.HistoryRepository;
import com.example.appengine.services.UserService;
import com.example.appengine.utility.Tracking;
import com.example.appengine.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/surway/user")
public class UserController {

    @Autowired
    private UserService userService; //injecting Userservice class object
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private JavaMailSender javaMailSender;


    @GetMapping("/getUserById/{id}")
    public ResponseWrapper getUserById(@PathVariable String id) {

        return this.userService.findUserById(id);
    }

    @PostMapping("/register")
    public ResponseWrapper register(@RequestBody User user, HttpServletResponse response) {
        //do changes adding httpservletresponse and injecting cookies at the time of register
        List<User> users = this.userService.getAllUsers();
        ResponseWrapper responseWrapper = this.userService.register(user, users);
            if(responseWrapper.isSuccess())
            {
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                Tracking tracking=new Tracking();
                tracking.setTimestamp(timestamp.toString());
                tracking.setActivity("Registered successfully at "+timestamp.toString());
                History history=new History();
                history.setUserMail(user.getEmail());
                history.setTrackingList(Arrays.asList(tracking));
                this.historyRepository.save(history);
            }
            return responseWrapper;
    }

    @PostMapping("/login")
    public ResponseWrapper login(@RequestBody User user, HttpServletResponse response, HttpServletRequest request)//Will retrieve user as a parameter when someone will send a json
    {
        try {
            //get list of users and send it to user service along with json of user
            List<User> users = this.userService.getAllUsers();
            String userId = "";
            for (User u : users) {
                if (u.getEmail().equals(user.getEmail())) {
                    userId = u.getId();
                    break;
                }
            }
            ResponseWrapper responseWrapper = this.userService.login(user, users);
            return responseWrapper;
        } catch (Exception e) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(e.getMessage());
            return responseWrapper;
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseWrapper logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            //all the necessary checks for if cookie exist or not
            ResponseWrapper responseWrapper = new ResponseWrapper();
            Cookie[] cookies = request.getCookies();
            Cookie cookie = null;
            if (cookies != null) {
                cookie = Arrays.stream(cookies).filter((c) -> c.getName().equals("user")).collect(Collectors.toList()).get(0);
            }
            if (cookie != null) {
                //if cookie exist then check service response
                List<User> users = this.userService.getAllUsers();
                User u = this.userService.findUserByCookieName(cookie.getValue(), users); //call service and get user

                if (this.userService.logout(u).isSuccess() == true) {
                    //if service response is positive then remove cookie means cookie exist
                    Cookie cookie2 = new Cookie("user", null);
                    cookie2.setMaxAge(0);
                    cookie2.setSecure(true);
                    cookie2.setHttpOnly(true);
                    response.addCookie(cookie2);
                    Date date = new Date();
                    Timestamp timestamp = new Timestamp(date.getTime());
                    History history=new History();
                    history.setUserMail(cookie.getValue().split("#")[0]);
                    List<Tracking> trackings=null;
                    if(this.historyRepository.findAll()!=null)
                    {
                        for(History history1 : this.historyRepository.findAll())
                        {
                            if(history1.getUserMail().equals(cookie.getValue().split("#")[0]))
                            {
                                trackings=history1.getTrackingList();
                                break;
                            }
                        }
                    }
                    if(trackings!=null)
                    {
                        Tracking tracking=new Tracking();
                        tracking.setTimestamp(timestamp.toString());
                        tracking.setActivity("Logged out of portal");
                        trackings.add(tracking);
                        history.setTrackingList(trackings);
                        this.historyRepository.save(history);
                    }
                    else
                    {
                        Tracking tracking=new Tracking();
                        tracking.setTimestamp(timestamp.toString());
                        tracking.setActivity("Logged out of portal");
                        history.setTrackingList(Arrays.asList(tracking));
                        this.historyRepository.save(history);
                    }

                    return this.userService.logout(u);
                } else {
                    //if success is false no need to remove cookie since it doesn't exist
                    //it means no user has either logged in
                    return this.userService.logout(u);
                }

            } else {
                //else return negative response
                responseWrapper = new ResponseWrapper();
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setMessage("User doesn't exist");
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                return responseWrapper;
            }

        } catch (Exception e) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setResponseCode(404);
            responseWrapper.setSuccess(false);
            responseWrapper.setHasException(true);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage(e.getMessage());
            return responseWrapper;

        }
    }

    //update user
    @PostMapping("/updateProfile")
    public ResponseWrapper updateProfile(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (userService.updateProfile(user).isSuccess() == true) {
                Cookie cookie2 = new Cookie("user", null);
                cookie2.setMaxAge(0);
                cookie2.setSecure(true);
                cookie2.setHttpOnly(true);
                response.addCookie(cookie2);
                User u = userService.getUserByMail(user);
                Cookie cookie = new Cookie("user", user.getEmail() + "#" + u.getId());
                cookie.setMaxAge(1 * 24 * 60 * 60); // expires in 1 day
                cookie.setSecure(true); //will provide security to our cookie
                cookie.setHttpOnly(true); //will prevent from cross site scripting
                response.addCookie(cookie);

            }
            ResponseWrapper responseWrapper=this.userService.updateProfile(user);
            return responseWrapper;
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            responseWrapper.setMessage(exception.getMessage());
            return responseWrapper;
        }

    }

    @PostMapping("/resetPassword")
    public ResponseWrapper reset(@RequestBody ResetPassword password) {
        ResponseWrapper responseWrapper=this.userService.resetPassword(password);
        return responseWrapper;
    }

    @PostMapping("/sendOtp")
    public ResponseWrapper sendOtp(@RequestBody EmailUser emailUser) {
        try {
            int number = ThreadLocalRandom.current().nextInt(100000, 1000000);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            StringBuilder stringBuilder = new StringBuilder();
            String otp;
            // this will convert any number sequence into 6 character.
            otp = String.format("%06d", number);
            stringBuilder.append("" +
                    "    <style type=\"text/css\">\n" +
                    "        @media screen {\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: normal;\n" +
                    "                font-weight: 400;\n" +
                    "                src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v11/qIIYRU-oROkIk8vfvxw6QvesZW2xOQ-xsNqO47m55DA.woff) format('woff');\n" +
                    "            }\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: normal;\n" +
                    "                font-weight: 700;\n" +
                    "                src: local('Lato Bold'), local('Lato-Bold'), url(https://fonts.gstatic.com/s/lato/v11/qdgUG4U09HnJwhYI-uK18wLUuEpTyoUstqEm5AMlJo4.woff) format('woff');\n" +
                    "            }\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: italic;\n" +
                    "                font-weight: 400;\n" +
                    "                src: local('Lato Italic'), local('Lato-Italic'), url(https://fonts.gstatic.com/s/lato/v11/RYyZNoeFgb0l7W3Vu1aSWOvvDin1pK8aKteLpeZ5c0A.woff) format('woff');\n" +
                    "            }\n" +
                    "            @font-face {\n" +
                    "                font-family: 'Lato';\n" +
                    "                font-style: italic;\n" +
                    "                font-weight: 700;\n" +
                    "                src: local('Lato Bold Italic'), local('Lato-BoldItalic'), url(https://fonts.gstatic.com/s/lato/v11/HkF_qI1x_noxlxhrhMQYELO3LdcAZYWl9Si6vvxL-qU.woff) format('woff');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        body,\n" +
                    "        table,\n" +
                    "        td,\n" +
                    "        a {\n" +
                    "            -webkit-text-size-adjust: 100%;\n" +
                    "            -ms-text-size-adjust: 100%;\n" +
                    "        }\n" +
                    "        table,\n" +
                    "        td {\n" +
                    "            mso-table-lspace: 0pt;\n" +
                    "            mso-table-rspace: 0pt;\n" +
                    "        }\n" +
                    "        img {\n" +
                    "            -ms-interpolation-mode: bicubic;\n" +
                    "        }\n" +
                    "        img {\n" +
                    "            border: 0;\n" +
                    "            height: auto;\n" +
                    "            line-height: 100%;\n" +
                    "            outline: none;\n" +
                    "            text-decoration: none;\n" +
                    "        }\n" +
                    "        table {\n" +
                    "            border-collapse: collapse !important;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            height: 100% !important;\n" +
                    "            margin: 0 !important;\n" +
                    "            padding: 0 !important;\n" +
                    "            width: 100% !important;\n" +
                    "        }\n" +
                    "        a[x-apple-data-detectors] {\n" +
                    "            color: inherit !important;\n" +
                    "            text-decoration: none !important;\n" +
                    "            font-size: inherit !important;\n" +
                    "            font-family: inherit !important;\n" +
                    "            font-weight: inherit !important;\n" +
                    "            line-height: inherit !important;\n" +
                    "        }\n" +
                    "        @media screen and (max-width:600px) {\n" +
                    "            h1 {\n" +
                    "                font-size: 32px !important;\n" +
                    "                line-height: 32px !important;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        div[style*=\"margin: 16px 0;\"] {\n" +
                    "            margin: 0 !important;\n" +
                    "        }\n" +
                    "    </style></head>\n");
            stringBuilder.append("" +
                    "<body style=\"background-color: #f4f4f4; margin: 0 !important; padding: 0 !important;\">\n" +
                    "    <!-- HIDDEN PREHEADER TEXT -->\n" +
                    "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                    "        <tr>\n" +
                    "            <td bgcolor=\"#FFA73B\" align=\"center\">\n" +
                    "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                    "                    <tr>\n" +
                    "                        <td align=\"center\" valign=\"top\" style=\"padding: 40px 10px 40px 10px;\"> </td>\n" +
                    "                    </tr>\n" +
                    "                </table>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td bgcolor=\"#FFA73B\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                    "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"center\" valign=\"top\" style=\"padding: 40px 20px 20px 20px; border-radius: 4px 4px 0px 0px; color: #111111; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 48px; font-weight: 400; letter-spacing: 4px; line-height: 48px;\">\n" +
                    "                            <h1 style=\"font-size: 48px; font-weight: 400; margin: 2;\">Welcome!</h1>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                </table>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td bgcolor=\"#f4f4f4\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                    "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                    "                            <p style=\"margin: 0;\">" + emailUser.getMessage() + "</p>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\">\n" +
                    "                            <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                    "                                <tr>\n" +
                    "                                    <td bgcolor=\"#ffffff\" align=\"center\" style=\"padding: 20px 30px 60px 30px;\">\n" +
                    "                                        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                    "                                            <tr>\n" +
                    "                                                <td align=\"center\" style=\"border-radius: 3px;\"><h2 style=\"background: #FFA73B;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" + otp + "</h2></td>\n" +
                    "                                            </tr>\n" +
                    "                                        </table>\n" +
                    "                                    </td>\n" +
                    "                                </tr>\n" +
                    "                            </table>\n" +
                    "                        </td>\n" +
                    "                    </tr> <!-- COPY -->\n" +
                    "                    <tr>\n" +
                    "                        <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 0px 30px 40px 30px; border-radius: 0px 0px 4px 4px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                    "                            <p style=\"margin: 0;\">Regards,<br>Team SurWay</p>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                </table>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "    </table>\n" +
                    "</body>");
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(emailUser.getTo());
            mimeMessageHelper.setFrom("surwaytool@gmail.com");
            mimeMessageHelper.setSubject(emailUser.getSubject());
            mimeMessageHelper.setText(stringBuilder.toString(), true);
            javaMailSender.send(mimeMessage);
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasError(false);
            EmailUser userToReturn = new EmailUser();
            userToReturn.setOtp(otp);
            userToReturn.setMessage(emailUser.getMessage() + " : " + otp);
            userToReturn.setSubject(emailUser.getSubject());
            userToReturn.setTo(emailUser.getTo());
            responseWrapper.setMessage(userToReturn);
            return responseWrapper;

        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage(exception.getMessage());
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            return responseWrapper;
        }
    }

    @PostMapping("/getUserByMail")
    public ResponseWrapper getUserByMail(@RequestBody User user) {
        try {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            User u = this.userService.getUserByMail(user);
            if (u != null) {
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("User exist in the database");
                return responseWrapper;
            } else {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("User doesn't exist in the database");
                return responseWrapper;
            }
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage(exception.getMessage());
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            return responseWrapper;

        }
    }

    //getmapping to export pdf
//    @GetMapping("/export/pdf")
//    public ResponseEntity<InputStreamResource> exportToPdf(HttpServletResponse response) throws IOException {
//
//        List<User> users =  this.userService.getAllUsers();
//        ByteArrayInputStream bais =  this.userService.exportPdf(users);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Disposition","attachment; filename=users.pdf");
//        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bais));
//    }

    @PostMapping("/addHuCoins")
    public ResponseWrapper addHuCoins(@RequestBody User user) {
        try {
            return this.userService.addHuCoins(user);
        } catch (Exception exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setMessage(exception.getMessage());
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(true);
            return responseWrapper;
        }
    }
}
