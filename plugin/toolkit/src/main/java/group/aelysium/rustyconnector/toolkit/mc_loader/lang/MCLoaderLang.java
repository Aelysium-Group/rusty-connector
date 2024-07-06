package group.aelysium.rustyconnector.toolkit.mc_loader.lang;

import group.aelysium.rustyconnector.toolkit.common.lang.ASCIIAlphabet;
import group.aelysium.rustyconnector.toolkit.common.lang.Lang;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class MCLoaderLang extends Lang {

    public MCLoaderLang(ASCIIAlphabet asciiAlphabet) {
        super(asciiAlphabet);
    }

    public Component magicLink() {
        return join(
                newlines(),
                space(),
                text("              /(¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯)\\"),
                text("          --<||(     MAGIC LINK -- CONNECTED     )||>--"),
                text("              \\(_.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._)/")
        ).color(DARK_PURPLE);
    }

    public Component paperFolia(boolean isFolia) {
        if(isFolia) return text("RustyConnector-Folia");
        return text("RustyConnector-Paper");
    };

    public Component paperFoliaLowercase(boolean isFolia) {
        if(isFolia) return text("rustyconnector-folia");
        return text("rustyconnector-paper");
    };
}
