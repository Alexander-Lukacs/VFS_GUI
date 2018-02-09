package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.classes.UserImpl;
import models.interfaces.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import tools.AlertWindows;

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

    public RestClient(String baseUrl, String iva_email, String iva_password) {
        HttpAuthenticationFeature authDetails = HttpAuthenticationFeature.basic(iva_email, iva_password);
        ClientConfig config = new ClientConfig(authDetails);
        Client client = ClientBuilder.newClient(config);
        client.register(authDetails);
        webTarget = client.target(baseUrl);
    }

    public HttpMessage registerNewUser(User user) throws IOException {
        Response response = webTarget.path("/user/addNewUser").request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));


        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = response.readEntity(String.class);

        HttpMessage obj = mapper.readValue(jsonInString, HttpMessage.class);
        obj.setHttpStatus(response.getStatus());
        return obj;
    }

    public User loginUser(User user) {
        ObjectMapper lob_mapper = new ObjectMapper();
        String lva_jsonInString;
        HttpMessage lob_httpMessage;
        UserImpl lob_user = new UserImpl();
        Response response = null;

        try {
            response = webTarget.path("/user/auth/login").request()
                    .put(Entity.entity(user, MediaType.APPLICATION_JSON));
        } catch (Exception ex ) {
            System.out.println(ex.getMessage());
        }

        lva_jsonInString = response.readEntity(String.class);

        // TODO return value

        try {
            if (response.getStatus() == GC_HTTP_OK) {
                lob_user = lob_mapper.readValue(lva_jsonInString, UserImpl.class);
            } else if (response.getStatus() == 401) {
                throw new IllegalArgumentException("Unauthorized");
            } else {
                lob_httpMessage = lob_mapper.readValue(lva_jsonInString, HttpMessage.class);
                lob_httpMessage.setHttpStatus(response.getStatus());
                throw new IllegalArgumentException(lob_httpMessage.getUserLoginStatus());
            }
        } catch (IOException ex) {
            //TODO
            AlertWindows.ExceptionAlert(ex.getMessage(), ex);
            ex.printStackTrace();
        }

        return lob_user;
    }

    public void changePassword(User iob_user) {

        String lva_jsonInString;

        Response response = webTarget.path("user/auth/changePassword").request()
                .put(Entity.entity(iob_user, MediaType.APPLICATION_JSON));

        lva_jsonInString = response.readEntity(String.class);

        // TODO Löschen
        System.out.println(response.readEntity(String.class));
    }

}
