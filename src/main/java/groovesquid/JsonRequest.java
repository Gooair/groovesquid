package groovesquid;

import java.util.HashMap;

/**
 *
 * @author Maino
 */
public class JsonRequest {
    private HashMap<String, Object> header;
    private HashMap<String, Object> parameters;
    private String method;
    
    public JsonRequest(HashMap<String, Object> header, HashMap<String, Object> parameters, String method) {
        this.header = header;
        this.parameters = parameters;
        this.method = method;
    }
}
