package group.aelysium.rustyconnector.toolkit.core.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LangFileMappings {
    public static Mapping LANGUAGE = new LangFileMappings.Mapping("language.yml", "language.yml");
    public static Mapping MCLOADER_CONFIG_TEMPLATE = new LangFileMappings.Mapping("paper_config_template", "configs/paper/config.yml");
    public static Mapping MCLOADER_CONNECTORS_TEMPLATE = new LangFileMappings.Mapping("paper_connectors_template", "configs/paper/connectors.yml");
    public static Mapping PROXY_CONFIG_TEMPLATE = new LangFileMappings.Mapping("velocity_config_template", "configs/velocity/config.yml");
    public static Mapping PROXY_CONNECTORS_TEMPLATE = new LangFileMappings.Mapping("velocity_connectors_template", "configs/velocity/connectors.yml");
    public static Mapping PROXY_FAMILIES_TEMPLATE = new LangFileMappings.Mapping("velocity_families_template", "configs/velocity/families.yml");

    public static Mapping PROXY_MAGIC_CONFIG_TEMPLATE = new LangFileMappings.Mapping("velocity_magic_config_template", "configs/velocity/magic_configs/magic_config.yml");


    public static Mapping PROXY_DATA_TRANSIT_TEMPLATE = new LangFileMappings.Mapping("velocity_data_transit_template", "configs/velocity/extras/data_transit.yml");
    public static Mapping PROXY_DYNAMIC_TELEPORT_TEMPLATE = new LangFileMappings.Mapping("velocity_dynamic_teleport_template", "configs/velocity/extras/dynamic_teleport.yml");
    public static Mapping PROXY_FRIENDS_TEMPLATE = new LangFileMappings.Mapping("velocity_friends_template", "configs/velocity/extras/friends.yml");
    public static Mapping PROXY_LOGGER_TEMPLATE = new LangFileMappings.Mapping("velocity_logger_template", "configs/velocity/extras/logger.yml");
    public static Mapping PROXY_PARTY_TEMPLATE = new LangFileMappings.Mapping("velocity_party_template", "configs/velocity/extras/party.yml");
    public static Mapping PROXY_WEBHOOKS_TEMPLATE = new LangFileMappings.Mapping("velocity_webhooks_template", "configs/velocity/extras/webhooks.yml");
    public static Mapping PROXY_WHITELIST_TEMPLATE = new LangFileMappings.Mapping("velocity_whitelist_template", "configs/velocity/whitelists/whitelist.yml");

    public static Mapping PROXY_SCALAR_FAMILY_TEMPLATE = new LangFileMappings.Mapping("velocity_scalar_family_template", "configs/velocity/families/family.scalar.yml");
    public static Mapping PROXY_STATIC_FAMILY_TEMPLATE = new LangFileMappings.Mapping("velocity_static_family_template", "configs/velocity/families/family.static.yml");
    public static Mapping PROXY_RANKED_FAMILY_TEMPLATE = new LangFileMappings.Mapping("velocity_ranked_family_template", "configs/velocity/families/family.ranked.yml");
    public static Mapping PROXY_LOAD_BALANCER_TEMPLATE = new LangFileMappings.Mapping("velocity_load_balancer_template", "configs/velocity/load_balancers/load_balancer.yml");
    public static Mapping PROXY_MATCHMAKER_TEMPLATE = new LangFileMappings.Mapping("velocity_matchmaker_template", "configs/velocity/matchmakers/matchmaker.yml");

    public static List<Mapping> toList() {
        List<Mapping> list = new ArrayList<>();
        list.add(LANGUAGE);
        list.add(MCLOADER_CONFIG_TEMPLATE);
        list.add(MCLOADER_CONNECTORS_TEMPLATE);
        list.add(PROXY_CONFIG_TEMPLATE);
        list.add(PROXY_CONNECTORS_TEMPLATE);
        list.add(PROXY_FAMILIES_TEMPLATE);

        list.add(PROXY_DATA_TRANSIT_TEMPLATE);
        list.add(PROXY_DYNAMIC_TELEPORT_TEMPLATE);
        list.add(PROXY_FRIENDS_TEMPLATE);
        list.add(PROXY_LOGGER_TEMPLATE);
        list.add(PROXY_PARTY_TEMPLATE);
        list.add(PROXY_WEBHOOKS_TEMPLATE);

        list.add(PROXY_WHITELIST_TEMPLATE);

        list.add(PROXY_LOAD_BALANCER_TEMPLATE);

        list.add(PROXY_MATCHMAKER_TEMPLATE);

        list.add(PROXY_SCALAR_FAMILY_TEMPLATE);
        list.add(PROXY_STATIC_FAMILY_TEMPLATE);
        list.add(PROXY_RANKED_FAMILY_TEMPLATE);

        return list;
    }

    public static Mapping mapping(String name) {
        return toList().stream().filter(entry -> Objects.equals(entry.name(), name)).findFirst().orElseThrow(NullPointerException::new);
    }

    public record Mapping (String name, String path) {
        @Override
        public String toString() {
            return String.valueOf(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Mapping mapping = (Mapping) o;
            return Objects.equals(name, mapping.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
