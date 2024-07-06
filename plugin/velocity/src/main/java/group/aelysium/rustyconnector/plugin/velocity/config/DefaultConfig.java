package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;

public class DefaultConfig extends Config {

    protected DefaultConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_CONFIG_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(DefaultConfig.class);
    }


    @SuppressWarnings("unchecked")
    protected void register(int configVersion) throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        try {
            this.processVersion(configVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        // Whitelist

        this.whitelist_enabled = IConfig.getValue(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = IConfig.getValue(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.word_id cannot be empty in order to use a whitelist on the proxy!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");

        // Hearts
        this.magicLink_serverTimeout = IConfig.getValue(this.data,"magic-link.server-timeout",Integer.class);
        if(this.magicLink_serverTimeout < 5) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout is set dangerously fast: " + this.magicLink_serverTimeout + "s. Setting to default of 5s.", NamedTextColor.YELLOW);
            this.magicLink_serverTimeout = 5;
        }
        this.magicLink_serverPingInterval = IConfig.getValue(this.data,"magic-link.server-ping-interval",Integer.class);
        if(this.magicLink_serverPingInterval < 5) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server ping interval is set dangerously fast: " + this.magicLink_serverPingInterval + "s. Setting to minimum of 5s.", NamedTextColor.YELLOW);
            this.magicLink_serverPingInterval = 5;
        }
        if(this.magicLink_serverTimeout <= this.magicLink_serverPingInterval) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout must be more than server ping interval!", NamedTextColor.YELLOW);
            this.magicLink_serverPingInterval = this.magicLink_serverTimeout - 2;
        }
    }

    public static DefaultConfig construct(Path dataFolder, LangService lang, int pluginConfigVersion, ConfigService configService) {
        DefaultConfig config = new DefaultConfig(dataFolder, "config.yml", "config", lang);
        config.register(pluginConfigVersion);
        configService.put(config);
        return config;
    }

    public void read(File target) {
        InputStream inputStream = new FileInputStream(target);
        Yaml yaml = new Yaml(new Constructor(Student.class));
        Student data = yaml.load(inputStream);
        System.out.println(data);
    }

    public void print(File target) {
        PrintWriter writer = new PrintWriter(new File("./src/main/resources/student_output.yml"));
        Yaml yaml = new Yaml();
        yaml.dump(dataMap, writer);
    }
}
