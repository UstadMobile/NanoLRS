package com.ustadmobile.nanolrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by varuna on 7/30/2017.
 */

public class ServletUtil {

    public static String getUsernameFromBasicAuth(HttpServletRequest request){
        String[] credentials = getCredentialStringFromBasicAuth(request);
        return credentials[0];
    }

    public static String getPasswordFromBasicAuth(HttpServletRequest request){
        String[] credentials = getCredentialStringFromBasicAuth(request);
        return credentials[1];
    }
    public static String[] getCredentialStringFromBasicAuth(HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = Base64Coder.decodeString(base64Credentials);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            return values;
        }
        return null;
    }

    public static String getHeaderVal(HttpServletRequest request, String headerName){
        return request.getHeader(headerName);
    }

    public static String getParamVal(HttpServletRequest request, String paramName){
        return request.getParameter(paramName);
    }

    public static Map<String, String> getHeadersFromRequest(HttpServletRequest request){
        Map<String, String> requestHeaders = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames != null && headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            requestHeaders.put(headerName, getHeaderVal(request, headerName));
        }
        if(headerNames == null){
            return null;
        }
        return requestHeaders;
    }

    public static Map<String, String> getParamsFromRequest(HttpServletRequest request){
        Map<String, String> requestParameters = new HashMap<>();
        Enumeration<String> parameterNames = request.getHeaderNames();
        while(parameterNames != null && parameterNames.hasMoreElements()){
            String paramName = parameterNames.nextElement();
            requestParameters.put(paramName, getHeaderVal(request, paramName));
        }
        if(parameterNames == null){
            return null;
        }
        return requestParameters;
    }

    public static String convertStreamToString(InputStream is, String encoding) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, encoding);
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    public static String encodeBasicAuth(String username, String password) {
        return "Basic " + Base64Coder.encodeString(username +
                ':' + password);
    }
}