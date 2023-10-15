package xmlParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

public class MovieParser {

    Set<String> genre_set = new HashSet<String>();
    HashMap<String, Integer> genre_dict = new HashMap<>();
    List<String> inconsistent_movie = new ArrayList<>();

    Document dom;
    int film_num;

    int duplicate_movie = 0;
    int genre_in_movie_num = 0;
    PreparedStatement insertgm;
    PreparedStatement insertrating;
    PreparedStatement insertmovie;
    private Connection conn;
    private File currentDirFile;
    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each employee element and create a Employee object
        MovieParser ();

        // iterate through the list and print the data
        print_inconsistent_Data();

        LoadData();
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            //dom = documentBuilder.parse("C:/Users/kg/Desktop/CS122b/stanford-movies/mains243.xml");
            dom = documentBuilder.parse("../stanford-movies/mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void MovieParser() {
        String currentDir = System.getProperty("user.dir");
        currentDirFile = new File(currentDir);
        File outputFile = new File(currentDirFile, "/movieinfo.csv");  // create a new file in the parent directory
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter(outputFile, false))){
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
            Statement statement = conn.createStatement();
            String query = "SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) AS maxId  FROM movies";
            ResultSet rs = statement.executeQuery(query);
            int maxMovieId = 0;
            if (rs.next()) {
                maxMovieId = rs.getInt("maxId");
            }
            int count = 0;
            dom.getDocumentElement().normalize();
            NodeList nList = dom.getElementsByTagName("film");

            //batch insert for genre_movie
            insertgm = conn.prepareStatement("INSERT INTO genres_in_movies VALUES (?, ?)");
            insertrating = conn.prepareStatement("INSERT INTO ratings VALUES (?, 0, 0)");
            insertmovie = conn.prepareStatement("INSERT INTO movies VALUES (?,?, ?, ?)");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    String filmName = eElement.getElementsByTagName("t").item(0).getTextContent();
                    if(SharedMovieTable.get(filmName) != null){
                        duplicate_movie += 1;
                        continue;
                    }
                    film_num += 1;

                    String filmYear = eElement.getElementsByTagName("year").item(0).getTextContent();
                    // situation that there are two or more year for a movie
                    if(filmYear.length() > 4){
                        filmYear = filmYear.substring(0,4);
                    }
                    if (!filmYear.matches("\\d{4}")) {
                        inconsistent_movie.add(filmName);
                        film_num += 1;
                        continue;
                    }

                    // Get the dirname from the grandparent node
                    Element grandParent = (Element) eElement.getParentNode().getParentNode();
                    Node director = grandParent.getElementsByTagName("dirname").item(0);
                    if(director == null){
                        director = grandParent.getElementsByTagName("dirn").item(0);
                    }
                    String directorName = director.getTextContent();

                    // Get the film categories from the grandparent node
                    Node catNodess = eElement.getElementsByTagName("cats").item(0);
                    // situation that there are no categories for this movie
                    if(catNodess == null || catNodess.getChildNodes().getLength() == 0) {
                        inconsistent_movie.add(filmName);
                        film_num += 1;
                        continue;
                    }

                    count += 1;
                    String filmId = String.format("tt%07d", maxMovieId + count);
                    SharedMovieTable.put(filmName, filmId);

                    NodeList catNodes = catNodess.getChildNodes();
                    for (int i = 0; i < catNodes.getLength(); i++) {
                        Node catNode = catNodes.item(i);
                        if (catNode != null && catNode.getNodeType() == Node.ELEMENT_NODE) {
                            //add to genre dictionary
                            String cat = catNode.getTextContent();
                            if(cat != null && !cat.isEmpty() && !genre_dict.containsKey(cat)){
                                String genre_query = "INSERT INTO genres (name) VALUES (?)";
                                PreparedStatement pstmt = conn.prepareStatement(genre_query);
                                pstmt.setString(1, cat);
                                pstmt.executeUpdate();
                                int generatedId = 300;
                                ResultSet resultSet = statement.executeQuery("SELECT LAST_INSERT_ID()");
                                if (resultSet.next()) {
                                    generatedId = resultSet.getInt(1);
                                }
                                    genre_dict.put(cat, generatedId);
                            }
                            if(!cat.equals("")){
                                insertgm.setInt(1, (genre_dict.get(cat)).intValue());
                                insertgm.setString(2, filmId);
                                insertgm.addBatch();
                                genre_in_movie_num += 1;

                                insertrating.setString(1,filmId);
                                insertrating.addBatch();
                            }
                        }
                    }
                    insertmovie.setString(1, filmId);
                    insertmovie.setString(2, filmName);
                    insertmovie.setString(3, filmYear);
                    insertmovie.setString(4, directorName);
                    insertmovie.addBatch();
//                    csvWriter.println(filmId + "|" + filmName + "|" + filmYear + "|" + directorName);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void print_inconsistent_Data() {
        System.out.println("Inserted " + (film_num-inconsistent_movie.size()) + " movies.");
        System.out.println("Inserted " + (genre_dict.size()) + " genres.");
        System.out.println("Inserted " + genre_in_movie_num + " genre_in_movies");
        // write to read me file
        try {
            FileWriter fileWriter = new FileWriter("README.md", true); // Second argument is true for append mode
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Inserted " + (film_num-inconsistent_movie.size()) + " movies.\n");
            bufferedWriter.write(duplicate_movie + " movies duplicate.\n");
            bufferedWriter.write((inconsistent_movie.size()) + " movies has wrong format.(no genre or wrong year)\n");
            bufferedWriter.write("Inserted " + (genre_dict.size()) + " genres.\n");
            bufferedWriter.write("Inserted " + genre_in_movie_num + " genre_in_movies\n");
            bufferedWriter.close();
        } catch(IOException e) {
            System.out.println("An error occurred while writing to the README file.");
            e.printStackTrace();
        }
    }
    private void LoadData(){
        try{
//            String query = "LOAD DATA INFILE ? \n" +
//                    "INTO TABLE movies \n" +
//                    "FIELDS TERMINATED BY '|' \n" +
//                    "ENCLOSED BY '\"' \n" +
//                    "LINES TERMINATED BY '\\r\\n'\n" +
//                    "(id,title, year, director,@dummy)";
//            PreparedStatement statement = conn.prepareStatement(query);
//            statement.setString(1, currentDirFile + "/movieinfo.csv");
//            statement.execute();
            insertmovie.executeBatch();
            insertgm.executeBatch();
            insertrating.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
