package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import javax.naming.AuthenticationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SyncedUser directly links into the MySQL database allowing you to run operations directly in MySQL.
 */
public class SyncedPermission {
    private String identifier;
    private String name;
    private String description;
    private final Update update = new Update();

    private SyncedPermission(String identifier, String name, String description) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    public String identifier() {
        return this.identifier;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Update update() {
        return this.update;
    }

    /**
     * Contains multiple values which can be updated.
     * Updates are saved to the database first before being set on the object.
     */
    public class Update {

        public void identifier(String identifier) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE permissions SET identifier = ? WHERE identifier = ?;");
            statement.setString(1, identifier);
            statement.setString(2, SyncedPermission.this.identifier);
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedPermission.this.identifier = identifier;
        }

        public void name(String name) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE roles SET name = ? WHERE identifier = ?;");
            statement.setString(1, name);
            statement.setString(2, SyncedPermission.this.identifier);
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedPermission.this.name = name;
        }

        public void description(String description) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE roles SET description = ? WHERE identifier = ?;");
            statement.setString(1, description);
            statement.setString(2, SyncedPermission.this.identifier);
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedPermission.this.description = description;
        }
    }

    /**
     * Deletes the user from the database.
     * This object should be immediately removed from all operations once this has been run.
     */
    public void delete() throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("DELETE FROM permissions WHERE identifier = ?;");
        statement.setString(1, this.identifier);
        mySQLService.execute(statement);

        mySQLService.close();
    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();

        object.add("identifier", new JsonPrimitive(this.identifier));
        object.add("name", new JsonPrimitive(this.name));
        object.add("description", new JsonPrimitive(this.description));

        return object;
    }

    /**
     * Fetch a list of all {@link SyncedPermission}.
     * @return A list of {@link SyncedPermission}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static List<SyncedPermission> all() throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(
                "SELECT * FROM permissions;"
        );
        ResultSet result = mySQLService.executeQuery(statement);

        List<SyncedPermission> permissions = new ArrayList<>();

        while(result.next()) {
            SyncedPermission permission = new SyncedPermission(
                    result.getString("identifier"),
                    result.getString("name"),
                    result.getString("description")
            );

            permissions.add(permission);
        }

        mySQLService.close();

        return permissions;
    }

    /**
     * Fetch a list of {@link SyncedPermission} which belong to a {@link SyncedRole} from the database.
     * @param role The {@link SyncedRole} to fetch from.
     * @return A list of {@link SyncedPermission}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static List<SyncedPermission> with(SyncedRole role) throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(
                "SELECT *" +
                        "FROM permissions AS p" +
                        "LEFT JOIN roles_permissions_fk AS rpf ON p.identifier=urf.permission_identifier" +
                        "WHERE rpf.role_name = ?;"
        );
        statement.setString(1, role.name());
        ResultSet result = mySQLService.executeQuery(statement);

        List<SyncedPermission> permissions = new ArrayList<>();

        while(result.next()) {
            SyncedPermission permission = new SyncedPermission(
                    result.getString("identifier"),
                    result.getString("name"),
                    result.getString("description")
            );

            permissions.add(permission);
        }

        mySQLService.close();

        return permissions;
    }

    /**
     * Create a new {@link SyncedPermission} and add it to the database.
     * @param identifier The identifier node of the permission.
     * @param name The name of the permission.
     * @param description The description to set.
     * @return A {@link SyncedPermission}.
     * @throws SQLException If there is an issue querying the database.
     */
    public static SyncedPermission create(String identifier, String name, String description) throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("REPLACE INTO permissions (identifier, name, description) VALUES(?, ?, ?);");
        statement.setString(1, identifier);
        statement.setString(2, name);
        statement.setString(3, description);
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        SyncedPermission permission = new SyncedPermission(
                result.getString("identifier"),
                result.getString("name"),
                result.getString("description")
        );

        mySQLService.close();

        return permission;
    }
}
