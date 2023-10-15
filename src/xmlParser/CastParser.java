package xmlParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CastParser {

    Document dom;
    private DataSource dataSource;
    int star_num;
    int scount = 0;
    int mcount = 0;
    int duplicate_star = 0;
    int star_in_movies_num = 0;
    private File currentDirFile;

    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each employee element and create a Employee object
        CastParser ();

        // iterate through the list and print the data
        print_inconsistent_Data();
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            //dom = documentBuilder.parse("C:/Users/kg/Desktop/CS122b/stanford-movies/casts124.xml");
            dom = documentBuilder.parse("../stanford-movies/casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void CastParser() {
        String currentDir = System.getProperty("user.dir");
        currentDirFile = new File(currentDir);
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
            dom.getDocumentElement().normalize();
            NodeList movieNodes = dom.getElementsByTagName("m");

            PreparedStatement additionalS = conn.prepareStatement("INSERT INTO stars VALUES (?, ?, null)");
            String insertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
            PreparedStatement insertStatement = conn.prepareStatement(insertQuery);

            //get max id from stars
            Statement statement = conn.createStatement();
            String query = "SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) AS maxId  FROM stars";
            ResultSet rs = statement.executeQuery(query);
            int maxStarId = 0;
            if (rs.next()) {
                maxStarId = rs.getInt("maxId");
            }
            for (int i = 0; i < movieNodes.getLength(); i++) {
                Node movieNode = movieNodes.item(i);
                if (movieNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element movieElement = (Element) movieNode;

                    // Extract the movie title and star name
                    String movieTitle = movieElement.getElementsByTagName("t").item(0).getTextContent();
                    String starName = movieElement.getElementsByTagName("a").item(0).getTextContent();

                    String movieId = SharedMovieTable.get(movieTitle);
                    String starId = SharedStarTable.get(starName);

                    if(movieId == null){
                        mcount += 1;
                        continue; //skip if is null
                    }
                    if(starId == null){ //if star not exist yet, add to stars
                        scount += 1;
                        starId = String.format("nm%07d", maxStarId + scount);
                        additionalS.setString(1, starId);
                        additionalS.setString(2, starName);
                        additionalS.addBatch();
                    }else{
                        duplicate_star += 1;
                    }

                    insertStatement.setString(1, starId);
                    insertStatement.setString(2, movieId);
                    insertStatement.addBatch();
                    star_in_movies_num += 1;
                }
            }
            //execute both batch insert
            additionalS.executeBatch();
            insertStatement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void print_inconsistent_Data() {
        System.out.println("Inserted " + star_in_movies_num + " stars_in_movies.");

        try {
            // write to read me file
            FileWriter fileWriter = new FileWriter("README.md", true); // Second argument is true for append mode
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Inserted " + star_in_movies_num + " stars_in_movies.\n");
            bufferedWriter.write(mcount + " movies not found.\n");
            bufferedWriter.write(scount + " stars not found.\n");
            bufferedWriter.write(duplicate_star + " stars duplicate.\n");
            bufferedWriter.close();
        } catch(IOException e) {
            System.out.println("An error occurred while writing to the README file.");
            e.printStackTrace();
        }
    }
}
