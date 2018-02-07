package builder;

import client.RestClient;
import javafx.fxml.FXML;

import java.awt.*;

/**
 * Created by Mesut on 07.02.2018.
 */

public class RestClientBuilder {

    public static RestClient buildRestClientWithAuth(String email, String password, String ip, String port){
        return new RestClient("http://"+ip+":"+port+"/api", email, password);
    }

    public static RestClient buildRestClient( String ip, String port){
        return new RestClient("http://"+ip+":"+port+"/api");
    }
}
