package tools.xmlTools;

import models.classes.MappedFile;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        lob_newFile.setAttribute(GC_ATTRIBUTE_PATH, iob_file.getFilePath());
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
        changeAttribute(GC_ATTRIBUTE_PATH, iva_newPath, iob_file.getFilePath());
    }

    public static void setVersion(MappedFile iob_file) {
        changeAttribute(GC_ATTRIBUTE_VERSION, String.valueOf(iob_file.getVersion()), iob_file.getFilePath());
    }

    public static void setLastModified(MappedFile iob_file) {
        changeAttribute(GC_ATTRIBUTE_LAST_MODIFIED, String.valueOf(iob_file.getLastModified()), iob_file.getFilePath());
    }

    public static MappedFile getFile(String filePath) {
        MappedFile lob_file = new MappedFile();
        String lva_version;
        String lva_lastModified;

        lob_file.setFilePath(getAttribute(filePath, GC_ATTRIBUTE_PATH));

        if ((lva_version = getAttribute(filePath, GC_ATTRIBUTE_VERSION)) != null) {
            lob_file.setVersion(Integer.parseInt(lva_version));
        } else {
            lob_file.setVersion(0);
        }

        if ((lva_lastModified = getAttribute(filePath, GC_ATTRIBUTE_LAST_MODIFIED)) != null) {
            lob_file.setVersion(Integer.parseInt(lva_lastModified));
        } else {
            lob_file.setLastModified(0L);
        }

        return lob_file;
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
        return Objects.requireNonNull(FileMapper.class.getClassLoader().getResource(GC_FILE_NAME)).getPath();
    }

    private static boolean fileExists() {
        return new File(getXmlPath()).exists();
    }
}
