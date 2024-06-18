package classes.mvc;

import java.util.HashMap;

public class ModelView {
    private String viewUrl;
    private HashMap<String, Object> viewData;

    public ModelView(String viewUrl) {
        this.setUrl(viewUrl);
        this.viewData = new HashMap<String,Object>();
    }

    public String getUrl() {
        return this.viewUrl;
    }

    public void setUrl(String viewUrl) {
        this.viewUrl = viewUrl; 
    }

    public HashMap<String, Object> getData() {
        return this.viewData;
    }

    public void setData(HashMap<String, Object> viewData) {
        this.viewData = viewData;
    }
    public void add(String key,Object value){
        this.getData().put(key, value);
    }
    public void remove(String key){
        this.getData().remove(key);
    }
}
