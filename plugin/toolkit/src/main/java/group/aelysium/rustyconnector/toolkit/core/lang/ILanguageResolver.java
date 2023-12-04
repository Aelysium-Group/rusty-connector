package group.aelysium.rustyconnector.toolkit.core.lang;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface ILanguageResolver extends Service {
    String getRaw(String key);

    String getRaw(String key, TagResolver... resolvers);

    Component get(String key);

    Component get(String key, TagResolver... resolvers);

    Component getArray(String key);

    Component getArray(String key, TagResolver... resolvers);
}
