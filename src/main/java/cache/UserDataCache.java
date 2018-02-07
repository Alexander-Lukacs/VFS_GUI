package cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mesut on 07.02.2018.
 */
public class UserDataCache implements Map{
    private static Map<String, String> userCache = new Map<String, String>() {
    };

    public static String getValue(String key){
        return userCache.get(key);
    }
    public static void put(String key, String value){
        userCache.put(key, value);
    }

    public static void clearCache(){
        userCache.clear();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object put(Object key, Object value) {
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set keySet() {
        return null;
    }

    @Override
    public Collection values() {
        return null;
    }

    @Override
    public Set<Entry> entrySet() {
        return null;
    }
}
