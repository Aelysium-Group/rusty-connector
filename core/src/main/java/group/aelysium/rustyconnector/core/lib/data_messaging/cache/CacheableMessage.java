package group.aelysium.rustyconnector.core.lib.data_messaging.cache;

import java.util.Date;

public class CacheableMessage {
    private final Long snowflake;
    private final Date date;
    private final String contents;

    public CacheableMessage(Long snowflake, String contents) {
        this.snowflake = snowflake;
        this.contents = contents;
        this.date = new Date();
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

    @Override
    public String toString() {
        return "Snowflake ID: "+this.snowflake.toString()+
               " Contents: "+this.contents+
               " Date: "+this.date;
    }
}
