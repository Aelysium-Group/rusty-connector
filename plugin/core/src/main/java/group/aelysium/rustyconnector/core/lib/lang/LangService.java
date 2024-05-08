package group.aelysium.rustyconnector.core.lib.lang;

import group.aelysium.rustyconnector.toolkit.core.lang.ILangService;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static net.kyori.adventure.text.Component.*;

public class LangService implements ILangService<LanguageResolver> {
    protected LanguageResolver resolver;
    protected String code;
    protected Map<LangFileMappings.Mapping, File> files;
    protected LangService(String languageCode, Map<LangFileMappings.Mapping, File> files) {
        this.code = languageCode;
        this.files = files;
        this.resolver = LanguageResolver.create(this, this.code.equals("en_us"));
    }

    /**
     * Returns the language code used by this {@link LangService}.
     */
    public String code() { return this.code; }

    public boolean isInline() { return this.code.equals("en_us"); }

    public LanguageResolver resolver() {
        return this.resolver;
    }

    /**
     * Fetches a file from the Lang file that this {@link LangService} is pointing to.
     * The returned file should
     * @param fileName The filename to fetch.
     * @return The file, or `null` if there is none for that name.
     */
    public File get(LangFileMappings.Mapping fileName) {
        return this.files.get(fileName);
    }

    @Override
    public void kill() {

    }

    public static LangService resolveLanguageCode(String languageCode, Path dataFolder) {
        languageCode = languageCode.toLowerCase();
        languageCode = languageCode.replace("-", "_");

        if(InternalLangConfigurations.internalCodes().contains(languageCode)) return LangService.loadInternal(InternalLangConfigurations.get(languageCode).build());

        File langFolder = new File(dataFolder.toString(), "lang/"+languageCode);
        if(!langFolder.exists())
            throw new RuntimeException("No lang configuration for "+languageCode+" exists!");

        try {
            return loadOther(languageCode, langFolder);
        } catch (Exception e) {
            try {
                return LangService.loadInternal(InternalLangConfigurations.ENGLISH.build());
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    protected static LangService loadOther(String code, File folder) {
        Path folderPath = Path.of(folder.toURI());
        try {
            Map<LangFileMappings.Mapping, File> files = new HashMap<>();
            for (LangFileMappings.Mapping path : LangFileMappings.toList()) {
                try {
                    File file = new File(folderPath.resolve(path.path()).toString());
                    files.put(path, file);
                } catch (NullPointerException e) {
                    throw new RuntimeException(e);
                }
            }

            return new LangService(code, files);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LangService loadInternal(String internal) throws NoSuchElementException {
        if(!InternalLangConfigurations.internalCodes().contains(internal)) throw new NoSuchElementException();

        List<LangFileMappings.Mapping> mappings = LangFileMappings.toList();

        Map<LangFileMappings.Mapping, File> files = new HashMap<>();
        for (LangFileMappings.Mapping path : mappings) {
            URL url = LangService.class.getClassLoader().getResource(internal+"/"+path.path());
            try {
                File file = new File(url.getPath());
                files.put(path, file);
            } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }
        }

        return new LangService(internal, files);
    }

    public static class InternalLangConfigurations {
        public static Entry ENGLISH = () -> "en_us";
        public static Entry SIMPLIFIED_CHINESE = () -> "zh_cn";

        public static List<Entry> internalEntries() {
            List<Entry> internalCodes = new ArrayList<>();

            internalCodes.add(ENGLISH);
            internalCodes.add(SIMPLIFIED_CHINESE);

            return internalCodes;
        }

        public static List<String> internalCodes() {
            List<String> internalCodes = new ArrayList<>();

            internalEntries().forEach(entry -> internalCodes.add(entry.build()));

            return internalCodes;
        }


        public static Entry get(String internal) {
            return internalEntries().stream().filter(entry -> entry.build().equals(internal)).findAny().orElseThrow();
        }

        public interface Entry {
            String build();
        }
    }
}
