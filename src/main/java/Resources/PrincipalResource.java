package Resources;

import lombok.Getter;
import lombok.Setter;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AmberWang on 6/9/17.
 */

    @Getter
    @Setter
    //@EqualsAndHashCode(of = "nameID")
    public class PrincipalResource implements Principal{
        private String serviceProviderEntityID;
        private String requestID;
        private String assertionConsumerServiceURL;
        private String relayState;

        private final List<AttributeResource> attributes = new ArrayList<>();

        private String nameID;
        private String nameIDType;


        public PrincipalResource(String nameID, String nameIDType, List<AttributeResource> attributes) {
            this.nameID = nameID;
            this.nameIDType = nameIDType;
            this.attributes.addAll(attributes);
        }

        public PrincipalResource(String nameID, String nameIDType, List<AttributeResource> attributes, String serviceProviderEntityID, String requestID, String assertionConsumerServiceURL, String relayState) {
            this(nameID, nameIDType, attributes);
            this.serviceProviderEntityID = serviceProviderEntityID;
            this.requestID = requestID;
            this.assertionConsumerServiceURL = assertionConsumerServiceURL;
            this.relayState = relayState;
        }

        @Override
        public String getName() {
            return nameID;
        }

        public String getNameIDType() {
            return nameIDType;
        }


        public String getAssertionConsumerServiceURL() {
            return assertionConsumerServiceURL;
        }

        public String getServiceProviderEntityID() {
            return serviceProviderEntityID;
        }

        public String getRequestID() {
            return requestID;
        }


        public List<AttributeResource> getAttributes() {
        return attributes;
    }

    /*public List<AttributeResource> attributes(String UID){
        return ConfigurationResource.getAttributes().entrySet().stream().map(entry -> {
            return entry.getKey().equals("urn:oasis:names:tc:SAML:2.0:") ? new AttributeResource(entry.getKey(), Collections.singletonList(UID)) : new AttributeResource(entry.getKey(), entry.getValue());
        }).collect(toList());
    }*/
}

