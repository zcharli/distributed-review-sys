package msg;

import exceptions.NotImplementedException;
import javax.annotation.Nullable;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by cli on 9/20/2016.
 */
public class AsyncResult implements Callable<Integer> {

    private Map<Number640,Data> payload;

    public AsyncResult() {}

    public AsyncResult payload(Map<Number640,Data>  p) {
        payload = p;
        return this;
    }

    @Nullable
    public Map<Number640,Data> payload() {
        return payload;
    }

    @Override
    public Integer call() throws Exception {
        throw new NotImplementedException("Must implement custom call method.");
    }
}
