package com.ustadmobile.nanolrs.core.sync;

import java.io.InputStream;
import java.util.Map;


/**
 * This class represents the sync result of UMSync. This object will be returned upon every
 * sync and can be looked into for status and response (headers and parameters)
 * Created by varuna on 6/27/2017.
 */

public class UMSyncResult {

    int status;
    Map headers;
    Map parameters;
    String response;
    String  responseMessage;

    public UMSyncResult(){
       //Blank space
    }

    public UMSyncResult(int status, Map headers, Map parameters, String response, String responseMessage) {
        this.status = status;
        this.headers = headers;
        this.parameters = parameters;
        this.response = response;
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map getHeaders() {
        return headers;
    }

    public void setHeaders(Map headers) {
        this.headers = headers;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Object getHeader(String headerName){
        if(headers.containsKey(headerName)){
            return this.headers.get(headerName);
        }else{
            return null;
        }
    }

    public Object getParameter(String parameterName){
        if(parameters.containsKey(parameterName)) {
            return this.parameters.get(parameterName);
        }else{
            return null;
        }
    }

    public boolean isSuccess(){
        if(this.status == 200){
            return true;
        }else{
            return false;
        }
    }
}
