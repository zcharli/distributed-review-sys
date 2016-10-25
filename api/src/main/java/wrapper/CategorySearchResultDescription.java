package wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 10/24/2016.
 */
public class CategorySearchResultDescription {

    @JsonProperty("title")
    public String m_searchTitle;


    @JsonProperty("description")
    public String m_matchedString;

    @JsonProperty("url")
    public String m_clickUrl;

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
        m_clickUrl = url;
        return this;
    }
}
