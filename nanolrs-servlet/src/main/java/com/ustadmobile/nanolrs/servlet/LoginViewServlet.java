package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.util.ServletUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by varuna on 7/25/2017.
 */

public class LoginViewServlet extends HttpServlet {

    public LoginViewServlet() {
        super();
        System.out.println("In HomeViewServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In HomeViewServlet.doGet()..");
        HttpSession session = request.getSession();
        Object loggedInUsernameObj = session.getAttribute("username");
        String loggedInUsername="";
        if(loggedInUsernameObj != null){
            loggedInUsername = loggedInUsernameObj.toString();
        }
        if (loggedInUsername.equals("admin")) {
            response.sendRedirect("../reports/ReportsView.jsp");
        }
        response.sendRedirect("../Login.jsp");
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
        System.out.println("In HomeViewServlet.doPOST()..");
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String username = req.getParameter("username");
        String password = req.getParameter("password");


        if(userManager.authenticate(dbContext, username, password)){
            if(!username.equals("admin")){

                //RequestDispatcher rs = req.getRequestDispatcher("/login/");
                //rs.include(req, resp);
                out.println("Sorry, only admins can use this portal.");
            }else {
                //List<User> users = userManager.findByUsername(dbContext, username);
                //User user = users.get(0);
                User user = userManager.findByUsername(dbContext, username);
                HttpSession session = req.getSession();
                session.setAttribute("admin", username);
                //RequestDispatcher rs = getServletContext().getRequestDispatcher("/home/");
                //rs.forward(req, resp);
                //resp.sendRedirect("home/");
                resp.sendRedirect("../reports/ReportsView.jsp");
            }
        }
        else
        {
            out.println("Username or Password incorrect");
            //RequestDispatcher rs = getServletContext().getRequestDispatcher("/login/");
            //rs.include(req, resp);
        }

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
}
