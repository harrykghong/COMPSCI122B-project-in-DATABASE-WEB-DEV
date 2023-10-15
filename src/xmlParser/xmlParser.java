package xmlParser;

public class xmlParser {
    public static void main(String[] args){
        System.out.println("start movie parser");
        MovieParser movieParser = new MovieParser();
        // call run movie parser
        movieParser.run();

        System.out.println("start star parser");
        StarParser starParser = new StarParser();
        // call run star parser
        starParser.run();

        System.out.println("start cast parser");
        CastParser castParser = new CastParser();
        // call run cast parser
        castParser.run();
    }
}
