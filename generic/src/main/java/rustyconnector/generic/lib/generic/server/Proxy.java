package rustyconnector.generic.lib.generic.server;

import rustyconnector.generic.database.Redis;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.List;

public class Proxy {
    private List<Family> registeredFamilies = new ArrayList<>();
    private Family rootFamily;
    private Redis redis;

    /**
     * Send a request over Redis asking all servers to register themselves
     */
    public void requestGlobalRegistration() {

    }

    public void connectRedis() {
        redis.connect(new Callback() {
            /**
             * Runs when the subscriber catches a message
             */
            public void call() {

            }
        });
    }
}
