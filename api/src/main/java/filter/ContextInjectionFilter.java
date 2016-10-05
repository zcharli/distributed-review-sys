package filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by czl on 04/10/16.
 */
public class ContextInjectionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Context inj hit");
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

    @Override
    public void destroy() {}
}