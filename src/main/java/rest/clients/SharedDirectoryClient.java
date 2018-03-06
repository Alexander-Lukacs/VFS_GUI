package rest.clients;

import com.thoughtworks.xstream.XStream;
import models.classes.RestResponse;
import models.classes.SharedDirectory;
import models.classes.User;
import tools.AlertWindows;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static rest.constants.RestResourcesPaths.*;

public class SharedDirectoryClient extends RestClient {
    public SharedDirectoryClient(String iva_baseUrl, String iva_email, String iva_password) {
        super(iva_baseUrl, iva_email, iva_password);
    }

    /**
     * Add a new shared directory to the database
     * @param iob_sharedDirectory the new shared directory
     * @return RestResponse with the status of the request
     */
    public RestResponse addNewSharedDirectory(SharedDirectory iob_sharedDirectory) {
        RestResponse lob_restResponse = new RestResponse();

        XStream lob_xmlParse = new XStream();
        XStream.setupDefaultSecurity(lob_xmlParse);

        String lva_sharedDirectoryXmlString = lob_xmlParse.toXML(iob_sharedDirectory);

        try {
            Response response = gob_webTarget.path("sharedDirectory/auth/addNewSharedDirectory").request()
                    .post(Entity.entity(lva_sharedDirectoryXmlString, MediaType.APPLICATION_XML));

            System.out.println(response.getStatus() + " SHARED");
            lob_restResponse.setResponseMessage(response.readEntity(String.class));
            lob_restResponse.setHttpStatus(response.getStatus());
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lob_restResponse;
    }

    /**
     * Add a new member to the shared directory
     * @param iob_sharedDirectory the shared directory
     * @param iob_member the new member
     * @return RestResponse with the status of the request
     */
    public RestResponse addNewMemberToSharedDirectory(SharedDirectory iob_sharedDirectory, User iob_member) {
        int lva_id = iob_sharedDirectory.getId();
        return createPutRequest(GC_REST_ADD_NEW_MEMBER_TO_SHARED_DIR + lva_id, iob_member);
    }

    /**
     * Delete a shared directory
     * @param iob_sharedDirectory the shared directory to delete
     * @return RestResponse with the status of the request
     */
    public RestResponse deleteSharedDirectory(SharedDirectory iob_sharedDirectory) {
        return createPostRequest(GC_REST_DELETE_SHARED_DIRECTORY, iob_sharedDirectory);
    }

    /**
     * Remove a member from shared directory
     * @param iob_sharedDirectory shared directory
     * @param iob_member the member to remove
     * @return RestResponse with the status of the request
     */
    public RestResponse removeMemberFromSharedDirectory(SharedDirectory iob_sharedDirectory, User iob_member) {
        int lva_id = iob_sharedDirectory.getId();
        return createPutRequest(GC_REST_REMOVE_MEMBER_FROM_SHARED_DIR + lva_id, iob_member);
    }

    /**
     * Get all shared directories of an user
     * @return List of all shared directories of an user
     */
    public List<SharedDirectory> getAllSharedDirectoriesOfUser() {
        List<SharedDirectory> lli_userSharedDirectory = null;
        XStream lob_xmlParser = new XStream();
        XStream.setupDefaultSecurity(lob_xmlParser); // to be removed after 1.5

        Class[] lar_allowedClasses = {SharedDirectory.class, User.class};
        lob_xmlParser.allowTypes(lar_allowedClasses);
        String lva_sharedDirectoryXmlString;

        try {
            Response response = gob_webTarget.path("sharedDirectory/auth/getAllSharedDirectories").request().get();
            lva_sharedDirectoryXmlString  = response.readEntity(String.class);
            lli_userSharedDirectory = (List<SharedDirectory>) lob_xmlParser.fromXML(lva_sharedDirectoryXmlString);
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lli_userSharedDirectory;
    }
}
