package group.aelysium.rustyconnector.core.lib.messenger.implementors.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class RedisClient extends io.lettuce.core.RedisClient {
    private final String dataChannel;

    protected RedisClient(ClientResources clientResources, RedisURI redisURI, @NotNull String dataChannel) {
        super(clientResources, redisURI);

        this.dataChannel = dataChannel;
    }

    public String dataChannel() {
        return dataChannel;
    }


    public static RedisClient create(ClientResources resources, RedisURI uri, @NotNull String dataChannel) {
        return new RedisClient(resources, uri, dataChannel);
    }

    public static class Builder {
        private ClientResources resources;
        private String host = "localhost";
        private int port = 3306;
        private String user = "default";
        private char[] password = null;
        private ProtocolVersion protocolVersion = ProtocolVersion.newestSupported();
        private String dataChannel = "rusty-connector";

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

        public Builder setPassword(char[] password) {
            this.password = password;
            return this;
        }

        public Builder setDataChannel(String dataChannel) {
            this.dataChannel = dataChannel;
            return this;
        }

        public Builder setProtocol(ProtocolVersion protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder setResources(ClientResources resources) {
            this.resources = resources;
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
                    .protocolVersion(this.protocolVersion)
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

            RedisClient client = RedisClient.create(this.resources, uri, dataChannel);
            client.setOptions(options);

            return client;
        }
    }
}