package cache;

import java.util.Map;

/**
 * Created by Mesut on 07.02.2018.
 */
public class UserDataCache  {
    private static Map<String, String> userCache; {
    }

    public static String getValue(String key){
        return userCache.get(key);
    }
    public static void put(String key, String value){
        userCache.put(key, value);
    }

    public static void clearCache(){
        userCache.clear();
    }
}
