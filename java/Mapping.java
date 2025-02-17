package classes.mvc;

public class Mapping {
    private String controller;
    private String method;
    private Class[] parameterTypes;
    private String session;
    private boolean restApi;
    private String verb;
    private String url; // New field for URL

    // Getter for URL
    public String getUrl() {
        return url;
    }

    // Setter for URL
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRestApi() {
        return restApi;
    }

    public void setRestApi(boolean restApi) {
        this.restApi = restApi;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getVerb() { // Accessor for the verb
        return verb;
    }

    public void setVerb(String verb) { // Mutator for the verb
        this.verb = verb;
    }
}
