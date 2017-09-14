package com.ustadmobile.nanolrs.util;

import java.util.List;
import java.util.Map;

/**
 * Module representation to make it easier
 * Created by varuna on 9/13/2017.
 */

public class Module {

    String shortID;
    List<String> ids;
    String name;
    Map<String, String> questionMap;

    public Module(String shortID, List<String> ids, String name, Map<String, String> questionMap) {
        this.shortID = shortID;
        this.ids = ids;
        this.name = name;
        this.questionMap = questionMap;
    }

    public Module(){

    }

    public String getShortID() {
        return shortID;
    }

    public void setShortID(String shortID) {
        this.shortID = shortID;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getQuestionMap() {
        return questionMap;
    }

    public void setQuestionMap(Map<String, String> questionMap) {
        this.questionMap = questionMap;
    }


    /* Passed statement eg:
    {
       "id":"9c3fa42e-221a-4480-92bf-39f9ac8c7ab1",
       "timestamp":"2017-08-04T20:11:52.796Z",
       "actor":{
          "objectType":"Agent",
          "account":{
             "name":"varunas2",
             "homePage":"https:\/\/umcloud1.ustadmobile.com\/umlrs\/"
          }
       },
       "verb":{
          "id":"http:\/\/adlnet.gov\/expapi\/verbs\/passed",
          "display":{
             "und":"passed"
          }
       },
       "result":{
          "success":true,
          "extensions":{
             "https:\/\/w3id.org\/xapi\/cmi5\/result\/extensions\/progress":100
          },
          "score":{
             "scaled":0.96875,
             "raw":15.5,
             "min":0,
             "max":16
          },
          "completion":true
       },
       "object":{
          "id":"epub:202b10fe-b028-4b84-9b84-852aa123456a",
          "objectType":"Activity"
       }
    }
     */

    /* Failed eg:
    {
       "id":"c285af5f-fd2e-4aaa-84c4-5411e1d8a5a8",
       "timestamp":"2017-08-12T17:21:02.103Z",
       "actor":{
          "objectType":"Agent",
          "account":{
             "name":"meena",
             "homePage":"https:\/\/umcloud1.ustadmobile.com\/umlrs\/"
          }
       },
       "verb":{
          "id":"http:\/\/adlnet.gov\/expapi\/verbs\/failed",
          "display":{
             "und":"failed"
          }
       },
       "result":{
          "success":false,
          "extensions":{
             "https:\/\/w3id.org\/xapi\/cmi5\/result\/extensions\/progress":100
          },
          "score":{
             "scaled":0.6,
             "raw":10.5,
             "min":0,
             "max":17.5
          },
          "completion":true
       },
       "object":{
          "id":"epub:023970e2-2d4b-4fd5-9bbd-de373bb2aad6",
          "objectType":"Activity"
       }
    }
     */

    /* Answered eg:
    {
       "id":"3c727ff2-901e-4b53-9f3a-03c090ea543d",
       "timestamp":"2017-08-12T11:17:04.752Z",
       "actor":{
          "objectType":"Agent",
          "account":{
             "name":"M.Reza ",
             "homePage":"https:\/\/umcloud1.ustadmobile.com\/umlrs\/"
          }
       },
       "verb":{
          "id":"http:\/\/adlnet.gov\/expapi\/verbs\/answered",
          "display":{
             "und":"answered"
          }
       },
       "result":{
          "extensions":{
             "https:\/\/w3id.org\/xapi\/cmi5\/result\/extensions\/progress":22
          }
       },
       "object":{
          "id":"epub:023970e2-2d4b-4fd5-9bbd-de373bb2aad6\/q1",
          "objectType":"Activity"
       }
    }
     */
}
