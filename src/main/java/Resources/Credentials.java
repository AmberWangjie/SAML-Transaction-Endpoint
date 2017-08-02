package Resources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by AmberWang on 6/18/17.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Credentials implements Serializable{

    private String certificate;
    private String key;
}
