package tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import static tools.constants.XmlConstants.*;

/**
 * Created by Mesut on 09.02.2018.
 */
public class XmlTool {

    public static String[] readFromXml() {

        String[] lob_ipPortEmailPasswordArray = new String[4];

        if (checkIfFileExist()) {

            try {

                File fXmlFile = new File(Utils.getUserBasePath() + "\\properties.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);

                //optional, but recommended
                //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();

                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                NodeList nList = doc.getElementsByTagName("Server");

                System.out.println("----------------------------");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);

                    System.out.println("\nCurrent Element :" + nNode.getNodeName());

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        lob_ipPortEmailPasswordArray[0] = eElement.getElementsByTagName("IP").item(0).getTextContent();
                        lob_ipPortEmailPasswordArray[1] = eElement.getElementsByTagName("Port").item(0).getTextContent();
                        lob_ipPortEmailPasswordArray[2] = eElement.getElementsByTagName("Email").item(0).getTextContent();
                        lob_ipPortEmailPasswordArray[3] = eElement.getElementsByTagName("Password").item(0).getTextContent();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            lob_ipPortEmailPasswordArray[0] = "";
            lob_ipPortEmailPasswordArray[1] = "";
            lob_ipPortEmailPasswordArray[2] = "";
            lob_ipPortEmailPasswordArray[3] = "";
        }
        return lob_ipPortEmailPasswordArray;
    }


    public static void createXml(String iva_ip, String iva_port, String iva_email, String iva_password) {

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

            Element email = doc.createElement(GC_USER_EMAIL);
            email.appendChild(doc.createTextNode(iva_email));
            server.appendChild(email);

            Element password = doc.createElement(GC_USER_PASSWORD);
            password.appendChild(doc.createTextNode(iva_password));
            server.appendChild(password);

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
            StreamResult result = new StreamResult(new File(Utils.getUserBasePath() + "\\properties.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            System.out.println(result);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean checkIfFileExist() {

        System.out.println("checkIfFileExist(): " + Utils.getUserBasePath() + "\\properties.xml");
        File lob_file = new File(Utils.getUserBasePath() + "\\properties.xml");

        return lob_file.exists();
    }
}
