package Resources;

import java.util.List;

/**
 * Created by AmberWang on 6/9/17.
 */
public class AttributeResource {
    private final String name;
    private final List<String> values;

    public AttributeResource(String name, List<String> values){
        this.name = name;
        this.values = values;
    }

    public String getName(){
        return name;
    }

    public List<String> getValues(){
        return values;
    }

    public String getValue(){
        return String.join(",", values);
    }

    @Override
    public String toString(){
        return "SAMLAttribute{" + "name='" + name + '\'' + ", values=" + values + '}';
    }


}
