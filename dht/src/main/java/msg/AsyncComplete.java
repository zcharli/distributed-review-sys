package msg;

import exceptions.NotImplementedException;

import java.util.concurrent.Callable;

/**
 * Created by cli on 9/20/2016.
 */
public class AsyncComplete implements Callable<Integer> {
    private boolean m_success;
    private String m_message;

    public AsyncComplete() {}

    public AsyncComplete isSuccessful(boolean ok) {
        m_success = ok;
        return this;
    }

    public boolean isSuccessful() {
        return m_success;
    }

    public void message(String msg) {
        m_message = msg;
    }

    public String message() {
        return m_message;
    }

    @Override
    public Integer call() throws Exception {
        throw new NotImplementedException("Must implement custom call method.");
    }
}
