package error;

import config.APIConfig;
import core.APIServer;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by cli on 10/18/2016.
 */
public class SPAErrorRedirectModule extends org.eclipse.jetty.server.handler.ErrorHandler
{

    private static Logger logger = LoggerFactory.getLogger(SPAErrorRedirectModule.class);
    private boolean _fistRequest = true;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("Handling request redirect for SPA application: " + target);
        // On 404 page we need to show index.html and let JS router do the work, otherwise show error page
        String redirectRoute = "/";
        if ((response.getStatus() == HttpServletResponse.SC_NOT_FOUND)) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(redirectRoute);
            if (dispatcher != null) {
                try {
                    // reset response
                    response.reset();
                    // On second 404 request we need to show original 404 page, otherwise will be redirect loop
                    _fistRequest = false;
                    dispatcher.forward(request, response);
                } catch (ServletException e) {
                    super.handle(target, baseRequest, request, response);
                }
            }
        } else if ((response.getStatus() == HttpServletResponse.SC_NOT_FOUND) && ! _fistRequest) {
            logger.error("Can not find internal redirect route " + redirectRoute + " on 404 error. Will show system 404 page");
        } else {
            super.handle(target, baseRequest, request, response);
        }
    }

    @Override
    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException
    {
        writeErrorPageMessage(request, writer, code, message, request.getRequestURI());
    }

    @Override
    protected void writeErrorPageMessage(HttpServletRequest request, Writer writer, int code, String message, String uri)
            throws IOException
    {
        String statusMessage = Integer.toString(code) + " " + message;
        logger.error("Problem accessing " + uri + ". " + statusMessage);
        writer.write(statusMessage);
    }
}