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

@WebServlet(name = "AddstarServlet", urlPatterns = "/_dashboard/api/addstar")
public class AddstarServlet extends HttpServlet {
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
        String star_name = request.getParameter("star_name");
        String birth_year = request.getParameter("birth_year");
        System.out.println(star_name);

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();
        PrintWriter out = response.getWriter();

        if(star_name==null || star_name==""){
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Star Name cannot be empty");
            response.getWriter().write(responseJsonObject.toString());
            out.close();
            return;
        }



        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            // Execute a query to get the maximum star ID
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) AS maxId FROM stars");
            resultSet.next();
            int maxId = resultSet.getInt("maxId");

            // Generate a new star ID
            String newStarId = "nm" + String.format("%07d", maxId + 1);

            // Insert a new star
            String query;
            String additional_message;
            if(isValidYear(birth_year)){
                int by=Integer.parseInt(birth_year);
                query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setString(1, newStarId);
                preparedStatement.setString(2, star_name);
                preparedStatement.setInt(3, by);
                preparedStatement.executeUpdate();
                additional_message = ", born in " + birth_year;
            }else{
                query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, null)";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setString(1, newStarId);
                preparedStatement.setString(2, star_name);
                preparedStatement.executeUpdate();
                additional_message = ", birthYear set to null";
            }

            //System.out.println("New star added with ID: " + newStarId);
            //request.getSession().setAttribute("user", new User(username,userId));

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "Success! new star added with ID: " + newStarId + additional_message);
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
