package core;

import config.APIConfig;
import error.SPAErrorRedirectModule;
import exceptions.InitializationFailedException;
import filter.AuthorizationFilter;
import filter.ContextInjectionFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;

/**
 * Created by cli on 9/27/2016.
 */
public class APIServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    private final Server m_apiServer;
    private ResourceConfig m_resourceConfig;
    private ServletContainer m_apiServletContainer;
    private ServletHolder m_webServletHolder;
    private ServletHolder m_apiServletHolder;
    private ServletContextHandler m_servletContextHandler;

    public APIServer() throws InitializationFailedException {
        this(APIConfig.DEFAULT_HOST, APIConfig.API_PORT);
    }

    public APIServer(int port) throws InitializationFailedException {
        this(APIConfig.DEFAULT_HOST, port);
    }

    public APIServer(String host, int port) throws InitializationFailedException {
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
        configureApi();
        configureManagementWeb();
        configureContextHandler();
        m_apiServer.setHandler(m_servletContextHandler);
    }

    private void configureApi() {
        m_resourceConfig = new APIResourceConfig();
        m_apiServletContainer = new ServletContainer(m_resourceConfig);
        m_apiServletHolder = new ServletHolder("api", m_apiServletContainer);
    }

    private void configureManagementWeb() {
        m_webServletHolder = new ServletHolder("management", DefaultServlet.class);
        m_webServletHolder.setInitParameter("dirAllowed","true");
    }

    private void configureContextHandler() {
        if (m_apiServletHolder == null) {
            return;
        }
        // Set up the base path
        m_servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        m_servletContextHandler.setContextPath("/");
        m_servletContextHandler.setResourceBase(APIConfig.WEB_RESOURCE_PATH);
        m_servletContextHandler.setErrorHandler(new SPAErrorRedirectModule());
        m_servletContextHandler.addFilter(ContextInjectionFilter.class, "/api/review/*", EnumSet.of(DispatcherType.REQUEST));
        m_servletContextHandler.addFilter(AuthorizationFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
        m_servletContextHandler.addServlet(m_apiServletHolder, "/api/*");
        m_servletContextHandler.addServlet(m_webServletHolder, "/*");
    }


    public void start() throws Exception {
        if (m_resourceConfig == null) {
            configure();
        }
        m_apiServer.start();;
    }
}
