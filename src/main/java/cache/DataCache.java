package cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Florian on 07.02.2018.
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

    public static DataCache getDataCache() {
        if (gob_dataCache == null) {
            gob_dataCache = new DataCache();
        }

        return gob_dataCache;
    }

    private DataCache() {
        gob_dataCacheMap = new HashMap<>();
    }

    public void put(String iva_key, String iva_value) {
        gob_dataCacheMap.put(iva_key, iva_value);
    }

    public String get(String iva_key) {
        return gob_dataCacheMap.get(iva_key);
    }
}
