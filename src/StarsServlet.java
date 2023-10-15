import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "StarsServlet", urlPatterns = "/api/movies")
public class StarsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
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
        long startTimeTS = System.nanoTime();

        response.setContentType("application/json"); // Response mime type

        String url_genre = request.getParameter("genre");
        String url_prefix = request.getParameter("prefix");
        String url_title = request.getParameter("title");
        String url_fTitle = request.getParameter("fullTitle");
        String url_year = request.getParameter("year");
        String url_dir = request.getParameter("dir");
        String url_star = request.getParameter("star");
        String url_pageNum = request.getParameter("pageNum");
        String url_sort = request.getParameter("sort");
        String url_changePage = request.getParameter("changePage");

        //print complete url
        String url = request.getQueryString();
//        request.getServletContext().log(url);
//        request.getServletContext().log("pagenum = "+ url_pageNum);

        HttpSession session = request.getSession();
        if(url_genre!=null||url_prefix!=null||url_title!=null||url_fTitle!=null||url_year!=null||url_dir!=null||url_star!=null){
            session.setAttribute("genre", null);
            session.setAttribute("prefix", null);
            session.setAttribute("title", null);
            session.setAttribute("fTitle", null);
            session.setAttribute("year", null);
            session.setAttribute("dir", null);
            session.setAttribute("star", null);
            session.setAttribute("pageNum", null);
            session.setAttribute("sort", null);
            session.setAttribute("page", 1);
        }
        if(url_sort!=null||url_pageNum!=null){
            session.setAttribute("page", 1);
        }

        if(url_genre!=null){
            session.setAttribute("genre", url_genre);
        }
        if(url_prefix!=null){
            session.setAttribute("prefix", url_prefix);
        }
        if(url_title!=null){
            session.setAttribute("title", url_title);
        }
        if(url_fTitle!=null){
            session.setAttribute("fTitle", url_fTitle);
        }
        if(url_year!=null){
            session.setAttribute("year", url_year);
        }
        if(url_dir!=null){
            session.setAttribute("dir", url_dir);
        }
        if(url_star!=null){
            session.setAttribute("star", url_star);
        }
        if(url_pageNum!=null){
            session.setAttribute("pageNum", url_pageNum);
        }
        if(url_sort!=null){
            session.setAttribute("sort", url_sort);
        }

        String pageNumS = (String) session.getAttribute("pageNum");
        if(pageNumS==null){
            session.setAttribute("pageNum", "25");
        }
        String sortS = (String) session.getAttribute("sort");
        if(sortS==null){
            session.setAttribute("sort", "tara");
        }

        if(url_changePage!=null){
            if(url_changePage.equals("previous")){
                Integer pageS = (Integer) session.getAttribute("page");
                session.setAttribute("page", pageS-1);
            } else if (url_changePage.equals("next")) {
                Integer pageS = (Integer) session.getAttribute("page");
                session.setAttribute("page", pageS+1);
            }
        }


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            //Statement statement = conn.createStatement();

            String query = "SELECT m.id, m.title, m.year, m.director, r.rating,\n" +
                    "       GROUP_CONCAT(DISTINCT s.name, '-', s.id) AS stars,\n" +
                    "       GROUP_CONCAT(DISTINCT g.name) AS genres\n" +
                    "FROM movies m, ratings r, stars_in_movies starM, stars s, genres_in_movies genreM, genres g\n" +
                    "WHERE m.id = r.movieId AND m.id = starM.movieId AND s.id = starM.starId AND m.id = genreM.movieId AND g.id = genreM.genreId";
            //request.getServletContext().log("getting " + url_year);


            String genreS = (String) session.getAttribute("genre");
            String prefixS = (String) session.getAttribute("prefix");
            String titleS = (String) session.getAttribute("title");
            String fTitleS = (String) session.getAttribute("fTitle");
            String yearS = (String) session.getAttribute("year");
            String dirS = (String) session.getAttribute("dir");
            String starS = (String) session.getAttribute("star");
            pageNumS = (String) session.getAttribute("pageNum");
            sortS = (String) session.getAttribute("sort");
            Integer pageS = (Integer) session.getAttribute("page");


            //Arraylist to keep everything needs to be added
            ArrayList<String> info = new ArrayList<String>();

            //PreparedStatement statement = conn.prepareStatement(query);
//            query += " AND g.name = ?;";
//            statement2.setString(1, genreS);

            if (genreS!= null){
                query += " AND g.name = ?";
                info.add(genreS);
            }
            if(prefixS != null){
                if(prefixS.equals("*")){
                    query += " AND title REGEXP ?";
                    info.add("^(\\.\\.\\.|[0-9])");
                }else {
                    query += " AND title LIKE ?";
                    info.add(prefixS+'%');
                }
            }

            if (titleS!= null && titleS!=""){
                query += " AND m.title LIKE ?";
                info.add('%'+titleS+'%');
            }

            if (fTitleS!= null && fTitleS!=""){
                query += " AND (MATCH (m.title) AGAINST (? IN BOOLEAN MODE) or edth(m.title, ? ,2))";
                String fullTextQuery = '+' + fTitleS.replace(" ", "* +") + '*';
                info.add(fullTextQuery);
                info.add(fTitleS);
//                request.getServletContext().log(query);
            }

            if (yearS!= null && yearS!=""){
                query += " AND m.year = ?";
                info.add(yearS);
            }
            if (dirS!= null && dirS!=""){
                query += " AND m.director LIKE ?";
                info.add('%'+dirS+'%');
            }
            if (starS!= null && starS!=""){
                query += " AND s.name LIKE ?";
                info.add('%'+starS+'%');
            }

            query += "\nGROUP BY m.id, m.title, m.year, m.director, r.rating";
            String total_query= query + ';';
            PreparedStatement statement2 = conn.prepareStatement(total_query);
            for(int i = 0; i<info.size(); i++){
                statement2.setString(i+1, info.get(i));
            }


            if(sortS.equals("tard")){
                query+="\nORDER BY title asc, rating desc";
            }else if(sortS.equals("tdra")){
                query+="\nORDER BY title desc, rating asc";
            }else if(sortS.equals("tdrd")){
                query+="\nORDER BY title desc, rating desc";
            }else if(sortS.equals("rata")){
                query+="\nORDER BY rating asc, title asc";
            }else if(sortS.equals("ratd")){
                query+="\nORDER BY rating asc, title desc";
            }else if(sortS.equals("rdta")){
                query+="\nORDER BY rating desc, title asc";
            }else if(sortS.equals("rdtd")){
                query+="\nORDER BY rating desc, title desc";
            }else{
                query+="\nORDER BY title asc, rating asc";
            }

            int n = Integer.parseInt(pageNumS);
            int offset = (pageS-1) * n;


            if(pageNumS.equals("10")){
                query+="\nLIMIT "+offset+", 10";
            }else if(pageNumS.equals("20")){
                query+="\nLIMIT "+offset+", 20";
            }else if(pageNumS.equals("50")){
                query+="\nLIMIT "+offset+", 50";
            }else if(pageNumS.equals("100")){
                query+="\nLIMIT "+offset+", 100";
            }else{
                query+="\nLIMIT "+offset+", 25";
            }

            query+=";";

//            request.getServletContext().log(statement2.toString());
            //find out how many pages there will be
            ResultSet total_rs = statement2.executeQuery();

            int ct = 0;
            while(total_rs.next()) {
                ct++;
            }
            int maxPage=ct/n;
            if(ct%n!=0){
                maxPage++;
            }

            PreparedStatement statement = conn.prepareStatement(query);
            for(int i = 0; i<info.size(); i++){
                statement.setString(i+1, info.get(i));
            }

//            request.getServletContext().log(statement.toString()); //Print entire query
            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            //info pass to js
            JsonObject jsonInfo = new JsonObject();
            jsonInfo.addProperty("pageNum",pageNumS);
            jsonInfo.addProperty("sort",sortS);
            jsonInfo.addProperty("page",pageS);
            jsonInfo.addProperty("maxPage",maxPage);
            jsonInfo.addProperty("count",ct);

            jsonArray.add(jsonInfo);

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
//            request.getServletContext().log("getting " + (jsonArray.size()-1) + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

            long endTimeTS = System.nanoTime();
            long totalExecutionTime = endTimeTS - startTimeTS;

            String path = request.getServletContext().getRealPath("/");
            String logFilePath = path + "loginfo";
            File myfile= new File(logFilePath);
            try {
                myfile.createNewFile();
                PrintWriter writer = new PrintWriter(new FileWriter(myfile, true));  // Append to existing file
                writer.println("TJ: " + totalExecutionTime);  // Write data to the file
                writer.close();  // Always close the writer when you're done!
            } catch (IOException e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());
                response.setStatus(500);
            }
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

