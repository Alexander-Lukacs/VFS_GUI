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
import fileTree.models.TreeDifferenceImpl;
import fileTree.models.TreeImpl;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import tools.Utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FileRestClient extends RestClient {
    public FileRestClient(String iva_baseUrl, String iva_email, String iva_password) {
        super(iva_baseUrl, iva_email, iva_password);
    }

    // ---------------------------------------------------------------------------------------------------------------------
// Upload a File to the Server
// ---------------------------------------------------------------------------------------------------------------------

    private static int getDirectoryIdFromRelativePath(String iva_path) {
        if (iva_path.startsWith("Shared")) {
            return 1;
        }

        if (iva_path.startsWith("Public")) {
            return 0;
        }

        return -1;
    }

// ---------------------------------------------------------------------------------------------------------------------
// Create a new Directory on the Server
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
// Delete a File or a Directory on the Server
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
// Delete a Directory and move the that it contained one up
// ---------------------------------------------------------------------------------------------------------------------

    public boolean deleteOnServer(String iva_relativePath) {
        Response lob_response = gob_webTarget.path("/auth/files/delete")
                .queryParam("directoryId", getDirectoryIdFromRelativePath(iva_relativePath))
                .request()
                .post(Entity.entity(iva_relativePath, MediaType.TEXT_PLAIN));
        return lob_response.getStatus() == 200;
    }

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
            lco_differences.add(compareTreeToServer("\\Private", iob_tree, -1));
            lco_differences.add(compareTreeToServer("\\Public", iob_tree, 0));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return lco_differences;
    }

    private TreeDifference compareTreeToServer(String iva_directoryName, Tree iob_tree, int iva_directoryId) throws IOException {
        XStream lob_xmlParser = new XStream();
        XStream.setupDefaultSecurity(lob_xmlParser); // to be removed after 1.5

        Class[] lar_allowedClasses = {TreeDifference.class, TreeDifferenceImpl.class};
        lob_xmlParser.allowTypes(lar_allowedClasses);

        Tree lob_tree = new TreeImpl(iob_tree.getRoot() + iva_directoryName);
        File lob_rootFile = new File(iob_tree.getRoot() + iva_directoryName);
        FileNode lob_privateNode = iob_tree.getRootNode().getChild(lob_rootFile);
        lob_tree.addFiles(getNodeSubFiles(new HashMap<>(), lob_privateNode));
        String lva_privateTreeXmlString = lob_xmlParser.toXML(lob_tree);

        Response lob_privateResponse = gob_webTarget.path("/auth/files/compare").queryParam("DirectoryId", iva_directoryId).request()
                .post(Entity.entity(lva_privateTreeXmlString, MediaType.APPLICATION_XML));

        String lva_xmlDifferenceString = lob_privateResponse.readEntity(String.class);
        System.out.println(lva_xmlDifferenceString);

        return (TreeDifference) lob_xmlParser.fromXML(lva_xmlDifferenceString);
    }

    public File downloadFile(String iva_filePath) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_filePath);

        Response lob_response = gob_webTarget.path("/auth/files/download")
                .queryParam("directoryId", lva_directoryId)
                .queryParam("path", iva_filePath).request().get();

        if (lob_response.getStatus() != 204 && lob_response.getStatus() != 200) {
            return null;
        }

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

        for (FileNode lob_child : iob_treeNodePinter.getChildren()) {
            getNodeSubFiles(iob_map, lob_child);
        }

        return iob_map;
    }
}
