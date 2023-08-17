package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SyncedUser directly links into the MySQL database allowing you to run operations directly in MySQL.
 */
public class SyncedRole {
    private String name;
    private String description;
    private String color;
    private List<SyncedPermission> permissions;
    private final Update update = new Update();

    private SyncedRole(String name, String description, String color, List<SyncedPermission> permissions) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.permissions = permissions;
    }

    public String name() {
        return this.name;
    }

    public String color() {
        return this.color;
    }

    public String description() {
        return this.description;
    }

    public List<SyncedPermission> permissions() {
        return this.permissions;
    }
    private void permissions(List<SyncedPermission> permissions) {
        this.permissions = permissions;
    }

    public Update update() {
        return this.update;
    }

    /**
     * Contains multiple values which can be updated.
     * Updates are saved to the database first before being set on the object.
     */
    public class Update {
        public void name(String name) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE roles SET name = ? WHERE name = ?;");
            statement.setString(1, name);
            statement.setString(2, SyncedRole.this.name);
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedRole.this.name = name;
        }

        public void description(String description) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE roles SET description = ? WHERE name = ?;");
            statement.setString(1, description);
            statement.setString(2, SyncedRole.this.name);
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedRole.this.description = description;
        }

        public void color(String color) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE roles SET color = ? WHERE name = ?;");
            statement.setString(1, color);
            statement.setString(2, SyncedRole.this.name);
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedRole.this.color = color;
        }
    }

    /**
     * Deletes the user from the database.
     * This object should be immediately removed from all operations once this has been run.
     */
    public void delete() throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        {
            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("DELETE FROM roles WHERE name = ?;");
            statement.setString(1, name);
            mySQLService.execute(statement);
            mySQLService.close();
        }
    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();

        object.add("name", new JsonPrimitive(this.name));
        object.add("description", new JsonPrimitive(this.description));
        object.add("color", new JsonPrimitive(this.color));

        JsonArray permissions = new JsonArray();
        this.permissions.forEach(permission -> permissions.add(permission.toJSON()));
        object.add("permissions", permissions);

        return object;
    }

    /**
     * Fetch a list of all {@link SyncedRole}.
     * @return A list of {@link SyncedRole}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static List<SyncedRole> all() throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(
                "SELECT * FROM roles;"
        );
        ResultSet result = mySQLService.executeQuery(statement);

        List<SyncedRole> roles = new ArrayList<>();

        while(result.next()) {
            SyncedRole role = new SyncedRole(
                    result.getString("name"),
                    result.getString("description"),
                    result.getString("color"),
                    new ArrayList<>()
            );

            role.permissions(SyncedPermission.with(role));

            roles.add(role);
        }

        mySQLService.close();

        return roles;
    }

    /**
     * Fetch a {@link SyncedRole} from the database.
     * @param name The name to fetch.
     * @return A {@link SyncedRole}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static SyncedRole with(String name) throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("SELECT * FROM roles WHERE name = ?;");
        statement.setString(1, name);
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        SyncedRole role = new SyncedRole(
                result.getString("name"),
                result.getString("color"),
                result.getString("description"),
                new ArrayList<>()
        );

        mySQLService.close();

        return role;
    }

    /**
     * Fetch a list of {@link SyncedRole} which belong to a {@link SyncedUser} from the database.
     * @param user The {@link SyncedUser} to fetch from.
     * @return A list of {@link SyncedRole}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static List<SyncedRole> with(SyncedUser user) throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(
                "SELECT *" +
                "FROM roles AS r" +
                "LEFT JOIN users_roles_fk AS urf ON r.id=urf.role_name" +
                "WHERE urf.user_uuid = ?;"
        );
        statement.setString(1, user.uuid().toString());
        ResultSet result = mySQLService.executeQuery(statement);

        List<SyncedRole> roles = new ArrayList<>();

        while (result.next()) {
            SyncedRole role = new SyncedRole(
                    result.getString("name"),
                    result.getString("color"),
                    result.getString("description"),
                    null
            );

            role.permissions(SyncedPermission.with(role));

            roles.add(role);
        }


        mySQLService.close();

        return roles;
    }

    /**
     * Create a new {@link SyncedRole} and add it to the database.
     * @param name The name to set.
     * @param description The description to set.
     * @param color The color to set. Should be in HEX format.
     * @return A {@link SyncedRole}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static SyncedRole create(String name, String description, String color) throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("REPLACE INTO roles (name, description, color) VALUES(?, ?, ?);");
        statement.setString(1, name);
        statement.setString(2, description);
        statement.setString(3, color);
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        SyncedRole role = new SyncedRole(
                result.getString("name"),
                result.getString("description"),
                result.getString("color"),
                new ArrayList<>()
        );

        mySQLService.close();

        return role;
    }
}
