import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        Gson gson = new Gson();
        long lastAccessTime = session.getLastAccessedTime();
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());
        Object auto_incremented = session.getAttribute("auto_incremented");
        if (auto_incremented != null){
            responseJsonObject.add("auto_incremented",gson.toJsonTree(auto_incremented));
        }
        Map<String, ConnectionUrlParser.Pair<Integer,String>> previousItems= (Hashtable<String, ConnectionUrlParser.Pair<Integer,String>>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new Hashtable<>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        responseJsonObject.add("previousItems", gson.toJsonTree(previousItems));
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_title = request.getParameter("mtitle");
        System.out.println(movie_title);
        String[] movie_title_id = movie_title.split("\\|");

        HttpSession session = request.getSession();
        // get the previous items in a ArrayList
        Map<String, ConnectionUrlParser.Pair<Integer,String>> previousItems= (Hashtable<String, ConnectionUrlParser.Pair<Integer,String>>) session.getAttribute("previousItems");
        String action = request.getParameter("action");
        if(action != null){
            synchronized (previousItems) {
                if ("add".equals(action)) {
                    // Add the quantity to the session (customize this code to match your session data structure)
                    previousItems.put(movie_title_id[0], new ConnectionUrlParser.Pair<>(previousItems.get(movie_title_id[0]).getFirst()+1,previousItems.get(movie_title_id[0]).getSecond()));
                } else if ("decrease".equals(action)) {
                    // Remove the movie from the session (customize this code to match your session data structure)
                    previousItems.put(movie_title_id[0], new ConnectionUrlParser.Pair<>(previousItems.get(movie_title_id[0]).getFirst()-1,previousItems.get(movie_title_id[0]).getSecond()));
                }else{
                    // delete action\
                    previousItems.remove(movie_title_id[0]);
                }
            }
            Gson gson = new Gson();
            String jsonString = gson.toJson(previousItems);
            response.getWriter().write(jsonString);
        }else{
            if (previousItems == null) {
                previousItems = new Hashtable<>();
                previousItems.put(movie_title_id[0], new ConnectionUrlParser.Pair<>(1,movie_title_id[1]));
                session.setAttribute("previousItems", previousItems);
            } else {
                // prevent corrupted states through sharing under multi-threads
                // will only be executed by one thread at a time
                synchronized (previousItems) {
                    ConnectionUrlParser.Pair<Integer,String> count = previousItems.get(movie_title_id[0]);
                    if(count == null){
                        previousItems.put(movie_title_id[0], new ConnectionUrlParser.Pair<>(1,movie_title_id[1]));
                    }else{
                        previousItems.put(movie_title_id[0], new ConnectionUrlParser.Pair<>(previousItems.get(movie_title_id[0]).getFirst()+1,movie_title_id[1]));
                    }
                }
            }
            Gson gson = new Gson();
            String jsonString = gson.toJson(previousItems);
            response.getWriter().write(jsonString);
        }

        // set response content type
        response.setContentType("text/html");

        // New location to be redirected, it is an example
        String domain = request.getRequestURI().split("api")[0];
        String site = domain + new String("movies.html");

        // We have different response status types.
        // It is an optional also. Here it is a valid site
        // and hence it comes with response.SC_ACCEPTED
        response.setStatus(response.SC_ACCEPTED);
        response.setHeader("Location", site);
        response.sendRedirect(site);
    }
}
