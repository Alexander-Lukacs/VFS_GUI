package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.classes.UserImpl;
import models.interfaces.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import tools.AlertWindows;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static client.constants.HttpStatusCodes.GC_HTTP_OK;
import static client.constants.RestResourcesPaths.*;

/**
 * Created by Mesut on 25.01.2018.
 */

public class RestClient {
    private WebTarget gob_webTarget;

    public RestClient(String baseUrl) {
        Client lob_client = ClientBuilder.newClient();
        gob_webTarget = lob_client.target(baseUrl);
    }

    public RestClient(String baseUrl, String iva_email, String iva_password) {
        HttpAuthenticationFeature lob_authDetails = HttpAuthenticationFeature.basic(iva_email, iva_password);

        ClientConfig lob_config = new ClientConfig(lob_authDetails);

        Client lob_client = ClientBuilder.newClient(lob_config);
        lob_client.register(lob_authDetails);

        gob_webTarget = lob_client.target(baseUrl);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Register new user
// ---------------------------------------------------------------------------------------------------------------------

    public HttpMessage registerNewUser(User iob_user) {
        return createRestRequest(GC_REST_ADD_USER_PATH, iob_user);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Login user
// ---------------------------------------------------------------------------------------------------------------------

    public User loginUser(User user) {
        ObjectMapper lob_mapper = new ObjectMapper();
        String lva_jsonInString;
        HttpMessage lob_httpMessage;
        UserImpl lob_user = new UserImpl();
        Response lob_response;

        lob_response = gob_webTarget.path(GC_REST_LOGIN_USER_PATH).request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        lva_jsonInString = lob_response.readEntity(String.class);

        try {
            if (lob_response.getStatus() == GC_HTTP_OK) {
                lob_user = lob_mapper.readValue(lva_jsonInString, UserImpl.class);
            } else if (lob_response.getStatus() == 401) {
                throw new IllegalArgumentException("Unauthorized");
            } else {
                lob_httpMessage = lob_mapper.readValue(lva_jsonInString, HttpMessage.class);
                lob_httpMessage.setHttpStatus(lob_response.getStatus());
                throw new IllegalArgumentException(lob_httpMessage.getUserLoginStatus());
            }
        } catch (IOException ex) {
            AlertWindows.createExceptionAlert(ex.getMessage(), ex);
            ex.printStackTrace();
        }

        return lob_user;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Change password
// ---------------------------------------------------------------------------------------------------------------------

    public HttpMessage changePassword(User iob_user) {
        return createRestRequest(GC_REST_CHANGE_PASSWORD_PATH, iob_user);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Get all users
// ---------------------------------------------------------------------------------------------------------------------

    public List<UserImpl> getAllUser() {
        List<UserImpl> lli_userList;

        lli_userList = gob_webTarget.path(GC_REST_GET_ALL_USERS_PATH).request()
                .get(new GenericType<List<UserImpl>>() {
                });

        return lli_userList;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Add new Admin
// ---------------------------------------------------------------------------------------------------------------------

    public HttpMessage addNewAdmin(User iob_user) {
        return createRestRequest(GC_REST_ADD_ADMIN_PATH, iob_user);
    }

//----------------------------------------------------------------------------------------------------------------------

    private HttpMessage createRestRequest(String iva_requestPath, User iob_user) {
        HttpMessage lob_httpMessage = null;

        try {
            Response response = gob_webTarget.path(iva_requestPath).request()
                    .put(Entity.entity(iob_user, MediaType.APPLICATION_JSON));

            lob_httpMessage = response.readEntity(HttpMessage.class);
            lob_httpMessage.setHttpStatus(response.getStatus());
        } catch (Exception ex) {
            AlertWindows.createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_httpMessage;
    }
}
