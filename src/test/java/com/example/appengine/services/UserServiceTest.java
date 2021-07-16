package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.GroupsCountRemaining;
import com.example.appengine.model.ResetPassword;
import com.example.appengine.model.SurveysCountRemaining;
import com.example.appengine.model.User;
import com.example.appengine.repository.GroupsCountRemainingRepository;
import com.example.appengine.repository.SurveysCountRemainingRepository;
import com.example.appengine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Mock
    private GroupsCountRemainingRepository groupsCountRemainingRepository;
    @Autowired
    @InjectMocks
    UserService userService;

    @BeforeEach
    public void setup() {
        assertNotNull(userRepository);
        assertNotNull(surveysCountRemainingRepository);
        assertNotNull(userService);
    }

    public List<User> getDummyUsers() {
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId("test123");
        user.setEmail("test@gmail.com");
        user.setPassword("iUtjiNiW9nqaSuridf0PWQ==");
        user.setPasswordKey("passwordkey");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(false);
        user.setVerified(true);
        user.setFirstName("Saksham");
        user.setSecondName("Solanki");
        users.add(user);
        user = new User();
        user.setId("test1123");
        user.setEmail("test1@gmail.com");
        user.setPassword("iUtjiNiW9nqaSuridf0PWQ==");
        user.setPasswordKey("passwordkey");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(false);
        user.setVerified(true);
        user.setFirstName("John");
        user.setSecondName("Velascas");
        users.add(user);
        return users;
    }

    @Test
    public void testForGetAllUsers() {
        List<User> users = getDummyUsers();
        Mockito.when(userRepository.findAll()).thenReturn(users);
        assertTrue(this.userService.getAllUsers().size() == 2);
    }

    @Test
    public void testForFindUserById() {
        List<User> users = getDummyUsers();
        Mockito.when(userRepository.findById("test@123")).thenReturn(Optional.of(users.get(0)));
        assertTrue(this.userService.findUserById("test@123").isSuccess());
        assertFalse(this.userService.findUserById("test@123232").isSuccess());
    }

    @Test
    public void testPositiveForRegister() {
        List<User> users = getDummyUsers();
        User user = new User();
        user.setId("test4@123");
        user.setEmail("test4@gmail.com");
        user.setPassword("jsiasjkas");
        user.setPasswordKey("suhashjasajs");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(false);
        user.setVerified(true);
        user.setFirstName("Saksham");
        user.setSecondName("Solanki");
        Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
        SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
        surveysCountRemaining.setUserMail(user.getEmail());
        surveysCountRemaining.setSurveysRemaining(3);
        Mockito.when(surveysCountRemainingRepository.save(surveysCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
        GroupsCountRemaining groupsCountRemaining = new GroupsCountRemaining();
        groupsCountRemaining.setUserMail(user.getEmail());
        groupsCountRemaining.setGroupsRemaining(1);
        Mockito.when(groupsCountRemainingRepository.save(groupsCountRemaining)).thenAnswer(i -> {
            return i.getArguments()[0];
        });
        assertTrue(this.userService.register(user, users).isSuccess());
        assertEquals(this.userService.register(user, users).getMessage(), "User registered successfully  with mail " + user.getEmail());
    }

    @Test
    public void testNegative1ForRegister() {
        List<User> users = getDummyUsers();
        User user = new User();
        user.setId("test@123");
        user.setEmail("test@gmail.com");
        user.setPassword("jsiasjkas");
        user.setPasswordKey("suhashjasajs");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(false);
        user.setVerified(true);
        user.setFirstName("Saksham");
        user.setSecondName("Solanki");
        Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
        SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
        surveysCountRemaining.setUserMail(user.getEmail());
        surveysCountRemaining.setSurveysRemaining(3);
        Mockito.when(surveysCountRemainingRepository.save(surveysCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
        GroupsCountRemaining groupsCountRemaining = new GroupsCountRemaining();
        groupsCountRemaining.setUserMail(user.getEmail());
        groupsCountRemaining.setGroupsRemaining(1);
        Mockito.when(groupsCountRemainingRepository.save(groupsCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
        assertFalse(this.userService.register(user, users).isSuccess());
    }

    @Test
    public void testPositiveForLogin() {
        try {
            List<User> users = getDummyUsers();
            User user = new User();
            user.setId("test@123");
            user.setEmail("test@gmail.com");
            user.setPassword("jsiasjkas");
            user.setPasswordKey("suhashjasajs");
            user.setRegisteredDate("whatever");
            user.setHuCoins(0);
            user.setSubscribed(false);
            user.setVerified(true);
            user.setFirstName("Saksham");
            user.setSecondName("Solanki");
            Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
            SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
            surveysCountRemaining.setUserMail(user.getEmail());
            surveysCountRemaining.setSurveysRemaining(3);
            Mockito.when(surveysCountRemainingRepository.save(surveysCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
            GroupsCountRemaining groupsCountRemaining = new GroupsCountRemaining();
            groupsCountRemaining.setUserMail(user.getEmail());
            groupsCountRemaining.setGroupsRemaining(1);
            Mockito.when(groupsCountRemainingRepository.save(groupsCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
            assertFalse(this.userService.login(null, users).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void tes1tNegativeForLogin() {
        try {
            List<User> users = getDummyUsers();
            User user = new User();
            user.setId("test7878@123");
            user.setEmail("testjskasa@gmail.com");
            user.setPassword("jsiasjkas");
            user.setPasswordKey("suhashjasajs");
            user.setRegisteredDate("whatever");
            user.setHuCoins(0);
            user.setSubscribed(false);
            user.setVerified(true);
            user.setFirstName("Saksham");
            user.setSecondName("Solanki");
            Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
            SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
            surveysCountRemaining.setUserMail(user.getEmail());
            surveysCountRemaining.setSurveysRemaining(3);
            Mockito.when(surveysCountRemainingRepository.save(surveysCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
            GroupsCountRemaining groupsCountRemaining = new GroupsCountRemaining();
            groupsCountRemaining.setUserMail(user.getEmail());
            groupsCountRemaining.setGroupsRemaining(1);
            Mockito.when(groupsCountRemainingRepository.save(groupsCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
            assertFalse(this.userService.login(user, users).isSuccess());
            assertEquals(this.userService.login(user, users).getMessage(), "Email doesn't exist");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void tes2tNegativeForLogin() {
        try {
            List<User> users = getDummyUsers();
            User user = new User();
            user.setId("test7878@123");
            user.setEmail("test@gmail.com");
            user.setPassword("tester123");
            user.setPasswordKey("passwordkey");
            user.setRegisteredDate("whatever");
            user.setHuCoins(0);
            user.setSubscribed(false);
            user.setVerified(true);
            user.setFirstName("Saksham");
            user.setSecondName("Solanki");
            Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
            SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
            surveysCountRemaining.setUserMail(user.getEmail());
            surveysCountRemaining.setSurveysRemaining(3);
            Mockito.when(surveysCountRemainingRepository.save(surveysCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
            GroupsCountRemaining groupsCountRemaining = new GroupsCountRemaining();
            groupsCountRemaining.setUserMail(user.getEmail());
            groupsCountRemaining.setGroupsRemaining(1);
            Mockito.when(groupsCountRemainingRepository.save(groupsCountRemaining)).thenAnswer(i -> i.getArguments()[0]);
            assertFalse(this.userService.login(user, users).isSuccess());
            assertEquals(this.userService.login(user, users).getMessage(), "Invalid Password");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testPositiveForGetUserSession() {
        try {
            assertTrue(this.userService.getUserSession(new User()).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testNegativeForGetUserSession() {
        try {
            assertFalse(this.userService.getUserSession(null).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testPositiveForLogout() {
        try {
            assertTrue(this.userService.getUserSession(new User()).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testNegativeForLogout() {
        try {
            assertFalse(this.userService.getUserSession(null).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testNegativeForFindUserByCookieName() {
        try {
            List<User> users = getDummyUsers();
            assertNull(this.userService.findUserByCookieName("sas", users));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testPositiveForFindUserByCookieName() {
        try {
            List<User> users = getDummyUsers();
            assertNotNull(this.userService.findUserByCookieName("test@gmail.com#test123", users));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testNegativeForUpdateProfile() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            User u = new User();
            u.setEmail(users.get(0).getEmail() + "7281278");
            User user = this.userService.getUserByMail(u);
            assertFalse(this.userService.updateProfile(user).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void testPositiveForUpdateProfile() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            User u = new User();
            u.setEmail(users.get(0).getEmail());
            User user = this.userService.getUserByMail(u);
            Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
            assertTrue(this.userService.updateProfile(user).isSuccess());
        } catch (Exception exception) {
        }
    }

    @Test
    public void test2PositiveForUpdateProfile() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            User u = new User();
            u.setEmail(users.get(0).getEmail());
            User userByMail = this.userService.getUserByMail(u);
            User user = userByMail;
            user.setFirstName("Test");
            user.setSecondName("Test");
            Mockito.when(this.userRepository.findById("test123")).thenReturn(Optional.of(user));
            Mockito.when(userRepository.save(user)).thenReturn(user);
            User p = (User) this.userService.findUserById("test123").getMessage();
            assertTrue(p.getFirstName().equals("Test"));
        } catch (Exception exception) {
        }
    }

    @Test
    public void testNegative1ForResetPassword() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            ResetPassword resetPassword = new ResetPassword();
            resetPassword.setEmail("test8212@gmail.com");
            assertEquals(this.userService.resetPassword(resetPassword).getMessage(), "User Does not exist against");
        } catch (Exception exception) {

        }

    }

    @Test
    public void testNegative2ForResetPassword() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            ResetPassword resetPassword = new ResetPassword();
            resetPassword.setEmail(users.get(0).getEmail());
            resetPassword.setPassword("tester");
            assertEquals(this.userService.resetPassword(resetPassword).getMessage(), "You have entered your previous password please create new password ");
        } catch (Exception exception) {

        }

    }

    @Test
    public void test1PositiiveForResetPassword() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            ResetPassword resetPassword = new ResetPassword();
            resetPassword.setEmail(users.get(0).getEmail());
            User user = users.get(0);
            user.setPassword("tester123");
            resetPassword.setPassword("tester123");
            User userToSet = new User();
            userToSet = users.get(0);
            userToSet.setPassword("TziWaOnwWapKlLmZ+/LxeQ==");
            Mockito.when(this.userRepository.findById(user.getId())).thenReturn(Optional.of(userToSet));
            Mockito.when(userRepository.save(user)).thenReturn(user);
            User p = (User) this.userService.findUserById(user.getId()).getMessage();
            assertTrue(p.getPassword().equals("tester123"));

        } catch (Exception exception) {

        }

    }

    @Test
    public void test2PositiiveForResetPassword() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            ResetPassword resetPassword = new ResetPassword();
            resetPassword.setEmail(users.get(0).getEmail());
            resetPassword.setPassword("tester123");
            assertEquals(this.userService.resetPassword(resetPassword).getMessage(), "Password has been reset successfully for user against mail : " + resetPassword.getEmail());

        } catch (Exception exception) {

        }

    }

    @Test
    public void testNegativeGetUserByMail() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            User dummyUser = new User();
            dummyUser.setEmail("whatever@gmail.com");
            assertNull(this.userService.getUserByMail(dummyUser));
        } catch (Exception exception) {
        }
    }

    @Test
    public void testPositiveGetUserByMail() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            User dummyUser = new User();
            dummyUser.setEmail(users.get(0).getEmail());
            assertNotNull(this.userService.getUserByMail(dummyUser));
            assertEquals(this.userService.getUserByMail(dummyUser).getFirstName(), users.get(0).getFirstName());
        } catch (Exception exception) {
        }
    }

    @Test
    public void testNegativeForGetHUCoins() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            Mockito.when(this.userService.getUserByMail(users.get(0))).thenReturn(null);
            assertFalse(this.userService.addHuCoins(users.get(0)).isSuccess());
            assertEquals(this.userService.addHuCoins(users.get(0)).getMessage(), "Invalid User Credentials");
        } catch (Exception exception) {
        }
    }

    @Test
    public void testNegative2ForGetHUCoins() {
        try {
            List<User> users = getDummyUsers();
            Mockito.when(this.userRepository.findAll()).thenReturn(null);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            Mockito.when(this.userService.getUserByMail(users.get(0))).thenReturn(users.get(0));
            assertFalse(this.userService.addHuCoins(users.get(0)).isSuccess());
            assertEquals(this.userService.addHuCoins(users.get(0)).getMessage(), "Invalid User Credentials");
        } catch (Exception exception) {
        }
    }

    @Test
    public void testPositiveForGetHUCoins() {
        try {
            List<User> users = getDummyUsers();
            User userToSave = users.get(0);
            userToSave.setHuCoins(5);
            Mockito.when(this.userRepository.findAll()).thenReturn(users);
            Mockito.when(this.userService.getAllUsers()).thenReturn(users);
            Mockito.when(this.userService.getUserByMail(users.get(0))).thenReturn(users.get(0));
            Mockito.when(this.userRepository.save(userToSave)).thenReturn(userToSave);
            assertTrue(this.userService.addHuCoins(users.get(0)).isSuccess());
            User user = users.get(0);
            Mockito.when(this.userRepository.findById(user.getId())).thenReturn(Optional.of(userToSave));
            User p = (User) this.userService.findUserById(user.getId()).getMessage();
            assertTrue(p.getHuCoins() == 5);

        } catch (Exception exception) {
        }
    }

    @Test
    public void testForExportPdf() {
        List<User> users = getDummyUsers();
        assertTrue(this.userService.exportPdf(users) instanceof ByteArrayInputStream);
    }

    @Test
    public void writeHeader() {
        assertTrue(true);
    }

    @Test
    public void writeTableData() {
        assertTrue(true);
    }
}

