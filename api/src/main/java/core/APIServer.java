package core;

import config.APIConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.jackson.JacksonFeature;
import servlet.DRSServlet;

/**
 * Created by cli on 9/27/2016.
 */
public class APIServer {

    public final Server m_apiServer;
    private ResourceConfig m_resourceConfig;
    private ServletContainer m_servletContainer;
    private ServletHolder m_servletHolder;
    private ServletContextHandler m_servletContextHandler;

    public APIServer() {
        this(APIConfig.API_PORT);
    }

    public APIServer(int port) {
        APIConfig.API_PORT = port;
        m_apiServer = new Server();
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
        m_apiServer.start();
        m_apiServer.join();
    }
}
