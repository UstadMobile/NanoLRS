package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.util.ServletUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by varuna on 9/7/2017.
 */

public class ExportReportServlet extends HttpServlet {

    public ExportReportServlet() {
        super();
        System.out.println("In ExportReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In ExportReportServlet.doGet()..");
        HttpSession session = request.getSession();
        super.doGet(request, response);
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In ExportReportServlet.doPOST()..");

        JSONArray dataJ = new JSONArray(request.getParameter("return_json"));
        String table_headers_html = request.getParameter("table_headers_html");
        LinkedHashMap<String, String> table_headers_html_map = ServletUtil.getLinkedMapFromString(table_headers_html);
        Iterator<Map.Entry<String, String>> t = table_headers_html_map.entrySet().iterator();
        while(t.hasNext()){
            Map.Entry<String, String> e = t.next();
            System.out.println("Map entry: " + e.getKey() + "/" + e.getValue());
        }


        File reportFile = ServletUtil.jsonToReportFile(dataJ, table_headers_html_map);

        response.setContentType("application/octet-stream");
        response.setContentLength((int) reportFile.length());
        response.setHeader( "Content-Disposition",
                String.format("attachment; filename=\"%s\"", reportFile.getName()));

        //send file to response's output stream.
        OutputStream out = response.getOutputStream();
        try (FileInputStream in = new FileInputStream(reportFile)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doTrace(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
        super.service(req, res);
    }
}
