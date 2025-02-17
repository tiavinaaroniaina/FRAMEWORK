package classes.mvc;


public class VerbMethod {
    private final String verb;
    private final String method;

    public VerbMethod(String verb, String method) {
        this.verb = verb;
        this.method = method;
    }

    public String getVerb() {
        return verb;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        return verb.hashCode() + method.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VerbMethod)) return false;
        VerbMethod other = (VerbMethod) obj;
        return verb.equals(other.verb) && method.equals(other.method);
    }
}
