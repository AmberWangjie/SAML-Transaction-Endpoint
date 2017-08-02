package Resources;

import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.*;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Created by AmberWang on 6/19/17.
 */
public class RequestParser {
    //decode and decompress the SAML request url
    public String xmlInflater(String SAMLRequest) {
        //base64 decode to deflated xml file string

        byte[] decoded = Base64.getDecoder().decode(SAMLRequest.getBytes());
        String inflated = new String(decoded, Charset.forName("UTF-8"));
        //System.out.println(new String(decoded, StandardCharsets.UTF_8));
        //inflate(uncompress) the deflated xml data
      /* Inflater decompresser = new Inflater();
       decompresser.setInput(decoded);
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decoded.length);
       byte[] buffer = new byte[4096];
      // while (!decompresser.finished()) {
       String inflated = "";  */
     /*  try{
          /* int bufferLen = decompresser.inflate(buffer);
           System.out.println("Inflater inflate");
           if(!decompresser.finished()){
               throw new RuntimeException("didn't allocate enough space to hold decompressed data");
           }
         decompresser.end();
         inflated = new String(buffer, 0, bufferLen, "UTF-8");*//*
         while(!decompresser.finished()){
             int count = decompresser.inflate(buffer);
             outputStream.write(buffer, 0, count);
          }
         outputStream.close();
         byte[] output = outputStream.toByteArray();
         System.out.println("output");

         }catch (DataFormatException e){
             Log.debug("dataformat exception");
           }   */
        return inflated;
    }

    public org.w3c.dom.Element StringToElement(String inflated) throws ParserConfigurationException, IOException, SAXException {
        ByteArrayInputStream stream = new ByteArrayInputStream(inflated.getBytes());
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = (org.w3c.dom.Document) docBuilder.parse(stream);
        org.w3c.dom.Element ele = doc.getDocumentElement();
        ele.normalize();
        return ele;
    }

    //TODO: marshal and encode the SAML Response xml object into element and string
    public String xmlToString(SAMLMessageContext messageContext) throws MessageEncodingException, ConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException, MarshallingException {
        SAMLObject samlObject = messageContext.getOutboundSAMLMessage();
        if (samlObject == null) {
            throw new MessageEncodingException("No outbound saml message in messagecontext");
        }
        /*if(messageContext.getRelayState() != null){*/
        /*    SOAPHelper.addHeaderBlock(messageContext, getRelayState(messageContext.getRelayState()));*/
        /*}*/
        org.opensaml.xml.XMLObject responseXmlObject = (org.opensaml.xml.XMLObject) messageContext.getOutboundSAMLMessage();
        System.out.println("XML object done");

        DefaultBootstrap.bootstrap();
        MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller((org.opensaml.xml.XMLObject) responseXmlObject);
        org.w3c.dom.Element element = marshaller.marshall((org.opensaml.xml.XMLObject) responseXmlObject);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS implementationLS = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSSerializer writer = implementationLS.createLSSerializer();
        LSOutput output = implementationLS.createLSOutput();
        output.setByteStream(byteArrayOutputStream);
        writer.write(element, output);
        String responseStr = new String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8"));
        System.out.println("response string done");

        byte[] encoded = Base64.getEncoder().encode(responseStr.getBytes());
        String deflated = new String(encoded, Charset.forName("UTF-8"));
        return deflated;
    }

    public org.opensaml.xml.XMLObject elementToXml(Element element) throws ConfigurationException, UnmarshallingException {
        DefaultBootstrap.bootstrap();
        UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        org.opensaml.xml.XMLObject requestXmlObj = (org.opensaml.xml.XMLObject) unmarshaller.unmarshall(element);
        return requestXmlObj;

    }
}
