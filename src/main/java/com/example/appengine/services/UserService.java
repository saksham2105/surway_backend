package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.*;
import com.example.appengine.repository.GroupsCountRemainingRepository;
import com.example.appengine.repository.SurveysCountRemainingRepository;
import com.example.appengine.repository.UserRepository;
import com.example.appengine.utility.PasswordUtility;
import com.example.appengine.wrapper.ResponseWrapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Autowired
    private GroupsCountRemainingRepository groupsCountRemainingRepository;
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }


    public ResponseWrapper findUserById(String id) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("User with this Id: " + id + " Does not exist");
            } else {
                User userDetails = new User();

                userDetails.setId(user.getId());
                userDetails.setFirstName(user.getFirstName());
                userDetails.setSecondName(user.getSecondName());
                userDetails.setEmail(user.getEmail());
                userDetails.setContact(user.getContact());
                userDetails.setCollaborators(user.getCollaborators());
                userDetails.setSubscribed(user.isSubscribed());
                userDetails.setVerified(user.isVerified());
                userDetails.setHuCoins(user.getHuCoins());
                userDetails.setPassword(PasswordUtility.decrypt(user.getPassword(), user.getPasswordKey()));

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage(userDetails);
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

    //register Service
    public ResponseWrapper register(User user, List<User> users) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            boolean userExist = false;
            userExist = users.stream().anyMatch((u) -> u.getEmail().equals(user.getEmail()));
            if (userExist) {
                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(302);
                responseWrapper.setHasError(true);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("User with this email already exists");
            } else {
                //generating random iad and password key
                String passwordKey = UUID.randomUUID().toString();
                String id = UUID.randomUUID().toString();
                passwordKey = passwordKey.replaceAll("-", "");

                String encryptedPassword = PasswordUtility.encrypt(user.getPassword(), passwordKey);
                user.setPassword(encryptedPassword);
                user.setId(id);
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                user.setRegisteredDate(timestamp.toString());
                user.setPasswordKey(passwordKey);
                this.userRepository.save(user);
                //add three surveys to his account
                SurveysCountRemaining surveysCountRemaining=new SurveysCountRemaining();
                surveysCountRemaining.setUserMail(user.getEmail());
                surveysCountRemaining.setSurveysRemaining(3);
                this.surveysCountRemainingRepository.save(surveysCountRemaining);
                //add one group in his account
                GroupsCountRemaining groupsCountRemaining=new GroupsCountRemaining();
                groupsCountRemaining.setUserMail(user.getEmail());
                groupsCountRemaining.setGroupsRemaining(1);
                this.groupsCountRemainingRepository.save(groupsCountRemaining);
                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(201);
                responseWrapper.setHasError(false);
                responseWrapper.setHasException(false);
                responseWrapper.setMessage("User registered successfully  with mail " + user.getEmail());

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

    //login Service
    public ResponseWrapper login(User user, List<User> users) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (user == null || users == null) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("User Doesn't Exist");
            return responseWrapper;

        }
        boolean userExist = true;
        userExist = users.stream().anyMatch((u) -> u.getEmail().equals(user.getEmail()));
        if (!userExist) {
            //if email doesn't exist
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Email doesn't exist");
            return responseWrapper;
        }
        User matchedUser = null;
        for(User u : users)
        {
          if(u.getEmail().equals(user.getEmail()))
          {
            matchedUser=u;
            break;
          }
        }
        //match password
        if (!(user.getPassword().equals(PasswordUtility.decrypt(matchedUser.getPassword(), matchedUser.getPasswordKey())))) {
            //if password is invalid
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasError(true);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage("Invalid Password");
            return responseWrapper;

        }
        User userToReturn = new User();
//        userToReturn.setId(matchedUser.getId());
//        userToReturn.setEmail(matchedUser.getEmail());
//        userToReturn.setHuCoins(matchedUser.getHuCoins());
//        userToReturn.setFirstName(matchedUser.getFirstName());
//        userToReturn.setSecondName(matchedUser.getSecondName());
//        userToReturn.setContact(matchedUser.getContact());
//        userToReturn.setVerified(matchedUser.isVerified());
//        userToReturn.setImageString(matchedUser.getImageString());
//        userToReturn.setCollaborators(matchedUser.getCollaborators());
//        userToReturn.setRegisteredDate(matchedUser.getRegisteredDate());
//        user.setSubscribed(matchedUser.isSubscribed());
        userToReturn=matchedUser;
        userToReturn.setPassword("");
        userToReturn.setPasswordKey("");
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasException(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage(userToReturn);
        return responseWrapper;
    }

    public ResponseWrapper getUserSession(User user) throws SurwayException {
        //create 2 blocks one to check if user already logged in
        //if logged in then send it's session else send 404 response in wrapper
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (user != null) //check wheather session attribute exist agains key "user"
        {
            //if user has logged in then send user's session
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasError(false);
            responseWrapper.setHasException(false);
            responseWrapper.setMessage(user);
            return responseWrapper;
        } else {
            //if user has not logged in
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No Session Exist");
            return responseWrapper;
        }

    }

    public ResponseWrapper logout(User user) throws SurwayException {
        //creating response wrapper instance
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (user == null) {
            //if no user has been signed in then return negative response
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("No User has logged in");
        } else {
            //if some user has signed in then return positive response and invalidate Session
            responseWrapper.setSuccess(true);
            responseWrapper.setResponseCode(200);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(false);
            responseWrapper.setMessage("User logged out successfully");
        }
        return responseWrapper;

    }

    public User findUserByCookieName(String cookieName, List<User> users) throws SurwayException {
        String email = cookieName.split("#")[0];
        User user = null;
        //check if user exist against cookie else return null
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                user = u;
                break;
            }
        }
        return user;
    }


    @Transactional
    public ResponseWrapper updateProfile(User user) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            User existedUser = getUserByMail(user);

            if (existedUser == null) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(404);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("User Does not Exist with this Mail : " + user.getEmail());

            } else {

                if (!existedUser.getFirstName().equals(user.getFirstName()) && user.getFirstName() != null) {
                    existedUser.setFirstName(user.getFirstName());
                }

                if (!existedUser.getSecondName().equals(user.getSecondName()) && user.getSecondName() != null) {
                    existedUser.setSecondName(user.getSecondName());
                }
                if (user.getImageString() != null)
                    existedUser.setImageString(user.getImageString());

                //u.setContact(user.getContact());
                this.userRepository.save(existedUser);

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(200);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(false);
                responseWrapper.setMessage("User Profile update successfully");

            }
        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(40);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage(e.getMessage());

        }
        return responseWrapper;
    }

    public ResponseWrapper resetPassword(ResetPassword password) {

        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            User oldUser = null;
            for (User u : getAllUsers()) {
                if (u.getEmail().equals(password.getEmail())) {
                    oldUser = u;
                    break;
                }
            }
            if (oldUser == null) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(400);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("User Does not exist against");

            } else if (PasswordUtility.decrypt(oldUser.getPassword(), oldUser.getPasswordKey()).equals(password.getPassword())) {

                responseWrapper.setSuccess(false);
                responseWrapper.setResponseCode(302);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("You have entered your previous password please create new password ");

            } else {

                String passwordKey = UUID.randomUUID().toString();
                passwordKey = passwordKey.replaceAll("-", "");
                String encryptedPassword = PasswordUtility.encrypt(password.getPassword(), passwordKey);
                oldUser.setPassword(encryptedPassword);
                oldUser.setPasswordKey(passwordKey);

                this.userRepository.save(oldUser);

                responseWrapper.setSuccess(true);
                responseWrapper.setResponseCode(201);
                responseWrapper.setHasException(false);
                responseWrapper.setHasError(true);
                responseWrapper.setMessage("Password has been reset successfully for user against mail : " + password.getEmail());
            }
        } catch (Exception e) {

            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(406);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage(e.getMessage());

        }
        return responseWrapper;
    }

    //get User By Mail Service
    public User getUserByMail(User user) throws SurwayException {
        User existedUser = null;
        for (User u : this.getAllUsers()) {
            if (u.getEmail().equals(user.getEmail())) {
                existedUser = u;
                break;
            }
        }
        return existedUser;
    }

    @Transactional
    public ResponseWrapper addHuCoins(User user) throws SurwayException {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (this.getUserByMail(user) == null || this.userRepository.findAll() == null || this.userRepository.findAll().size() == 0) {
            responseWrapper.setSuccess(false);
            responseWrapper.setResponseCode(404);
            responseWrapper.setHasException(false);
            responseWrapper.setHasError(true);
            responseWrapper.setMessage("Invalid User Credentials");
            return responseWrapper;
        }
        int previousHuCoins = 0;
        //finding user by its mail
        User userForHuCoins = this.getUserByMail(user);
        previousHuCoins = userForHuCoins.getHuCoins();
        previousHuCoins += user.getHuCoins();
        userForHuCoins.setHuCoins(previousHuCoins);
        this.userRepository.save(userForHuCoins);
        responseWrapper.setSuccess(true);
        responseWrapper.setResponseCode(200);
        responseWrapper.setHasError(false);
        responseWrapper.setHasException(false);
        responseWrapper.setMessage("HU Coins Successfully added to this user's account");
        return responseWrapper;
    }

    public ByteArrayInputStream exportPdf(List<User> users) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();
        document.add(new Paragraph("List of All Users"));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);

        writeTableHeader(table);
        writeTableData(table, users);

        document.add(table);

        document.close();

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private void writeTableHeader(PdfPTable table) {

        PdfPCell pdfPCell = new PdfPCell();
        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        pdfPCell.setBackgroundColor(Color.BLUE);
        pdfPCell.setPadding(5);

        pdfPCell.setPhrase(new Phrase("S.N.", font));

        table.addCell(pdfPCell);
        pdfPCell.setPhrase(new Phrase("First Name", font));
        table.addCell(pdfPCell);
        pdfPCell.setPhrase(new Phrase("Last Name", font));
        table.addCell(pdfPCell);
        pdfPCell.setPhrase(new Phrase("email", font));
        table.addCell(pdfPCell);
        pdfPCell.setPhrase(new Phrase("Hu Coins", font));
        table.addCell(pdfPCell);
    }

    private void writeTableData(PdfPTable table, List<User> users) {

        Integer i = 1;
        for (User user : users) {
            table.addCell(String.valueOf(i));
            table.addCell(user.getFirstName());
            table.addCell(user.getSecondName());
            table.addCell(user.getEmail());
            table.addCell(String.valueOf(user.getHuCoins()));
            i++;
        }

    }
}
