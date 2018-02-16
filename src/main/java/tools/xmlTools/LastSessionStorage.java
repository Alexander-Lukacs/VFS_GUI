package tools.xmlTools;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;

import static tools.constants.LastSessionConstants.*;
import static tools.xmlTools.XmlTools.*;

/**
 * Created by Mesut on 09.02.2018.
 */
public class LastSessionStorage {
    private static final String GC_FILE_NAME = "properties.xml";

    public static String getIp() {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        return readXml(GC_IP_ELEMENT_NAME, GC_FILE_NAME);
    }

    public static void setIp(String iva_newIp) {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        modify(GC_IP_ELEMENT_NAME, iva_newIp, GC_FILE_NAME);
    }

    public static String getPort() {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        return readXml(GC_PORT_ELEMENT_NAME, GC_FILE_NAME);
    }

    public static void setPort(String iva_newPort) {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        modify(GC_PORT_ELEMENT_NAME, iva_newPort, GC_FILE_NAME);
    }

    public static String getEmail() {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        return readXml(GC_EMAIL_ELEMENT_NAME, GC_FILE_NAME);
    }

    public static void setEmail(String iva_newEmail) {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        modify(GC_EMAIL_ELEMENT_NAME, iva_newEmail, GC_FILE_NAME);
    }

    public static String getPassword() {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        return readXml(GC_PASSWORD_ELEMENT_NAME, GC_FILE_NAME);
    }

    public static void setPassword(String iva_password) {
        if (checkIfFileNotExist(GC_FILE_NAME)) {
            createXml();
        }

        modify(GC_PASSWORD_ELEMENT_NAME, iva_password, GC_FILE_NAME);
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

            lva_xmlFilePath = getXmlFilePath(GC_FILE_NAME);
            lob_xmlOutput.output(lob_doc, new FileWriter(lva_xmlFilePath));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
