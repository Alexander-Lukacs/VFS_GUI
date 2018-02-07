package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.interfaces.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by Mesut on 25.01.2018.
 */

public class RestClient {

    private WebTarget webTarget;

    public RestClient(String baseUrl) {
       new RestClient(baseUrl,"","");
    }

    public RestClient(String baseUrl, String email, String password) {
        ClientConfig clientConfig = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
        .nonPreemptive().credentials(email, password).build();
        clientConfig.register(feature);

        Client client = ClientBuilder.newClient(clientConfig);
        webTarget = client.target(baseUrl);
    }

    public HttpMessage registerNewUser(User user) throws IOException{
        Response response = webTarget.path("/user/addNewUser").request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));


        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = response.readEntity(String.class);

//JSON from String to Object
        HttpMessage obj = mapper.readValue(jsonInString, HttpMessage.class);
        obj.setHttpStatus(response.getStatus());
        return obj;
    }

    public void loginUser (User user){
        Response response = webTarget.path("/user/login").request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        System.out.println(response.readEntity(String.class));
    }

}
