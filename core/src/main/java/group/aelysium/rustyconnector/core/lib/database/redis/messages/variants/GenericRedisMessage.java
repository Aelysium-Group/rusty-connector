package group.aelysium.rustyconnector.core.lib.database.redis.messages.variants;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;

import java.net.InetSocketAddress;

public class GenericRedisMessage extends RedisMessage {
    public GenericRedisMessage(RedisMessageType type, InetSocketAddress address, MessageOrigin origin) {
        super(type, address, origin);
    }
    public GenericRedisMessage(int messageVersion, String rawMessage, char[] key, RedisMessageType type, InetSocketAddress address, MessageOrigin origin) {
        super(messageVersion, rawMessage, key, type, address, origin);
    }
}
