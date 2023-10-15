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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet{
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT \n" +
                    "    movies.id,\n" +
                    "    movies.title,\n" +
                    "    movies.director,\n" +
                    "    movies.year, \n" +
                    "    GROUP_CONCAT(DISTINCT genres.name) AS genres,\n" +
                    "    GROUP_CONCAT(DISTINCT CONCAT(stars.name, '-', stars.id)) AS stars,\n" +
                    "    ratings.rating\n" +
                    "FROM movies\n" +
                    "JOIN ratings ON movies.id = ratings.movieId\n" +
                    "JOIN stars_in_movies ON movies.id = stars_in_movies.movieId\n" +
                    "JOIN stars ON stars_in_movies.starId = stars.id\n" +
                    "JOIN genres_in_movies ON movies.id = genres_in_movies.movieId\n" +
                    "JOIN genres ON genres_in_movies.genreId = genres.id\n" +
                    "WHERE movies.id = ?\n" +
                    "GROUP BY movies.id, movies.title, movies.director, ratings.rating;\n";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            System.out.println(statement.toString());
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String starName = rs.getString("stars");
                String movieRate = rs.getString("rating");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieGen = rs.getString("genres");
                String movieDirector = rs.getString("director");
                String movieid = rs.getString("id");
                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGen);
                jsonObject.addProperty("movie_stars", starName);
                jsonObject.addProperty("movie_rating", movieRate);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movieId", movieid);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
