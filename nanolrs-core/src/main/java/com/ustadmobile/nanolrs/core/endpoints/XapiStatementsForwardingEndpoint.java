package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mike on 9/13/16.
 */
public class XapiStatementsForwardingEndpoint {

    public static final String LOGTAG = "XapiStatementsForwardingEndpoint";

    public static List<XapiStatementsForwardingListener> queueStatusListeners;

    static {
        queueStatusListeners = Collections.synchronizedList(new ArrayList<XapiStatementsForwardingListener>());
    }

    public static void addQueueStatusListener(XapiStatementsForwardingListener listener) {
        queueStatusListeners.add(listener);
    }

    public static void removeQueueStatusListener(XapiStatementsForwardingListener listener) {
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
        fireStatementQueuedEvent(statement);
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
        fireQueueStatusUpdated(new XapiStatementsForwardingEvent(manager.getUnsentStatementCount(dbContext)));
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
                fireStatementSentEvent(stmt.getStatement());
            }else{
                stmt.setStatus(XapiForwardingStatementProxy.STATUS_TRYAGAIN);
                stmt.setTryCount(stmt.getTryCount() + 1);
            }

            manager.persistSync(dbContext, stmt);
        }

        if(statementsSent > 0) {
            fireQueueStatusUpdated(new XapiStatementsForwardingEvent(
                    manager.getUnsentStatementCount(dbContext)));
        }

        return statementsSent;
    }

    protected static void fireStatementQueuedEvent(XapiStatementProxy statement) {
        if(queueStatusListeners.isEmpty())
            return;

        XapiStatementsForwardingEvent event = new XapiStatementsForwardingEvent(statement);
        for(int i = 0; i < queueStatusListeners.size(); i++){
            queueStatusListeners.get(i).statementQueued(event);
        }
    }

    protected static void fireStatementSentEvent(XapiStatementProxy statement){
        if(queueStatusListeners.isEmpty())
            return;

        XapiStatementsForwardingEvent event = new XapiStatementsForwardingEvent(statement);
        for(int i = 0; i < queueStatusListeners.size(); i++){
            queueStatusListeners.get(i).queueStatementSent(event);
        }
    }


    /**
     * Called internally when there's a change to the queue status to fire event off
     *
     * @param event
     */
    protected static void fireQueueStatusUpdated(XapiStatementsForwardingEvent event) {
        for(int i = 0; i < queueStatusListeners.size(); i++) {
            queueStatusListeners.get(i).queueStatusUpdated(event);
        }
    }


}
