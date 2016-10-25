package wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import config.APIConfig;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cli on 10/24/2016.
 */
public class CategorySearchResult implements ThreadSafeCategorySearchResult {

    @JsonProperty("name")
    public String m_displayedName;

    @JsonProperty("results")
    public Queue<CategorySearchResultDescription> m_searchMeta;

    public CategorySearchResult() {
        m_searchMeta = new ConcurrentLinkedQueue<>();
    }

    public CategorySearchResult addCategory(CategorySearchResultDescription result) {
        if (m_searchMeta.size() >= APIConfig.MAX_RESULTS_PER_CATEGORY) {
            return this;
        }
        if (result != null) {
            m_searchMeta.add(result);
        }
        return this;
    }

    public CategorySearchResult setDisplayName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return this;
        }
        m_displayedName = name;
        return this;
    }

}
