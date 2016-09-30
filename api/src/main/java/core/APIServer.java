package core;

import config.APIConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.DRSServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by cli on 9/27/2016.
 */
public class APIServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(APIServer.class);


    public final Server m_apiServer;
    private ResourceConfig m_resourceConfig;
    private ServletContainer m_servletContainer;
    private ServletHolder m_servletHolder;
    private ServletContextHandler m_servletContextHandler;

    public APIServer() {
        this(APIConfig.DEFAULT_HOST, APIConfig.API_PORT);
    }

    public APIServer(int port) {
        this(APIConfig.DEFAULT_HOST, port);
    }

    public APIServer(String host, int port) {
        APIConfig.API_PORT = port;
        InetSocketAddress currentAddress = null;
        try {
            currentAddress = new InetSocketAddress(InetAddress.getByName(host), APIConfig.API_PORT);
        } catch(UnknownHostException e) {
            LOGGER.error("Could not find host on boot: " + e.getMessage());
            System.exit(0);
        }
        m_apiServer = new Server(currentAddress);
    }

    /**
     * Default configuration
     */
    public void configure() {
        m_resourceConfig = new ResourceConfig();
        registerServlets();
        m_resourceConfig.register(JacksonFeature.class);
        m_servletContainer = new ServletContainer(m_resourceConfig);
        m_servletHolder = new ServletHolder(m_servletContainer);
        m_servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        m_servletContextHandler.setContextPath("/");
        m_servletContextHandler.addServlet(m_servletHolder, "/*");
    }

    private void registerServlets() {
        if (m_resourceConfig == null) {
            return;
        }
        m_resourceConfig.packages(DRSServlet.class.getPackage().getName());
    }

    public void start() throws Exception {
        if (m_resourceConfig == null) {
            configure();
        }
        m_apiServer.setHandler(m_servletContextHandler);


//        // The ServletHandler is a dead simple way to create a context handler
//        // that is backed by an instance of a Servlet.
//        // This handler then needs to be registered with the Server object.
//        ServletHandler handler = new ServletHandler();
//        m_apiServer.setHandler(handler);
//
//        // Passing in the class for the Servlet allows jetty to instantiate an
//        // instance of that Servlet and mount it on a given context path.
//
//        // IMPORTANT:
//        // This is a raw Servlet, not a Servlet that has been configured
//        // through a web.xml @WebServlet annotation, or anything similar.
//        handler.addServletWithMapping(HelloServlet.class, "/*");


        m_apiServer.start();
        m_apiServer.join();
    }
//
//    @SuppressWarnings("serial")
//    public static class HelloServlet extends HttpServlet
//    {
//        @Override
//        protected void doGet( HttpServletRequest request,
//                              HttpServletResponse response ) throws ServletException,
//                IOException
//        {
//            response.setContentType("text/html");
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().println("<h1>Hello from HelloServlet</h1>");
//        }
//    }
}
