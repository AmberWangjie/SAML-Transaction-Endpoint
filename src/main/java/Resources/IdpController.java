package Resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by AmberWang on 6/19/17.
 */
@RestController

@RequestMapping(path = "/api", consumes = "appplication/json")
public class IdpController extends SharedController{
    @Autowired
    public IdpController(ConfigurationResource configuration) {
        super(configuration);
    }

    @PutMapping("/attributes")
    public void setAttributes(@RequestBody Map<String, List<String>> attributes) {
        LOG.debug("Request to replace all attributes {}", attributes);
        configuration().setAttributes(attributes);
    }

    @PutMapping("/attributes/{name:.+}")
    public void setAttribute(@PathVariable String name, @RequestBody List<String> values) {
        LOG.debug("Request to set attribute {} to {}", name, values);
        configuration().getAttributes().put(name, values);
    }

    @DeleteMapping("/attributes/{name:.+}")
    public void removeAttribute(@PathVariable String name) {
        LOG.debug("Request to remove attribute {}", name);
        configuration().getAttributes().remove(name);
    }

    @PutMapping("/User")
    public void addUser(@RequestBody User user) {
        LOG.debug("Request to add User {}", user);
        configuration().getUsers().add(new UsernamePasswordAuthenticationToken(
                user.getName(),
                user.getPassword(),
                user.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(toList())));
    }


// @PutMapping("authmethod")
//    public void setAuthenticationMethod(@RequestBody String authenticationMethod) {
//        LOG.debug("Request to set auth method to {}", authenticationMethod);
//        configuration().setAuthenticationMethod(AuthenticationMethod.valueOf(authenticationMethod));
//    }


    @PutMapping("/acsendpoint")
    public void setAcsEndpoint(@RequestBody String acsEndpoint) {
        LOG.debug("Request to set Assertion Consumer Service Endpoint to {}", acsEndpoint);
        configuration().setAcsEndpoint(acsEndpoint);
    }


    private ConfigurationResource configuration() {
        return ConfigurationResource.class.cast(super.configuration);
    }

 }

