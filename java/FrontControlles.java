package classes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class FrontControlles extends HttpServlet {

    private String controllerPackage;
    private ScannerController scanner = new ScannerController();
    private Map<String, Mapping> urlMappings;

    public void init(ServletConfig config) throws ServletException {
        try {
            super.init(config);
            ServletContext context = config.getServletContext();
            this.controllerPackage = context.getInitParameter("base_package");
            this.urlMappings = scanner.scanPackage(controllerPackage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String requestUrl = request.getRequestURI();
        Mapping mapping = urlMappings.get(requestUrl);

        PrintWriter out = response.getWriter();

        out.println("<h1>Hello, World!</h1>");
        out.println("<h1>LINK : " + request.getRequestURL() + "</h1>");

        if (mapping != null) {
            out.println("<h2>Mapping Found:</h2>");
            out.println("<ul>");
            out.println("<li>Controller: " + mapping.getController() + "</li>");
            out.println("<li>Method: " + mapping.getMethod() + "</li>");
            out.println("</ul>");
        } else {
            out.println("<h2>No Mapping Found for URL: " + requestUrl + "</h2>");
        }

        out.println("<h2>List of Controllers and Methods:</h2>");
        out.println("<ul>");
        for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
            out.println("<li>URL: " + entry.getKey() + ", Controller: " + entry.getValue().getController() + ", Method: " + entry.getValue().getMethod() + "</li>");
        }
        out.println("</ul>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
