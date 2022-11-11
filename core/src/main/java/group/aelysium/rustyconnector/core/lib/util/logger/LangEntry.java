package group.aelysium.rustyconnector.core.lib.util.logger;

import java.util.ArrayList;
import java.util.List;

public class LangEntry {
    private List<String> rows = new ArrayList<>();

    public LangEntry(String string) {
        this.rows.add(string);
    }
    public LangEntry(List<String> rows) {
        this.rows = rows;
    }

    public List<String> getRows() {
        return this.rows;
    }

    @Override
    public String toString() {
        try {
            return this.rows.get(0);
        } catch (Exception e) {
            return "LangEntry";
        }
    }
}
