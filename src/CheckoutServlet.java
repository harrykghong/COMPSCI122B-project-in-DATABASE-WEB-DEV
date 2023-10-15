import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
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
        String card_number = request.getParameter("card_number");
        String first_name = request.getParameter("first_name");
        String last_name = request.getParameter("last_name");
        String date = request.getParameter("date-picker");
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "select * from creditcards where id = '" + card_number + "' and firstName = '" + first_name + "' and lastName = '" + last_name + "' and expiration = '" + date + "';\n";
            System.out.println(query);
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            JsonObject responseJsonObject = new JsonObject();
            if(rs.next()){
                HttpSession session = request.getSession();
                Map<String, ConnectionUrlParser.Pair<Integer,String>> previousItems= (Hashtable<String, ConnectionUrlParser.Pair<Integer,String>>) session.getAttribute("previousItems");
                if(previousItems == null || previousItems.toString().equals("{}")){
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Your Cart is empty");
                }
                else{
                    JsonArray auto_incremented_id = new JsonArray();
                    String id = ((User)session.getAttribute("user")).getId();
                    //get movie name from session previousItem
                    for (Map.Entry<String, ConnectionUrlParser.Pair<Integer,String>> entry : previousItems.entrySet()) {
                        // value here is a pair that the first element is quantity and second value is its movie id
                        ConnectionUrlParser.Pair<Integer, String> value = entry.getValue();
                        String movieId = value.getSecond();
                        String d = java.time.LocalDate.now().toString();
                        String insertQuery = "INSERT INTO sales(customerId,movieId,saleDate) values (?,?,'" + d + "');";
                        // Prepare the statement
                        PreparedStatement preparedStatement = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                        // Set the values for the placeholders
                        preparedStatement.setString(1, id);
                        preparedStatement.setString(2, movieId);
                        // Execute the prepared statement
                        int rowsAffected = preparedStatement.executeUpdate();
                        System.out.println("Inserted " + rowsAffected + " rows.");
                        ResultSet resultSet = preparedStatement.getGeneratedKeys();

                        if (resultSet.next()) {
                            int autoIncrementedId = resultSet.getInt(1);
                            auto_incremented_id.add(autoIncrementedId);
                            System.out.println("The auto-incremented ID is: " + autoIncrementedId);
                        }
                        preparedStatement.close();
                    }
                    session.setAttribute("auto_incremented", auto_incremented_id);
                    responseJsonObject.add("auto_incremented",auto_incremented_id);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
            }else{
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Credit card information not found");
            }
            response.getWriter().write(responseJsonObject.toString());
            conn.close();
            rs.close();
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
}
