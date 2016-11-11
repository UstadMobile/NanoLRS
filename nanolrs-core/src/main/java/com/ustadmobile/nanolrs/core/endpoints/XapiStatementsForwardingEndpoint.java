package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.Base64Coder;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mike on 9/13/16.
 */
public class XapiStatementsForwardingEndpoint {

    public static final String LOGTAG = "XapiStatementsForwardingEndpoint";

    public static List<XapiQueueStatusListener> queueStatusListeners;

    static {
        queueStatusListeners = Collections.synchronizedList(new ArrayList<XapiQueueStatusListener>());
    }

    public static void addQueueStatusListener(XapiQueueStatusListener listener) {
        queueStatusListeners.add(listener);
    }

    public static void removeQueueStatusListener(XapiQueueStatusListener listener) {
        queueStatusListeners.remove(listener);
    }

    public static void putAndQueueStatement(Object dbContext, JSONObject statement, String destinationURL, String httpUser, String httpPassword) {
        String uuid = XapiStatementsEndpoint.putStatement(statement, dbContext);
        XapiStatementProxy statementProxy = PersistenceManager.getInstance().getStatementManager().findByUuidSync(dbContext, uuid);
        queueStatement(dbContext, statementProxy, destinationURL, httpUser, httpPassword);
    }

    /**
     * Queue an individual xAPI statement to be sent to the given destinationURL XAPI server
     *
     * @param dbContext Database context object
     * @param statement XapiStatement to send
     * @param destinationURL Xapi endpoint to send to (eg http://server.com/xAPI
     * @param httpUser HTTP Auth Username (will be removed from database after statement is sent)
     * @param httpPassword HTTP Auth Password (will be removed from database after statement is sent)
     */
    public static void queueStatement(Object dbContext, XapiStatementProxy statement, String destinationURL, String httpUser, String httpPassword) {
        queueStatements(dbContext, new XapiStatementProxy[]{statement}, destinationURL, httpUser, httpPassword);
    }

    /**
     * Queue an individual xAPI statement to be sent to the given destinationURL XAPI server
     *
     * @param dbContext Database context object
     * @param statements Array of XapiStatements to send
     * @param destinationURL Xapi endpoint to send to (eg http://server.com/xAPI
     * @param httpUser HTTP Auth Username (will be removed from database after statement is sent)
     * @param httpPassword HTTP Auth Password (will be removed from database after statement is sent)
     */
    public static void queueStatements(Object dbContext, XapiStatementProxy[] statements, String destinationURL, String httpUser, String httpPassword) {
        XapiForwardingStatementManager manager =PersistenceManager.getInstance().getForwardingStatementManager();
        for(int i = 0; i < statements.length; i++) {
            XapiForwardingStatementProxy fwdStmt = manager.createSync(dbContext, statements[i].getId());
            fwdStmt.setDestinationURL(destinationURL);
            fwdStmt.setHttpAuthUser(httpUser);
            fwdStmt.setHttpAuthPassword(httpPassword);
            fwdStmt.setStatement(statements[i]);
            fwdStmt.setStatus(XapiForwardingStatementProxy.STATUS_QUEUED);
            manager.persistSync(dbContext, fwdStmt);
        }
        fireQueueStatusChangedEvent(manager.getUnsentStatementCount(dbContext));
    }


    /**
     * Attempt to send all statements in the Queue that are pending
     *
     * @param dbContext Database context object
     *
     * @return The number of statements sent (successfully) on this run.
     */
    public static int sendQueue(Object dbContext) {
        XapiForwardingStatementManager manager = PersistenceManager.getInstance().getForwardingStatementManager();
        List<XapiForwardingStatementProxy> toForward = manager.getAllUnsentStatementsSync(dbContext);
        int statementsSent = 0;
        for(XapiForwardingStatementProxy stmt : toForward) {
            HttpLrs.LrsResponse response = new HttpLrs(stmt.getDestinationURL()).putStatement(
                new JSONObject(stmt.getStatement().getFullStatement()), stmt.getHttpAuthUser(),
                    stmt.getHttpAuthPassword());

            if(response.getStatus() == 204) {
                stmt.setStatus(XapiForwardingStatementProxy.STATUS_SENT);
                statementsSent++;
                stmt.setHttpAuthUser(null);//no longer needed - can be removed from database
                stmt.setHttpAuthPassword(null);
            }else{
                stmt.setStatus(XapiForwardingStatementProxy.STATUS_TRYAGAIN);
                stmt.setTryCount(stmt.getTryCount() + 1);
            }

            manager.persistSync(dbContext, stmt);
        }

        if(statementsSent > 0) {
            fireQueueStatusChangedEvent(manager.getUnsentStatementCount(dbContext));
        }

        return statementsSent;
    }

    /**
     * Called internally when there's a change to the queue status to fire event off
     * 
     * @param numStatementsRemaining
     */
    protected static void fireQueueStatusChangedEvent(int numStatementsRemaining) {
        XapiQueueStatusEvent evt = new XapiQueueStatusEvent(numStatementsRemaining);
        for(int i = 0; i < queueStatusListeners.size(); i++) {
            queueStatusListeners.get(i).queueStatusUpdated(evt);
        }
    }

}
