package review.request;

import config.APIConfig;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 * Created by czl on 09/10/16.
 */
public class LimitQueryParam {

    @DefaultValue(APIConfig.DEFAULT_STEP)
    @QueryParam("step")
    public int step; // TODO: must be greater than 1

    @DefaultValue("1")
    @QueryParam("page")
    public int page; // must be greater than 1
}
