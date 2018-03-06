package builder;

import cache.DataCache;
import rest.clients.FileRestClient;
import rest.clients.RestClient;
import rest.clients.SharedDirectoryClient;
import rest.clients.UserClient;

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

    public static SharedDirectoryClient buildSharedDirectoryClientWithAuth() {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_ip = lob_dataCache.get(GC_IP_KEY);
        String lva_port = lob_dataCache.get(GC_PORT_KEY);
        String lva_email = lob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = lob_dataCache.get(GC_PASSWORD_KEY);

        return new SharedDirectoryClient("http://"+lva_ip+":"+lva_port+"/api", lva_email, lva_password);
    }

    public static UserClient buildUserClient( String iva_ip, String iva_port) {
        return new UserClient("http://"+iva_ip+":"+iva_port+"/api");
    }

    public static UserClient buildUserClientWithAuth(String iva_ip, String iva_port, String iva_email, String iva_password) {
        return new UserClient("http://"+iva_ip+":"+iva_port+"/api", iva_email, iva_password);
    }

    public static UserClient buildUserClientWithAuth() {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_ip = lob_dataCache.get(GC_IP_KEY);
        String lva_port = lob_dataCache.get(GC_PORT_KEY);
        String lva_email = lob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = lob_dataCache.get(GC_PASSWORD_KEY);

        return new UserClient("http://"+lva_ip+":"+lva_port+"/api", lva_email, lva_password);
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
