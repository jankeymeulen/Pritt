/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pritt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 *
 * @author jkeymeulen
 */
public class IO {

    public static void writeTextFile(StringBuilder buf, File path, String name) {
        try {
            File file = new File(path.getAbsolutePath() + name);
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                bw.append(buf);
            }

        } catch (IOException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static void writeSingleQAXML(Witness ws, QA qa, File outFile) {
        Document dom;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootE = dom.createElement("text");
            Element bodyE = dom.createElement("body");
            rootE.appendChild(bodyE);
            Element qaE = dom.createElement("div");
            qaE.setAttribute("n", "QA" + qa.getNumber());
            bodyE.appendChild(qaE);

            Element qE = dom.createElement("div");
            qE.setAttribute("n", "Q");
            qE.setTextContent(ws.getTranscriptionTextQ(qa));
            qaE.appendChild(qE);

            Element aE = dom.createElement("div");
            aE.setAttribute("n", "A");
            aE.setTextContent(ws.getTranscriptionTextA(qa));
            qaE.appendChild(aE);

            dom.appendChild(rootE);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(outFile)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

}
