package wrapper;

/**
 * Created by cli on 10/24/2016.
 *
 * Made to expose the only thread safe operation
 */
public interface ThreadSafeCategorySearchResult {
    CategorySearchResult addCategory(CategorySearchResultDescription result);
}
