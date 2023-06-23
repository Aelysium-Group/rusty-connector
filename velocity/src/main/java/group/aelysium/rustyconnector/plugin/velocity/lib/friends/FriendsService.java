package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.core.lib.database.MySQLService;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;

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

    public static FriendsService init(DefaultConfig config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, SQLException {
        FriendsService.Builder builder = new FriendsService.Builder();

        return builder.build();
    }

    protected static class Builder {
        protected final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public FriendsService.Builder setMySQLService(MySQLService service) {
            this.services.put(MySQLService.class, service);
            return this;
        }

        public FriendsService build() {
            if(this.services.get(MySQLService.class) == null) throw new NullPointerException("You must provide a MySQL service for the Friends service to use!");
            return new FriendsService(true, this.services);
        }
    }
}
