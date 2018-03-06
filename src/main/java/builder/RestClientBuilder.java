package builder;

import cache.DataCache;
import restful.clients.FileRestClient;
import restful.clients.RestClient;
import restful.clients.SharedDirectoryRestClient;
import restful.clients.UserRestClient;

import static cache.DataCache.*;

/**
 * Created by Mesut on 07.02.2018.
 */

public abstract class RestClientBuilder {
    public static RestClient buildRestClient(String iva_ip, String iva_port) {
        return new RestClient("http://"+iva_ip+":"+iva_port+"/api");
    }

    public static RestClient buildRestClientWithAuth(String iva_ip, String iva_port, String iva_email, String iva_password) {
        return new RestClient("http://"+iva_ip+":"+iva_port+"/api", iva_email, iva_password);
    }

    public static RestClient buildRestClientWithAuth() {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_ip = lob_dataCache.get(GC_IP_KEY);
        String lva_port = lob_dataCache.get(GC_PORT_KEY);
        String lva_email = lob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = lob_dataCache.get(GC_PASSWORD_KEY);

        return new RestClient("http://"+lva_ip+":"+lva_port+"/api", lva_email, lva_password);
    }

    public static SharedDirectoryRestClient buildSharedDirectoryClientWithAuth() {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_ip = lob_dataCache.get(GC_IP_KEY);
        String lva_port = lob_dataCache.get(GC_PORT_KEY);
        String lva_email = lob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = lob_dataCache.get(GC_PASSWORD_KEY);

        return new SharedDirectoryRestClient("http://"+lva_ip+":"+lva_port+"/api", lva_email, lva_password);
    }

    public static UserRestClient buildUserClient(String iva_ip, String iva_port) {
        return new UserRestClient("http://"+iva_ip+":"+iva_port+"/api");
    }

    public static UserRestClient buildUserClientWithAuth(String iva_ip, String iva_port, String iva_email, String iva_password) {
        return new UserRestClient("http://"+iva_ip+":"+iva_port+"/api", iva_email, iva_password);
    }

    public static UserRestClient buildUserClientWithAuth() {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_ip = lob_dataCache.get(GC_IP_KEY);
        String lva_port = lob_dataCache.get(GC_PORT_KEY);
        String lva_email = lob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = lob_dataCache.get(GC_PASSWORD_KEY);

        return new UserRestClient("http://"+lva_ip+":"+lva_port+"/api", lva_email, lva_password);
    }

    public static FileRestClient buildFileRestClientWithAuth() {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_ip = lob_dataCache.get(GC_IP_KEY);
        String lva_port = lob_dataCache.get(GC_PORT_KEY);
        String lva_email = lob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = lob_dataCache.get(GC_PASSWORD_KEY);

        return new FileRestClient("http://"+lva_ip+":"+lva_port+"/api", lva_email, lva_password);
    }
}
