package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

/**
 * Created by czl on 04/10/16.
 */
@PreMatching
@WebFilter
public class AuthorizationFilter implements Filter {
    private ServletContext m_context;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Auth filter hit");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

//        String uri = req.getRequestURI();
//        this.context.log("Requested Resource::"+uri);
//
//        HttpSession session = req.getSession(false);
//
//        if(session == null && !(uri.endsWith("html") || uri.endsWith("LoginServlet"))){
//            this.context.log("Unauthorized access request");
//            res.sendRedirect("login.html");
//        }else{
            // pass the request along the filter chain
        chain.doFilter(request, response);
//        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        m_context = filterConfig.getServletContext();
    }

    @Override
    public void destroy() {}
}

