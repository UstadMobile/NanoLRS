package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.core.util.ParseUtil;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by mike on 9/13/16.
 */
public class XapiStatementsEndpoint {

    /*
    public class StatementSaver implements PersistenceReceiver {

        JSONObject stmtObj;

        XapiStatementProxy stmt;

        Object dbContext;

        static final int STEP_INIT = 0;

        public StatementSaver(Object dbContext, JSONObject stmtObj, PersistenceReceiver endReceiver) {
            this.dbContext = dbContext;
            this.stmtObj = stmtObj;
            PersistenceManager.getInstance().getStatementManager().create(dbContext, STEP_INIT, this);
        }




        @Override
        public void onPersistenceSuccess(Object result, int requestId) {
            switch(requestId) {
                case STEP_INIT:
                    stmt = (XapiStatementProxy)result;
                    if(stmtObj.has("uuid")) {
                        stmt.setUuid(stmtObj.getString("uuid"));
                    }else {
                        //stmt.setUuid()
                    }

                    //find the agent

            }
        }

        @Override
        public void onPersistenceFailure(Object result, int requestId) {

        }

    }*/


    public static String putStatement(JSONObject stmt, Object dbContext) {
        XapiStatementProxy stmtProxy =PersistenceManager.getInstance().getStatementManager().createSync(dbContext);
        if(stmt.has("id")) {
            stmtProxy.setId(stmt.getString("id"));
        }else {
            stmtProxy.setId(UUID.randomUUID().toString());
            stmt.put("id", stmtProxy.getId());
        }

        if(stmt.has("timestamp")) {
            Calendar cal = ParseUtil.parse8601Timestamp(stmt.getString("timestamp"));
            stmtProxy.setTimestamp(cal.getTime().getTime());
        }else {
            stmtProxy.setTimestamp(new Date().getTime());
        }

        stmtProxy.setFullStatement(stmt.toString());

        PersistenceManager.getInstance().getStatementManager().persistSync(dbContext, stmtProxy);

        return stmtProxy.getId();

    }


}
