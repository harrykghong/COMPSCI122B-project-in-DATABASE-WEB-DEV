package xmlParser;

import java.util.Hashtable;

public class SharedMovieTable {
    private static Hashtable<String, String> SharedMovieTable = new Hashtable<>();

    public static synchronized void put(String key, String value) {
        SharedMovieTable.put(key, value);
    }

    public static synchronized String get(String key) {
        return SharedMovieTable.get(key);
    }

    public static synchronized void remove(String key) {
        SharedMovieTable.remove(key);
    }
}