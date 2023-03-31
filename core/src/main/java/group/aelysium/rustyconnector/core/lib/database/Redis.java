package group.aelysium.rustyconnector.core.lib.database;

import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import group.aelysium.rustyconnector.core.central.PluginRuntime;

import java.net.InetSocketAddress;
import java.util.Map;

// TODO: Move this to be a RedisIO implementation
public class Redis {
    private String host;
    private int port;
    private String password;
    private String dataChannel;
    private Jedis client;
    private Jedis jedisSubscriber;
    private Subscriber subscriber;
    private JedisPool pool;
    private Thread subscriberThread;

    public Redis() {}

    public void setConnection(String host, int port, String password, String dataChannel) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.dataChannel = dataChannel;
    }


    public void connect(PluginAPI api) throws ExceptionInInitializerError {
        try{
            if(!(this.client == null)) return;

            this.client = new Jedis(this.host, this.port);
            this.client.auth(this.password);
            this.client.connect();

            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            this.pool = new JedisPool(poolConfig, this.host, this.port, 0);

            this.jedisSubscriber = this.pool.getResource();
            this.jedisSubscriber.auth(this.password);
            this.subscriber = new Subscriber(api);

            this.subscriberThread = new Thread(() -> {
                try {
                    jedisSubscriber.subscribe(subscriber, dataChannel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            this.subscriberThread.start();
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(api.getLogger(), Component.text("REDIS: "+ e.getMessage()), NamedTextColor.RED);
        }
    }

    protected void publish(String message) throws IllegalArgumentException {
        this.client.publish(this.dataChannel, message);
    }

    public void disconnect() throws ExceptionInInitializerError {
        try {
            this.subscriber.unsubscribe();
            this.jedisSubscriber.close();
            this.jedisSubscriber.disconnect();

            this.subscriberThread.interrupt();
            this.client.close();
            this.client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When redis receives a message
     *
     * @param rawMessage The raw message that is received
     */
    public void onMessage(String rawMessage) {}

    public class Subscriber extends JedisPubSub {
        private PluginAPI api;

        public Subscriber(PluginAPI api) {
            this.api = api;
        }

        @Override
        public void onMessage(String channel, String rawMessage) {
            try {
                Redis.this.onMessage(rawMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a message over the data channel on this Redis instance
     * @param privateKey The private key to send
     * @param type The type of message being sent
     * @param address The address of the server we're sending from
     * @param parameters Additional parameters
     * @throws IllegalArgumentException If message parameters contains parameters: `pk`, `type`, or `ip`
     */
    public void sendMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {}
}
