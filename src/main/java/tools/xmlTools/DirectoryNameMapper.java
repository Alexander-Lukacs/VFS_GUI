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
import java.util.List;

import static tools.constants.DirectoryNameMapperConstants.*;
import static tools.xmlTools.XmlTools.*;

public class DirectoryNameMapper {
    private static final String GC_FILE_NAME = "mapping.xml";

    private static void createXmlFile() {
        Document lob_doc;

        Element lob_rootElement;
        Element lob_publicDirectory;
        Element lob_privateDirectory;
        Element lob_sharedDirectoryRoot;

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

            // shared directory root element
            lob_sharedDirectoryRoot = new Element(GC_SHARED_DIR_ROOT_ELEMENT);


            lob_doc.getRootElement().addContent(lob_privateDirectory);
            lob_doc.getRootElement().addContent(lob_publicDirectory);
            lob_doc.getRootElement().addContent(lob_sharedDirectoryRoot);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lva_xmlFilePath = getXmlFilePath(GC_FILE_NAME);
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    public static String getPrivateDirectoryName() {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXmlFile();
        }

        return readXml(GC_PRIVATE_DIR_ELEMENT, GC_FILE_NAME);
    }

    public static void setPrivateDirectoryName(String iva_dirName) {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXmlFile();
        }

        modify(GC_PRIVATE_DIR_ELEMENT, iva_dirName,GC_FILE_NAME);
    }

    public static String getPublicDirectoryName() {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXmlFile();
        }

        return readXml(GC_PUBLIC_DIR_ELEMENT, GC_FILE_NAME);
    }

    public static void setPublicDirectoryName(String iva_dirName) {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXmlFile();
        }

        modify(GC_PUBLIC_DIR_ELEMENT, iva_dirName, GC_FILE_NAME);
    }

    public static String getRenamedSharedDirectoryName(int iva_originalDirId) {
        return findSharedDirectory(String.valueOf(iva_originalDirId));
    }

    public static void setNameOfSharedDirectory(int iva_sharedDirOriginalId, String iva_newSharedDirName) {
        if (!modifySharedDirectoryName(String.valueOf(iva_sharedDirOriginalId), iva_newSharedDirName)) {
            throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NOT_FOUND);
        }
    }

    public static void addNewSharedDirectory(int iva_sharedDirectoryId, String iva_sharedDirectoryName) {
        addSharedDirectoryElement(String.valueOf(iva_sharedDirectoryId), iva_sharedDirectoryName);
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


        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXmlFile();
        }

        try {
            lob_inputFile = new File(getXmlFilePath(GC_FILE_NAME));
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);

            lob_rootElement = lob_doc.getRootElement();

            lob_selectedElement = lob_rootElement.getChild(GC_SHARED_DIR_ROOT_ELEMENT);
            lob_selectedElement.addContent(lob_newSharedDirectory);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lva_xmlFilePath = getXmlFilePath(GC_FILE_NAME);
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

        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXmlFile();
        }

        try {
            lob_inputFile = new File(getXmlFilePath(GC_FILE_NAME));
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
            xmlOutput.output(lob_doc, new FileWriter(getXmlFilePath(GC_FILE_NAME)));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }

        return lva_hasChanged;
    }

    private static String findSharedDirectory(String iva_sharedDirectory) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;

        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;
        List<Element> lob_nodeList;

        try {
            lob_inputFile = new File(getXmlFilePath(GC_FILE_NAME));
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
}
