package group.aelysium.rustyconnector.core.lib.util.logger;

import java.util.ArrayList;
import java.util.List;

public class LangMessage {
    private List<String> message = new ArrayList<>();
    private Logger logger;

    public LangMessage(Logger logger) {
        this.logger = logger;
    }

    /**
     * Insert a new line into the lang message.
     * @param string The text to add.
     * @return The lang message.
     */
    public LangMessage insert(String string) {
        this.message.add(string);
        return this;
    }

    /**
     * Insert a new line into the lang message.
     * @param strings The text to add.
     * @return The lang message.
     */
    public LangMessage insert(LangEntry strings) {
        strings.getRows().forEach(this::insert);
        return this;
    }

    /**
     * Print the compiled lang message.
     */
    public void print() {
        message.forEach(logger::log);
    }
}
