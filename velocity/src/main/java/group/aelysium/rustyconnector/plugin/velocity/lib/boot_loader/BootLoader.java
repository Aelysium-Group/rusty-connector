package group.aelysium.rustyconnector.plugin.velocity.lib.boot_loader;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BootLoader {
    private int amount = 0;

    public void incrementFive() {
        this.amount = this.amount + 5;
        if(this.amount > 100) this.amount = 100;
    }

    public Component print() {
        StringBuilder filled = new StringBuilder();
        StringBuilder empty = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if(i < this.amount * 0.1) filled.append("█");
            else empty.append("-");
        }
        return Component.text("Loading: ["+filled + empty+"] "+this.amount+"%", NamedTextColor.GRAY);
    }
}
