package group.aelysium.rustyconnector.plugin.common.config;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.GitOperator;
import group.aelysium.declarative_yaml.annotations.AllContents;
import group.aelysium.declarative_yaml.annotations.Comment;
import group.aelysium.declarative_yaml.annotations.Config;
import group.aelysium.declarative_yaml.annotations.Node;
import group.aelysium.declarative_yaml.lib.Printer;
import group.aelysium.rustyconnector.common.crypt.AES;
import group.aelysium.rustyconnector.proxy.util.LiquidTimestamp;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Config("plugins/rustyconnector/gitops.yml")
@Comment({
        "Backs all of the RustyConnector configs behind GitOps so you can sync then to a Git repository.",
        "If you make changes to this config you must restart RustyConnector to see them."
})
public class GitOpsConfig {
    @Node()
    private final String repository = "";

    @Node(1)
    private final String branch = "main";

    @Node(2)
    private final String fetchPeriod = LiquidTimestamp.from(1, TimeUnit.MINUTES).toString();

    public GitOperator.Config config() {
        LiquidTimestamp period = LiquidTimestamp.from(1, TimeUnit.MINUTES);
        try {
            period = LiquidTimestamp.from(this.fetchPeriod);
        } catch (Exception ignore) {}

        return new GitOperator.Config(URI.create(this.repository))
                .branch(this.branch)
                .fetchPeriod(period.value(), period.unit())
                .location(Path.of("plugins/rustyconnector/git"));

    }

    public static @Nullable GitOpsConfig New() throws IOException {
        GitOpsConfig config = new GitOpsConfig();
        DeclarativeYAML.reload(config, new Printer());
        if(config.repository.isEmpty()) return null;

        return config;
    }
}
