package group.aelysium.rustyconnector.toolkit.mc_loader.lang;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.ASCIIAlphabet;
import org.jetbrains.annotations.NotNull;

public class MCLoaderLangLibrary implements Particle {
    private final MCLoaderLang lang;
    private final ASCIIAlphabet asciiAlphabet;
    private final IConfig git;
    private final IConfig config;

    protected MCLoaderLangLibrary(
            @NotNull MCLoaderLang lang,
            @NotNull ASCIIAlphabet asciiAlphabet,
            @NotNull IConfig git,
            @NotNull IConfig config
    ) {
        this.lang = lang;
        this.asciiAlphabet = asciiAlphabet;
        this.git = git;
        this.config = config;
    }

    @Override
    public void close() throws Exception {

    }

    public static class Tinder extends Particle.Tinder<MCLoaderLangLibrary> {
        private final Settings settings;

        public Tinder(@NotNull Settings settings) {
            this.settings = settings;
        }

        @Override
        public @NotNull MCLoaderLangLibrary ignite() throws Exception {
            return new MCLoaderLangLibrary(
                    settings.lang(),
                    settings.asciiAlphabet(),
                    settings.git(),
                    settings.config()
            );
        }
    }

    public record Settings(
            @NotNull MCLoaderLang lang,
            @NotNull ASCIIAlphabet asciiAlphabet,
            @NotNull IConfig git,
            @NotNull IConfig config
    ) {}
}
