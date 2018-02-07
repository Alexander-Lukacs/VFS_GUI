package client;

import builder.ModelBuilder;
import models.interfaces.User;

import javax.swing.text.html.HTML;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Mesut on 25.01.2018.
 */

public class RestClient {

    private WebTarget webTarget;


    public RestClient(String baseUrl) {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(baseUrl);
    }

    public void registerNewUser(User user){
        Response response = webTarget.path("/user/addNewUser").request().
                put(Entity.entity(user, MediaType.APPLICATION_JSON));

        System.out.println(response.readEntity(String.class));
    }

    public void loginUser (User user){
        Response response = webTarget.path("/user/auth/login").request().
                put(Entity.entity(user, MediaType.APPLICATION_JSON));

        System.out.println(response.readEntity(String.class));
    }

}
