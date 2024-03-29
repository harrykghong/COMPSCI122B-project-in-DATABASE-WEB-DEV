import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "TopServlet", urlPatterns = "/api/top")
public class TopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT movies.id, movies.title, movies.year, movies.director, ratings.rating,\n" +
                    "GROUP_CONCAT(DISTINCT CONCAT(stars.name, '-', stars.id)) AS stars,\n" +
                    "GROUP_CONCAT(DISTINCT genres.name) AS genres\n" +
                    "FROM movies\n" +
                    "JOIN ratings ON movies.id = ratings.movieId\n" +
                    "JOIN stars_in_movies ON movies.id = stars_in_movies.movieId\n" +
                    "JOIN stars ON stars_in_movies.starId = stars.id\n" +
                    "JOIN genres_in_movies ON movies.id = genres_in_movies.movieId\n" +
                    "JOIN genres ON genres_in_movies.genreId = genres.id\n" +
                    "GROUP BY movies.id, movies.title, movies.year, movies.director, ratings.rating\n" +
                    "ORDER BY ratings.rating DESC\n" +
                    "LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieName = rs.getString("title");
                String movieId = rs.getString("id");
                String movieDir = rs.getString("director");
                String movieYear = rs.getString("year");
                String movieRate = rs.getString("rating");
                String movieGen = rs.getString("genres");
                String movieStar = rs.getString("stars");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieName", movieName);
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("movieDir", movieDir);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieRate", movieRate);
                jsonObject.addProperty("movieGen", movieGen);
                jsonObject.addProperty("movieStar", movieStar);


                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
