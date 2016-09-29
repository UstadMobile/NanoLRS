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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mike on 9/13/16.
 */
public class XapiStatementsForwardingEndpoint {

    public static final String LOGTAG = "XapiStatementsForwardingEndpoint";

    public static void putAndQueueStatement(Object dbContext, JSONObject statement, String destinationURL, String httpUser, String httpPassword) {
        String uuid = XapiStatementsEndpoint.putStatement(statement, dbContext);
        XapiStatementProxy statementProxy = PersistenceManager.getInstance().getStatementManager().findByUuidSync(dbContext, uuid);
        queueStatement(dbContext, statementProxy, destinationURL, httpUser, httpPassword);
    }

    public static void queueStatement(Object dbContext, XapiStatementProxy statement, String destinationURL, String httpUser, String httpPassword) {
        XapiForwardingStatementManager manager =PersistenceManager.getInstance().getForwardingStatementManager();
        XapiForwardingStatementProxy fwdStmt = manager.createSync(dbContext, statement.getId());
        fwdStmt.setDestinationURL(destinationURL);
        fwdStmt.setHttpAuthUser(httpUser);
        fwdStmt.setHttpAuthPassword(httpPassword);
        fwdStmt.setStatement(statement);
        fwdStmt.setStatus(XapiForwardingStatementProxy.STATUS_QUEUED);
        manager.persistSync(dbContext, fwdStmt);
    }

    public static void sendQueue(Object dbContext) {
        XapiForwardingStatementManager manager = PersistenceManager.getInstance().getForwardingStatementManager();
        List<XapiForwardingStatementProxy> toForward = manager.getAllUnsentStatementsSync(dbContext);
        for(XapiForwardingStatementProxy stmt : toForward) {
            HttpLrs.LrsResponse response = new HttpLrs(stmt.getDestinationURL()).putStatement(
                new JSONObject(stmt.getStatement().getFullStatement()), stmt.getHttpAuthUser(),
                    stmt.getHttpAuthPassword());

            if(response.getStatus() == 204) {
                stmt.setStatus(XapiForwardingStatementProxy.STATUS_SENT);
                stmt.setHttpAuthUser(null);//no longer needed - can be removed from database
                stmt.setHttpAuthPassword(null);
            }else{
                stmt.setStatus(XapiForwardingStatementProxy.STATUS_TRYAGAIN);
                stmt.setTryCount(stmt.getTryCount() + 1);
            }

            manager.persistSync(dbContext, stmt);
        }
    }

}
