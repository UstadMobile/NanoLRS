package com.ustadmobile.nanolrs.http;

import com.ustadmobile.nanolrs.core.endpoints.XapiStateEndpoint;
import com.ustadmobile.nanolrs.core.model.XapiStateProxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 10/8/16.
 */

public class StateUriResponder implements RouterNanoHTTPD.UriResponder {

    private String getFirstParamVal(NanoHTTPD.IHTTPSession session, String paramName) {
        if(session.getParameters().containsKey(paramName)) {
            return session.getParameters().get(paramName).get(0);
        }else {
            return null;
        }
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String activityId = getFirstParamVal(session, "activityid");
        String agentJson = getFirstParamVal(session, "agent");
        String registration = getFirstParamVal(session, "registration");
        String stateId = getFirstParamVal(session, "stateid");

        Object dbContext = uriResource.initParameter(0, Object.class);
        XapiStateProxy stateProxy = XapiStateEndpoint.getState(dbContext, activityId, agentJson, registration, stateId);
        if(stateProxy == null) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                    "text/plain", "Not Found");
        }

        byte[] stateData = stateProxy.getContent();
        ByteArrayInputStream bin = new ByteArrayInputStream(stateData);
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, stateProxy.getContentType(),
                bin, stateData.length);
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String activityId = getFirstParamVal(session, "activityid");
        String agentJson = getFirstParamVal(session, "agent");
        String registration = getFirstParamVal(session, "registration");
        String stateId = getFirstParamVal(session, "stateid");

        if(activityId == null || agentJson == null || stateId == null) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
                    "Missing required parameters: sent " + urlParams.toString());
        }

        byte[] stateData = NanoLrsHttpd.getRequestContent(session);
        Object dbContext = uriResource.initParameter(0, Object.class);
        XapiStateEndpoint.createOrUpdateState(dbContext, "put", session.getHeaders().get("content-type"),
                activityId,agentJson, registration, stateId, stateData);

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT, "application/json",
                null);
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }
}
