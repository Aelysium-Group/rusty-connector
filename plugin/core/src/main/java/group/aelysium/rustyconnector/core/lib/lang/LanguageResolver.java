package group.aelysium.rustyconnector.core.lib.lang;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.ILanguageResolver;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class LanguageResolver implements ILanguageResolver {
    private MiniMessage miniMessage = MiniMessage.miniMessage();
    private ConfigurationNode mappings;

    protected LanguageResolver(ConfigurationNode mappings) {
        this.mappings = mappings;
    }

    protected ConfigurationNode parseNodeQuery(String path) {
        String[] steps = path.split("\\.");

        final ConfigurationNode[] currentNode = {this.mappings};
        Arrays.stream(steps).forEach(step -> {
            currentNode[0] = currentNode[0].node(step);
        });

        if(currentNode[0] == null) throw new IllegalArgumentException("The called YAML node `"+path+"` was null.");

        return currentNode[0];
    }

    protected <T> T getNode(String node, Class<T> type) throws IllegalStateException {
        try {
            Object objectData = parseNodeQuery(node).get(type);
            if(objectData == null) throw new NullPointerException();

            return type.cast(objectData);
        } catch (NullPointerException e) {
            throw new IllegalStateException("The node ["+node+"] is missing!");
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node ["+node+"] is of the wrong data type! Make sure you are using the correct type of data!");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to register the node: "+node);
        }
    }
    public String getRaw(String key) {
        try {
            return PlainTextComponentSerializer.plainText().serialize(
                    miniMessage.deserialize(this.getNode(key, String.class))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "[Parsing Error]";
        }
    }
    public String getRaw(String key, TagResolver...resolvers){
        try {
            return PlainTextComponentSerializer.plainText().serialize(
                    miniMessage.deserialize(this.getNode(key, String.class), resolvers)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "[Parsing Error]";
        }
    }

    public Component get(String key) {
        try {
            return miniMessage.deserialize(this.getNode(key, String.class));
        } catch (Exception e) {
            e.printStackTrace();
            return Component.text("[Parsing Error]");
        }
    }
    public Component get(String key, TagResolver ...resolvers){
        try {
            return miniMessage.deserialize(this.getNode(key, String.class), resolvers);
        } catch (Exception e) {
            e.printStackTrace();
            return Component.text("[Parsing Error]");
        }
    }

    public Component getArray(String key){
        try {
            final Component[] component = {Component.empty()};
            ((List<String>) this.getNode(key, List.class)).forEach(string -> {
                component[0] = component[0].append(miniMessage.deserialize(string)).appendNewline();
            });

            return component[0];
        } catch (Exception e) {
            e.printStackTrace();
            return Component.text("[Parsing Error]");
        }
    }

    public Component getArray(String key, TagResolver ...resolvers){
        try {
            final Component[] component = {Component.empty()};
            ((List<String>) this.getNode(key, List.class)).forEach(string -> {
                component[0] = component[0].append(miniMessage.deserialize(string, resolvers)).appendNewline();
            });

            return component[0];
        } catch (Exception e) {
            e.printStackTrace();
            return Component.text("[Parsing Error]");
        }
    }

    public static TagResolver tagHandler(String key, Object value) {
        return TagResolver.resolver(key, Tag.preProcessParsed(value.toString()));
    }

    public static LanguageResolver create(LangService lang, boolean internal) {
        InputStream stream = null;
        try {
            if(internal)
                stream = IYAML.getResource("en_us/"+ LangFileMappings.LANGUAGE);
            else
                stream = new FileInputStream(lang.get(LangFileMappings.LANGUAGE));

            try(BufferedReader buffer = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return new LanguageResolver(
                        YamlConfigurationLoader.builder()
                                .indent(2)
                                .source(() -> buffer)
                                .build().load()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stream.close();
            } catch (Exception ignore) {}
        }
    }

    @Override
    public void kill() {
        this.miniMessage = null;
        this.mappings = null;
    }
}