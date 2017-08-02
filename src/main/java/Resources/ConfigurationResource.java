package Resources;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by AmberWang on 6/11/17.
 */
@Getter
@Setter
@Component
public class ConfigurationResource extends SharedConfigResource{

    private String defaultEntityId = ""; //"http://localhost:9010/saml";
    private Map<String, List<String>> attributes = new TreeMap<>();
    private List<UsernamePasswordAuthenticationToken> users = new ArrayList<>();
    //private String acsEndpoint = "http://localhost:8080/request/acs.jsp";
    private String acsEndpoint;
    private final String idpPrivateKey;
    private final String idpCertificate;
   // private AuthenticationMethod authenticationMethod;
    //private AuthenticationMethod defaultAuthenticationMethod;
    //private final String idpPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKIuAF6uluGzM91SB4XR7hih6Xfw7zzO3fFKmhzF80bBbfe/I5VrPwIvCgv95lUihcxbe1f+DFxCb1iLoJoR8hUPGSL4YX1uB+oBgJJZ0eRxEAtAoqYWlj+hV+aO4zH3vZcounvfxPgxHHXQoUfv6NoXCzywbse1bQ7zmGH38HnrAgMBAAECgYAFSm7kp9lwP4Jfo/9u1CxqTU3qhceoOi9vYbmpWqPXGRRXUrr8cuU5UwOG5J+RA3FN+2YIttb8Y4ZyYwu4OwGktO6EqvpY+l147L6DybYhQTgGuw05fNF9rNBmb1W1aq9uZE42d1RlF8KsoVtWsRpJEh9ANb68r5czFo1L4f4oAQJBANHI+Axem0UQjcXBP2oO+mwZvCaZSCFZcd1XQK3snG0jLOoayL8m+iGGuxBlzSXHoI/dl4Y1d2d8+VDGUwxM6+sCQQDF6EhwFJwlzJQ+sCxak9xyfSPd7vXH2/8PqwFj8IHbcdCIT2H/8f96qJnwXuqSdE1bE4ClM4PAqR5Jcyx2DSoBAkEAyNna8yzZCATTV9SvfEGe7USuaV40OwnCBKL3IA37rloRNIo0TR9qOBMgopB1G4jHZzZHTo1+Jqz2nqli9dHnxQJADI2gUqOsB+XDHXrVRWWNnSF87e3jXysAMcE6FFnEEuRUQuEuKSrzdpdNNGcA4AtKgNVq2o+68rSHfEc28cICAQJAMDmaEvPUYaB/g0QzR0+uX2FeFcfvaWNQ1QKiE+uzgtWbX8iq/axbezhtUDLBg46rbSevD3pEDx6vquJKtyWCmA==";
    //private final String idpCertificate = "MIIDHDCCAgSgAwIBAgIUEgYXgB4CjCn+kcGdhno1NG/3Tr4wDQYJKoZIhvcNAQELBQAwGjEYMBYGA1UEAwwPY2FzLmV4YW1wbGUub3JnMB4XDTE3MDUyMzEzMzIwOFoXDTM3MDUyMzEzMzIwOFowGjEYMBYGA1UEAwwPY2FzLmV4YW1wbGUub3JnMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlxZy2Wy3iIznoFKyCS/MVZ/SI+4z+Q4pNP+Xy13JcCvRbXZva4zF2DLdQvkeRabCViemZMkbwmY5fzhm7Un+A67ordKeuwksjZjU4avF02Yv8h6xKXxv/hsPOC1gs3VZm5KVLZw09ySZj2EGWsxCpYlaSC7ZIu08hNxEvG5zfLf/Hy6dm77z9BsgqzsQSXtR+bmfWWZoNRoxxOcWvy0NRRBbzwbi8hHC2AppNcYOnmxb7w7gb34cZL4LQ/Ybt7/lzPQlTa876yeHnaioUP5R9+GKiJXr4oxKAuH59EzwbPIcoIqyYlvniG9QoT2+ZuskbfDGOoy6XzKP4VXK228tTQIDAQABo1owWDAdBgNVHQ4EFgQUFIg9FSwPlJjGZdpENeGpIcBLl2QwNwYDVR0RBDAwLoIPY2FzLmV4YW1wbGUub3JnhhtjYXMuZXhhbXBsZS5vcmdpZHAvbWV0YWRhdGEwDQYJKoZIhvcNAQELBQADggEBAAEYFG4HZyuGtHrr5VdjgbrUIujyEJl+3Cb4JEfA4Wy6QnNj8or7CFwIWGw+ZNlzuXiCXdTijHWhOknfpL7CU/ssju09Rx4t3o498F1PsHG9vQ2zMOPYbp7E+YF12ikbJDem7vNnBcUhwMdc2jsDimFP7jQPJFVcZZUrLOFPY9+gj1SavaFNBjFaIPW+6uQgHkj9WZuLOasI66Ka2v4GxWZhvI5eGASwezq2hJX27CDma8/8fOffxFOgx7QI7F5qEr22IVqoVRxDYHMorfghC4X32GPj+YYjw4CNko7I6ked/9rcxsBQwGZDW8kWuA3yEx7IfBf5fw/4W6709oRBWCA=";

    @Autowired
    public ConfigurationResource(JKSKeyManager keyManager, String defaultEntityId, String idpPrivateKey, String idpCertificate){//@Value("${idp.entity_id}") String defaultEntityId, @Value("${idp.private_key}") String idpPrivateKey, @Value("${idp.certificate}") String idpCertificate, @Value("${idp.auth_method}") String authMethod){
        super(keyManager);
        this.defaultEntityId = defaultEntityId;
        this.idpPrivateKey = idpPrivateKey;
        this.idpCertificate = idpCertificate;
        //this.defaultAuthenticationMethod = AuthenticationMethod.valueOf(authMethod);

    }


    public void reset(){
        setEntityId(defaultEntityId);
        resetAttributes();
        resetKeyStore(defaultEntityId, idpPrivateKey, idpCertificate);
        //resetUsers();
        setAcsEndpoint(null);
        //setAuthenticationMethod(this.defaultAuthenticationMethod);
        setSignatureAlgorithm(getDefaultSignatureAlgorithm());
    }

    private void resetUsers(){
        users.clear();
        users.addAll(Arrays.asList(
                new UsernamePasswordAuthenticationToken("admin", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))),
                new UsernamePasswordAuthenticationToken("User", "secret", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))));
    }

    private void resetAttributes(){
        attributes.clear();
        putAttribute("urn:oasis:names:tc:SAML:2.0:attribute-def:uid", "amber.wang");
        putAttribute("urn:oasis:names:tc:SAML:2.0:attribute-def:cn", "Amber");
        putAttribute("urn:oasis:names:tc:SAML:2.0:attribute-def:sn", "Wang");
        putAttribute("urn:oasis:names:tc:SAML:2.0:attribute-def:mail", "jiewang@yufuid.com");
        putAttribute("urn:oasis:names:tc:SAML:2.0:attribute-def:organization", "yufuid.com");

    }

    private void putAttribute(String key, String... values){
        this.attributes.put(key, Arrays.asList(values));
    }


}
