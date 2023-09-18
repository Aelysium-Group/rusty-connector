package group.aelysium.rustyconnector.core.lib.lang.config;

import group.aelysium.rustyconnector.core.lib.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangService extends Service {
    protected String code;
    protected Map<LangFileMappings.Mapping, File> files;
    protected LangService(String languageCode, Map<LangFileMappings.Mapping, File> files) {
        this.code = languageCode;
        this.files = files;
    }

    /**
     * Returns the language code used by this {@link LangService}.
     */
    public String code() { return this.code; }

    public boolean isInline() { return this.code.equals("en_us"); }

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

        if(languageCode.equals("en_us")) return LangService.loadEnglish();

        File langFolder = new File(dataFolder.toString(), "lang/"+languageCode);
        if(!langFolder.exists())
            throw new RuntimeException("No lang configuration for "+languageCode+" exists!");

        try {
            return loadOther(languageCode, langFolder);
        } catch (Exception e) {
            try {
                return LangService.loadEnglish();
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

    public static LangService loadEnglish() {
        List<LangFileMappings.Mapping> mappings = LangFileMappings.toList();

        Map<LangFileMappings.Mapping, File> files = new HashMap<>();
        for (LangFileMappings.Mapping path : mappings) {
            URL url = LangService.class.getClassLoader().getResource("en_us/"+path.path());
            try {
                File file = new File(url.getPath());
                files.put(path, file);
            } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }
        }

        return new LangService("en_us", files);
    }
}
