package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.ustadmobile.nanolrs.util.ServletUtil.*;

/**
 * Created by Varuna on 4/6/2017.
 */
public class StatementsServlet extends HttpServlet {

    public StatementsServlet() {
        super();
        System.out.println("In StatementsServlet()..");
    }

    public static final String XAPI_HEADER_AUTH="Authorization";
    public static final String XAPI_HEADER_VER="X-Experience-API-Version";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        System.out.println("Statements doGet()..");
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);

        //So, we authenticate..
        System.out.println("StatementsServlet: Authentication started..");
        String authString = getHeaderVal(request, XAPI_HEADER_AUTH);
        String xapiVersionString = getHeaderVal(request, XAPI_HEADER_VER);
        boolean badRequest = false;
        boolean forbiddenRequest = false;
        if(authString == null || authString.isEmpty()){
            System.out.println("StatementsServlet: Null Auth String..");
            badRequest = true;
        }
        if(xapiVersionString == null || xapiVersionString.isEmpty()){
            System.out.println("StatementsServlet: Null Headers..");
            badRequest = true;
        }
        if(getCredentialStringFromBasicAuth(request) == null){
            System.out.println("StatementsServlet: Null Credential String..");
            badRequest = true;
        }
        if(badRequest){
            //SEND BAD REQUEST
            System.out.println("StatementServlet: BAD request");
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else {
            String username = getUsernameFromBasicAuth(request);
            String password = getPasswordFromBasicAuth(request);

            boolean correctLogin = userManager.authenticate(dbContext, username, password);

            if (correctLogin) {
                //Authorized OK
                System.out.println("StatementsServlet: Login OK");
                response.setContentType("text/plain");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                //Unauthorized
                System.out.println("StatementsServlet: Login FAIL");
                response.setContentType("text/plain");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doTrace(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        super.service(req, res);
    }

    /* Do this */
    static String extractPostRequestBody(HttpServletRequest request) throws IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        if ("PUT".equalsIgnoreCase(request.getMethod())) {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        if ("POST".equalsIgnoreCase(request.getMethod()))
        {
            //return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return "";
    }

}
