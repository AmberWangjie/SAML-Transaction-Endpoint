package Resources;

import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.saml.key.JKSKeyManager;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by AmberWang on 6/9/17.
 */
public class BuildResource {
    private static final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

    @SuppressWarnings({"unused","unchecked"})
    public static <T> T buildSAMLObject(final Class<T> objectClass, QName qName){
        return (T)builderFactory.getBuilder(qName).buildObject(qName);
    }

    private static Subject buildSubject(String subjectNameID, String subjectNameIDType, String recipient, String inResponseTo){
        NameID nameID = buildSAMLObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(subjectNameID);
        nameID.setFormat(subjectNameIDType);

        Subject subject = buildSAMLObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID(nameID);

        SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);

        SubjectConfirmationData subjectConfirmationData = buildSAMLObject(SubjectConfirmationData.class, SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        subjectConfirmationData.setRecipient(recipient);
        subjectConfirmationData.setInResponseTo(inResponseTo);
        subjectConfirmationData.setNotOnOrAfter(new DateTime().plusMinutes(1000)); //how long should set?
        subjectConfirmationData.setAddress(recipient);

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        subject.getSubjectConfirmations().add(subjectConfirmation);

        return subject;
    }


    public static Issuer buildIssuer(String issueEntityName){
        Issuer issuer = buildSAMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issueEntityName);
        issuer.setFormat(NameIDType.ENTITY);
        return issuer;
    }

    public static Status buildStatus(String value){
        Status status = buildSAMLObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
        if(status == null){
            return null;
        }
        StatusCode statusCode = buildSAMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(value);
        status.setStatusCode(statusCode);
        return status;
    }

    public static Status buildStatus(String value, String subStatus, String message){
        Status status = buildStatus(value);

        StatusCode subStatusCode = buildSAMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        subStatusCode.setValue(subStatus);
        status.getStatusCode().setStatusCode(subStatusCode);

        StatusMessage statusMessage = buildSAMLObject(StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);
        statusMessage.setMessage(message);
        status.setStatusMessage(statusMessage);

        return status;
    }

    public static String randomSAMLId(){
        return "_" + UUID.randomUUID().toString();
    }

    private static AuthnStatement buildAuthnStatement(DateTime authnInstant, String entityID){
        AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);

        AuthenticatingAuthority authenticatingAuthority = buildSAMLObject(AuthenticatingAuthority.class, AuthenticatingAuthority.DEFAULT_ELEMENT_NAME);
        authenticatingAuthority.setURI(entityID);

        AuthnContext authnContext = buildSAMLObject(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnContext.getAuthenticatingAuthorities().add(authenticatingAuthority);

        AuthnStatement authnStatement = buildSAMLObject(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(authnInstant);

        return authnStatement;
    }

    private static Attribute buildAttribute(String name, List<String> values){
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);

        Attribute attribute = buildSAMLObject(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(name);
        attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified");

        List<XSString> xsStringList = values.stream().map(value -> {
            XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            stringValue.setValue(value);
            return stringValue;
        }).collect(Collectors.toList());

        attribute.getAttributeValues().addAll(xsStringList);

        return attribute;
    }
    private static AttributeStatement buildAttributeStatement(List<AttributeResource> attributes){
        AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);

        attributes.forEach(entry->attributeStatement.getAttributes().add((buildAttribute(entry.getName(), entry.getValues()))));

        return attributeStatement;
    }


    public static Assertion buildAssertion(PrincipalResource principalAttributes, Status status, String entityID){
        Assertion assertion = buildSAMLObject(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        if(status.getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)){
            Subject subject = buildSubject(principalAttributes.getName(), principalAttributes.getNameIDType(), principalAttributes.getAssertionConsumerServiceURL(), principalAttributes.getRequestID());
            assertion.setSubject(subject);
        }
        Issuer issuer = buildIssuer(entityID);

        Audience audience = buildSAMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(principalAttributes.getServiceProviderEntityID());//org.opensaml.saml2.core.impl.IssuerImpl@33da40d
        AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class, AudienceRestriction.DEFAULT_ELEMENT_NAME);
        audienceRestriction.getAudiences().add(audience);

        Conditions conditions = buildSAMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(conditions);

        AuthnStatement authnStatement = buildAuthnStatement(new DateTime(), entityID);

        assertion.setIssuer(issuer);
        assertion.getAuthnStatements().add(authnStatement);
        assertion.getAttributeStatements().add(buildAttributeStatement(principalAttributes.getAttributes()));
        assertion.setID(randomSAMLId());
        assertion.setIssueInstant(new DateTime());

        return assertion;
    }


    public static void signAssertion(SignableXMLObject signableXMLObject, Credential signingCredential) throws MarshallingException, SignatureException, org.opensaml.xml.io.MarshallingException, SecurityException, CertificateEncodingException {
        Signature signature = buildSAMLObject(Signature.class, Signature.DEFAULT_ELEMENT_NAME);


        signature.setSigningCredential(signingCredential);
        //KeyInfo keyInfo = new KeyInfo()
//        KeyInfoGenerator keyInfoGenerator = credential -> null;
//        KeyInfo keyInfo = keyInfoGenerator.generate(signingCredential);
//        signature.setKeyInfo(keyInfo);
       // try{
           KeyInfo keyInfo = (KeyInfo)buildSAMLObject(KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME);
           X509Data data = (X509Data)buildSAMLObject(X509Data.class,X509Data.DEFAULT_ELEMENT_NAME);
           org.opensaml.xml.signature.X509Certificate certificate = (org.opensaml.xml.signature.X509Certificate) buildSAMLObject(org.opensaml.xml.signature.X509Certificate.class, org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
           String value = Base64.encode(((BasicX509Credential) signingCredential).getEntityCertificate().getEncoded());
           certificate.setValue(value);
           data.getX509Certificates().add(certificate);
           keyInfo.getX509Datas().add(data);
           signature.setKeyInfo(keyInfo);
       // } catch (CertificateEncodingException e){

           // throw new EntitlementProxyException("Error getting the certificate");
        //}
        signature.setSignatureAlgorithm(Configuration.getGlobalSecurityConfiguration().getSignatureAlgorithmURI(signingCredential));
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        //((BasicX509Credential) signingCredential).getEntityCertificate().getSignature().length == 256
        //signature.getSigningCredential().getPublicKey().getEncoded().length
        signableXMLObject.setSignature(signature);

        Configuration.getMarshallerFactory().getMarshaller(signableXMLObject).marshall(signableXMLObject);
        Signer.signObject(signature);
    }


    @Autowired
    @Bean
    public JKSKeyManager buildkeyManager(String idpEntityId, String idpPrivateKey, String idpCertificate, String idpPassphrase) throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore keyStore = KeyStoreLocator.createKeyStore(idpPassphrase);
        KeyStoreLocator.addPrivateKey(keyStore, idpEntityId, idpPrivateKey, idpCertificate, idpPassphrase);
        JKSKeyManager jksKeyManager = new JKSKeyManager(keyStore, Collections.singletonMap(idpEntityId, idpPassphrase), idpEntityId);
        return jksKeyManager;
    }


}
