package com.ustadmobile.nanolrs.core.http;

import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.core.util.Base64Coder;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to handle talking with a remote LRS
 *
 * Created by mike on 9/28/16.
 */

public class HttpLrs {

    private String endpoint;

    public static class LrsResponse {

        private int status;

        private String serverResponse;

        private Exception exception;

        public LrsResponse() {

        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getServerResponse() {
            return serverResponse;
        }

        public void setServerResponse(String serverResponse) {
            this.serverResponse = serverResponse;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public int getStatus() {
            return status;
        }

    }


    public HttpLrs(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public LrsResponse putStatement(JSONObject stmt, String httpUsername, String httpPassword) {
        HttpURLConnection connection = null;
        OutputStream out;
        String destURL = endpoint;
        LrsResponse response = new LrsResponse();
        try {
            if(!destURL.endsWith("/")) {
                destURL += "/";
            }

            if(!stmt.has("id")) {
                stmt.put("id", UUID.randomUUID().toString());
            }

            destURL += "statements?statementId=" + stmt.getString("id");
            URL url = new URL(destURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");

            byte[] payload = stmt.toString().getBytes();
            connection.setFixedLengthStreamingMode(payload.length);
            connection.setDoOutput(true);

            connection.setRequestProperty("X-Experience-API-Version", "1.0.1");
            connection.setRequestProperty("Authorization", "Basic " + Base64Coder.encodeString(
                    httpUsername + ":" + httpPassword));
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Content-Type", "application/json");

            out = connection.getOutputStream();
            out.write(payload);
            out.flush();
            out.close();
            out = null;

            int statusCode = connection.getResponseCode();
            response.setStatus(statusCode);
            if(statusCode >= 400) {
                InputStream errorStream = connection.getErrorStream();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int bytesRead = 0;

                while((bytesRead = errorStream.read(buf)) != -1) {
                    bout.write(buf, 0, bytesRead);
                }

                response.setServerResponse(new String(bout.toByteArray()));
            }

        }catch (IOException e){
            e.printStackTrace();
            response.setException(e);
        }finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        return response;

    }


}
