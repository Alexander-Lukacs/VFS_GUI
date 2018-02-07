package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.interfaces.User;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static client.constants.HttpStatusCodes.GC_HTTP_OK;

/**
 * Created by Mesut on 25.01.2018.
 */

public class RestClient {

    private WebTarget webTarget;

    public RestClient(String baseUrl) {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(baseUrl);
    }

    public HttpMessage registerNewUser(User user) throws IOException{
        Response response = webTarget.path("/user/addNewUser").request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));


        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = response.readEntity(String.class);

        HttpMessage obj = mapper.readValue(jsonInString, HttpMessage.class);
        obj.setHttpStatus(response.getStatus());
        return obj;
    }

    public void loginUser (User user) {
        ObjectMapper lob_mapper = new ObjectMapper();
        String lva_jsonInString;
        HttpMessage lob_httpMessage;
        User lob_user;

        Response response = webTarget.path("/user/login").request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        lva_jsonInString = response.readEntity(String.class);

        // TODO return value

        try {
            if (response.getStatus() == GC_HTTP_OK) {
                lob_user = lob_mapper.readValue(lva_jsonInString, User.class);
            } else {
                lob_httpMessage = lob_mapper.readValue(lva_jsonInString, HttpMessage.class);
                lob_httpMessage.setHttpStatus(response.getStatus());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }



        System.out.println(response.readEntity(String.class));
    }

}
