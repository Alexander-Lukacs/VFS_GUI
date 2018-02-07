package builder;

import client.RestClient;
import javafx.fxml.FXML;

import java.awt.*;

/**
 * Created by Mesut on 07.02.2018.
 */

public class RestClientBuilder {
    public static RestClient buildRestClient( String ip, String port){
        return new RestClient("http://"+ip+":"+port+"/api");
    }
}
