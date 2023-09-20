package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.naming.AuthenticationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SyncedUser directly links into the MySQL database allowing you to run operations directly in MySQL.
 */
public class SyncedUser {
    private final UUID uuid;
    private String username;
    private String email;
    private boolean locked;
    private List<SyncedRole> roles;
    private final Update update = new Update();

    private SyncedUser(UUID uuid, String username, String email, List<SyncedRole> roles, boolean locked) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.locked = locked;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public String username() {
        return this.username;
    }

    public String email() {
        return this.email;
    }

    public boolean locked() {
        return this.locked;
    }

    public List<SyncedRole> roles() {
        return this.roles;
    }
    private void roles(List<SyncedRole> roles) {
        this.roles = roles;
    }

    public Update update() {
        return this.update;
    }

    /**
     * Contains multiple values which can be updated.
     * Updates are saved to the database first before being set on the object.
     */
    public class Update {
        public void username(String username) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE users SET username = ? WHERE uuid = ?;");
            statement.setString(1, username);
            statement.setString(2, SyncedUser.this.uuid.toString());
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedUser.this.username = username;
        }

        public void email(String email) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE users SET email = ? WHERE uuid = ?;");
            statement.setString(1, email);
            statement.setString(2, SyncedUser.this.uuid.toString());
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedUser.this.email = email;
        }

        public void password(char[] password) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE users SET password = ? WHERE uuid = ?;");
            statement.setString(1, BCrypt.withDefaults().hashToString(12, password));
            statement.setString(2, SyncedUser.this.uuid.toString());
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedUser.this.email = email;
        }

        public void locked(boolean locked) throws SQLException {
            MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

            mySQLService.connect();
            PreparedStatement statement = mySQLService.prepare("UPDATE users SET locked = ? WHERE uuid = ?;");
            if(locked)
                statement.setString(1, "TRUE");
            else
                statement.setString(1, "FALSE");
            statement.setString(2, SyncedUser.this.uuid.toString());
            mySQLService.execute(statement);

            mySQLService.close();
            SyncedUser.this.locked = locked;
        }
    }

    /**
     * Deletes the user from the database.
     * This object should be immediately removed from all operations once this has been run.
     */
    public void delete() throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("DELETE FROM users WHERE username = ?;");
        statement.setString(1, username);
        mySQLService.execute(statement);

        mySQLService.close();
    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();

        object.add("uuid", new JsonPrimitive(this.uuid.toString()));
        object.add("username", new JsonPrimitive(this.username));
        object.add("email", new JsonPrimitive(this.email));
        object.add("locked", new JsonPrimitive(this.locked));

        JsonArray roles = new JsonArray();
        this.roles.forEach(role -> roles.add(role.toJSON()));
        object.add("roles", roles);

        return object;
    }

    /**
     * Fetch a User account from the database.
     * User accounts are fully resolved and will contain roles and permissions.
     * @param username The username to fetch.
     * @param password The password to authenticate with.
     * @return A User.
     * @throws SQLException If there is an issue querying the database.
     * @throws AuthenticationException If the password is incorrect.
     */
    public static SyncedUser with(String username, char[] password) throws SQLException, AuthenticationException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("SELECT * FROM users WHERE username = ?;");
        statement.setString(1, username);
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        if(!BCrypt.verifyer().verify(password, result.getString("password")).verified)
            throw new AuthenticationException("The provided password is incorrect!");

        SyncedUser user = new SyncedUser(
                UUID.fromString(result.getString("uuid")),
                result.getString("username"),
                result.getString("email"),
                null,
                result.getBoolean("locked")
        );

        user.roles(SyncedRole.with(user));

        mySQLService.close();

        return user;
    }

    /**
     * Fetch a {@link SyncedUser} from the database.
     * {@link SyncedUser} are fully resolved and will contain roles and permissions.
     * @param uuid The uuid to fetch.
     * @return A {@link SyncedUser}.
     * @throws SQLException If there is an issue querying the database.
     * @throws AuthenticationException If the password is incorrect.
     */
    public static SyncedUser with(UUID uuid) throws SQLException, AuthenticationException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("SELECT * FROM users WHERE username = ?;");
        statement.setString(1, uuid.toString());
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        SyncedUser user = new SyncedUser(
                UUID.fromString(result.getString("uuid")),
                result.getString("username"),
                result.getString("email"),
                null,
                result.getBoolean("locked")
        );

        user.roles(SyncedRole.with(user));

        mySQLService.close();

        return user;
    }

    /**
     * Create a new {@link SyncedUser} and add it to the database.
     * `password` should be the raw string representation of the password to be saved.
     * The password will be encrypted before being saved to the database.
     * @param username The username to set.
     * @param email The email to set.
     * @param password The password to set.
     * @return A User.
     * @throws SQLException If there is an issue querying the database.
     */
    public static SyncedUser create(String username, String email, char[] password) throws SQLException {
        MySQLService mySQLService = VelocityAPI.get().services().viewportService().orElseThrow().services().mySQLService();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare("REPLACE INTO users (username, email, password, mfa_key, locked) VALUES(?, ?, ?, \"\", 0);");
        statement.setString(1, username);
        statement.setString(2, email);
        statement.setString(3, BCrypt.withDefaults().hashToString(12, password));
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        SyncedUser user = new SyncedUser(
                UUID.fromString(result.getString("uuid")),
                result.getString("username"),
                result.getString("email"),
                new ArrayList<>(),
                result.getBoolean("locked")
        );

        mySQLService.close();

        return user;
    }
}
