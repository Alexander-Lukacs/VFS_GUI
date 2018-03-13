package tools.xmlTools;

import cache.DataCache;
import models.classes.MappedFile;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import tools.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.nio.file.Path;
import java.util.Objects;

public abstract class FileMapper {
    private static final String GC_ROOT_ELEMENT_NAME = "files";
    private static final String GC_FILE_NAME = "fileMapping.xml";
    private static final String GC_FILE = "file";
    private static final String GC_ATTRIBUTE_PATH = "path";
    private static final String GC_ATTRIBUTE_VERSION = "version";
    private static final String GC_ATTRIBUTE_LAST_MODIFIED = "lastModified";

    public static void addFile(MappedFile iob_file) {
        Element lob_newFile = new Element(GC_FILE);
        lob_newFile.setAttribute(GC_ATTRIBUTE_PATH, iob_file.getFilePath().toString());
        lob_newFile.setAttribute(GC_ATTRIBUTE_VERSION, String.valueOf(iob_file.getVersion()));
        lob_newFile.setAttribute(GC_ATTRIBUTE_LAST_MODIFIED, String.valueOf(iob_file.getLastModified()));

        XMLOutputter lob_xmlOutput;
        String lva_xmlFilePath;
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;

        if (!fileExists()) {
            createXml();
        }

        try {
            lob_inputFile = new File(getXmlPath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();
            lob_rootElement.addContent(lob_newFile);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lva_xmlFilePath = getXmlPath();
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException | JDOMException ex) {
            ex.printStackTrace();
        }
    }

    public static void setPath(MappedFile iob_file, String iva_newPath) {
        changeAttribute(GC_ATTRIBUTE_PATH, iva_newPath, iob_file.getFilePath().toString());
    }

    public static void setVersion(MappedFile iob_file) {
        changeAttribute(GC_ATTRIBUTE_VERSION, String.valueOf(iob_file.getVersion()), iob_file.getFilePath().toString());
    }

    public static void setLastModified(MappedFile iob_file) {
        changeAttribute(GC_ATTRIBUTE_LAST_MODIFIED, String.valueOf(iob_file.getLastModified()), iob_file.getFilePath().toString());
    }

    public static List<MappedFile> getAllFiles() {
        List<MappedFile> mappedFileList = new ArrayList<>();
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        MappedFile lob_mappedFile;
        File lob_file;

        if (!fileExists()) {
            createXml();
        }

        try {
            lob_inputFile = new File(getXmlPath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();

            for (Element lob_element : lob_rootElement.getChildren()) {
                lob_mappedFile = new MappedFile();
                lob_file = new File(lob_element.getAttributeValue(GC_ATTRIBUTE_PATH));
                lob_mappedFile.setFilePath(lob_file.toPath());
                lob_mappedFile.setVersion(Integer.parseInt(lob_element.getAttributeValue(GC_ATTRIBUTE_VERSION)));
                lob_mappedFile.setLastModified(Long.parseLong(lob_element.getAttributeValue(GC_ATTRIBUTE_LAST_MODIFIED)));

                mappedFileList.add(lob_mappedFile);
            }
        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        return mappedFileList;
    }

    public static MappedFile getFile(String filePath) {
        MappedFile lob_file = new MappedFile();
        String lva_version;
        String lva_lastModified;
        String lva_filePath;
        Path lob_path;

//        lob_file.setFilePath(getAttribute(filePath, GC_ATTRIBUTE_PATH));

        lva_filePath = getAttribute(filePath, GC_ATTRIBUTE_PATH);

        if (lva_filePath != null) {
            lob_path = new File(lva_filePath).toPath();
            lob_file.setFilePath(lob_path);
        }

        if ((lva_version = getAttribute(filePath, GC_ATTRIBUTE_VERSION)) != null) {
            lob_file.setVersion(Integer.parseInt(lva_version));
        }

        if ((lva_lastModified = getAttribute(filePath, GC_ATTRIBUTE_LAST_MODIFIED)) != null) {
            lob_file.setLastModified(Long.parseLong(lva_lastModified));
        }

        return lob_file;
    }

    public static void removeFile(String iva_filePath) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_element;

        if (!fileExists()) {
            createXml();
        }

        try {
            lob_inputFile = new File(getXmlPath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();

            for (Iterator<Element> lob_elementIterator = lob_rootElement.getChildren().iterator() ;
                 lob_elementIterator.hasNext();) {

                lob_element = lob_elementIterator.next();

                if (lob_element.getAttributeValue(GC_ATTRIBUTE_PATH).equals(iva_filePath)) {
                    lob_elementIterator.remove();
                }
            }

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(lob_doc, new FileWriter(getXmlPath()));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getAttribute(String iva_path, String iva_attribute) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;

        if (!fileExists()) {
            createXml();
        }

        try {
            lob_inputFile = new File(getXmlPath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();

            for (Element lob_element : lob_rootElement.getChildren()) {
                if (lob_element.getAttributeValue(GC_ATTRIBUTE_PATH).equals(iva_path)) {
                    return lob_element.getAttributeValue(iva_attribute);
                }
            }

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(lob_doc, new FileWriter(getXmlPath()));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static void createXml() {
        DataCache lob_dataCache = DataCache.getDataCache();
        File lob_file;
        Document lob_doc;
        Element lob_rootElement;
        XMLOutputter lob_xmlOutput;
        String lva_xmlFilePath;

        try {
            // root element
            lob_rootElement = new Element(GC_ROOT_ELEMENT_NAME);
            lob_doc = new Document(lob_rootElement);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lob_file = new File(Utils.getUserBasePath() + "\\" + lob_dataCache.get(DataCache.GC_IP_KEY) + "_" +
                    lob_dataCache.get(DataCache.GC_PORT_KEY) + "\\" + lob_dataCache.get(DataCache.GC_EMAIL_KEY)
                    + "\\config");

            if (!lob_file.exists()) {
                lob_file.mkdir();
            }

            Files.setAttribute(lob_file.toPath(), "dos:hidden", true);

            lva_xmlFilePath = getXmlPath();
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void changeAttribute(String iva_attributeName, String iva_value, String iva_path) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_elementToModify;

        if (!fileExists()) {
            createXml();
        }

        try {
            lob_inputFile = new File(getXmlPath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();

            for (Element lob_element : lob_rootElement.getChildren()) {
                if (lob_element.getAttributeValue(GC_ATTRIBUTE_PATH).equals(iva_path)) {
                    lob_elementToModify = lob_element;
                    lob_elementToModify.setAttribute(iva_attributeName, iva_value);
                }
            }

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(lob_doc, new FileWriter(getXmlPath()));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getXmlPath() {
        DataCache lob_dataCache = DataCache.getDataCache();

        return Utils.getUserBasePath() + "\\" + lob_dataCache.get(DataCache.GC_IP_KEY) + "_" +
                lob_dataCache.get(DataCache.GC_PORT_KEY) + "\\" + lob_dataCache.get(DataCache.GC_EMAIL_KEY)
                + "\\config\\" + GC_FILE_NAME;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean fileExists() {
        return new File(getXmlPath()).exists();
    }
}
