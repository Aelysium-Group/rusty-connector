package group.aelysium.rustyconnector.common.config.common;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MemberKeyConfig {
    protected InputStream data;
    protected File configPointer;
    public MemberKeyConfig(File configPointer) {
        this.configPointer = configPointer;
    }

    public boolean generateFilestream(List<Component> outputLog) {
        if (!this.configPointer.exists()) return true;
        outputLog.add(Component.text("Building "+this.configPointer.getName()+"...", NamedTextColor.DARK_GRAY));

        try {
            this.data = new FileInputStream(this.configPointer);
            outputLog.add(Component.text("Finished building "+this.configPointer.getName(), NamedTextColor.GREEN));
            return true;
        } catch (Exception e) {
            outputLog.add(Component.text("Failed to build "+this.configPointer.getName(), NamedTextColor.RED));
            return false;
        }
    }

    public char[] get() throws IOException {
        return new String(this.data.readAllBytes(), StandardCharsets.UTF_8).toCharArray();
    }
}
