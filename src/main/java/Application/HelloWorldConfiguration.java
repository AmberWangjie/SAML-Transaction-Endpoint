package Application; /**
 * Created by AmberWang on 5/26/17.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class HelloWorldConfiguration extends Configuration {

    @NotEmpty
    String companyName;

    @JsonProperty
    public String getCompanyName(){
        return companyName;
    }
}
