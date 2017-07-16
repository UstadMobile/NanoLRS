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

    private InputStream responseData;
    private long responseLength;

    public UMSyncResult(){
       //Blank space
    }

    public long getResponseLength() {
        return responseLength;
    }

    public void setResponseLength(long responseLength) {
        this.responseLength = responseLength;
    }

    public UMSyncResult(int status, Map headers, InputStream responseData,
                        long responseLength) {
        this.status = status;
        this.headers = headers;
        this.responseData = responseData;
        this.responseLength = responseLength;
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

    public Object getHeader(String headerName){
        if(headers.containsKey(headerName)){
            return this.headers.get(headerName);
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

    public InputStream getResponseData() {
        return responseData;
    }

    public void setResponseData(InputStream responseData) {
        this.responseData = responseData;
    }
}
