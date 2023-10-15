import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "AddmovieServlet", urlPatterns = "/_dashboard/api/addmovie")
public class AddmovieServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starname = request.getParameter("starname");
        String genre = request.getParameter("genre");
        //System.out.println(star_name);

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();
        PrintWriter out = response.getWriter();

        if(title==null||title==""||year==null||year==""||director==null||director==""||
                starname==null||starname==""||genre==null||genre==""){
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "All Fields are REQUIRED");
            response.getWriter().write(responseJsonObject.toString());
            out.close();
            return;
        }

        if(!isValidYear(year)){
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Invalid Year Value");
            response.getWriter().write(responseJsonObject.toString());
            out.close();
            return;
        }



        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            // Execute a query to get the maximum star ID

            String query = "{CALL add_movie(?, ?, ?, ?, ?)}";

            CallableStatement statement = conn.prepareCall(query);

            statement.setString(1, title);
            statement.setInt(2, Integer.parseInt(year));
            statement.setString(3, director);
            statement.setString(4, starname);
            statement.setString(5, genre);
            System.out.println(statement);
            statement.execute();
            System.out.println("su");
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                String statusMessage = resultSet.getString(1);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", statusMessage);
            }
            response.getWriter().write(responseJsonObject.toString());
            // Close the connection
            conn.close();

            out.close();
            statement.close();
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
    }

    public static boolean isValidYear(String input) {
        return input.matches("\\d{4}");
    }

}
