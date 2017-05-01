package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
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
        XapiStatement statementProxy = PersistenceManager.getInstance().getManager(XapiStatementManager.class).findByUuidSync(dbContext, uuid);
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
    public static void queueStatement(Object dbContext, XapiStatement statement, String destinationURL, String httpUser, String httpPassword) {
        queueStatements(dbContext, new XapiStatement[]{statement}, destinationURL, httpUser, httpPassword);
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
    public static void queueStatements(Object dbContext, XapiStatement[] statements, String destinationURL, String httpUser, String httpPassword) {
        XapiForwardingStatementManager manager =PersistenceManager.getInstance().getManager(XapiForwardingStatementManager.class);
        for(int i = 0; i < statements.length; i++) {
            XapiForwardingStatement fwdStmt = manager.createSync(dbContext, statements[i].getUuid());
            fwdStmt.setDestinationURL(destinationURL);
            fwdStmt.setHttpAuthUser(httpUser);
            fwdStmt.setHttpAuthPassword(httpPassword);
            fwdStmt.setStatement(statements[i]);
            fwdStmt.setStatus(XapiForwardingStatement.STATUS_QUEUED);
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
        XapiForwardingStatementManager manager = PersistenceManager.getInstance().getManager(XapiForwardingStatementManager.class);
        List<XapiForwardingStatement> toForward = manager.getAllUnsentStatementsSync(dbContext);
        int statementsSent = 0;
        for(XapiForwardingStatement stmt : toForward) {
            try {
                HttpLrs.LrsResponse response = new HttpLrs(stmt.getDestinationURL()).putStatement(
                        new JSONObject(stmt.getStatement().getFullStatement()), stmt.getHttpAuthUser(),
                        stmt.getHttpAuthPassword());
                if(response.getStatus() == 204 || response.getStatus() == 409) {
                    //Previous versions might not persist the status if event hnadler threw an exception.
                    // Very unlikely another statement with the exact same id got generated by an error - statement is already there
                    stmt.setStatus(XapiForwardingStatement.STATUS_SENT);
                    statementsSent++;
                    stmt.setHttpAuthUser(null);//no longer needed - can be removed from database
                    stmt.setHttpAuthPassword(null);
                    manager.persistSync(dbContext, stmt);
                    fireStatementSentEvent(stmt.getStatement());
                }else{
                    stmt.setStatus(XapiForwardingStatement.STATUS_TRYAGAIN);
                    stmt.setTryCount(stmt.getTryCount() + 1);
                    manager.persistSync(dbContext, stmt);
                }

            }catch(Exception e) {
                System.err.println("Exception attempting to forward statement");
                e.printStackTrace(System.err);
            }
        }

        if(statementsSent > 0) {
            fireQueueStatusUpdated(new XapiStatementsForwardingEvent(
                    manager.getUnsentStatementCount(dbContext)));
        }

        return statementsSent;
    }

    protected static void fireStatementQueuedEvent(XapiStatement statement) {
        if(queueStatusListeners.isEmpty())
            return;

        XapiStatementsForwardingEvent event = new XapiStatementsForwardingEvent(statement);
        for(int i = 0; i < queueStatusListeners.size(); i++){
            queueStatusListeners.get(i).statementQueued(event);
        }
    }

    protected static void fireStatementSentEvent(XapiStatement statement){
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
