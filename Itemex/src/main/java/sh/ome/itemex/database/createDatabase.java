package sh.ome.itemex.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import sh.ome.itemex.Itemex;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class createDatabase {

    public static Connection createConnection() {
        Connection c = null;
        try {
            if (Itemex.database_type.equalsIgnoreCase("mariadb") || Itemex.database_type.equalsIgnoreCase("mysql")) {
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://" + Itemex.db_hostname + ":" + Itemex.db_port + "/" + Itemex.db_name;
                c = DriverManager.getConnection(url, Itemex.db_username, Itemex.db_passwd);
            } else {
                Class.forName("org.sqlite.JDBC");
                Plugin plugin = Bukkit.getPluginManager().getPlugin("Itemex");
                if (plugin != null) {
                    String dbPath = new File(plugin.getDataFolder(), "itemex.db").getAbsolutePath();
                    c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                } else {
                    System.err.println("Could not find plugin Itemex");
                }
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return c;
    }


    public static void createDBifNotExists_mariadb() {
        Connection c = null;
        Statement stmt = null;

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS SELLORDERS " +
                        "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " player_uuid      TEXT, " +
                        " itemid           TEXT, " +
                        " ordertype        TEXT, " +
                        " amount           INT, " +
                        " price            REAL, " +
                        " timestamp        TEXT)";

                String sql2 = "CREATE TABLE IF NOT EXISTS BUYORDERS " +
                        "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " player_uuid      TEXT, " +
                        " itemid           TEXT, " +
                        " ordertype        TEXT, " +
                        " amount           INT, " +
                        " price            REAL, " +
                        " timestamp        TEXT)";

                String sql3 = "CREATE TABLE IF NOT EXISTS FULFILLEDORDERS " +
                        "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " seller_uuid      TEXT, " +
                        " buyer_uuid       TEXT, " +
                        " itemid           TEXT, " +
                        " amount           INT, " +
                        " price            REAL, " +
                        " timestamp        TEXT)";

                String sql4 = "CREATE TABLE IF NOT EXISTS PAYOUTS " +
                        "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " player_uuid      TEXT, " +
                        " itemid           TEXT, " +
                        " amount           INT, " +
                        " timestamp        TEXT)";

                String sql5 = "CREATE TABLE IF NOT EXISTS SETTINGS " +
                        "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " player_uuid           TEXT UNIQUE, " +
                        " set1                  TEXT, " +
                        " set2                  TEXT, " +
                        " set3                  TEXT, " +
                        " set4                  TEXT, " +
                        " set5                  TEXT, " +
                        " set6                  TEXT, " +
                        " withdraw_threshold    INT)";

                String sql6 = "CREATE TABLE IF NOT EXISTS SELL_NOTIFICATION " +
                        "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " player_uuid      TEXT, " +
                        " itemid           TEXT, " +
                        " amount           INT, " +
                        " price            REAL, " +
                        " timestamp        TEXT)";

                stmt.executeUpdate(sql);
                stmt.executeUpdate(sql2);
                stmt.executeUpdate(sql3);
                stmt.executeUpdate(sql4);
                stmt.executeUpdate(sql5);
                stmt.executeUpdate(sql6);

                stmt.close();
                c.close();

            } catch (SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());

            }
        }
    }






    public static void createDBifNotExists() {
        Connection c = null;
        Statement stmt = null;

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                String sql = "CREATE TABLE IF NOT EXISTS SELLORDERS " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT     ," +
                        " player_uuid      TEXT    , " +
                        " itemid           TEXT    , " +
                        " ordertype        TEXT    , " +
                        " amount           TEXT     , " +
                        " price            REAL     , " +
                        " timestamp        TEXT    )";

                String sql2 = "CREATE TABLE IF NOT EXISTS BUYORDERS " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT     ," +
                        " player_uuid      TEXT    , " +
                        " itemid           TEXT    , " +
                        " ordertype        TEXT    , " +
                        " amount           TEXT     , " +
                        " price            REAL     , " +
                        " timestamp        TEXT    )";

                String sql3 = "CREATE TABLE IF NOT EXISTS FULFILLEDORDERS " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT     ," +
                        " seller_uuid      TEXT    , " +
                        " buyer_uuid       TEXT    , " +
                        " itemid           TEXT    , " +
                        " amount           TEXT     , " +
                        " price            TEXT     , " +
                        " timestamp        TEXT    )";

                String sql4 = "CREATE TABLE IF NOT EXISTS PAYOUTS " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT     ," +
                        " player_uuid      TEXT    , " +
                        " itemid           TEXT    , " +
                        " amount           TEXT     , " +
                        " timestamp        TEXT    )";

                String sql5 = "CREATE TABLE IF NOT EXISTS SETTINGS " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT     ," +
                        " player_uuid           TEXT UNIQUE    , " +
                        " set1                  TEXT    , " +
                        " set2                  TEXT    , " +
                        " set3                  TEXT    , " +
                        " set4                  TEXT    , " +
                        " set5                  TEXT    , " +
                        " set6                  TEXT    , " +
                        " withdraw_threshold    INT     )";

                String sql6 = "CREATE TABLE IF NOT EXISTS SELL_NOTIFICATION " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT     ," +
                        " player_uuid      TEXT    , " +
                        " itemid           TEXT    , " +
                        " amount           TEXT     , " +
                        " price            TEXT     , " +
                        " timestamp        TEXT    )";

                stmt.executeUpdate(sql);
                stmt.executeUpdate(sql2);
                stmt.executeUpdate(sql3);
                stmt.executeUpdate(sql4);
                stmt.executeUpdate(sql5);
                stmt.executeUpdate(sql6);

                stmt.close();
                c.close();

            } catch (SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());

            }
        }

    } // end createDatabaseIfNotExists



}
