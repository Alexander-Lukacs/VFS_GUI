package rest;

import cache.DataCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import models.classes.RestResponse;
import models.classes.SharedDirectory;
import models.classes.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import tools.AlertWindows;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static rest.constants.HttpStatusCodes.GC_HTTP_OK;
import static rest.constants.RestResourcesPaths.*;

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

    public RestResponse registerNewUser(User iob_user) {
        return createPutRequest(GC_REST_ADD_USER_PATH, iob_user);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Login user
// ---------------------------------------------------------------------------------------------------------------------

    public User loginUser(User user) {
        ObjectMapper lob_mapper = new ObjectMapper();
        String lva_responseString;
        RestResponse lob_restResponse = new RestResponse();
        User lob_user = new User();
        Response lob_response;

        lob_response = gob_webTarget.path(GC_REST_LOGIN_USER_PATH).request()
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        lva_responseString = lob_response.readEntity(String.class);

        try {
            if (lob_response.getStatus() == GC_HTTP_OK) {
                lob_user = lob_mapper.readValue(lva_responseString, User.class);

            } else {
                lob_restResponse.setResponseMessage(lva_responseString);
                lob_restResponse.setHttpStatus(lob_response.getStatus());

                // TODO eigene Exception schreiben
                throw new IllegalArgumentException(lob_restResponse.getResponseMessage());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ProcessingException ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_user;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Change password
// ---------------------------------------------------------------------------------------------------------------------

    public RestResponse changePassword(User iob_user) {
        return createPutRequest(GC_REST_CHANGE_PASSWORD_PATH, iob_user);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Get all users
// ---------------------------------------------------------------------------------------------------------------------

    public List<User> getAllUser() {
        List<User> lli_userList = null;

        try {
            lli_userList = gob_webTarget.path(GC_REST_GET_ALL_USERS_PATH).request()
                    .get(new GenericType<List<User>>() {
                    });
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lli_userList;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Add new Admin
// ---------------------------------------------------------------------------------------------------------------------

    public RestResponse addNewAdmin(User iob_user) {
        return createPutRequest(GC_REST_ADD_ADMIN_PATH, iob_user);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Upload a File to the Server
// ---------------------------------------------------------------------------------------------------------------------

    public boolean uploadFilesToServer(File iob_filesToUpload, String iva_relativeFilePath) {
        DataCache lob_dataCache = DataCache.getDataCache();

        HttpAuthenticationFeature lob_authDetails = HttpAuthenticationFeature.basic(
                lob_dataCache.get(DataCache.GC_EMAIL_KEY),
                lob_dataCache.get(DataCache.GC_PASSWORD_KEY)

        );
        ClientConfig lob_config = new ClientConfig(lob_authDetails);
        Client lob_client = ClientBuilder.newClient(lob_config);
        lob_client.register(lob_authDetails);

        WebTarget lob_target = lob_client.target("http://" + lob_dataCache.get(DataCache.GC_IP_KEY) + ":" + lob_dataCache.get(DataCache.GC_PORT_KEY) + "/api/auth/files/upload").queryParam("path", iva_relativeFilePath);
        lob_target.register(MultiPartWriter.class);

        final FileDataBodyPart lob_filePart = new FileDataBodyPart("attachment", iob_filesToUpload);
        MultiPart lob_multiPart = new FormDataMultiPart().bodyPart(lob_filePart);

        Response lob_response = lob_target.request().post(Entity.entity(lob_multiPart, lob_multiPart.getMediaType()));

        System.out.println(iob_filesToUpload.getName() + ": " + lob_response.getStatus());
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Create a new Directory on the Server
// ---------------------------------------------------------------------------------------------------------------------

    public boolean createDirectoryOnServer(String iva_relativeDirectoryPath) {
        Response lob_response = gob_webTarget.path("/auth/files/createDirectory").request()
                .post(Entity.entity(iva_relativeDirectoryPath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete a File or a Directory on the Server
// ---------------------------------------------------------------------------------------------------------------------

    public boolean deleteOnServer(String iva_relativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/delete").request()
                .post(Entity.entity(iva_relativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete a Directory and move the that it contained one up
// ---------------------------------------------------------------------------------------------------------------------

    public void deleteDirectoryOnly(String iva_relativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/removeDirectoryOnly").request()
                .post(Entity.entity(iva_relativePath, MediaType.TEXT_PLAIN));
        System.out.println(lob_response.getStatus());
    }

// ---------------------------------------------------------------------------------------------------------------------
// Move a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public void moveFile(String iva_relativePath, String iva_newRelativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/move").queryParam("path", iva_relativePath).request()
                .post(Entity.entity(iva_newRelativePath, MediaType.TEXT_PLAIN));
        System.out.println(lob_response.getStatus());
    }


// ---------------------------------------------------------------------------------------------------------------------
// Rename a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public void renameFile(String iva_relativePath, String iva_newRelativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/rename").queryParam("path", iva_relativePath).request()
                .post(Entity.entity(iva_newRelativePath, MediaType.TEXT_PLAIN));
        System.out.println(lob_response.getStatus());
    }

//----------------------------------------------------------------------------------------------------------------------

// ---------------------------------------------------------------------------------------------------------------------
// Unregister the Client from Server
// ---------------------------------------------------------------------------------------------------------------------

    public void unregisterClient() {
        gob_webTarget.path(GC_REST_UNREGISTER_CLIENT).request().get();
    }

// ---------------------------------------------------------------------------------------------------------------------
// Add new shared directory
// ---------------------------------------------------------------------------------------------------------------------

    public RestResponse addNewSharedDirectory(SharedDirectory iob_sharedDirectory) {
        return createPostRequest(GC_REST_ADD_NEW_SHARED_DIRECTORY, iob_sharedDirectory);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Add new member to shared directory
// ---------------------------------------------------------------------------------------------------------------------

    public RestResponse addNewMemberToSharedDirectory(SharedDirectory iob_sharedDirectory, User iob_member) {
        int lva_id = iob_sharedDirectory.getId();
        return createPutRequest(GC_REST_ADD_NEW_MEMBER_TO_SHARED_DIR + lva_id, iob_member);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete shared directory
// ---------------------------------------------------------------------------------------------------------------------

    public RestResponse deleteSharedDirectory(SharedDirectory iob_sharedDirectory) {
        return createPostRequest(GC_REST_DELETE_SHARED_DIRECTORY, iob_sharedDirectory);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Remove member from shared directory
// ---------------------------------------------------------------------------------------------------------------------

    public RestResponse removeMemberFromSharedDirectory(SharedDirectory iob_sharedDirectory, User iob_member) {
        int lva_id = iob_sharedDirectory.getId();
        return createPutRequest(GC_REST_REMOVE_MEMBER_FROM_SHARED_DIR + lva_id, iob_member);
    }

//----------------------------------------------------------------------------------------------------------------------

    private RestResponse createPutRequest(String iva_requestPath, Object iob_user) {
        RestResponse lob_restResponse = new RestResponse();

        try {
            Response response = gob_webTarget.path(iva_requestPath).request()
                    .put(Entity.entity(iob_user, MediaType.APPLICATION_JSON));

            lob_restResponse.setResponseMessage(response.readEntity(String.class));
            lob_restResponse.setHttpStatus(response.getStatus());
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_restResponse;
    }

    private RestResponse createPostRequest(String iva_requestPath, Object iob_entity) {
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
