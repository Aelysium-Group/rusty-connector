package group.aelysium.rustyconnector.core.lib.data_messaging;

import net.kyori.adventure.text.format.NamedTextColor;

public enum MessageStatus {
    UNDEFINED,
    AUTH_DENIAL, // If the message didn't contain the proper credentials (IP Address (for message tunnel), private-key, etc)
    PARSING_ERROR, // If the message failed to be parsed
    TRASHED, // If the message isn't intended for us.
    ACCEPTED, // Just cause a message was accepted doesn't mean it was processed. It could still cause an error
    EXECUTED;

    public NamedTextColor getColor() {
        if(this == AUTH_DENIAL) return NamedTextColor.RED;
        if(this == TRASHED) return NamedTextColor.DARK_GRAY;
        if(this == PARSING_ERROR) return NamedTextColor.DARK_RED;
        if(this == ACCEPTED) return NamedTextColor.YELLOW;
        if(this == EXECUTED) return NamedTextColor.GREEN;
        return NamedTextColor.GRAY;
    }
}
