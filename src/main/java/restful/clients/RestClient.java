package restful.clients;

import models.classes.RestResponse;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import tools.AlertWindows;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static restful.constants.RestResourcesPaths.GC_REST_UNREGISTER_CLIENT;

/**
 * Created by Mesut on 25.01.2018.
 */
public class RestClient {
    WebTarget gob_webTarget;

// ---------------------------------------------------------------------------------------------------------------------
// Constructors
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * Constructor without authorisation
     *
     * @param iva_baseUrl the base url of the server
     */
    public RestClient(String iva_baseUrl) {
        Client lob_client = ClientBuilder.newClient();
        gob_webTarget = lob_client.target(iva_baseUrl);
    }

    /**
     * Constructor with authorisation
     *
     * @param iva_baseUrl  the base url of the server
     * @param iva_email    email of the user
     * @param iva_password password of the user
     */
    public RestClient(String iva_baseUrl, String iva_email, String iva_password) {
        HttpAuthenticationFeature lob_authDetails = HttpAuthenticationFeature.basic(iva_email, iva_password);
        ClientConfig lob_config = new ClientConfig(lob_authDetails);

        Client lob_client = ClientBuilder.newClient(lob_config);
        lob_client.register(lob_authDetails);

        gob_webTarget = lob_client.target(iva_baseUrl);
    }

// ---------------------------------------------------------------------------------------------------------------------

    /**
     * Unregister client from notify server
     */
    public void unregisterClient() {
        gob_webTarget.path(GC_REST_UNREGISTER_CLIENT).request().get();
    }

    RestResponse createPutRequest(String iva_requestPath, Object iob_object) {
        RestResponse lob_restResponse = new RestResponse();

        try {
            Response response = gob_webTarget.path(iva_requestPath).request()
                    .put(Entity.entity(iob_object, MediaType.APPLICATION_JSON));

            lob_restResponse.setResponseMessage(response.readEntity(String.class));
            lob_restResponse.setHttpStatus(response.getStatus());
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_restResponse;
    }

    RestResponse createPostRequest(String iva_requestPath, Object iob_entity) {
        RestResponse lob_restResponse = new RestResponse();

        try {
            Response response = gob_webTarget.path(iva_requestPath).request()
                    .post(Entity.entity(iob_entity, MediaType.APPLICATION_JSON));

            lob_restResponse.setResponseMessage(response.readEntity(String.class));
            lob_restResponse.setHttpStatus(response.getStatus());
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_restResponse;
    }
}
