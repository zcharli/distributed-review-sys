package filter;

import javax.servlet.*;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

/**
 * Created by czl on 04/10/16.
 */
@PreMatching
public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Auth filter hit");
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

    @Override
    public void destroy() {}
}

