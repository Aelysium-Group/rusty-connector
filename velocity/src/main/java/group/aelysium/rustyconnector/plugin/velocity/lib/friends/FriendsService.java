package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.config.FriendsConfig;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FriendsService extends ServiceableService {
    private FriendsService(boolean enabled, Map<Class<? extends Service>, Service> services) {
        super(enabled, services);
    }

    @Override
    public void kill() {

    }

    public static FriendsService init(FriendsConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {
        FriendsService.Builder builder = new FriendsService.Builder();

        FriendsMySQLService mySQLService = new FriendsMySQLService.Builder()
                .setHost(config.getMysql_host())
                .setPort(config.getMysql_port())
                .setDatabase(config.getMysql_database())
                .setUser(config.getMysql_user())
                .setPassword(config.getMysql_password())
                .build();

        builder.setMySQLService(mySQLService);

        return builder.build();
    }

    protected static class Builder {
        protected final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public FriendsService.Builder setMySQLService(FriendsMySQLService service) {
            this.services.put(FriendsMySQLService.class, service);
            return this;
        }

        public FriendsService build() {
            if(this.services.get(FriendsMySQLService.class) == null) throw new NullPointerException("You must provide a MySQL service for the Friends service to use!");
            return new FriendsService(true, this.services);
        }
    }
}
