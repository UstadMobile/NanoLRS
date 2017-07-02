package com.ustadmobile.nanolrs.core.sync;

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

    public UMSyncResult(int status, Map headers, Map parameters) {
        this.status = status;
        this.headers = headers;
        this.parameters = parameters;
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