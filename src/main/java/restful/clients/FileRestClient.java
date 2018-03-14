package restful.clients;

import cache.DataCache;
import cache.DirectoryCache;
import cache.FileMapperCache;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import com.thoughtworks.xstream.XStream;
import models.classes.DownloadedContent;
import models.classes.MappedFile;
import models.classes.TreeDifference;
import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
        long lva_version;
        MappedFile lob_mappedFile;

        lva_relativeFilePath = Utils.buildRelativeFilePathForServer(iob_filesToUpload.toPath()).toString();

        if (!iob_filesToUpload.exists()) {
            return false;
        }

        HttpAuthenticationFeature lob_authDetails = HttpAuthenticationFeature.basic(
                lob_dataCache.get(DataCache.GC_EMAIL_KEY),
                lob_dataCache.get(DataCache.GC_PASSWORD_KEY)
        );

        lob_mappedFile = FileMapperCache.getFileMapperCache().get(iob_filesToUpload.toPath());

        if (lob_mappedFile == null) {
            return false;
        }

        lva_version = lob_mappedFile.getVersion();

        ClientConfig lob_config = new ClientConfig(lob_authDetails);
        Client lob_client = ClientBuilder.newClient(lob_config);
        lob_client.register(lob_authDetails);

        WebTarget lob_target = lob_client.target("http://" + lob_dataCache.get(DataCache.GC_IP_KEY) + ":" +
                lob_dataCache.get(DataCache.GC_PORT_KEY) + "/api/auth/files/upload")
                .queryParam("path", lva_relativeFilePath)
                .queryParam("directoryId", lva_directoryId)
                .queryParam("version", lva_version);
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
// Compare the Client the tree to the Server version
// ---------------------------------------------------------------------------------------------------------------------
    public TreeDifference compareClientAndServerTree() {
        XStream lob_xmlParser = new XStream();
        XStream.setupDefaultSecurity(lob_xmlParser);
        Collection<MappedFile> lco_mappedFiles = filterFilesForComparison();
        Collection<MappedFile> lco_mappedFilesForServer = new ArrayList<>();
        String lva_mappedFilesAsXmlString;
        String lva_fileDifferenceAsXml;

        Class[] lar_allowedClasses = {TreeDifference.class, MappedFile.class};
        lob_xmlParser.allowTypes(lar_allowedClasses);


        lco_mappedFiles.forEach(lob_mappedFile ->
            lco_mappedFilesForServer.add(new MappedFile(
                    lob_mappedFile.getFilePath(),
                    lob_mappedFile.getVersion(),
                    lob_mappedFile.getLastModified()
            ))
        );

        lco_mappedFilesForServer.forEach(lob_mappedFile ->
            lob_mappedFile.setFilePath(Utils.buildRelativeFilePathForServer(lob_mappedFile.getFilePath()))
        );

        lva_mappedFilesAsXmlString = lob_xmlParser.toXML(lco_mappedFilesForServer);

        Response lob_response = gob_webTarget.path("/auth/files/compare").request()
                .post(Entity.entity(lva_mappedFilesAsXmlString, MediaType.APPLICATION_XML));


        if (lob_response.getStatus() == 200) {
            lva_fileDifferenceAsXml = lob_response.readEntity(String.class);
            return (TreeDifference) lob_xmlParser.fromXML(lva_fileDifferenceAsXml);
        }
        return null;
    }

// ---------------------------------------------------------------------------------------------------------------------
// download a file from the server
// ---------------------------------------------------------------------------------------------------------------------
    public DownloadedContent downloadFile(String iva_filePath) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_filePath, false);
        DownloadedContent rob_downloadedContent;
        String lva_versionAsString;
        int lva_version;
        byte[] lar_fileContent;

        Response lob_response = gob_webTarget.path("/auth/files/download")
                .queryParam("directoryId", lva_directoryId)
                .queryParam("path", iva_filePath).request().get();

        if (lob_response.getStatus() != 204 && lob_response.getStatus() != 200) {
            return null;
        }

        try {
            lva_versionAsString = (String) lob_response.getHeaders().get("Content-Disposition").get(0);
            lva_version = Integer.parseInt(lva_versionAsString);

            if (lob_response.getStatus() == 204) {
                rob_downloadedContent = new DownloadedContent(null, true, lva_version);
            } else {

                InputStream lob_inputStream = lob_response.readEntity(InputStream.class);
                lar_fileContent = IOUtils.toByteArray(lob_inputStream);

                rob_downloadedContent = new DownloadedContent(lar_fileContent, false, lva_version);
            }

            return rob_downloadedContent;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Collection<MappedFile> filterFilesForComparison() {
        Collection<MappedFile> lco_mappedFiles = FileMapperCache.getFileMapperCache().getAll();
        DirectoryCache lob_directoryCache = DirectoryCache.getDirectoryCache();

        return lco_mappedFiles.stream().filter(lob_mappedFile -> {
            if (lob_mappedFile.getFilePath().toFile().equals(lob_directoryCache.getPrivateDirectory())) {
                return false;
            }

            if (lob_mappedFile.getFilePath().toFile().equals(lob_directoryCache.getPublicDirectory())) {
                return false;
            }

            if (lob_mappedFile.getFilePath().toFile().equals(lob_directoryCache.getSharedDirectory())) {
                return false;
            }

            if (lob_mappedFile.getFilePath().startsWith(lob_directoryCache.getSharedDirectory().toPath())) {
                return (lob_mappedFile.getFilePath().getNameCount() > lob_directoryCache.getSharedDirectory().toPath().getNameCount() + 1);
            }

            return !lob_mappedFile.getFilePath().toFile().equals(lob_directoryCache.getUserDirectory());
        }).collect(Collectors.toList());
    }

//    private Map<File, Boolean> getNodeSubFiles(Map<File, Boolean> iob_map, FileNode iob_treeNodePinter) {
//        boolean lva_isDirectory = iob_treeNodePinter.getFile().isDirectory();
//        iob_map.put(iob_treeNodePinter.getFile(), lva_isDirectory);
//
//        for (FileNode lob_child : iob_treeNodePinter.getChildren()) {
//            getNodeSubFiles(iob_map, lob_child);
//        }
//
//        return iob_map;
//    }
}
