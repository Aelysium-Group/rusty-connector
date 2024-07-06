package group.aelysium.rustyconnector.toolkit.common.magic_link.packet;

import net.kyori.adventure.text.format.NamedTextColor;

public enum PacketStatus {
    UNDEFINED, // The message hasn't had any status set yet.
    AUTH_DENIAL, // If the message didn't contain the proper credentials (IP Address (for message tunnel), private-key, over max length, etc)
    PARSING_ERROR, // If the message failed to be parsed
    TRASHED, // If the message isn't intended for us.
    ACCEPTED, // Just cause a message was accepted doesn't mean it was processed. It could still cause an error
    EXECUTING_ERROR, // If the message failed to be parsed
    EXECUTED; // The message has successfully processed and handled.

    public NamedTextColor color() {
        if(this == AUTH_DENIAL) return NamedTextColor.RED;
        if(this == TRASHED) return NamedTextColor.DARK_GRAY;
        if(this == PARSING_ERROR) return NamedTextColor.DARK_RED;
        if(this == ACCEPTED) return NamedTextColor.YELLOW;
        if(this == EXECUTED) return NamedTextColor.GREEN;
        if(this == EXECUTING_ERROR) return NamedTextColor.DARK_RED;
        return NamedTextColor.GRAY;
    }
}
