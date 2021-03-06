package restful.clients;

import cache.DataCache;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import com.thoughtworks.xstream.XStream;
import fileTree.interfaces.FileNode;
import fileTree.interfaces.Tree;
import fileTree.interfaces.TreeDifference;
import fileTree.classes.TreeDifferenceImpl;
import fileTree.classes.TreeImpl;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static tools.Utils.getDirectoryIdFromRelativePath;

public class FileRestClient extends RestClient {
    public FileRestClient(String iva_baseUrl, String iva_email, String iva_password) {
        super(iva_baseUrl, iva_email, iva_password);
    }

// ---------------------------------------------------------------------------------------------------------------------
// Create a new Directory on the Server
// ---------------------------------------------------------------------------------------------------------------------

    public boolean uploadFilesToServer(File iob_filesToUpload) {
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_relativeFilePath = Utils.buildRelativeFilePath(iob_filesToUpload);
        int lva_directoryId = getDirectoryIdFromRelativePath(lva_relativeFilePath, false);
        long lva_lastModified = 0;
        BasicFileAttributes lob_basicFileAttributes;

        if (!iob_filesToUpload.exists()) {
            return false;
        }

        HttpAuthenticationFeature lob_authDetails = HttpAuthenticationFeature.basic(
                lob_dataCache.get(DataCache.GC_EMAIL_KEY),
                lob_dataCache.get(DataCache.GC_PASSWORD_KEY)
        );

        try {
            lob_basicFileAttributes = Files.readAttributes(iob_filesToUpload.toPath(), BasicFileAttributes.class);
            lva_lastModified = lob_basicFileAttributes.lastModifiedTime().toMillis();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        ClientConfig lob_config = new ClientConfig(lob_authDetails);
        Client lob_client = ClientBuilder.newClient(lob_config);
        lob_client.register(lob_authDetails);

        WebTarget lob_target = lob_client.target("http://" + lob_dataCache.get(DataCache.GC_IP_KEY) + ":" +
                lob_dataCache.get(DataCache.GC_PORT_KEY) + "/api/auth/files/upload")
                .queryParam("path", lva_relativeFilePath)
                .queryParam("directoryId", lva_directoryId)
                .queryParam("lastModified", lva_lastModified);
        lob_target.register(MultiPartWriter.class);

        final FileDataBodyPart lob_filePart = new FileDataBodyPart("attachment", iob_filesToUpload);
        MultiPart lob_multiPart = new FormDataMultiPart().bodyPart(lob_filePart);

        Response lob_response = lob_target.request().post(Entity.entity(lob_multiPart, lob_multiPart.getMediaType()));

        System.out.println("UPLOAD: " + iob_filesToUpload.getName() + ": " + lob_response.getStatus());
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete a File or a Directory on the Server
// ---------------------------------------------------------------------------------------------------------------------

    public boolean createDirectoryOnServer(File iob_file) {
        String lva_relativePath = Utils.buildRelativeFilePath(iob_file);
        int lva_directoryId = getDirectoryIdFromRelativePath(lva_relativePath, false);

        Response lob_response = gob_webTarget.path("/auth/files/createDirectory")
                .queryParam("directoryId", lva_directoryId)
                .request()
                .post(Entity.entity(lva_relativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete a Directory and move the that it contained one up
// ---------------------------------------------------------------------------------------------------------------------

    public boolean deleteOnServer(File iob_file) {
        String lva_relativePath = Utils.buildRelativeFilePath(iob_file);
        int lva_directoryId = getDirectoryIdFromRelativePath(lva_relativePath, false);

        Response lob_response = gob_webTarget.path("/auth/files/delete")
                .queryParam("directoryId", lva_directoryId)
                .request()
                .post(Entity.entity(lva_relativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Delete only a directory on the server, move all files that the directory contains to the parent directory
// ---------------------------------------------------------------------------------------------------------------------
    public boolean deleteDirectoryOnly(File iob_file) {
        String lva_relativePath = Utils.buildRelativeFilePath(iob_file);

        Response lob_response = gob_webTarget.path("/auth/files/removeDirectoryOnly")
                .queryParam("directoryId", getDirectoryIdFromRelativePath(lva_relativePath, false))
                .request()
                .post(Entity.entity(lva_relativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Move a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public int moveFile(String iva_relativePath, String iva_newRelativePath) {
        int lva_sourceDirectoryId = getDirectoryIdFromRelativePath(iva_relativePath, false);
        int lva_destinationDirectoryId = getDirectoryIdFromRelativePath(iva_newRelativePath, false);

        Response lob_response = gob_webTarget.path("/auth/files/move")
                .queryParam("path", iva_relativePath)
                .queryParam("sourceDirectoryId", lva_sourceDirectoryId)
                .queryParam("destinationDirectoryId", lva_destinationDirectoryId)
                .request()
                .post(Entity.entity(iva_newRelativePath, MediaType.TEXT_PLAIN));

        switch (lob_response.getStatus()) {
            case 200: return 0;
            case 400: return 1;
            case 422: return 2;
            default: return 3;
        }
    }

// ---------------------------------------------------------------------------------------------------------------------
// Rename a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public boolean renameFile(File iob_file, String iva_newRelativePath) {
        int lva_directoryId;
        String lva_relativePath = Utils.buildRelativeFilePath(iob_file);
        lva_directoryId  = getDirectoryIdFromRelativePath(lva_relativePath, false);

        Response lob_response = gob_webTarget.path("/auth/files/rename")
                .queryParam("path", lva_relativePath)
                .queryParam("directoryId", lva_directoryId)
                .request()
                .post(Entity.entity(iva_newRelativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Rename a file on the server
// ---------------------------------------------------------------------------------------------------------------------
    public Collection<TreeDifference> compareClientAndServerTree(Tree iob_tree) {
        Collection<TreeDifference> lco_differences = new ArrayList<>();

        try {
            lco_differences.add(compareTreeToServer("\\" + DirectoryNameMapper.getPrivateDirectoryName(), iob_tree, -1));
            lco_differences.add(compareTreeToServer("\\" + DirectoryNameMapper.getPublicDirectoryName(), iob_tree, 0));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return lco_differences;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Compare the Client the tree to the Server version
// ---------------------------------------------------------------------------------------------------------------------
    private TreeDifference compareTreeToServer(String iva_directoryName, Tree iob_tree, int iva_directoryId) throws IOException {
        XStream lob_xmlParser = new XStream();
        XStream.setupDefaultSecurity(lob_xmlParser); // to be removed after 1.5

        Class[] lar_allowedClasses = {TreeDifference.class, TreeDifferenceImpl.class};
        lob_xmlParser.allowTypes(lar_allowedClasses);

        Tree lob_tree = new TreeImpl(iob_tree.getRoot() + iva_directoryName);
        File lob_rootFile = new File(iob_tree.getRoot() + iva_directoryName);
        FileNode lob_privateNode = iob_tree.getRootNode().getChild(lob_rootFile);
        lob_tree.addFiles(getNodeSubFiles(new HashMap<>(), lob_privateNode));
        String lva_treeXmlString = lob_xmlParser.toXML(lob_tree);
//        System.out.println(lva_treeXmlString);

        Response lob_privateResponse = gob_webTarget.path("/auth/files/compare").queryParam("DirectoryId", iva_directoryId).request()
                .post(Entity.entity(lva_treeXmlString, MediaType.APPLICATION_XML));

        String lva_xmlDifferenceString = lob_privateResponse.readEntity(String.class);
        System.out.println(lva_xmlDifferenceString);
        return (TreeDifference) lob_xmlParser.fromXML(lva_xmlDifferenceString);
    }

// ---------------------------------------------------------------------------------------------------------------------
// download a file from the server
// ---------------------------------------------------------------------------------------------------------------------
    public Object downloadFile(String iva_filePath) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_filePath, false);

        Response lob_response = gob_webTarget.path("/auth/files/download")
                .queryParam("directoryId", lva_directoryId)
                .queryParam("path", iva_filePath).request().get();

        if (lob_response.getStatus() != 204 && lob_response.getStatus() != 200) {
            return null;
        }

        try {
            if (lob_response.getStatus() == 204) {
                return 0;
            }

            InputStream lob_inputStream = lob_response.readEntity(InputStream.class);
            return IOUtils.toByteArray(lob_inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map<File, Boolean> getNodeSubFiles(Map<File, Boolean> iob_map, FileNode iob_treeNodePinter) {
        boolean lva_isDirectory = iob_treeNodePinter.getFile().isDirectory();
        iob_map.put(iob_treeNodePinter.getFile(), lva_isDirectory);

        for (FileNode lob_child : iob_treeNodePinter.getChildren()) {
            getNodeSubFiles(iob_map, lob_child);
        }

        return iob_map;
    }
}
