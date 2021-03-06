package cache;

import models.classes.SharedDirectory;

import java.util.HashMap;
import java.util.Map;

public class SharedDirectoryCache {
    private static Map<Integer, SharedDirectory> gob_dataCacheMap;
    private static SharedDirectoryCache gob_dataCache;

    private SharedDirectoryCache() {
        gob_dataCacheMap = new HashMap<>();
    }

    public static SharedDirectoryCache getInstance() {
        if (gob_dataCache == null) {
            gob_dataCache = new SharedDirectoryCache();
        }

        return gob_dataCache;
    }

    /**
     * Store a value in the cache
     *
     * @param iva_key   key
     * @param iva_value value
     */
    public void put(Integer iva_key, SharedDirectory iva_value) {
        gob_dataCacheMap.put(iva_key, iva_value);
    }

    /**
     * Get a value from the data cache
     *
     * @param iva_key key
     * @return value
     */
    public SharedDirectory get(Integer iva_key) {
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
     * @param iva_key   key
     * @param iva_value new value
     */
    public void replaceData(Integer iva_key, SharedDirectory iva_value) {
        gob_dataCacheMap.replace(iva_key, iva_value);
    }

    public void removeData(Integer iva_key) {
        gob_dataCacheMap.remove(iva_key);
    }

    @Override
    public String toString() {
        StringBuilder lob_stringBuilder = new StringBuilder();

        for (Map.Entry<Integer, SharedDirectory> lob_entry : gob_dataCacheMap.entrySet()) {
            lob_stringBuilder
                    .append("Key: ")
                    .append(lob_entry.getKey())
                    .append(" Value: ")
                    .append(lob_entry.getValue())
                    .append("\n");
        }

        return lob_stringBuilder.toString();
    }
}
