package Resources;

import Application.HelloWorldConfiguration;
import api.Saying;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.AuthnRequestImpl;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.signature.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static Resources.BuildResource.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Created by AmberWang on 5/26/17.
 */
@Path("/saml")
public class SAMLHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SAMLHandler.class);

    private HelloWorldConfiguration configuration;

    public SAMLHandler(HelloWorldConfiguration configuration) {
        this.configuration = configuration;
        //finalkeyManager = new KeyManager();
    }

    public void receiveHello(@Valid Saying saying) {
        LOGGER.info("Received a saying: {}", saying);
    }

    private ConfigurationResource configurationResource;
    private KeyManager finalkeyManager;

    final private String idpPrivateKey = "MIIEwgIBADANBgkqhkiG9w0BAQEFAASCBKwwggSoAgEAAoIBAgCl4L8tvFkyK9YyAMAPtdZ6mKlbS/6O67g7WMn/9HbRkiC/YA3Vp74gof4RcruEu0zp5e3WcbtFlglV/Ngm1LWHePYiqERdtkX3wWnKjBWUGUPlJp8qF+3bzXwNVqmkgfVgB/pfdVNVLVQZIOHjziOeNQxEaTlztERhZsHmlaz+bj83p2Q15pA/d9dXXlnMJSPznD14wiy3I8OUEjoWBf8AzZh/TaIPdH4YqPgOe4MtFiCLbvo8WyrnNQghyjrMyX+veOp4CPmtvON+gJggv0248f+xMLgv0zExrcxEuEXs7oezCYkuhgFuY1K61oeC/WkwR9QCSJDxRUZbElLxLdbLKQIDAQABAoIBAgCJ8pxLBsYO6VHah7Ex34Ect3SwTkTGytWN1Us4Jy/hHWtKfDFFK5j8PnuyOv4jZjiSk/r1SeP5/MqKzhlcQa0tmrTnIZSpcnpBjoHSRilz3ocOPM5FDV1sXDj8RsQR4VoUoM3hMQs6XNYBh80TzEzmtysagX+b76Cmd/RtNLCHb0aCeCnwvnR+tgOrwrKyxbn+ea1AAdlubES3j3PKk5WsLGosqXwZwn8kdIiJl2OwMO23m76lv9/HZfAO2oBW0G/P6l7yDF9oIsHGCZ//Tirv4Znmy+98pHz29cJE2I2PUFTxuh0hunbWPHeq1tL0slki2tfwJWW0WRk+ppOG90hbeQKBgQ2lJpB/YSvxcISlko9WY6qEV6DGpjSo28cLMF6U07rz/7PRLGgLk+irsU6oFj3ITO+swDPZrdLTKF2/QX7EUxxjF07/5Xv3EsRgY5y3vEb/AogvOJPpK2Yn7P90qK4OXzwRlpjcASxiJIsP6v2dbZsNxgM3CgbV7XwgE6WR90volwKBgQwoFUflQ7YkI0ACty2NTg3hS7ch4pUuWq9TMaK3hpFYoilwuMnL0c/Vgc60HR40CWjk4ECzHeXIeZCrXUWFCN4w5KRrgbeKoiV23zmJ6zOaGJrWVFzdF+7VGHN7HXnT126RNf6vzgAngD0lD320kxM97cAryB4VrKsDzhcAfO+iPwKBgQUoaWRzkgKvWE373GOsTy5Uql8O2CLNtY4Oz7Dg5wwjUYPiTLvjrHxl8jb+WLHv9g0fKuQuo4Mu6HJPam2FvrjbSCNHnbJ3WbO7j8vctCHOIWHjH6rhl9Mcwnxp1507t4oYR+nuN89UJeYfFT/a82SKF1B9zJ963DNwWJzciTM7bwKBgQdGZZXlCiSSc9X2yhCko7WCwoER8js8xmBt9SJfmlSZKlKKdsutAh7SsyA0gch1c7wmXF3b02SUGMwPAX59ItV1hR8whyyD9o1dGjGg6/Sxn0/qPApm9c+kD0Hsx7MEp8QvepS0OR+2g/QTnbWovuuhAAslKFl4tyCQ4mC5lgsCmQKBgQmv/U7nlmM5RnhDlS6KAgrB3hjx1QRJbfafk/plRc2Ao0g/HUKRRFU6B1iYci6uoDj1Q2Aiy7qF3DZr5AimhtVCiiqyF4uDTajiX7YqNLtPufNSISZseS2IWjoBxGHwi9aCcPOkh6iwFwiJ6Z3ckqWcI7ZQ80bIEnQEdSFymm7A6w==";
    final private String idpCertificate = "MIIDiTCCAnCgAwIBAgIBADANBgkqhkiG9w0BAQ0FADBeMQswCQYDVQQGEwJ1czEXMBUGA1UECAwOTm9ydGggQ2Fyb2xpbmExGDAWBgNVBAoMD0R1a2UgVW5pdmVyc2l0eTEcMBoGA1UEAwwTbG9jYWxob3N0OjkwMTAvc2FtbDAeFw0xNzA2MTUwMjU1MTFaFw0xODA2MTUwMjU1MTFaMF4xCzAJBgNVBAYTAnVzMRcwFQYDVQQIDA5Ob3J0aCBDYXJvbGluYTEYMBYGA1UECgwPRHVrZSBVbml2ZXJzaXR5MRwwGgYDVQQDDBNsb2NhbGhvc3Q6OTAxMC9zYW1sMIIBIzANBgkqhkiG9w0BAQEFAAOCARAAMIIBCwKCAQIApeC/LbxZMivWMgDAD7XWepipW0v+juu4O1jJ//R20ZIgv2AN1ae+IKH+EXK7hLtM6eXt1nG7RZYJVfzYJtS1h3j2IqhEXbZF98FpyowVlBlD5SafKhft2818DVappIH1YAf6X3VTVS1UGSDh484jnjUMRGk5c7REYWbB5pWs/m4/N6dkNeaQP3fXV15ZzCUj85w9eMIstyPDlBI6FgX/AM2Yf02iD3R+GKj4DnuDLRYgi276PFsq5zUIIco6zMl/r3jqeAj5rbzjfoCYIL9NuPH/sTC4L9MxMa3MRLhF7O6HswmJLoYBbmNSutaHgv1pMEfUAkiQ8UVGWxJS8S3WyykCAwEAAaNQME4wHQYDVR0OBBYEFDhN/JdXSEQLjvv1KGTn2MxrmimFMB8GA1UdIwQYMBaAFDhN/JdXSEQLjvv1KGTn2MxrmimFMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQENBQADggECAD5zUB4jlbi0YsjswYlHKXI8HXbgyMAmAST1rJZD+A5QtTEutVPNdEtPjAfiHWrPoxWtoUw/phA1dR1G/pD4tI1HN+2/I7RTM0jDVB+y5AZkBjbgRDE/2vTWNLRnMDB7jpJIhxyOEuDCACqyx+tK8Psf2//fdbS3mSWMwgBZQW/LAaUNenCp4GOuFvpQPsLcX0Ss1x60OpiMNGg0SdB0ZC2NggCax+9or4Qq3ZXLncwFkwwz/JCSoKv8Tpq2ySDLe6f+r8B3G/JuSb1dDBKlGbP5dtt8B/NgmmgcO9kxWkrDdlcpVOWqpGIQe+tHikux/eFYcWFIvxL13UMGSoccf/H0";
    final private String idpPassphrase = "Password";

    final private String username = "amber.wang";

    private Credential resolveCredential(String entityId) throws SecurityException, SecurityException {
        try {
            return finalkeyManager.resolveSingle(new CriteriaSet(new EntityIDCriteria(entityId)));
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

     public class requestInfo{
         private String assertionConsumerServiceURL;
         private String providerName ;
         private String protocolBinding;
         private String requestIssuer;
         private String id;
         private String entityID;
         NameIDPolicy nameIDPolicy;
         org.opensaml.saml2.core.RequestedAuthnContext requestedAuthnContext;

         public requestInfo() {
             assertionConsumerServiceURL = null;
             providerName = null;
             protocolBinding = null;
             requestIssuer = null;
             id = null;
             entityID = null;
             nameIDPolicy = null;
             requestedAuthnContext = null;
         }

         public void extractInfo(requestInfo info, Object requestXmlObj){
              info.assertionConsumerServiceURL = ((AuthnRequestImpl) requestXmlObj).getAssertionConsumerServiceURL();
              info.protocolBinding = ((AuthnRequestImpl) requestXmlObj).getProtocolBinding();
              info.providerName = ((AuthnRequestImpl) requestXmlObj).getProviderName();
              info.requestIssuer = String.valueOf(((AuthnRequestImpl) requestXmlObj).getIssuer().getValue()); //changed Issuer to String
              info.id = ((AuthnRequestImpl) requestXmlObj).getID();
              info.entityID = ((AuthnRequestImpl) requestXmlObj).getDestination();
              info.nameIDPolicy = ((AuthnRequestImpl) requestXmlObj).getNameIDPolicy();
              info.requestedAuthnContext = ((AuthnRequestImpl) requestXmlObj).getRequestedAuthnContext();
         }
     }


     private List<AttributeResource> attributes(String uid) {
         return configurationResource.getAttributes().entrySet().stream()
           .map(entry ->  entry.getKey().equals("urn:oasis:names:tc:SAML:2.0:attribute-def:uid") ?
             new AttributeResource(entry.getKey(), singletonList(uid)) :
             new AttributeResource(entry.getKey(), entry.getValue()))
           .collect(toList());
       }


    public Assertion securityConfig(requestInfo info, String EntityId, String relayState, Status status, Credential signCredential, JKSKeyManager keyManager) throws NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException, SecurityException, org.opensaml.core.xml.io.MarshallingException, SignatureException, MarshallingException {
        //TODO: get all the attributes and signature from requestXmlObj, config the security/principal object, construct the assertion and other attributes
        //principal instance
        String defaultEntityId = info.entityID;

       // Credential signCredential = resolveCredential(info.entityID);
        configurationResource = new ConfigurationResource(keyManager, defaultEntityId, idpPrivateKey, idpCertificate);
        configurationResource.setAcsEndpoint(info.assertionConsumerServiceURL);
        configurationResource.reset();

        PrincipalResource principalResource = new PrincipalResource(
               // info.providerName, //authentication.getName(),
                username,
                NameIDType.UNSPECIFIED,
                attributes(username),  //attributes(authentication.getName())
                info.requestIssuer,//authnRequest.getIssuer().getValue,
                info.id, //authnRequest.getID(),
                info.assertionConsumerServiceURL, //assertionConsumerServiceURL,
                relayState//messageContext.getRelayState();
        );


        Assertion assertion = buildAssertion(principalResource, status, info.entityID); //entityID
        //use this when need to sign the request
        //TODO: Modify the signAssertion, add <Signature:KeyInfo><x509Cert> token  //class:XMLSignature  method:addKeyInfo
        signAssertion(assertion, signCredential);
        return assertion;
    }

     public SAMLMessageContext buildMessageContext(requestInfo info, Credential signCredential, HttpServletResponse servletResponse, Assertion assertion, Status status, String relayState) {
          org.opensaml.saml2.core.Response response = buildSAMLObject(org.opensaml.saml2.core.Response.class, org.opensaml.saml2.core.Response.DEFAULT_ELEMENT_NAME);
          Issuer issuer = buildIssuer(info.entityID);//entityID

          response.setIssuer(issuer);
          response.setID(randomSAMLId());
          response.setIssueInstant(new DateTime());
          response.setInResponseTo(info.id);


          response.getAssertions().add(assertion);
          response.setDestination(info.assertionConsumerServiceURL);
          response.setStatus(status);
          //public SAMLMessageContext buildMessageContext(requestInfo info, Credential signCredential)
          Endpoint endpoint = buildSAMLObject(Endpoint.class, SingleSignOnService.DEFAULT_ELEMENT_NAME);
          endpoint.setLocation(info.assertionConsumerServiceURL);
          HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(servletResponse, false);
          SAMLMessageContext messageContext = new BasicSAMLMessageContext();

          messageContext.setOutboundMessageTransport(outTransport);
          messageContext.setPeerEntityEndpoint(endpoint);
          messageContext.setOutboundSAMLMessage(response);
          messageContext.setOutboundSAMLMessageSigningCredential((org.opensaml.xml.security.credential.Credential) signCredential);
          messageContext.setOutboundMessageIssuer(info.id);
          messageContext.setRelayState(relayState);
          return messageContext;
     }

    public javax.ws.rs.core.Response buildResponseURI(String param, String acs, String relayState) throws URISyntaxException {
         Client client = ClientBuilder.newClient();
         if(acs == null){
             acs = "http://www.baidu.com";
         }
            URIBuilder uriBuilder = new URIBuilder(acs);

            if(param != null){

                uriBuilder.addParameter("SAMLResponse", param);
            }

            if(relayState != null){
                uriBuilder.addParameter("RelayState", relayState);
            }

            WebTarget target = client.target(uriBuilder.build());

         Response responseReturn = target.request().post(null);
         return responseReturn;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public  javax.ws.rs.core.Response SAMLResponseHandler(
            @QueryParam("SAMLRequest") String SAMLRequest,
            @QueryParam("RelayState") String relayState,
            @Context HttpServletRequest servletRequest,
            @Context HttpServletResponse servletResponse
    ) throws IOException, URISyntaxException, MarshallingException, NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, KeyStoreException, MessageEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException, ParserConfigurationException, SAXException, UnmarshallingException, org.opensaml.core.xml.io.MarshallingException, SignatureException, SecurityException, JSONException {

        //TODO: get request uri, decode & decompress to a plain text string
        String reqUrl = servletRequest.getRequestURI();
        RequestParser requestParser = new RequestParser();
        String inflated = null;
        String deflated = null;

        requestInfo info = new requestInfo();
        if(SAMLRequest != null){
            inflated = requestParser.xmlInflater(SAMLRequest);


        //TODO: parse the xml string into document object, extract the assertion info

           // requestInfo info = new requestInfo();
            org.w3c.dom.Element ele = requestParser.StringToElement(inflated);
            XMLObject requestXmlObj = (XMLObject) requestParser.elementToXml(ele);
            //info -> object containing information about sp in SAMLrequest
            info.extractInfo(info, requestXmlObj);
            //System.out.println("get information");


        //TODO: get all the attributes and signature from requestXmlObj, config the security/principal object, construct the assertion and other attributes
            BuildResource buildResource = new BuildResource();
            JKSKeyManager keyManager = buildResource.buildkeyManager(info.entityID, idpPrivateKey, idpCertificate, idpPassphrase);
            finalkeyManager = keyManager;

            Status status = buildStatus(StatusCode.SUCCESS_URI);
            Credential signCredential = resolveCredential(info.entityID);
            Assertion assertion = securityConfig(info, info.entityID, relayState, status, signCredential, keyManager);

            SAMLMessageContext messageContext = buildMessageContext(info, signCredential, servletResponse, assertion, status, relayState);

        //TODO: marshal and encode the SAML Response xml object into element and string
            deflated = requestParser.xmlToString(messageContext);
            System.out.println("response string done");
        }


        //TODO: build the URI to the sp assertion target with the response parameter and the optional relayState parameter(redirect to where after sp validating the response)
       // String returnTo = "http://www.baidu.com"; //can be set to null, no redirect, only showing whether authenticated and user attributes
        String returnTo = relayState;//has to be set to null if choose attrs.jsp, otherwise 404; if use the parameter from request url, register the complete domain name as parameter in the login function of sp program
        //String returnTo = null;
        //String casTarget = "localhost:8080/cas/samlValidate";
        Response responseReturn = buildResponseURI(deflated, info.assertionConsumerServiceURL, returnTo);
       //Response responseReturn = buildResponseURI(deflated, casTarget, returnTo);
        System.out.println("post target done");
        return responseReturn;
    }


}