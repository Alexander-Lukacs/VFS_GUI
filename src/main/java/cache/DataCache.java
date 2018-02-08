package cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Florian on 07.02.2018.
 *
 * Data Cache to store user information
 */
public class DataCache {
    public static final String GC_IP_KEY = "IP";
    public static final String GC_PORT_KEY = "PORT";
    public static final String GC_EMAIL_KEY = "EMAIL";
    public static final String GC_PASSWORD_KEY = "PASSWORD";
    public static final String GC_NAME_KEY = "NAME";
    public static final String GC_ADMIN = "ADMIN";

    private static Map<String, String> gob_dataCacheMap;
    private static DataCache gob_dataCache;

    /**
     * Singleton instance of the data cache
     *
     * @return instance of the data cache
     */
    public static DataCache getDataCache() {
        if (gob_dataCache == null) {
            gob_dataCache = new DataCache();
        }

        return gob_dataCache;
    }

    private DataCache() {
        gob_dataCacheMap = new HashMap<>();
    }

    /**
     * Store a value in the cache
     *
     * @param iva_key key
     * @param iva_value value
     */
    public void put(String iva_key, String iva_value) {
        gob_dataCacheMap.put(iva_key, iva_value);
    }

    /**
     * Get a value from the data cache
     *
     * @param iva_key key
     * @return value
     */
    public String get(String iva_key) {
        return gob_dataCacheMap.get(iva_key);
    }

    /**
     * Remove all data from data cache
     */
    public void clearDataCache() {
        gob_dataCacheMap.clear();
    }

    /**
     * Replace a value
     *
     * @param iva_key key
     * @param iva_value new value
     */
    public void replaceData(String iva_key, String iva_value) {
        gob_dataCacheMap.remove(iva_key, iva_value);
    }
}
