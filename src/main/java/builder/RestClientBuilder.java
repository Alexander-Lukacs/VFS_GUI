package builder;

import client.RestClient;

/**
 * Created by Mesut on 07.02.2018.
 */

public abstract class RestClientBuilder {
    public static RestClient buildRestClient( String iva_ip, String iva_port){
        return new RestClient("http://"+iva_ip+":"+iva_port+"/api");
    }

    public static RestClient buildRestClientWithAuth(String iva_ip, String iva_port, String iva_email, String iva_password) {
        return new RestClient("http://"+iva_ip+":"+iva_port+"/api", iva_email, iva_password);
    }
}
