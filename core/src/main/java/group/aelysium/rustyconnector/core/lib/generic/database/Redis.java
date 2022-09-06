package group.aelysium.rustyconnector.core.lib.generic.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.generic.MessageCache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import group.aelysium.rustyconnector.core.RustyConnector;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

public class Redis {
    private MessageCache messageCache;
    private String host;
    private int port;
    private String password;
    private String dataChannel;
    private Jedis client;
    private Jedis jedisSubscriber;
    private Subscriber subscriber;
    private JedisPool pool;

    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    public Redis() {}

    /**
     * Sets the connection
     */
    public void setConnection(String host, int port, String password, String dataChannel) throws IllegalAccessException {
        if(password.length() < 16) {
            throw new IllegalAccessException("Your Redis password is to short! For security purposes, please use a longer password! "+password.length()+" < 16");
        }

        this.host = host;
        this.port = port;
        this.password = password;
        this.dataChannel = dataChannel;
    }

    /**
     * Tests the connection to the provided Redis server
     */
    public void connect(RustyConnector plugin) throws ExceptionInInitializerError{
        try{
            if(!(this.client == null)) return;

            this.client = new Jedis(this.host, this.port);
            this.client.auth(this.password);
            this.client.connect();

            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            this.pool = new JedisPool(poolConfig, this.host, this.port, 0);
            this.jedisSubscriber = this.pool.getResource();
            this.jedisSubscriber.auth(this.password);
            this.subscriber = new Subscriber(plugin);
            this.messageCache = new MessageCache(50);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jedisSubscriber.subscribe(subscriber, dataChannel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError();
        }
    }

    protected void publish(String message) throws IllegalArgumentException {
        this.client.publish(this.dataChannel, message);
    }

    /**
     * When redis disconnects
     */
    public void onDisconnect() throws ExceptionInInitializerError {
        try {
            this.subscriber.unsubscribe();
            this.jedisSubscriber.close();
            this.client.close();
            this.client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When redis receives a message
     *
     * @param message The messsage that is received
     */
    public void onMessage(String message, Long messageSnowflake) {}

    public class Subscriber extends JedisPubSub {
        private RustyConnector plugin;

        public Subscriber(RustyConnector plugin) {
            this.plugin = plugin;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                Long snowflake = Redis.this.messageCache.cacheMessage(message);
                Redis.this.onMessage(message, snowflake);
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
    public void sendMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {
        Gson gson = new Gson();

        JsonObject object = new JsonObject();
        object.addProperty("pk",privateKey);
        object.addProperty("type",type.toString());
        object.addProperty("from",address.toString());

        JsonObject parameterObject = new JsonObject();
        parameters.forEach(object::addProperty);

        parameterObject.entrySet().forEach(entry -> {
            if(Objects.equals(entry.getKey(), "pk")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "type")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "ip")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "to")) throw new IllegalArgumentException("You can't send this parameters in a message!");
            if(Objects.equals(entry.getKey(), "from")) throw new IllegalArgumentException("You can't send this parameters in a message!");

            object.addProperty(entry.getKey(),entry.getValue().getAsString());
        });

        this.publish(gson.toJson(object));
    }
}
