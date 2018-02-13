package tools;

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

import static tools.constants.XmlConstants.*;

/**
 * Created by Mesut on 09.02.2018.
 */
public class XmlTools {
    public static String getIp() {
        return readXml(GC_IP_ELEMENT_NAME);
    }

    public static void setIp(String iva_newIp) {
        modify(GC_IP_ELEMENT_NAME, iva_newIp);
    }

    public static String getPort() {
        return readXml(GC_PORT_ELEMENT_NAME);
    }

    public static void setPort(String iva_newPort) {
        modify(GC_PORT_ELEMENT_NAME, iva_newPort);
    }

    public static String getEmail() {
        return readXml(GC_EMAIL_ELEMENT_NAME);
    }

    public static void setEmail(String iva_newEmail) {
        modify(GC_EMAIL_ELEMENT_NAME, iva_newEmail);
    }

    public static String getPassword() {
        return readXml(GC_PASSWORD_ELEMENT_NAME);
    }

    public static void setPassword(String iva_password) {
        modify(GC_PASSWORD_ELEMENT_NAME, iva_password);
    }

    private static void modify(String iva_elementName, String iva_newValue) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_elementToModify;

        if (checkIfFileNotExist()) {
            createXml();
        }

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

    private static String readXml(String iva_elementToRead) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;

        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;
        String lob_elementValue = "";

        if (checkIfFileNotExist()) {
            createXml();
        }

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

    private static void createXml() {
        Document lob_doc;

        Element lob_rootElement;
        Element lob_ipElement;
        Element lob_portElement;
        Element lob_emailElement;
        Element lob_passwordElement;

        XMLOutputter lob_xmlOutput;
        String lva_xmlFilePath;

        try {
            // root element
            lob_rootElement = new Element(GC_ROOT_ELEMENT_NAME);
            lob_doc = new Document(lob_rootElement);

            // ip element
            lob_ipElement = new Element(GC_IP_ELEMENT_NAME);

            // port element
            lob_portElement = new Element(GC_PORT_ELEMENT_NAME);

            // email element
            lob_emailElement = new Element(GC_EMAIL_ELEMENT_NAME);

            // password element
            lob_passwordElement = new Element(GC_PASSWORD_ELEMENT_NAME);

            lob_doc.getRootElement().addContent(lob_ipElement);
            lob_doc.getRootElement().addContent(lob_portElement);
            lob_doc.getRootElement().addContent(lob_emailElement);
            lob_doc.getRootElement().addContent(lob_passwordElement);

            lob_xmlOutput = new XMLOutputter();
            lob_xmlOutput.setFormat(Format.getPrettyFormat());

            lva_xmlFilePath = getXmlFilePath();
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean checkIfFileNotExist() {
        return !new File(getXmlFilePath()).exists();
    }

    private static String getXmlFilePath() {
        return Objects.requireNonNull(XmlTools.class.getClassLoader().getResource("properties.xml")).getPath();
    }
}
