package group.aelysium.rustyconnector.core.lib.connectors.storage;

/**
 * A {@link StorageQuery} contains variants of a query which performs the same task.
 * These variants are focused for the different storage options they support.
 */
public class StorageQuery {
    protected String mysql;

    protected StorageQuery(String mysql) {
        this.mysql = mysql;
    }

    public String mysql() { return this.mysql; }

    public static StorageQuery create(String mysql) {
        return new StorageQuery(mysql);
    }
}
