package net.aelysium.screencontrol.lib.database;

import com.google.common.io.Closeables;
import net.aelysium.screencontrol.ScreenControl;
import net.aelysium.screencontrol.lib.generic.ScreenChannel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import redis.clients.jedis.*;

import java.io.IOException;

public class Redis {
    private String host;
    private int port;
    private String password;
    private String dataChannel;
    private Jedis client;
    private Jedis jedisSubscriber;
    private RedisSubscriber subscriber;
    private JedisPool pool;

    public Redis() {}

    /**
     * Sets the connection
     */
    public void setConnection(String host, int port, String password, String dataChannel) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.dataChannel = dataChannel;
    }

    /**
     * Tests the connection to the provided Redis server
     */
    public void connect() throws ExceptionInInitializerError{
        try{
            if(!(this.client == null)) return;

            this.client = new Jedis(this.host, this.port);
            this.client.auth(this.password);
            this.client.connect();
            ScreenControl.log("Redis authenticated!");
            ScreenControl.log("Pinging server...");
            ScreenControl.log(this.client.ping());

            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            this.pool = new JedisPool(poolConfig, this.host, this.port, 0);
            this.jedisSubscriber = this.pool.getResource();
            this.jedisSubscriber.auth(this.password);
            this.subscriber = new RedisSubscriber();
            ScreenControl.log("Subscribing...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ScreenControl.log("Initializing Thread...");
                        jedisSubscriber.subscribe(subscriber, dataChannel);
                        ScreenControl.log("Subscription ended.");
                    } catch (Exception e) {
                        ScreenControl.log("Subscription failed.");
                        e.printStackTrace();
                        ScreenControl.log("Killing plugin...");
                        Bukkit.getPluginManager().disablePlugin(ScreenControl.getProvidingPlugin(ScreenControl.class));
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError();
        }
    }

    public void publish(String message) {
        if(ScreenControl.getPlugin(ScreenControl.class).debug) ScreenControl.log("Sent message: "+message+" on channel "+this.dataChannel);
        this.client.publish(this.dataChannel, message);
    }

    /**
     * Disconnects
     */
    public void disconnect() throws ExceptionInInitializerError {
        try {
            this.subscriber.unsubscribe();
            this.jedisSubscriber.close();
            this.client.close();
            this.client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
