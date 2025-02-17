package classes.mvc;

public class Mapping {
    private String controller;
    private String method;
    private Class[] parameterTypes;
    private String session;
    private boolean restApi;
    private String verb;
    private String url; // URL associée
    private String viewOnError; // Vue à utiliser en cas d'erreur

    // Getter pour URL
    public String getUrl() {
        return url;
    }

    // Setter pour URL
    public void setUrl(String url) {
        this.url = url;
    }

    // Getter pour viewOnError
    public String getViewOnError() {
        return viewOnError;
    }

    // Setter pour viewOnError
    public void setViewOnError(String viewOnError) {
        this.viewOnError = viewOnError;
    }

    // Indique si c'est une API REST
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

    public String getVerb() { // Accesseur pour le verbe HTTP
        return verb;
    }

    public void setVerb(String verb) { // Mutateur pour le verbe HTTP
        this.verb = verb;
    }
}
