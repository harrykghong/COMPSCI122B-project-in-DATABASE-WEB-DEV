package xmlParser;

import jakarta.servlet.ServletConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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

public class StarParser {

    Set<String> h = new HashSet<String>();
    List<String> inconsistent_movie = new ArrayList<>();
    Document dom;
    private DataSource dataSource;
    int star_num = 0;
    int duplicate_star = 0;
    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each employee element and create a Employee object
        StarParser();

        print_inconsistent_Data();
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            //dom = documentBuilder.parse("C:/Users/kg/Desktop/CS122b/stanford-movies/actors63.xml");
            dom = documentBuilder.parse("../stanford-movies/actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void StarParser() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
            Statement statement = conn.createStatement();
            PreparedStatement starinsert = conn.prepareStatement("INSERT INTO stars VALUES (?, ?, ?)");
            String query = "SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED)) AS maxId FROM stars";
            ResultSet rs = statement.executeQuery(query);
            int maxStarId = 0;
            if (rs.next()) {
                maxStarId = rs.getInt("maxId");
            }
            int count = 0;
            dom.getDocumentElement().normalize();
            NodeList nList = dom.getElementsByTagName("actor");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    // get star Name
                    String starName = eElement.getElementsByTagName("stagename").item(0).getTextContent();
                    if(SharedStarTable.get(starName) != null){
                        duplicate_star += 1;
                        continue;
                    }
                    // get star dob
                    Node starDobb = eElement.getElementsByTagName("dob").item(0);
                    int starDob = 0;
                    if(starDobb != null){
                        String a = starDobb.getTextContent();
                        if (a.matches("\\d{4}")){
                            starDob = Integer.parseInt(a);
                        }
                    }

                    // get star id
                    count += 1;
                    String starId = String.format("nm%07d", maxStarId + count);
                    star_num += 1;
                    SharedStarTable.put(starName,starId);

                    starinsert.setString(1, starId);
                    starinsert.setString(2, starName);
                    starinsert.setInt(3, starDob);
                    starinsert.addBatch();

                }
            }
            starinsert.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void print_inconsistent_Data() {
        System.out.println("Total number of star: " + star_num);
        System.out.println(duplicate_star+ " duplicate stars");
        try {
            // write to read me file
            FileWriter fileWriter = new FileWriter("README.md", true); // Second argument is true for append mode
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Inserted " + star_num + " stars.\n");
            bufferedWriter.write(duplicate_star + " stars duplicate.\n");
            bufferedWriter.close();
        } catch(IOException e) {
            System.out.println("An error occurred while writing to the README file.");
            e.printStackTrace();
        }

    }
}
