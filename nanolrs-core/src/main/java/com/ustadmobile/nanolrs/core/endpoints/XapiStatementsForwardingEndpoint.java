package com.ustadmobile.nanolrs.core.endpoints;

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
            HttpURLConnection connection = null;
            String destURL = stmt.getDestinationURL();
            OutputStream out;
            try {
                if(!destURL.endsWith("/")) {
                    destURL += "/";
                }
                destURL += "statements?statementId=" + stmt.getStatement().getId();
                URL url = new URL(destURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");

                byte[] payload = stmt.getStatement().getFullStatement().getBytes();
                connection.setFixedLengthStreamingMode(payload.length);
                connection.setDoOutput(true);

                connection.setRequestProperty("X-Experience-API-Version", "1.0.1");
                connection.setRequestProperty("Authorization", "Basic " + Base64Coder.encodeString(
                    stmt.getHttpAuthUser() + ":" + stmt.getHttpAuthPassword()));
                connection.setRequestProperty("Accept", "*/*");
                connection.setRequestProperty("Content-Type", "application/json");

                out = connection.getOutputStream();
                out.write(payload);
                out.flush();
                out.close();
                out = null;

                int statusCode = connection.getResponseCode();
                String serverSays = null;
                if(statusCode >= 400) {
                    InputStream errorStream = connection.getErrorStream();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int bytesRead = 0;

                    while((bytesRead = errorStream.read(buf)) != -1) {
                        bout.write(buf, 0, bytesRead);
                    }

                    serverSays = new String(bout.toByteArray());
                }

                if(statusCode == 204) {
                    stmt.setStatus(XapiForwardingStatementProxy.STATUS_SENT);
                    stmt.setHttpAuthUser(null);//no longer needed - can be removed from database
                    stmt.setHttpAuthPassword(null);
                }else{
                    stmt.setStatus(XapiForwardingStatementProxy.STATUS_TRYAGAIN);
                    stmt.setTryCount(stmt.getTryCount() + 1);
                }

                manager.persistSync(dbContext, stmt);
            }catch (IOException e){
                Logger.getLogger(LOGTAG).log(Level.WARNING, "Exception sending statement",e);
            }finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

}
