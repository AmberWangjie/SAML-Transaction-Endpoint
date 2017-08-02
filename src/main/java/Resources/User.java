package Resources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by AmberWang on 6/18/17.
 */
@Getter
@ToString
@AllArgsConstructor
public class User implements Serializable{
    private String name;
    private String password;
    private List<String> authorities;
}
