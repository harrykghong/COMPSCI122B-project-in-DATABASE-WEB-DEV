import com.google.gson.JsonObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.*;

@WebFilter(filterName = "ExecutionTimeFilter", urlPatterns = "/api/movies")
public class ExecutionTimeFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        long startTime = System.nanoTime();

        // pass the request along the filter chain
        chain.doFilter(request, response);

        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        String path = request.getServletContext().getRealPath("/");
        String logFilePath = path + "loginfo";

        File myfile= new File(logFilePath);
        try {
            myfile.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(myfile, true));  // Append to existing file
            writer.println("TS: " + executionTime);  // Write data to the file
            writer.close();  // Always close the writer when you're done!
        } catch (IOException e) {

        }
    }

    public void destroy() {
    }
}
