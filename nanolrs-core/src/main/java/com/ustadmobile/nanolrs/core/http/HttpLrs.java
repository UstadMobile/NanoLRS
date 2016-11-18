package com.ustadmobile.nanolrs.core.http;

import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.core.util.Base64Coder;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.IOUtils;


/**
 * Utility class to handle talking with a remote LRS
 *
 * Created by mike on 9/28/16.
 */

public class HttpLrs {

    private String endpoint;

    public static class LrsResponse {

        private int status;

        //private String serverResponse;

        private Exception exception;

        private byte[] serverResponse;

        public LrsResponse() {

        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getServerResponseAsString() {
            try {
                return new String(serverResponse, "UTF-8");
            }catch(IOException e) {
                //should never happen - only when there is no UTF-8 support
                throw new RuntimeException(e);
            }
        }

        public byte[] getServerResponse() {
            return serverResponse;
        }

        public void setServerResponse(byte[] serverResponse) {
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

    public LrsResponse saveState(String method, String httpUsername, String httpPassword, String activityId, String agentJson, String registration, String stateId, String contentType, byte[] content) {
        LrsResponse response = new LrsResponse();
        String destURL = makeStateURL(endpoint, activityId, agentJson, registration, stateId);

        HttpURLConnection con = null;
        OutputStream out = null;
        try {
            URL url = new URL(destURL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method.toUpperCase());
            con.setFixedLengthStreamingMode(content.length);
            con.setDoOutput(true);
            setXapiHeaders(con, httpUsername, httpPassword);
            if(contentType != null) {
                con.setRequestProperty("Content-Type", contentType);
            }

            out = con.getOutputStream();
            out.write(content);
            out.flush();
            out.close();
            out = null;

            int statusCode = con.getResponseCode();
            response.setStatus(statusCode);
        }catch(IOException e) {
            System.err.println("saveState Exception");
            e.printStackTrace();
        }finally {
            if(out != null) {
                try { out.close(); }
                catch(IOException e) {}
            }

            if(con != null) {
                con.disconnect();
            }
        }

        return response;
    }

    public LrsResponse loadState(String httpUsername, String httpPassword, String activityId, String agentJson, String registration, String stateId) {
        LrsResponse response = new LrsResponse();
        String destURL = makeStateURL(endpoint, activityId, agentJson, registration, stateId);
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            URL url = new URL(destURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            setXapiHeaders(connection, httpUsername, httpPassword);
            response.setStatus(connection.getResponseCode());

            in = response.getStatus() >= 200 && response.getStatus() <= 300 ? connection.getInputStream() : connection.getErrorStream();
            byte[] buf = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            while((bytesRead = in.read(buf)) != -1) {
                bout.write(buf, 0, bytesRead);
            }
            bout.flush();
            response.setServerResponse(bout.toByteArray());
        }catch(IOException e) {
            System.err.println("Exception loading state");
            e.printStackTrace();
        }finally {
            if(in != null) {
                try { in.close(); }
                catch(IOException e) {}
            }

            if(connection != null) {
                connection.disconnect();
            }
        }


        return response;
    }

    /**
     * Returns the URL for talking to the LRS about a given statement
     *
     * @param xapiBaseURL
     * @param activityId
     * @param agentJson
     * @param registration
     * @param stateId
     * @return
     */
    private String makeStateURL(String xapiBaseURL, String activityId, String agentJson, String registration, String stateId) {
        String destURL = xapiBaseURL;

        if(!destURL.endsWith("/")) {
            destURL += "/";
        }
        try {
            destURL += "activities/state";
            destURL += "?activityId=" + URLEncoder.encode(activityId, "UTF-8");
            destURL += "&agent=" + URLEncoder.encode(agentJson, "UTF-8");
            if(registration != null) {
                destURL += "&registration=" + registration;//UUID does not URL escaped
            }
            destURL += "&stateId=" + stateId;
            return destURL;
        }catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);//NO UTF-8 - Not going to happen!
        }
    }

    private void setXapiHeaders(HttpURLConnection connection, String httpUsername, String httpPassword) throws IOException {
        connection.setRequestProperty("X-Experience-API-Version", "1.0.1");
        connection.setRequestProperty("Authorization", "Basic " + Base64Coder.encodeString(
                httpUsername + ":" + httpPassword));
    }


    public LrsResponse putStatement(JSONObject stmt, String httpUsername, String httpPassword) {
        HttpURLConnection connection = null;
        OutputStream out = null;
        InputStream errStream = null;
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
            setXapiHeaders(connection, httpUsername, httpPassword);

            //connection.setRequestProperty("X-Experience-API-Version", "1.0.1");
            //connection.setRequestProperty("Authorization", "Basic " + Base64Coder.encodeString(
            //        httpUsername + ":" + httpPassword));
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
                errStream= connection.getErrorStream();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int bytesRead = 0;

                while((bytesRead = errStream.read(buf)) != -1) {
                    bout.write(buf, 0, bytesRead);
                }

                response.setServerResponse(bout.toByteArray());
            }

        }catch (IOException e){
            e.printStackTrace();
            response.setException(e);
        }finally {
            if(errStream != null) {
                try { errStream.close(); }
                catch(IOException e) {
                    e.printStackTrace();
                }
                errStream = null;
            }

            if(out != null) {
                try { out.close(); }
                catch(IOException e) {
                    e.printStackTrace();
                }
                out = null;
            }

            if(connection != null) {
                connection.disconnect();
            }
        }

        return response;

    }


}
