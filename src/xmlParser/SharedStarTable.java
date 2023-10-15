package xmlParser;

import java.util.Hashtable;

public class SharedStarTable {
    private static Hashtable<String, String> SharedStarTable = new Hashtable<>();

    public static synchronized void put(String key, String value) {
        SharedStarTable.put(key, value);
    }

    public static synchronized String get(String key) {
        return SharedStarTable.get(key);
    }

}