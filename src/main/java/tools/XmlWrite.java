package tools;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static tools.constants.XmlConstants.*;

public class XmlWrite {

    public static void createXml(String iva_ip, String iva_port, String iva_name) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(GC_SERVER_PROPERTIES);
            doc.appendChild(rootElement);

            // server elements
            Element server = doc.createElement(GC_SERVER_NAME);
            rootElement.appendChild(server);

            // ip elements
            Element ip = doc.createElement(GC_SERVER_IP);
            ip.appendChild(doc.createTextNode(iva_ip));
            server.appendChild(ip);

            // port elements
            Element port = doc.createElement(GC_SERVER_PORT);
            port.appendChild(doc.createTextNode(iva_port));
            server.appendChild(port);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\Users\\properties.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            System.out.println(result);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }
}