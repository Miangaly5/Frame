package main.java.com.etu2728.modele;

import java.util.HashMap;

public class ModelView {
    String url;
    HashMap<String, Object> data;
    
    public ModelView() {
        this.data = new HashMap<>();
    }
    public ModelView(String url) {
        this.url = "view/" + url;
        this.data = new HashMap<>();
    }
    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = "view/" + url;
    }
    public HashMap<String, Object> getData() {
        return data;
    }
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    
    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }
}
