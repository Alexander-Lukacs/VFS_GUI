package client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by Mesut on 25.01.2018.
 */

public class RestClient {

    private WebTarget webTarget;


    public RestClient(String baseUrl) {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(baseUrl);
    }

}
