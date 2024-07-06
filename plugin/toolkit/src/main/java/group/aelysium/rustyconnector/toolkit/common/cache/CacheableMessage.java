package group.aelysium.rustyconnector.toolkit.common.cache;

import group.aelysium.rustyconnector.toolkit.common.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketStatus;

import java.util.Date;

public class CacheableMessage implements ICacheableMessage {
    private final Long snowflake;
    private final Date date;
    private final String contents;
    private String reason;
    private PacketStatus status;

    public CacheableMessage(Long snowflake, String contents, PacketStatus status) {
        this.snowflake = snowflake;
        this.contents = contents;
        this.date = new Date();
        this.status = status;
    }

    public Long getSnowflake() {
        return this.snowflake;
    }

    public String getContents() {
        return this.contents;
    }

    public Date getDate() {
        return this.date;
    }

    public PacketStatus getSentence() {
        return this.status;
    }
    public String getSentenceReason() {
        return this.reason;
    }

    public void sentenceMessage(PacketStatus status) {
        this.status = status;
        this.reason = null;
    }

    public void sentenceMessage(PacketStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Snowflake ID: "+this.snowflake.toString()+
               " Contents: "+this.contents+
               " Date: "+this.date+
               " Status: "+this.contents;
    }
}
