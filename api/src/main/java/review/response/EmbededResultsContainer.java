package review.response;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cli on 10/31/2016.
 */
public class EmbededResultsContainer {
    public int status;
    public List<EmbededContainer> results;

    public EmbededResultsContainer(int status) {
        this(status, new LinkedList<>());
    }

    public EmbededResultsContainer(int status, List<EmbededContainer> list) {
        this.status = status;
        this.results = list;
    }
}

