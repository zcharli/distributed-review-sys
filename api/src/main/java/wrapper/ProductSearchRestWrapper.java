package wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cli on 10/24/2016.
 */
public class ProductSearchRestWrapper {

    @JsonProperty("results")
    public Map<String, CategorySearchResult> m_rootElement;

    @JsonProperty("success")
    public boolean success = true;

    public ProductSearchRestWrapper() {
        m_rootElement = new ConcurrentHashMap<>();
    }

    public void setCategories(String catName, CategorySearchResult category) {
        m_rootElement.put(catName, category);
    }
}
