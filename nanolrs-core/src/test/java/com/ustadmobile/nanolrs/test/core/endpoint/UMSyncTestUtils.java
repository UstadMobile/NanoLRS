package com.ustadmobile.nanolrs.test.core.endpoint;

import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by varuna on 10/10/2017.
 */

public class UMSyncTestUtils {

    public static User addUser(String username, Object context) throws SQLException {
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);

        String newTestingUserID = UUID.randomUUID().toString();
        User testingUser = (User)userManager.makeNew();
        testingUser.setUuid(newTestingUserID);
        testingUser.setUsername(username);
        testingUser.setPassword("secret");

        userManager.persist(context, testingUser);

        return testingUser;
    }

    public static void generateRandomStatement(User user, int statementsCount, Object context){
        XapiStatementManager statementManager =
                PersistenceManager.getInstance().getManager(XapiStatementManager.class);

        for(int i=0; i<statementsCount; i++) {
            String regId = UUID.randomUUID().toString();
            String statementString = "{\n" +
                    "  \"actor\": {\n" +
                    "    \"account\": {\n" +
                    "      \"homePage\": \"http://umcloud1.ustadmobile.com/umlrs\",\n" +
                    "      \"name\": \"" + user.getUsername() + "\"\n" +
                    "    },\n" +
                    "    \"objectType\": \"Agent\"\n" +
                    "  },\n" +
                    "  \"verb\": {\n" +
                    "    \"id\": \"http://activitystrea.ms/schema/1.0/host\",\n" +
                    "    \"display\": {\n" +
                    "      \"en-US\": \"hosted\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"result\": {\n" +
                    "    \"success\" : true,\n" +
                    "    \"completion\" : true,\n" +
                    "    \"score\" : {\n" +
                    "      \"scaled\" : 0.5\n" +
                    "    },\n" +
                    "    \"extensions\": {\n" +
                    "      \"https://w3id.org/xapi/cmi5/result/extensions/progress\" : 50\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"object\": {\n" +
                    "    \"id\": \"http://www.ustadmobile.com/activities/attended-class/CLASSID\",\n" +
                    "    \"objectType\": \"Activity\",\n" +
                    "    \"definition\": {\n" +
                    "      \"name\": {\n" +
                    "        \"en-US\": \"Class Name\"\n" +
                    "      },\n" +
                    "      \"description\": {\n" +
                    "        \"en-US\": \"Class Desc\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"context\" : {\n" +
                    "    \"registration\" : \"" + regId + "\"\n" +
                    "  }\n" +
                    "}";

            JSONObject stmt = new JSONObject(statementString);
            String res = XapiStatementsEndpoint.putStatement(stmt, context);
            System.out.println(res);
        }
    }

    /**
     * Create random user fields..
     * @param user
     * @param context
     * @return
     * @throws SQLException
     */
    public static UserCustomFields updateUserCustomFieldsAtRandom(User user, Object context)
            throws SQLException {
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        String universityName = "Web University";
        String name = "Bob Burger";
        String gender = "M";
        String email = "bob@bobsburgers.com";
        String phoneNumber = "+0123456789";
        String faculty = "A faculty";

        Map<Integer, String> map = new HashMap<>();
        map.put(980, universityName);
        map.put(981, name);
        map.put(982, gender);
        map.put(983, email);
        map.put(984, phoneNumber);
        map.put(985, faculty);

        userCustomFieldsManager.createUserCustom(map,user, context);
        List relUCFs = userCustomFieldsManager.findByUser(user,context);

        return (UserCustomFields) relUCFs.get(0);
    }

    /**
     * Checks and creates this Node
     * @throws SQLException
     */
    public static void checkAndCreateThisNode(String url, Object context) throws SQLException {
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        String thisNodeUUID = UUID.randomUUID().toString();
        String thisNodeUrl = "set-my-url";
        if(url != null){
            thisNodeUrl = url;
        }
        String thisNodeName = "node:" + thisNodeUUID;

        nodeManager.createThisDeviceNode(thisNodeUUID, thisNodeName,
                thisNodeUrl, false, false, context);
    }

    /**
     * Updates a node to master for test.
     *
     * @param context
     * @throws SQLException
     */
    public static void updateNodeType(boolean master, boolean proxy, Object context)
            throws SQLException {
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        Node thisNode = nodeManager.getThisNode(context);
        if(thisNode == null){
            checkAndCreateThisNode(null, context);
            thisNode = nodeManager.getThisNode(context);
        }
        thisNode.setMaster(master);
        thisNode.setProxy(proxy);

        nodeManager.persist(context, thisNode);
    }

    /**
     * Updates a user with random data
     * @param username
     * @param dbContext
     * @return
     */
    public static String updateThisUserWithRandomNotes(String username, Object dbContext)
            throws SQLException {
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        User user = userManager.findByUsername(dbContext, username);
        if(user == null){
            return "";
        }
        String newNote = UUID.randomUUID().toString();
        user.setNotes(newNote);
        userManager.persist(dbContext, user);
        return newNote;
    }


}
