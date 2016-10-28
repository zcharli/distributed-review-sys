package review.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 10/24/2016.
 */
public class CategorySearchResultDescription {

    @JsonProperty("title")
    public String m_searchTitle;


    @JsonProperty("description")
    public String m_matchedString;

    @JsonProperty("route")
    public String m_route;

    @JsonProperty("param")
    public String m_routeParam;

    @JsonProperty("model")
    public String m_model;

    public CategorySearchResultDescription() {}

    public CategorySearchResultDescription setTitle(String title) {
        m_searchTitle = title;
        return this;
    }

    public CategorySearchResultDescription setDescription(String desc) {
        m_matchedString = desc;
        return this;
    }

    public CategorySearchResultDescription setURL(String url) {
        m_route = url;
        return this;
    }

    public CategorySearchResultDescription setParam(String param) {
        m_routeParam = param;
        return this;
    }

    public CategorySearchResultDescription setModel(String model) {
        m_model = model;
        return this;
    }
}
