package group.aelysium.rustyconnector.api.core.message_cache;

import group.aelysium.rustyconnector.api.core.packet.PacketStatus;

import java.util.Date;

public interface ICacheableMessage {
    Long getSnowflake();

    String getContents();

    Date getDate();

    PacketStatus getSentence();

    String getSentenceReason();

    void sentenceMessage(PacketStatus status);

    void sentenceMessage(PacketStatus status, String reason);
}
