package tools.xmlTools;

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

    public static void addFile(File iob_file, String iob_version, String iob_lastModified) {
        Element lob_newFile = new Element(GC_FILE);
        lob_newFile.setAttribute(GC_ATTRIBUTE_PATH, iob_file.getPath());
        lob_newFile.setAttribute(GC_ATTRIBUTE_VERSION, iob_version);
        lob_newFile.setAttribute(GC_ATTRIBUTE_LAST_MODIFIED, iob_lastModified);

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

    public static void setPath(File iob_file, String iva_newPath) {
        changeAttribute(GC_ATTRIBUTE_PATH, iva_newPath, iob_file.getPath());
    }

    public static void setVersion(File iob_file, int iva_version) {
        changeAttribute(GC_ATTRIBUTE_VERSION, String.valueOf(iva_version), iob_file.getPath());
    }

    public static void setLastModified(File iob_file, long iva_lastModified) {
        changeAttribute(GC_ATTRIBUTE_LAST_MODIFIED, String.valueOf(iva_lastModified), iob_file.getPath());
    }

    public static File getFile(String filePath) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
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
                if (lob_element.getAttributeValue(GC_ATTRIBUTE_PATH).equals(filePath)) {
                    lob_file = new File(lob_element.getAttributeValue(GC_ATTRIBUTE_PATH));
                    lob_file.setLastModified(Long.parseLong(lob_element.getAttributeValue(GC_ATTRIBUTE_LAST_MODIFIED)));
                    return lob_file;
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
