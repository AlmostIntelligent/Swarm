

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PerformanceTest extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
    	PrintWriter out = response.getWriter();
    	String r = request.getParameter("repeat");
    	int repeats = 1;
    	if (r != null) {
    		try {
    			repeats = Integer.parseInt(r);
    		} catch (Exception e) {
    			
    		}
    	}
    	out.println("<html>");
        out.println("<head>");
        out.println("<title>Perf Test</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
    	for (int i = 0; i < repeats; i++) {
    		out.println("Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  " +
    				"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna " +
    				"aliquyam erat, sed diam voluptua. At vero eos et accusam et justo " +
    				"duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
    				"sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, " +
    				"consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt" +
    				" ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero" +
    				" eos et accusam et justo duo dolores et ea rebum. Stet clita kasd" +
    				" gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. " +
    				"Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam " +
    				"nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam " +
    				"erat, sed diam voluptua. At vero eos et accusam et justo duo " +
    				"dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
    				"sanctus est Lorem ipsum dolor sit amet.");
    		
    	}
    	out.println("</body>");
        out.println("</html>");
    	
    }
}
