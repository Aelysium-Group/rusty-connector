package group.aelysium.rustyconnector.api.core.lang;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.io.File;

public interface ILangService<TLanguageResolver extends ILanguageResolver> extends Service {
    String code();

    boolean isInline();

    TLanguageResolver resolver();

    File get(LangFileMappings.Mapping fileName);
}
