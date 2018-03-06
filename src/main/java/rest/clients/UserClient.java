package rest.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.classes.RestResponse;
import models.classes.User;
import tools.AlertWindows;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static rest.constants.HttpStatusCodes.GC_HTTP_OK;
import static rest.constants.RestResourcesPaths.*;
import static rest.constants.RestResourcesPaths.GC_REST_ADD_ADMIN_PATH;

public class UserClient extends RestClient {
    public UserClient(String iva_baseUrl) {
        super(iva_baseUrl);
    }

    public UserClient(String iva_baseUrl, String iva_email, String iva_password) {
        super(iva_baseUrl, iva_email, iva_password);
    }

    /**
     * Register a new user to the database
     * @param iob_user the new user
     * @return RestResponse with the status of the request
     */
    public RestResponse registerNewUser(User iob_user) {
        return createPutRequest(GC_REST_ADD_USER_PATH, iob_user);
    }

    /**
     * Login a user
     * @param iob_user the user to login
     * @return a complete user object
     */
    public User loginUser(User iob_user) {
        ObjectMapper lob_mapper = new ObjectMapper();
        String lva_responseString;
        RestResponse lob_restResponse = new RestResponse();
        User lob_user = new User();
        Response lob_response;

        lob_response = gob_webTarget.path(GC_REST_LOGIN_USER_PATH).request()
                .put(Entity.entity(iob_user, MediaType.APPLICATION_JSON));

        lva_responseString = lob_response.readEntity(String.class);

        try {
            if (lob_response.getStatus() == GC_HTTP_OK) {
                lob_user = lob_mapper.readValue(lva_responseString, User.class);

            } else {
                lob_restResponse.setResponseMessage(lva_responseString);
                lob_restResponse.setHttpStatus(lob_response.getStatus());

                throw new IllegalArgumentException(lob_restResponse.getResponseMessage());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ProcessingException ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_user;
    }

    /**
     * Change the users password in the database
     * @param iob_user the user
     * @return RestResponse with the status of the request
     */
    public RestResponse changePassword(User iob_user) {
        return createPutRequest(GC_REST_CHANGE_PASSWORD_PATH, iob_user);
    }

    /**
     * Get all users from the database
     * @return List of users
     */
    public List<User> getAllUser() {
        List<User> lli_userList = null;

        try {
            lli_userList = gob_webTarget.path(GC_REST_GET_ALL_USERS_PATH).request()
                    .get(new GenericType<List<User>>() {});
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lli_userList;
    }

    /**
     * Add a new admin to the database
     * @param iob_user the new admin
     * @return RestResponse with the status of the request
     */
    public RestResponse addNewAdmin(User iob_user) {
        return createPutRequest(GC_REST_ADD_ADMIN_PATH, iob_user);
    }
}
