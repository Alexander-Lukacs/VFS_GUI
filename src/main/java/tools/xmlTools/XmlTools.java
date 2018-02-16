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

public class XmlTools {
    public static String readXml(String iva_elementToRead, String iva_fileName) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;

        Document lob_doc;
        Element lob_rootElement;
        Element lob_selectedElement;
        String lob_elementValue = "";

        try {
            lob_inputFile = new File(getXmlFilePath(iva_fileName));
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

    public static void modify(String iva_elementName, String iva_newValue, String iva_fileName) {
        File lob_inputFile;
        SAXBuilder lob_saxBuilder;
        Document lob_doc;
        Element lob_rootElement;
        Element lob_elementToModify;

        try {
            lob_inputFile = new File(getXmlFilePath(iva_fileName));
            lob_saxBuilder = new SAXBuilder();
            lob_doc = lob_saxBuilder.build(lob_inputFile);
            lob_rootElement = lob_doc.getRootElement();


            lob_elementToModify = lob_rootElement.getChild(iva_elementName);
            lob_elementToModify.setText(iva_newValue);

            XMLOutputter xmlOutput = new XMLOutputter();

            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(lob_doc, new FileWriter(getXmlFilePath(iva_fileName)));

        } catch (JDOMException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean checkIfFileNotExist(String iva_fileName) {
        return !new File(getXmlFilePath(iva_fileName)).exists();
    }

    public static String getXmlFilePath(String iva_fileName) {
        return Objects.requireNonNull(LastSessionStorage.class.getClassLoader().getResource(iva_fileName)).getPath();
    }
}
