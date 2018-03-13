package tools.xmlTools;

import cache.DataCache;
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
import java.util.Iterator;
import java.util.List;

import static tools.constants.DirectoryNameMapperConstants.*;

public abstract class DirectoryNameMapper {
    private static final String GC_FILE_NAME = "mapping.xml";

    private static void createXmlFile() {
        DataCache lob_dataCache = DataCache.getDataCache();
        File lob_file;
        Document lob_doc;

        Element lob_rootElement;
        Element lob_publicDirectory;
        Element lob_privateDirectory;
        Element lob_sharedDirectoryRoot;
        Element lob_sharedDirectory;

        XMLOutputter lob_xmlOutput;
        String lva_xmlFilePath;

        try {
            // root element
            lob_rootElement = new Element(GC_ROOT_ELEMENT);
            lob_doc = new Document(lob_rootElement);

            // private directory element
            lob_privateDirectory = new Element(GC_PRIVATE_DIR_ELEMENT);
            lob_privateDirectory.setText("Private");

            // public directory element
            lob_publicDirectory = new Element(GC_PUBLIC_DIR_ELEMENT);
            lob_publicDirectory.setText("Public");

            // shared directory name element
            lob_sharedDirectory = new Element(GC_SHARED_DIR_ELEMENT);
            lob_sharedDirectory.setText("Shared");

            // shared directory root element
            lob_sharedDirectoryRoot = new Element(GC_SHARED_DIR_ROOT_ELEMENT);


            lob_doc.getRootElement().addContent(lob_privateDirectory);
            lob_doc.getRootElement().addContent(lob_publicDirectory);
            lob_doc.getRootElement().addContent(lob_sharedDirectory);
            lob_doc.getRootElement().addContent(lob_sharedDirectoryRoot);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lob_file = new File(Utils.getUserBasePath() + "\\" + lob_dataCache.get(DataCache.GC_IP_KEY) + "_" +
                    lob_dataCache.get(DataCache.GC_PORT_KEY) + "\\" + lob_dataCache.get(DataCache.GC_EMAIL_KEY)
                    + "\\config");

            if (!lob_file.exists()) {
                lob_file.mkdir();
            }

            Files.setAttribute(lob_file.toPath(), "dos:hidden", true);

            lva_xmlFilePath = getXmlFilePath();
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getPrivateDirectoryName() {
        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        return readXml(GC_PRIVATE_DIR_ELEMENT, GC_FILE_NAME);
    }

    public static void setPrivateDirectoryName(String iva_dirName) {
        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        modify(GC_PRIVATE_DIR_ELEMENT, iva_dirName, GC_FILE_NAME);
    }

    public static String getPublicDirectoryName() {
        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        return readXml(GC_PUBLIC_DIR_ELEMENT, GC_FILE_NAME);
    }

    public static void setPublicDirectoryName(String iva_dirName) {
        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        modify(GC_PUBLIC_DIR_ELEMENT, iva_dirName, GC_FILE_NAME);
    }

    public static String getSharedDirectoryName() {
        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        return readXml(GC_SHARED_DIR_ELEMENT, GC_FILE_NAME);
    }

    public static void setSharedDirectoryName(String iva_dirName) {
        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        modify(GC_SHARED_DIR_ELEMENT, iva_dirName, GC_FILE_NAME);
    }

    public static String getRenamedSharedDirectoryName(int iva_originalDirId) {
        return findSharedDirectoryById(String.valueOf(iva_originalDirId));
    }

    public static int getIdOfSharedDirectory(String lva_sharedDirectoryName) {
        return Integer.parseInt(findSharedDirectoryByText(lva_sharedDirectoryName));
    }

    public static void setNameOfSharedDirectory(int iva_sharedDirOriginalId, String iva_newSharedDirName) {
        if (!modifySharedDirectoryName(String.valueOf(iva_sharedDirOriginalId), iva_newSharedDirName)) {
            throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NOT_FOUND);
        }
    }

    public static void addNewSharedDirectory(int iva_sharedDirectoryId, String iva_sharedDirectoryName) {
        addSharedDirectoryElement(String.valueOf(iva_sharedDirectoryId), iva_sharedDirectoryName);
    }

    public static boolean removeSharedDirectory(int iva_sharedDirectoryId) {
        return removeElement(String.valueOf(iva_sharedDirectoryId));
    }

    private static boolean removeElement(String iva_elementId) {
        XMLOutputter lob_xmlOutput;
        String lva_xmlFilePath;
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        List<Element> lob_nodeList;
        Element lob_selectedElement;
        boolean hasChanged = false;
        Element lob_element;


        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();

            lob_selectedElement = lob_rootElement.getChild(GC_SHARED_DIR_ROOT_ELEMENT);
            lob_nodeList = lob_selectedElement.getChildren();


            for (Iterator<Element> lob_elementIterator = lob_nodeList.iterator(); lob_elementIterator.hasNext();) {
                lob_element = lob_elementIterator.next();
                if (lob_element.getAttributeValue(GC_ATTRIBUTE_ID).equals(iva_elementId)) {
//                    lob_selectedElement.removeContent(lob_element);
                    lob_elementIterator.remove();
                    hasChanged = true;
                }
            }

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lva_xmlFilePath = getXmlFilePath();
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException | JDOMException ex) {
            ex.printStackTrace();
        }

        return hasChanged;
    }

    private static void addSharedDirectoryElement(String iva_elementId, String iva_elementText) {
        Element lob_newSharedDirectory = new Element(GC_SHARED_DIR);
        lob_newSharedDirectory.setAttribute(GC_ATTRIBUTE_ID, iva_elementId);
        lob_newSharedDirectory.setText(iva_elementText);

        XMLOutputter lob_xmlOutput;
        String lva_xmlFilePath;
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;

        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();

            lob_selectedElement = lob_rootElement.getChild(GC_SHARED_DIR_ROOT_ELEMENT);
            lob_selectedElement.addContent(lob_newSharedDirectory);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lva_xmlFilePath = getXmlFilePath();
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException | JDOMException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean modifySharedDirectoryName(String iva_elementName, String iva_newDirName) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_elementToModify;
        List<Element> lob_nodeList;
        boolean lva_hasChanged = false;

        if (checkIfFileNotExist()) {
            createXmlFile();
        }

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();


            lob_elementToModify = lob_rootElement.getChild(GC_SHARED_DIR_ROOT_ELEMENT);
            lob_nodeList = lob_elementToModify.getChildren();

            for (Element lob_element : lob_nodeList) {
                if (lob_element.getAttributeValue(GC_ATTRIBUTE_ID).equals(iva_elementName)) {
                    lob_element.setText(iva_newDirName);
                    lva_hasChanged = true;
                }
            }

            XMLOutputter xmlOutput = new XMLOutputter();

            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(lob_doc, new FileWriter(getXmlFilePath()));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        return lva_hasChanged;
    }

    private static String findSharedDirectoryById(String iva_sharedDirectory) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;

        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;
        List<Element> lob_nodeList;

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();

            lob_selectedElement = lob_rootElement.getChild(GC_SHARED_DIR_ROOT_ELEMENT);
            lob_nodeList = lob_selectedElement.getChildren();

            for (Element lob_element : lob_nodeList) {
                if (lob_element.getAttributeValue(GC_ATTRIBUTE_ID).equals(iva_sharedDirectory)) {
                    return lob_element.getText();
                }
            }

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NOT_FOUND);
    }

    private static String findSharedDirectoryByText(String iva_sharedDirectoryName) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;

        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;
        List<Element> lob_nodeList;

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();

            lob_selectedElement = lob_rootElement.getChild(GC_SHARED_DIR_ROOT_ELEMENT);
            lob_nodeList = lob_selectedElement.getChildren();

            for (Element lob_element : lob_nodeList) {
                if (lob_element.getText().equals(iva_sharedDirectoryName)) {
                    return lob_element.getAttributeValue(GC_ATTRIBUTE_ID);
                }
            }

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NOT_FOUND);
    }

    private static boolean checkIfFileNotExist() {
        return !new File(getXmlFilePath()).exists();
    }

    private static String getXmlFilePath() {
        DataCache lob_dataCache = DataCache.getDataCache();
        return Utils.getUserBasePath() + "\\" + lob_dataCache.get(DataCache.GC_IP_KEY) + "_" +
                lob_dataCache.get(DataCache.GC_PORT_KEY) + "\\" + lob_dataCache.get(DataCache.GC_EMAIL_KEY)
                + "\\config\\" + GC_FILE_NAME;
    }

    private static String readXml(String iva_elementToRead, String iva_fileName) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;

        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;
        String lob_elementValue = "";

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();

            lob_selectedElement = lob_rootElement.getChild(iva_elementToRead);
            lob_elementValue = lob_selectedElement.getText();

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        return lob_elementValue;
    }

    private static void modify(String iva_elementName, String iva_newValue, String iva_fileName) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_elementToModify;

        try {
            lob_inputFile = new File(getXmlFilePath());
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();


            lob_elementToModify = lob_rootElement.getChild(iva_elementName);
            lob_elementToModify.setText(iva_newValue);

            XMLOutputter xmlOutput = new XMLOutputter();

            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(lob_doc, new FileWriter(getXmlFilePath()));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
