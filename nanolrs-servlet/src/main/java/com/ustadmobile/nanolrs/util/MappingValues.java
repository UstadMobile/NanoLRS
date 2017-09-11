package com.ustadmobile.nanolrs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by varuna on 9/5/2017.
 */

public class MappingValues {

    //TODO: Get from build prop
    public static final String USER_COLUMN_USERNAME = "username";
    public static final String USER_COLUMN_UNIVERSITY = "university";    //, 980);
    public static final String USER_COLUMN_FULLNAME = "fullname";    //, 981);
    public static final String USER_COLUMN_GENDER = "gender";    //, 983);
    public static final String USER_COLUMN_EMAIL = "email";    //,982);
    public static final String USER_COLUMN_PHONENUMBER = "phonenumber";    //,984);
    public static final String USER_COLUMN_FACULTY = "faculty";    //,985);
    public static final String USER_COLUMN_FATHER_NAME = "father_name";    //,986);
    public static final String USER_COLUMN_ADDRESS = "address";    //,987);
    public static final String USER_COLUMN_TAZKIRA_ID = "tazkira_id";    //,988);
    public static final String USER_COLUMN_RELATIONSHIP = "relationship";    //,989);
    public static final String USER_COLUMN_DEPARTMENT = "department";    //,990);
    public static final String USER_COLUMN_ACADEMIC_YEAR = "academic_year";    //,991);
    public static final String USER_COLUMN_GPA = "gpa";    //,992);
    public static final String USER_COLUMN_WOULD_WORK = "would_work";    //,993);
    public static final String USER_COLUMN_WOULD_WORK_ELABORATE = "would_work_elaborate";    //,994);
    public static final String USER_COLUMN_WORK_EXPERIENCE = "work_experience";    //,995);
    public static final String USER_COLUMN_TYPE_JOB = "type_job";    //,996);
    public static final String USER_COLUMN_ENGLISH_PROFICIENCY = "english_proficiency";    //,997);
    public static final String USER_COLUMN_COMPUTER_APPLICATION = "computer_application";    //,998);
    public static final String USER_COLUMN_POST_GRADUATE = "post_graduate";    //,999);
    public static final String USER_COLUMN_COMMENTS = "comments";    //,1000);
    public static final String USER_COLUMN_WORK_EXPERIENCE_ELABORATE = "work_experience_elaborate";    //,1001);

    public static final String MODULE_1_NAME = "CV Writing";
    public static final String MODULE_2_NAME = "Cover Letter Writing";
    public static final String MODULE_3_NAME = "Job Search Skills";
    public static final String MODULE_4_NAME = "Job Interview Skills";

    public static final String MODULE_1_ID = "m1";
    public static final String MODULE_2_ID = "m2";
    public static final String MODULE_3_ID = "m3";
    public static final String MODULE_4_ID = "m4";

    public static final String SUPER_ADMIN_USERNAME = "admin";

    public static String[] USER_COLUMN_FIELDS = {USER_COLUMN_USERNAME, USER_COLUMN_UNIVERSITY, USER_COLUMN_FULLNAME, USER_COLUMN_TAZKIRA_ID, USER_COLUMN_GENDER};

    public static Map<String, String> MODULES = new HashMap<>();
    public static Map<String, List<String>> MODULE_EPUB_ID = new HashMap<>();

    public static Map<String, String> MODULE_1_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_2_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_3_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_4_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_5_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_6_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_7_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_8_QUESTIONS = new HashMap<>();
    public static Map<String, String> MODULE_9_QUESTIONS = new HashMap<>();


    //TODO: Get from build properties

    public static Map<String, Integer> custom_fields_map = new HashMap<>();
    public static Map<String, String> uni_map = new HashMap<>();
    public static Map<String, String> custom_fields_label = new HashMap<>();

    public static List<String> MODULE_1_IDS = new ArrayList<String>();
    public static List<String> MODULE_2_IDS = new ArrayList<String>();
    public static List<String> MODULE_3_IDS = new ArrayList<String>();
    public static List<String> MODULE_4_IDS = new ArrayList<String>();
    public static List<String> MODULE_5_IDS = new ArrayList<String>();
    public static List<String> MODULE_6_IDS = new ArrayList<String>();
    public static List<String> MODULE_7_IDS = new ArrayList<String>();
    public static List<String> MODULE_8_IDS = new ArrayList<String>();
    public static List<String> MODULE_9_IDS = new ArrayList<String>();

    public static final String XAPI_PASSED_VERB = "http://adlnet.gov/expapi/verbs/passed";

    public static String[] universities = {"Kabul University", "Kabul Polytechnic University",
            "Kabul Education University", "Other", "I don't know"};
    public static String[] gender = {"Female", "Male"};
    public static String[] faculty = {};
    public static String[] relationship={"Single", "Married"};
    public static String[] academic_year={};
    public static String[] english_proficiency = {"Fluent", "Good", "Fair", "Poor"};
    public static String[] yes_no_choices = {"Yes", "No"};
    public static String[] job_type = {"Short-term", "Long-term", "Part-time", "Full-time"};

    static{
        custom_fields_map.put(USER_COLUMN_UNIVERSITY, 980);
        custom_fields_map.put(USER_COLUMN_FULLNAME, 981);
        custom_fields_map.put(USER_COLUMN_GENDER, 983);
        custom_fields_map.put(USER_COLUMN_EMAIL,982);
        custom_fields_map.put(USER_COLUMN_PHONENUMBER,984);
        custom_fields_map.put(USER_COLUMN_FACULTY,985);
        custom_fields_map.put(USER_COLUMN_FATHER_NAME,986);
        custom_fields_map.put(USER_COLUMN_ADDRESS,987);
        custom_fields_map.put(USER_COLUMN_TAZKIRA_ID,988);
        custom_fields_map.put(USER_COLUMN_RELATIONSHIP,989);
        custom_fields_map.put(USER_COLUMN_DEPARTMENT,990);
        custom_fields_map.put(USER_COLUMN_ACADEMIC_YEAR,991);
        custom_fields_map.put(USER_COLUMN_GPA,992);
        custom_fields_map.put(USER_COLUMN_WOULD_WORK,993);
        custom_fields_map.put(USER_COLUMN_WOULD_WORK_ELABORATE,994);
        custom_fields_map.put(USER_COLUMN_WORK_EXPERIENCE,995);
        custom_fields_map.put(USER_COLUMN_TYPE_JOB,996);
        custom_fields_map.put(USER_COLUMN_ENGLISH_PROFICIENCY,997);
        custom_fields_map.put(USER_COLUMN_COMPUTER_APPLICATION,998);
        custom_fields_map.put(USER_COLUMN_POST_GRADUATE,999);
        custom_fields_map.put(USER_COLUMN_COMMENTS,1000);
        custom_fields_map.put(USER_COLUMN_WORK_EXPERIENCE_ELABORATE,1001);

        custom_fields_label.put(USER_COLUMN_UNIVERSITY, "University");
        custom_fields_label.put(USER_COLUMN_FULLNAME, "Name");
        custom_fields_label.put(USER_COLUMN_GENDER, "Gender");
        custom_fields_label.put(USER_COLUMN_EMAIL, "Email");
        custom_fields_label.put(USER_COLUMN_PHONENUMBER,"Phone Number");
        custom_fields_label.put(USER_COLUMN_FACULTY, "Faculty");
        custom_fields_label.put(USER_COLUMN_FATHER_NAME, "Father's Name");
        custom_fields_label.put(USER_COLUMN_ADDRESS, "Address");
        custom_fields_label.put(USER_COLUMN_TAZKIRA_ID, "Tazkira ID");
        custom_fields_label.put(USER_COLUMN_RELATIONSHIP, "Relationship");
        custom_fields_label.put(USER_COLUMN_DEPARTMENT, "Department");
        custom_fields_label.put(USER_COLUMN_ACADEMIC_YEAR, "Academic Year");
        custom_fields_label.put(USER_COLUMN_GPA, "GPA (in %)");
        custom_fields_label.put(USER_COLUMN_WOULD_WORK, "Would you want to work?");
        custom_fields_label.put(USER_COLUMN_WOULD_WORK_ELABORATE, "Why would you want/not want to work?");
        custom_fields_label.put(USER_COLUMN_WORK_EXPERIENCE,"Work Experience");
        custom_fields_label.put(USER_COLUMN_TYPE_JOB, "Type of Job");
        custom_fields_label.put(USER_COLUMN_ENGLISH_PROFICIENCY, "English Proficiency");
        custom_fields_label.put(USER_COLUMN_COMPUTER_APPLICATION, "Computer Skills");
        custom_fields_label.put(USER_COLUMN_POST_GRADUATE, "Plans post Graduation");
        custom_fields_label.put(USER_COLUMN_COMMENTS, "Comments");
        custom_fields_label.put(USER_COLUMN_WORK_EXPERIENCE_ELABORATE, "Work Experience Comments");

        uni_map.put("Kabul University", "KU");
        uni_map.put("Kabul Polytechnic University", "KPU");
        uni_map.put("Kabul Education University", "KEU");


        MODULE_1_IDS.add("epub:202b10fe-b028-4b84-9b84-852aa123456a");
        MODULE_1_IDS.add("epub:202b10fe-b028-4b84-9b84-852aa123456b");
        MODULE_1_IDS.add("epub:202b10fe-b028-4b84-9b84-852aa123456c");

        MODULE_2_IDS.add("epub:eb0476a2-b8b1-43e3-bb85-f0e51e143afe");
        MODULE_2_IDS.add("epub:023970e2-2d4b-4fd5-9bbd-de373bb2aad6");
        MODULE_2_IDS.add("epub:6f747783-9ec3-4195-a3c7-3c417efaf8ea");

        MODULE_3_IDS.add("epub:114f6e63-80f2-4d10-9a70-efa113eb9f65");
        MODULE_3_IDS.add("epub:6c27c4c0-6fc7-4c89-b8f8-54cd270666e9");
        MODULE_3_IDS.add("epub:16f526b1-90e4-4e3d-a0e9-05e73bba9953");

        MODULE_4_IDS.add("epub:3ce0e992-050c-4fbf-90c9-4dcb2b82bc64");
        MODULE_4_IDS.add("epub:e95ec3d7-d56b-4541-8d45-4684dfdf64a6");
        MODULE_4_IDS.add("epub:31e04e55-e29d-422f-9e99-c3f2fd1f6f4a");

        MODULES.put(MODULE_1_ID, MODULE_1_NAME);
        MODULES.put(MODULE_2_ID, MODULE_2_NAME);
        MODULES.put(MODULE_3_ID, MODULE_3_NAME);
        MODULES.put(MODULE_4_ID, MODULE_4_NAME);

        MODULE_EPUB_ID.put(MODULE_1_ID, MODULE_1_IDS);
        MODULE_EPUB_ID.put(MODULE_2_ID, MODULE_2_IDS);
        MODULE_EPUB_ID.put(MODULE_3_ID, MODULE_3_IDS);
        MODULE_EPUB_ID.put(MODULE_4_ID, MODULE_4_IDS);

        MODULE_1_QUESTIONS.put("q1","Anoosha needs to explain what a CV is. What should she say? (Select one");
        MODULE_1_QUESTIONS.put("q2","Which items should Anoosha include on her CV? (Select all that apply");
        MODULE_1_QUESTIONS.put("q3","How should Anoosha format her CV? (Select one");
        MODULE_1_QUESTIONS.put("q4","Anoosha needs to explain the difference between the objective section and the personal skills section. What should she say? (Select one");
        MODULE_1_QUESTIONS.put("q5","What can Anoosha include under experience?(Select all that apply");
        MODULE_1_QUESTIONS.put("q6","What does Anoosha need to keep in mind when choosing referees? (Select one");
        MODULE_1_QUESTIONS.put("q7","What are the five C's that Anoosha needs to keep in mind? (Choose all that apply");
        MODULE_1_QUESTIONS.put("q8","Which of these should Anoosha include on her CV? (Choose all that apply");

        //MODULE_2_QUESTIONS.put("./2-coverletter/en/EPUB/main.html";
        MODULE_2_QUESTIONS.put("q1","Anoosha needs to explain what the purpose of a cover letter is. What should she say? (Select one");
        MODULE_2_QUESTIONS.put("q2","What should Anoosha include in her cover letter? (Select all that apply");
        MODULE_2_QUESTIONS.put("q3","What does Anoosha need to achieve in the first paragraph of her cover letter? (Select one");
        MODULE_2_QUESTIONS.put("q4","How should Anoosha outline her skills in her cover letter? (Select one");
        MODULE_2_QUESTIONS.put("q5","Which of these tips should Anoosha keep in mind when writing her cover letter? (Select one");
        MODULE_2_QUESTIONS.put("q6","What should Anoosha include at the end of her cover letter? (Select one");
        MODULE_2_QUESTIONS.put("q7","What are some mistakes that Anoosha should avoid when writing a cover letter? (Select all that apply");
        MODULE_2_QUESTIONS.put("q8","How many cover letters will Anoosha need to write to apply for ten positions? (Select one");
        MODULE_2_QUESTIONS.put("q9","What is the best way for Anoosha to reach out to an organization that has not advertised a position publicly? (Select one");
        
        //MODULE_3_QUESTIONS.put("./3-jobsearch/en/EPUB/main.html";
        MODULE_3_QUESTIONS.put("q1","Which of these are important parts of Anoosha’s professional online presence? (Select all that apply");
        MODULE_3_QUESTIONS.put("q2","How did Lina use the Internet to find job opportunities online? (Select one");
        MODULE_3_QUESTIONS.put("q3","What are some job search sites that Anoosha should use to find job opportunities online? (Select all that apply");
        MODULE_3_QUESTIONS.put("q4","Anoosha hasn't heard back from any prospective employers. What does Lina suggest she change about her approach? (Select one");
        MODULE_3_QUESTIONS.put("q5","What does Lina suggest Anoosha keep in mind when reviewing job application requirements? (Select one");
        MODULE_3_QUESTIONS.put("q6","Lina tells Anoosha to conduct a targeted job search. Why is this important? (Select one");
        MODULE_3_QUESTIONS.put("q7","How should Anoosha use her network to get a job interview? (Select one");
        MODULE_3_QUESTIONS.put("q8","Anoosha calls the organization. Which of the statements below is false? (Select one");
        
        //MODULE_4_QUESTIONS.put("./4-interview/en/EPUB/main.html";
        MODULE_4_QUESTIONS.put("q1","It is important to give a proper handshake at a job interview. What should Lina do? (Select one");
        MODULE_4_QUESTIONS.put("q2","\"Tell me about yourself\" is a common interview question. How should Lina respond? (Select one");
        MODULE_4_QUESTIONS.put("q3","What should Lina do to prepare for the interview? (Select all that apply");
        MODULE_4_QUESTIONS.put("q4","Lina needs to explain why she is a good fit for this position. What should she say? (Select one");
        MODULE_4_QUESTIONS.put("q5","Lina needs to explain what her greatest strength is. What should she say? (Select one");
        MODULE_4_QUESTIONS.put("q6","Lina needs to explain what her greatest weakness is. What should she say? (Select one");
        MODULE_4_QUESTIONS.put("q7","Lina needs to describe what type of team member she is. What should she say? (Select one");
        MODULE_4_QUESTIONS.put("q8","Lina needs to explain why she wants to work for the company. What should she say? (Select one");
        MODULE_4_QUESTIONS.put("q9","Lina needs to describe how she would react if a colleague disappointed her. What should she say? (Select one");
        MODULE_4_QUESTIONS.put("q10","It's Lina's turn to ask a question. What should she say? (Select one");
        
        //MODULE_5_QUESTIONS.put("./5-communication/en/EPUB/main.html";
        MODULE_5_QUESTIONS.put("q1","Non-verbal communication is important. How you say something in a professional setting matters. Lina has to rearrange her schedule to meet her boss's request. How should she respond? (Select all that apply");
        MODULE_5_QUESTIONS.put("q2","Verbal communication is also important. What you say in a professional setting matters. Lina has to rearrange her schedule to meet her boss's request. How should she respond? (Select one");
        MODULE_5_QUESTIONS.put("q3","Good communication is complete, concise, considerate, concrete, clear, courteous, and correct. This is called ‘The Seven C’s of Effective Communication’. Lina needs to speak with a client about a missing payment. It is important that she is considerate. What should she say? (Select one");
        MODULE_5_QUESTIONS.put("q4","The client wrote the wrong account number on the transfer form. Lina needs to inform the client. It is important that she communicates the complete error. What should she say? (Select one");
        MODULE_5_QUESTIONS.put("q5","The client needs instructions on how to make a bank transfer payment. It is important that Lina is clear. How should she communicate this in an email? (Select one");
        MODULE_5_QUESTIONS.put("q6","Lina needs to update her boss on the status of the late payment. It is important that she is concise. What should she say? (Select one");
        MODULE_5_QUESTIONS.put("q7","According to company policy, existing services may be terminated if a payment is more than four weeks late. Lina needs to inform the client. It is important that she is concrete. How should she communicate this in an email? (Select one");
        MODULE_5_QUESTIONS.put("q8","The client still hasn’t paid. Lina needs to send a final warning. How should she communicate this in an email? (Select one");
        MODULE_5_QUESTIONS.put("q9","Lina needs to acknowledge receipt of the client’s payment. It is important that she is courteous. How should she communicate this in an email? (Select one");
        MODULE_5_QUESTIONS.put("q10","Lina needs to identify some professional email etiquette tips from the list below. Which should she choose? (Select all that apply");
        
        //MODULE_6_QUESTIONS.put("./6-ethics/en/EPUB/main.html";
        MODULE_6_QUESTIONS.put("q1","Lina needs to guess what some common ethical values are. What should she say? (Select all that apply");
        MODULE_6_QUESTIONS.put("q2","Lina needs to decide whether or not it is ethical to tell her sister about the price a client paid for a website. What should she do? (Select one");
        MODULE_6_QUESTIONS.put("q3","Lina spoke with her best friend for half an hour during working hours. Amina asks her if this was ethical. What should Lina say? (Select one");
        MODULE_6_QUESTIONS.put("q4","Lina needs to guess what kind of things could influence her personal ethical code. What should she say? (Select all that apply");
        MODULE_6_QUESTIONS.put("q5","Lina needs to think of some questions that she should ask herself before she makes a work decision. What should she say? (Select all that apply");
        MODULE_6_QUESTIONS.put("q6","Lina needs to think of some examples of bad office etiquette. What should she say? (Select all that apply");
        MODULE_6_QUESTIONS.put("q7","What amount should Lina ask the shop manager to put on the bill? (Select one");
        MODULE_6_QUESTIONS.put("q8","By asking the shop manager to put the correct amount of money on the bill, Lina made an ethical decision. It was a good decision for the company and for Lina. How will Lina benefit from this decision personally? (Select all that apply");
        MODULE_6_QUESTIONS.put("q9","Before Lina can go home she needs to identify which of the following statements are not true. (Select all that apply");
        
        //MODULE_7_QUESTIONS.put("./7-timemanagement/en/EPUB/main.html";
        MODULE_7_QUESTIONS.put("q1","Lina needs to complete accounting entries for a client by next Thursday: is this a goal or an objective? (Select one");
        MODULE_7_QUESTIONS.put("q2","Lina would like to become a senior accountant. Is this a goal or an objective? (Select one");
        MODULE_7_QUESTIONS.put("q3","How can Lina avoid time stealers and be more productive? (Select all that apply");
        MODULE_7_QUESTIONS.put("q4","Which of these are SMART objectives? (Select all that apply");
        MODULE_7_QUESTIONS.put("q5","Which of these are valid action steps for the objective of completing the client audit report by the end of the week? (Select all that apply");
        MODULE_7_QUESTIONS.put("q6","How should we categorize the objective: Complete an audit of AHG within two days? (Select all that apply");
        MODULE_7_QUESTIONS.put("q7","How should we categorize the objective: Write suggestions for the company’s strategy in the upcoming year? (Select all that apply");
        MODULE_7_QUESTIONS.put("q8","How should we categorize the objective: Arrange a meeting with a potential new customer before they can be acquired by a competitor? (Select all that apply");
        MODULE_7_QUESTIONS.put("q9","How should we categorize the objective: Attend a voluntary professional development workshop on multitasking this afternoon? (Select all that apply");
        MODULE_7_QUESTIONS.put("q8","Waheed asks Malik for advice on how to increase sales. What should Malik say? (Select one");
        
        //MODULE_8_QUESTIONS.put("./8-reportwriting/en/EPUB/main.html";
        MODULE_8_QUESTIONS.put("q1","How should Lina determine the goal and audience of a report? (Select all that apply");
        MODULE_8_QUESTIONS.put("q2","Why does Lina need to learn how to classify a report based on length, tone, subject matter, timing, importance and style? (Select one");
        MODULE_8_QUESTIONS.put("q3","Lina needs to guess the difference between the tone and style of a report. What should she say? (Select one");
        MODULE_8_QUESTIONS.put("q4","Lina needs to guess the difference between the summary and introduction of a report. What should she say? (Select one");
        MODULE_8_QUESTIONS.put("q5","Lina needs to guess why it is important to attach appendices to a report. What should she say? (Select one");
        MODULE_8_QUESTIONS.put("q6","The first step of report writing is to ‘analyze’. Lina needs to guess what this means. What should she say? (Select all that apply");
        MODULE_8_QUESTIONS.put("q7","Lina needs to guess why Step 2 of report writing (plan) is different from Step 1 (analyze). What should she say? (Select one");
        MODULE_8_QUESTIONS.put("q8","Lina needs to guess what the purpose of Step 3 (draft) is. What should she say? (Select one");
        MODULE_8_QUESTIONS.put("q9","Lina needs to guess why is it important to edit the draft created in Step 3 (draft). What should she say? (Select one");
        
        //MODULE_9_QUESTIONS.put("./9-entrepreneurship/en/main.html";
        MODULE_9_QUESTIONS.put("q1","Malik explains to Waheed that they need more than an idea to become successful entrepreneurs. What are some additional qualities that they should have? (Select all that apply");
        MODULE_9_QUESTIONS.put("q2","Waheed needs to guess what some advantages of being an entrepreneur are. What should he say? (Select all that apply");
        MODULE_9_QUESTIONS.put("q3","Waheed needs to guess what some disadvantages of being an entrepreneur are. What should he say? (Select all that apply");
        MODULE_9_QUESTIONS.put("q4","Waheed needs to guess where an entrepreneur can get ideas from. What should he say? (Select all that apply");
        MODULE_9_QUESTIONS.put("q5","Waheed needs to guess which of the following statements about being a services entrepreneur are true. (Select all that apply");
        MODULE_9_QUESTIONS.put("q6","Waheed needs to guess which of the following statements about packaging products are true. (Select all that apply");
        MODULE_9_QUESTIONS.put("q7","Waheed needs to guess which of the following products and services require consultative selling. (Select all that apply");

    }
}
