package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

/**
 * Created by czl on 04/10/16.
 */
@PreMatching
@WebFilter
public class ContextInjectionFilter implements Filter {
    private ServletContext m_context;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Context inj hit");

//        response.setBufferSize();
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        m_context = filterConfig.getServletContext();
    }

    @Override
    public void destroy() {}
}