package group.aelysium.rustyconnector.core.lib.database.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.bootstrap.Bootstrap;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE;

public class RedisClient extends io.lettuce.core.RedisClient {
    private final char[] privateKey;
    private final String dataChannel;

    protected RedisClient(ClientResources clientResources, RedisURI redisURI, @NotNull String dataChannel, char @NotNull [] privateKey) {
        super(clientResources, redisURI);

        this.dataChannel = dataChannel;
        this.privateKey = privateKey;
    }

    public String getDataChannel() {
        return dataChannel;
    }

    /*
     * RedisClient is specifically never used in a way that it can be accessed through the public plugin API.
     * And it should remain that way.
     * That's why this field is allowed to be public.
     */
    public char[] getPrivateKey() {
        return privateKey;
    }


    public static RedisClient create(ClientResources resources, RedisURI uri, @NotNull String dataChannel, char @NotNull [] privateKey) {
        return new RedisClient(resources, uri, dataChannel, privateKey);
    }

    public static class Builder {
        private static final ClientResources resources = ClientResources.create();
        private String host = "localhost";
        private int port = 3306;
        private String user = "default";
        private char[] password = null;
        private String dataChannel = "rusty-connector";
        private char[] privateKey = null;

        public Builder() {}

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password.toCharArray();
            return this;
        }

        public Builder setDataChannel(String dataChannel) {
            this.dataChannel = dataChannel;
            return this;
        }

        public Builder setPrivateKey(char[] privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public RedisClient build() {
            SocketOptions socket = SocketOptions.builder()
                    .keepAlive(true)
                    .build();

            TimeoutOptions timeout = TimeoutOptions.builder()
                    .fixedTimeout(Duration.ofMinutes(1))
                    .build();

            ClientOptions options = ClientOptions.builder()
                    .autoReconnect(true)
                    .socketOptions(socket)
                    .timeoutOptions(timeout)
                    .build();

            RedisURI uri;
            if(this.password == null)
                uri = RedisURI.builder()
                        .withHost(this.host)
                        .withPort(this.port)
                        .withAuthentication(this.user, "")
                        .build();
            else
                uri = RedisURI.builder()
                        .withHost(this.host)
                        .withPort(this.port)
                        .withAuthentication(this.user, this.password)
                        .build();

            RedisClient client = RedisClient.create(resources, uri, dataChannel, privateKey);
            client.setOptions(options);

            return client;
        }
    }
}
