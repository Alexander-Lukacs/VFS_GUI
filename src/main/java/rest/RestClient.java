package rest;

import cache.DataCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import com.thoughtworks.xstream.XStream;
import fileTree.interfaces.FileNode;
import fileTree.interfaces.Tree;
import fileTree.interfaces.TreeDifference;
import fileTree.models.FileNodeImpl;
import fileTree.models.TreeDifferenceImpl;
import fileTree.models.TreeImpl;
import models.classes.RestResponse;
import models.classes.SharedDirectory;
import models.classes.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.internal.HttpUrlConnector;
import tools.AlertWindows;
import tools.Utils;
import tools.xmlTools.XmlTools;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

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
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_relativeFilePath);

        HttpAuthenticationFeature lob_authDetails = HttpAuthenticationFeature.basic(
                lob_dataCache.get(DataCache.GC_EMAIL_KEY),
                lob_dataCache.get(DataCache.GC_PASSWORD_KEY)

        );

        ClientConfig lob_config = new ClientConfig(lob_authDetails);
        Client lob_client = ClientBuilder.newClient(lob_config);
        lob_client.register(lob_authDetails);

        WebTarget lob_target = lob_client.target("http://" + lob_dataCache.get(DataCache.GC_IP_KEY) + ":" +
                lob_dataCache.get(DataCache.GC_PORT_KEY) + "/api/auth/files/upload")
                .queryParam("path", iva_relativeFilePath)
                .queryParam("directoryId", lva_directoryId);
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
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_relativeDirectoryPath);

        Response lob_response = gob_webTarget.path("/auth/files/createDirectory")
                .queryParam("directoryId", lva_directoryId)
                .request()
                .post(Entity.entity(iva_relativeDirectoryPath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete a File or a Directory on the Server
// ---------------------------------------------------------------------------------------------------------------------

    public boolean deleteOnServer(String iva_relativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/delete")
                .queryParam("directoryId", getDirectoryIdFromRelativePath(iva_relativePath))
                .request()
                .post(Entity.entity(iva_relativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete a Directory and move the that it contained one up
// ---------------------------------------------------------------------------------------------------------------------

    public void deleteDirectoryOnly(String iva_relativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/removeDirectoryOnly")
                .queryParam("directoryId", getDirectoryIdFromRelativePath(iva_relativePath))
                .request()
                .post(Entity.entity(iva_relativePath, MediaType.TEXT_PLAIN));
        System.out.println(lob_response.getStatus());
    }

// ---------------------------------------------------------------------------------------------------------------------
// Move a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public void moveFile(String iva_relativePath, String iva_newRelativePath) {
        int lva_sourceDirectoryId = getDirectoryIdFromRelativePath(iva_relativePath);
        int lva_destinationDirectoryId = getDirectoryIdFromRelativePath(iva_newRelativePath);

        Response lob_response = gob_webTarget.path("/auth/files/move")
                .queryParam("path", iva_relativePath)
                .queryParam("sourceDirectoryId", lva_sourceDirectoryId)
                .queryParam("destinationDirectoryId", lva_destinationDirectoryId)
                .request()
                .post(Entity.entity(iva_newRelativePath, MediaType.TEXT_PLAIN));
        System.out.println(lob_response.getStatus());
    }


// ---------------------------------------------------------------------------------------------------------------------
// Rename a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public void renameFile(String iva_relativePath, String iva_newRelativePath) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_relativePath);

        Response lob_response = gob_webTarget.path("/auth/files/rename")
                .queryParam("path", iva_relativePath)
                .queryParam("directoryId", lva_directoryId)
                .request()
                .post(Entity.entity(iva_newRelativePath, MediaType.TEXT_PLAIN));
        System.out.println(lob_response.getStatus());
    }

// ---------------------------------------------------------------------------------------------------------------------
// Rename a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public Collection<TreeDifference> compareClientAndServerTree(Tree iob_tree) {
        Collection<TreeDifference> lco_differences = new ArrayList<>();

        try {
            XStream lob_xmlParser = new XStream();
            XStream.setupDefaultSecurity(lob_xmlParser); // to be removed after 1.5

            Class[] lar_allowedClasses = {TreeDifference.class, TreeDifferenceImpl.class};
            lob_xmlParser.allowTypes(lar_allowedClasses);

            Tree lob_privateTree = new TreeImpl(iob_tree.getRoot() + "\\Private");
            Tree lob_publicTree = new TreeImpl(iob_tree.getRoot() + "\\Public");

            File lob_privateRootFile = new File(iob_tree.getRoot() + "\\Private");
            File lob_publicRootFile = new File(iob_tree.getRoot() + "\\Public");
            
            FileNode lob_privateNode = iob_tree.getRootNode().getChild(lob_privateRootFile);
            FileNode lob_publicNode = iob_tree.getRootNode().getChild(lob_publicRootFile);
            
            lob_privateTree.addFiles(getNodeSubFiles(new HashMap<>(), lob_privateNode));
            lob_publicTree.addFiles(getNodeSubFiles(new HashMap<>(), lob_publicNode));
            
            String lva_privateTreeXmlString = lob_xmlParser.toXML(lob_privateTree);
            String lva_publicTreeXmlString = lob_xmlParser.toXML(lob_publicTree);

            Response lob_privateResponse = gob_webTarget.path("/auth/files/compare").queryParam("DirectoryId", -1).request()
                    .post(Entity.entity(lva_privateTreeXmlString, MediaType.APPLICATION_XML));

            Response lob_publicResponse = gob_webTarget.path("/auth/files/compare").queryParam("DirectoryId", 0).request()
                    .post(Entity.entity(lva_publicTreeXmlString, MediaType.APPLICATION_XML));


            String lva_xmlPrivateDifferenceString = lob_privateResponse.readEntity(String.class);
            String lva_xmlPublicDifferenceString = lob_publicResponse.readEntity(String.class);

            System.out.println(lva_xmlPrivateDifferenceString);
            TreeDifference lob_privateDifference = (TreeDifference) lob_xmlParser.fromXML(lva_xmlPrivateDifferenceString);

            System.out.println(lva_xmlPublicDifferenceString);
            TreeDifference lob_publicDifference = (TreeDifference) lob_xmlParser.fromXML(lva_xmlPublicDifferenceString);

            lco_differences.add(lob_privateDifference);
            lco_differences.add(lob_publicDifference);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return lco_differences;
    }

    public File downloadFile(String iva_filePath) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_filePath);

        Response lob_response = gob_webTarget.path("/auth/files/download")
                .queryParam("directoryId", lva_directoryId)
                .queryParam("path", iva_filePath).request().get();

        try {
            String lva_newFilePath = Utils.getUserBasePath() + "\\" +
                    DataCache.getDataCache().get(DataCache.GC_IP_KEY) +
                    "_" +
                    DataCache.getDataCache().get(DataCache.GC_PORT_KEY) +
                    "\\" +
                    DataCache.getDataCache().get(DataCache.GC_EMAIL_KEY) +
                    "\\";

            if (lva_directoryId < 0) {
                lva_newFilePath += "Private";
            } else if (lva_directoryId > 0) {
                lva_newFilePath += "Shared";
            }

            lva_newFilePath += iva_filePath;

            if (lob_response.getStatus() == 204) {
                File lob_newDirectory = new File(lva_newFilePath);
                lob_newDirectory.mkdir();
                return lob_newDirectory;
            }

            InputStream o = lob_response.readEntity(InputStream.class);
            FileOutputStream os = new FileOutputStream(lva_newFilePath);
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = o.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            os.close();
            o.close();

            return new File(lva_newFilePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map<File, Boolean> getNodeSubFiles(Map<File, Boolean> iob_map, FileNode iob_treeNodePinter) {
        boolean lva_isDirectory = iob_treeNodePinter.getFile().isDirectory();
        iob_map.put(iob_treeNodePinter.getFile(), lva_isDirectory);

        for (FileNode lob_child: iob_treeNodePinter.getChildren()) {
            getNodeSubFiles(iob_map, lob_child);
        }

        return iob_map;
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

// ---------------------------------------------------------------------------------------------------------------------
// Get all shared directories of user
// ---------------------------------------------------------------------------------------------------------------------

    public List<SharedDirectory> getAllSharedDirectoriesOfUser() {
        List<SharedDirectory> lli_userSharedDirectory = null;

        try {
            lli_userSharedDirectory = gob_webTarget.path("/getAllSharedDirectories").request()
                    .get(new GenericType<List<SharedDirectory>>(){});
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        return lli_userSharedDirectory;
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

    public static int getDirectoryIdFromRelativePath(String iva_path) {
        if (iva_path.startsWith("Shared")) {
            return 1;
        }

        if (iva_path.startsWith("Public")) {
            return 0;
        }

        return -1;
    }
}
