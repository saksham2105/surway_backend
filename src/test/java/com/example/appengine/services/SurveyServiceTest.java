package com.example.appengine.services;

import com.example.appengine.exception.SurwayException;
import com.example.appengine.model.*;
import com.example.appengine.repository.*;
import com.example.appengine.utility.Tracking;
import com.example.appengine.wrapper.ResponseWrapper;
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

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SurveyServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveysCreatedRepository surveysCreatedRepository;
    @Mock
    private SurveyRepository surveyRepository;
    @Mock
    private ViewRepository viewRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private SurveyResponsesRepository surveyResponsesRepository;
    @Mock
    private GroupsCountRemainingRepository groupsCountRemainingRepository;
    @Mock
    private SurveysCountRemainingRepository surveysCountRemainingRepository;
    @Mock
    private AssignedSurveysRepository assignedSurveysRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private UserService userService;
    @Autowired
    @InjectMocks
    private SurveyResponsesService surveyResponsesService;
    @Autowired
    @InjectMocks
    private SurveyService surveyService;

    @BeforeEach
    public void setup() {
        assertNotNull(userRepository);
        assertNotNull(this.surveysCreatedRepository);
        assertNotNull(this.surveyRepository);
        assertNotNull(this.viewRepository);
        assertNotNull(this.groupRepository);
        assertNotNull(this.surveyResponsesService);
        assertNotNull(this.surveyResponsesRepository);
        assertNotNull(this.groupsCountRemainingRepository);
        assertNotNull(this.surveysCountRemainingRepository);
        assertNotNull(this.assignedSurveysRepository);
        assertNotNull(this.historyRepository);
        assertNotNull(this.userService);
        assertNotNull(this.surveyService);
    }

    public List<Survey> getDummySurveys() {
        List<Survey> surveys = new ArrayList<>();
        Survey survey = new Survey();
        survey.setId("101");
        survey.setSurveyCategory("events");
        survey.setPassword("");
        survey.setHasPassword(false);
        survey.setUserEmail("test@deloitte.com");
        survey.setAllowedUsers(new ArrayList<>(Arrays.asList("test1@test.com", "test2@test.com", "test3@test.com")));
        survey.setPasswordKey("");
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        survey.setTimestamp(timestamp.toString());
        survey.setActive(true);
        survey.setQuestions(new ArrayList<Question>());
        survey.setName("Survey 1");
        surveys.add(survey);
        survey = new Survey();
        survey.setId("102");
        survey.setSurveyCategory("hr");
        survey.setPassword("");
        survey.setHasPassword(false);
        survey.setUserEmail("test2@deloitte.com");
        survey.setPasswordKey("");
        date = new Date();
        timestamp = new Timestamp(date.getTime());
        survey.setTimestamp(timestamp.toString());
        survey.setActive(true);
        survey.setAllowedUsers(new ArrayList<>(Arrays.asList("test1@gmail.com", "test2@yahoo.com")));
        survey.setQuestions(new ArrayList<Question>());
        survey.setName("Survey 2");
        surveys.add(survey);
        survey = new Survey();
        survey.setId("103");
        survey.setSurveyCategory("customer");
        survey.setPassword("iUtjiNiW9nqaSuridf0PWQ==");
        survey.setHasPassword(true);
        survey.setUserEmail("test@deloitte.com");
        survey.setPasswordKey("passwordkey");
        date = new Date();
        timestamp = new Timestamp(date.getTime());
        survey.setTimestamp(timestamp.toString());
        survey.setActive(false);
        survey.setAllowedUsers(new ArrayList<>(Arrays.asList("test1@gmail.com", "test2@yahoo.com")));
        survey.setQuestions(new ArrayList<Question>());
        survey.setName("Survey 3");
        surveys.add(survey);
        return surveys;
    }

    public List<View> getDummyViews() {
        List<View> views = new ArrayList<>();
        View view = new View();
        view.setSurveyId("101");
        view.setViewCount(3);
        views.add(view);
        view = new View();
        view.setSurveyId("102");
        view.setViewCount(4);
        views.add(view);
        view = new View();
        view.setSurveyId("103");
        view.setViewCount(5);
        views.add(view);
        return views;
    }

    public List<SurveysCreated> getDummySurveysCreated() {
        List<SurveysCreated> surveysCreatedList = new ArrayList<>();
        SurveysCreated surveysCreated = new SurveysCreated();
        surveysCreated.setCountOfSurveys(2);
        surveysCreated.setUserMail("test@deloitte.com");
        surveysCreatedList.add(surveysCreated);
        surveysCreated = new SurveysCreated();
        surveysCreated.setUserMail("test2@deloitte.com");
        surveysCreated.setCountOfSurveys(1);
        surveysCreatedList.add(surveysCreated);
        return surveysCreatedList;
    }

    public List<History> getDummyHistories() {
        List<History> histories = new ArrayList<>();
        History history = new History();
        history.setUserMail("test@deloitte.com");
        List<Tracking> trackings = new ArrayList<>();
        Tracking tracking = new Tracking();
        tracking.setActivity("User logged in");
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        tracking.setTimestamp(timestamp.toString());
        trackings.add(tracking);
        tracking = new Tracking();
        tracking.setActivity("Created a survey");
        tracking.setTimestamp(timestamp.toString());
        trackings.add(tracking);
        history.setTrackingList(trackings);
        histories.add(history);
        history = new History();
        history.setUserMail("test2@deloitte.com");
        history.setTrackingList(trackings);
        histories.add(history);
        return histories;
    }

    public List<User> getDummyUsers() {
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId("test123");
        user.setEmail("test@deloitte.com");
        user.setPassword("iUtjiNiW9nqaSuridf0PWQ==");
        user.setPasswordKey("passwordkey");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(true);
        user.setVerified(true);
        user.setFirstName("Saksham");
        user.setSecondName("Solanki");
        users.add(user);
        user = new User();
        user.setId("test1123");
        user.setEmail("test2@deloitte.com");
        user.setPassword("iUtjiNiW9nqaSuridf0PWQ==");
        user.setPasswordKey("passwordkey");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(false);
        user.setVerified(true);
        user.setFirstName("John");
        user.setSecondName("Velascas");
        users.add(user);
        user = new User();
        user.setId("test1123");
        user.setEmail("test3@deloitte.com");
        user.setPassword("iUtjiNiW9nqaSuridf0PWQ==");
        user.setPasswordKey("passwordkey");
        user.setRegisteredDate("whatever");
        user.setHuCoins(0);
        user.setSubscribed(false);
        user.setVerified(true);
        user.setFirstName("Kayo");
        user.setSecondName("Mclanghan");
        users.add(user);
        return users;
    }

    public List<SurveysCountRemaining> dummySurveysCountRemainingList() {
        List<SurveysCountRemaining> surveysCountRemainings = new ArrayList<>();
        SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
        surveysCountRemaining.setSurveysRemaining(5);
        surveysCountRemaining.setUserMail("test@deloitte.com");
        surveysCountRemainings.add(surveysCountRemaining);
        surveysCountRemaining = new SurveysCountRemaining();
        surveysCountRemaining.setUserMail("test2@deloitte.com");
        surveysCountRemaining.setSurveysRemaining(10);
        surveysCountRemainings.add(surveysCountRemaining);
        return surveysCountRemainings;
    }

    public List<SurveyResponses> getDummySurveyResponsesList() {
        List<SurveyResponses> surveyResponsesList = new ArrayList<>();
        SurveyResponses surveyResponses = new SurveyResponses();
        surveyResponses.setSurveyId("101");
        List<SurveyResponse> surveyResponseList = new ArrayList<>();
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUserMail("ksalskla@deloitte.com");
        surveyResponse.setSurveyAnswers(new ArrayList<>());
        surveyResponseList.add(surveyResponse);
        surveyResponses.setSurveyResponseList(surveyResponseList);
        surveyResponsesList.add(surveyResponses);
        return surveyResponsesList;
    }

    public List<Group> getDummyGroups() {
        List<Group> groups = new ArrayList<>();
        Group group = new Group();
        group.setName("Group 1");
        group.setId("1001");
        group.setUserMail("test@deloitte.com");
        group.setMembers(new ArrayList<>(Arrays.asList("test1@test.com", "test2@test.com", "test3@test.com")));
        groups.add(group);
        group = new Group();
        group.setName("Group 2");
        group.setId("1002");
        group.setUserMail("test@deloitte.com");
        group.setMembers(new ArrayList<>(Arrays.asList("test1@test.com", "test2@test.com", "test3@test.com")));
        groups.add(group);
        group = new Group();
        group.setName("Group 3");
        group.setId("1003");
        group.setUserMail("test2@deloitte.com");
        group.setMembers(new ArrayList<>(Arrays.asList("test1@test.com", "test2@test.com", "test3@test.com")));
        groups.add(group);
        return groups;
    }
    public List<GroupsCountRemaining> dummyGetGroupCountRemainingList()
    {
     List<GroupsCountRemaining> groupsCountRemainingList=new ArrayList<>();
     GroupsCountRemaining groupsCountRemaining=new GroupsCountRemaining();
     groupsCountRemaining.setGroupsRemaining(5);
     groupsCountRemaining.setUserMail("test@deloitte.com");
     groupsCountRemainingList.add(groupsCountRemaining);
     groupsCountRemaining=new GroupsCountRemaining();
     groupsCountRemaining.setGroupsRemaining(10);
     groupsCountRemaining.setUserMail("test2@deloitte.com");
     groupsCountRemainingList.add(groupsCountRemaining);
     return groupsCountRemainingList;
    }
    @Test
    public void test1ForGetSurveyById() {
        try {
            List<Survey> surveys = getDummySurveys();
            Mockito.when(this.surveyRepository.findAll()).thenReturn(surveys);
            assertNull(this.surveyService.getSurveyById("104"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForGetSurveyById() {
        try {
            List<Survey> surveys = getDummySurveys();
            Mockito.when(this.surveyRepository.findAll()).thenReturn(surveys);
            assertNotNull(this.surveyService.getSurveyById("101"));
            assertTrue(this.surveyService.getSurveyById("101").getName().equals("Survey 1"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForGetSurveyById() {
        try {
            List<Survey> surveys = getDummySurveys();
            Mockito.when(this.surveyRepository.findAll()).thenReturn(surveys);
            assertTrue(this.surveyService.getSurveyById("103").getPassword().equals("tester"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForIsSurveyEnabled() {
        Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
        assertFalse(this.surveyService.isSurveyEnabled("104").isSuccess());
        assertTrue(this.surveyService.isSurveyEnabled("104").getMessage().equals("Invalid Survey Id"));
    }

    @Test
    public void test2ForIsSurveyEnabled() {
        Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
        assertTrue(this.surveyService.isSurveyEnabled("101").isSuccess());
    }

    @Test
    public void test3ForIsSurveyEnabled() {
        Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
        assertFalse(this.surveyService.isSurveyEnabled("103").isSuccess());
    }

    @Test
    public void test1ForRemoveSurveyViews() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            assertFalse(this.surveyService.removeSurveyViews("103"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForRemoveSurveyViews() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Survey survey = this.surveyService.getSurveyById("106");
            assertNull(survey);
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForRemoveSurveyViews() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Survey survey = this.surveyService.getSurveyById("103");
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertFalse(this.surveyService.removeSurveyViews("104"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test4ForRemoveSurveyViews() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            Survey survey = getDummySurveys().get(2);
            assertTrue(this.surveyService.removeSurveyViews("103"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForDecrementSurveyCount() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertFalse(this.surveyService.decrementSurveyCount("105"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForDecrementSurveyCount() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(null);
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertFalse(this.surveyService.decrementSurveyCount("102"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForDecrementSurveyCount() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertTrue(this.surveyService.decrementSurveyCount("102"));
            SurveysCreated surveysCreated = new SurveysCreated();
            surveysCreated.setCountOfSurveys(1);
            surveysCreated.setUserMail("test@deloitte.com");
            Mockito.when(this.surveysCreatedRepository.save(getDummySurveysCreated().get(0))).thenReturn(surveysCreated);
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(Arrays.asList(surveysCreated));
            this.surveysCreatedRepository.save(getDummySurveysCreated().get(0));
            assertTrue(this.surveysCreatedRepository.findAll().get(0).getCountOfSurveys() == 1);
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForDeleteSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            assertFalse(this.surveyService.deleteSurvey("103").isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForDeleteSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertFalse(this.surveyService.deleteSurvey("104").isSuccess());
            assertEquals(this.surveyService.deleteSurvey("104").getMessage(), "Invalid Survey Id");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForDeleteSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey surveyToDelete = this.surveyService.getSurveyById("103");
            assertTrue(this.surveyService.deleteSurvey("102").isSuccess());
            assertEquals(this.surveyService.deleteSurvey("102").getMessage(), "Survey Removed Successfully");
            ResponseWrapper responseWrapper = this.surveyService.deleteSurvey("103");
            Mockito.verify(this.surveyRepository).delete(surveyToDelete);
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test4ForDeleteSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey surveyToDelete = this.surveyService.getSurveyById("103");
            History history = getDummyHistories().get(0);
            History historyToGet = getDummyHistories().get(0);
            Tracking t = new Tracking();
            t.setActivity("User has deleted survey 102");
            List<Tracking> trackingList = historyToGet.getTrackingList();
            trackingList.add(t);
            historyToGet.setTrackingList(trackingList);
            assertTrue(this.surveyService.deleteSurvey("102").isSuccess());
            Mockito.when(this.historyRepository.findAll()).thenReturn(getDummyHistories());
            Mockito.when(this.historyRepository.save(history)).thenReturn(historyToGet);
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForAddSurvey() {
        try {
            User user = getDummyUsers().get(2);
            Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
            Mockito.when(this.userService.getUserByMail(user)).thenReturn(user);
            Mockito.when(this.userService.getAllUsers()).thenReturn(getDummyUsers());
            Survey survey = getDummySurveys().get(0);
            assertEquals(this.surveyService.addSurvey(survey).getMessage(), "No surveys has left in your account Spend Hu coins to get surveys");

        } catch (Exception exception) {
        }
    }

    @Test
    public void test2ForAddSurvey() {
        try {
            User user = getDummyUsers().get(0);
            Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
            Mockito.when(this.userService.getUserByMail(user)).thenReturn(user);
            Mockito.when(this.userService.getAllUsers()).thenReturn(getDummyUsers());
            Survey survey = getDummySurveys().get(0);
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Mockito.when(this.surveysCountRemainingRepository.findAll()).thenReturn(null);
            assertEquals(this.surveyService.addSurvey(survey).getMessage(), "No surveys has left in your account Spend Hu coins to get surveys");
        } catch (Exception exception) {
        }
    }

    @Test
    public void test3ForAddSurvey() {
        try {
            User user = getDummyUsers().get(0);
            Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
            Mockito.when(this.userService.getUserByMail(user)).thenReturn(user);
            Mockito.when(this.userService.getAllUsers()).thenReturn(getDummyUsers());
            List<SurveysCountRemaining> surveysCountRemainings = new ArrayList<>();
            SurveysCountRemaining surveysCountRemaining = new SurveysCountRemaining();
            surveysCountRemaining.setSurveysRemaining(5);
            surveysCountRemaining.setUserMail("testsa@deloitte.com");
            surveysCountRemainings.add(surveysCountRemaining);
            Survey survey = getDummySurveys().get(0);
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Mockito.when(this.surveysCountRemainingRepository.findAll()).thenReturn(surveysCountRemainings);
            assertEquals(this.surveyService.addSurvey(survey).getMessage(), "No surveys has left in your account Spend Hu coins to get surveys");
        } catch (Exception exception) {
        }
    }

    @Test
    public void test4ForAddSurvey() {
        try {
            User user = getDummyUsers().get(0);
            Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
            Mockito.when(this.userService.getUserByMail(user)).thenReturn(user);
            Mockito.when(this.userService.getAllUsers()).thenReturn(getDummyUsers());
            Survey survey = getDummySurveys().get(0);
            Mockito.when(this.surveysCreatedRepository.findAll()).thenReturn(getDummySurveysCreated());
            Mockito.when(this.surveysCountRemainingRepository.findAll()).thenReturn(dummySurveysCountRemainingList());
            Mockito.when(this.surveyRepository.save(survey)).thenReturn(survey);
            assertTrue(this.surveyService.addSurvey(survey).isSuccess());
        } catch (Exception exception) {
        }
    }

    @Test
    public void test1ForGetViewsOfSurvey() {
        try {
            View view = getDummyViews().get(0);
            Mockito.when(this.viewRepository.findAll()).thenReturn(null);
            assertFalse(this.surveyService.getViewsOfSurvey(view).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }

    }

    @Test
    public void test2ForGetViewsOfSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            View view = getDummyViews().get(0);
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertEquals(this.surveyService.getViewsOfSurvey(view).getMessage(), "No Survey Exist in the database");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }

    }

    @Test
    public void test3ForGetViewsOfSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            View view = getDummyViews().get(0);
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertTrue(this.surveyService.getViewsOfSurvey(view).isSuccess());
            View v = (View) this.surveyService.getViewsOfSurvey(view).getMessage();
            assertEquals(v.getViewCount(), 3);
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }

    }

    @Test
    public void test1ForEditSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            Survey survey = getDummySurveys().get(0);
            assertEquals(this.surveyService.editSurvey(survey).getMessage(), "No Survey Exist in the database");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForEditSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(0);
            assertEquals(this.surveyService.editSurvey(null).getMessage(), "Survey Id Can't be null");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForEditSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(0);
            survey.setUserEmail("gshasa@deloitte.com");
            assertEquals(this.surveyService.editSurvey(survey).getMessage(), "Invalid User mail");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test4ForEditSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(1);
            survey.setId("105");
            assertEquals(this.surveyService.editSurvey(survey).getMessage(), "Invalid Survey Id");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test5ForEditSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(1);
            assertTrue(this.surveyService.editSurvey(survey).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test6ForEditSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(0);
            Mockito.when(this.surveyResponsesRepository.findAll()).thenReturn(getDummySurveyResponsesList());
            assertEquals(this.surveyService.editSurvey(survey).getMessage(), "User can't edit survey as some response/responses has already recorded on this survey");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForGetSurveyViews() {
        try {
            View view = getDummyViews().get(0);
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            assertEquals(this.surveyService.getSurveyViews(view), 0);
        } catch (Exception exception) {

        }
    }

    @Test
    public void test2ForGetSurveyViews() {
        try {
            View view = getDummyViews().get(0);
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.viewRepository.findAll()).thenReturn(null);
            assertEquals(this.surveyService.getSurveyViews(view), 0);
        } catch (Exception exception) {

        }
    }

    @Test
    public void test3ForGetSurveyViews() {
        try {
            View view = getDummyViews().get(0);
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.viewRepository.findAll()).thenReturn(getDummyViews());
            assertEquals(this.surveyService.getSurveyViews(view), view.getViewCount());
        } catch (Exception exception) {

        }
    }

    @Test
    public void test1ForGetUserSurvey() {
        try {
            User user = getDummyUsers().get(0);
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            assertEquals(this.surveyService.getUserSurvey(user).getMessage(), "No Survey is Present in DB Please Add one.");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForGetUserSurvey() {
        try {
            User user = getDummyUsers().get(0);
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Mockito.when(this.surveyResponsesRepository.findAll()).thenReturn(getDummySurveyResponsesList());
            assertFalse(this.surveyService.getUserSurvey(user).isSuccess());
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForEnableSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertFalse(this.surveyService.enableSurvey("104"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2orEnableSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertFalse(this.surveyService.enableSurvey("101"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3orEnableSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertTrue(this.surveyService.enableSurvey("103"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForDisableSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertFalse(this.surveyService.disableSurvey("104"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForDisableSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertFalse(this.surveyService.disableSurvey("103"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForDisableSurvey() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertTrue(this.surveyService.disableSurvey("101"));
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForAddToAssignedSurveys() {
        try {
            assertEquals(this.surveyService.addToAssignedSurveys(null).getMessage(), "Some Problem");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForAddToAssignedSurveys() {
        try {
            AssignedSurveys surveys = new AssignedSurveys();
            surveys.setSurveyId("101");
            surveys.setFromUser("test@deloitte.com");
            surveys.setToUsers(new ArrayList<>());
            assertEquals(this.surveyService.addToAssignedSurveys(surveys).getMessage(), "Some Problem");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForAddToAssignedSurveys() {
        try {
            AssignedSurveys surveys = new AssignedSurveys();
            surveys.setSurveyId("101");
            surveys.setFromUser("test@deloitte.com");
            surveys.setToUsers(new ArrayList<>(Arrays.asList("test3@test.com", "test3@test.com")));
            Mockito.when(this.userRepository.findAll()).thenReturn(null);
            assertEquals(this.surveyService.addToAssignedSurveys(surveys).getMessage(), "No user present in the database");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test4ForAddToAssignedSurveys() {
        try {
            AssignedSurveys surveys = new AssignedSurveys();
            surveys.setSurveyId("101");
            surveys.setFromUser("test4@deloitte.com");
            surveys.setToUsers(new ArrayList<>(Arrays.asList("test3@test.com", "test3@test.com")));
            Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
            Mockito.when(this.userService.getAllUsers()).thenReturn(getDummyUsers());
            assertEquals(this.surveyService.addToAssignedSurveys(surveys).getMessage(), "User doesn't exist");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test5ForAddToAssignedSurveys() {
        try {
            AssignedSurveys surveys = new AssignedSurveys();
            surveys.setSurveyId("105");
            surveys.setFromUser("test@deloitte.com");
            surveys.setToUsers(new ArrayList<>(Arrays.asList("test3@test.com", "test3@test.com")));
            Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
            Mockito.when(this.userService.getAllUsers()).thenReturn(getDummyUsers());
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertEquals(this.surveyService.addToAssignedSurveys(surveys).getMessage(), "Survey with given id either doesn't exist or either it is not mapped to user");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForAssignSurveyUsingGroup() {
        try {
            Mockito.when(this.groupRepository.findAll()).thenReturn(getDummyGroups());
            Group group = new Group();
            group.setId("1005");
            assertEquals(this.surveyService.assignSurveyUsingGroup(group.getId(), "101").getMessage(), "Invalid Group Id");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForAssignSurveyUsingGroup() {
        try {
            Mockito.when(this.groupRepository.findAll()).thenReturn(getDummyGroups());
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Group group = getDummyGroups().get(0);
            System.out.println(group.getId());
            Survey survey = getDummySurveys().get(0);
            assertEquals(this.surveyService.assignSurveyUsingGroup(group.getId(), survey.getId()).getMessage(), "Survey Assigned to user successfully");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test1ForDeassignSurvey() {
        try {
            AssignedSurveys assignedSurveys = new AssignedSurveys();
            assignedSurveys.setSurveyId("101");
            assignedSurveys.setFromUser("test@deloitte.com");
            assignedSurveys.setToUsers(new ArrayList<>(Arrays.asList("test1@test.com", "test2@test.com")));
            Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
            assertEquals(this.surveyService.deassignSurvey(assignedSurveys).getMessage(), "Can't deassign as no user exist");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test2ForDeassignSurvey() {
        try {
            AssignedSurveys assignedSurveys = new AssignedSurveys();
            assignedSurveys.setSurveyId("106");
            assignedSurveys.setFromUser("test@deloitte.com");
            assignedSurveys.setToUsers(new ArrayList<>(Arrays.asList("test1@test.com", "test2@test.com")));
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertEquals(this.surveyService.deassignSurvey(assignedSurveys).getMessage(), "Can't deassign as no user exist");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }
    }

    @Test
    public void test3ForDeassignSurvey() {
        try {
            AssignedSurveys assignedSurveys = new AssignedSurveys();
            assignedSurveys.setSurveyId("101");
            assignedSurveys.setFromUser("test@deloitte.com");
            assignedSurveys.setToUsers(new ArrayList<>(Arrays.asList("test1@test.com", "test6@test.com")));
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            assertEquals(this.surveyService.deassignSurvey(assignedSurveys).getMessage(), "Deassigned all the users successfully");
        } catch (Exception exception) {
            assertTrue(exception instanceof SurwayException);
        }

    }

    @Test
    public void test1ForGetAllAssignedSurveys() {
        Mockito.when(this.surveyRepository.findAll()).thenReturn(null);
        assertTrue(this.surveyService.getAllAssignedSurveys().size() == 0);
    }

    @Test
    public void test2ForGetAllAssignedSurveys() {
        Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
        assertTrue(this.surveyService.getAllAssignedSurveys().size() == 3);
    }

    @Test
    public void test1ForIncrementSurveyViewCount() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(0);
            View view = getDummyViews().get(0);
            view.setSurveyId("107");
            Mockito.when(this.surveyService.getSurveyById(survey.getId())).thenReturn(survey);
            assertEquals(this.surveyService.incrementSurveyViewCount(view).getMessage(), "Invalid Survey Id");
        } catch (Exception exception) {

        }
    }

    @Test
    public void test2ForIncrementSurveyViewCount() {
        try {
            Mockito.when(this.surveyRepository.findAll()).thenReturn(getDummySurveys());
            Survey survey = getDummySurveys().get(0);
            View view = getDummyViews().get(0);
            Mockito.when(this.surveyService.getSurveyById(survey.getId())).thenReturn(survey);
            assertEquals(this.surveyService.incrementSurveyViewCount(view).getMessage(), "Count of view increased successfully");
        } catch (Exception exception) {

        }
    }

    @Test
    public void test1ForAddToGroup() {
     try {
        Group group=getDummyGroups().get(0);
        Mockito.when(this.userRepository.findAll()).thenReturn(getDummyUsers());
        User user=new User();
        user.setEmail("test@deloitte.com");
        Mockito.when(this.userService.getUserByMail(user)).thenReturn(null);
        assertEquals(this.surveyService.addToGroup(group).getMessage(),"Invalid User mail");
     }catch(Exception exception){
         assertTrue(exception instanceof SurwayException);
     }
    }

}
