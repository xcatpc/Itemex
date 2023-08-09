package sh.ome.itemex.functions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.files.CategoryFile;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.*;

import static org.bukkit.Bukkit.createExplorerMap;
import static org.bukkit.Bukkit.getLogger;
import static sh.ome.itemex.Itemex.econ;
import static sh.ome.itemex.commands.ItemexCommand.*;

public class sqliteDb {
    private static int MAX_BUFFER = 10000000; // 10 MIO
    private String uuid;
    private String itemid;
    private String ordertype;
    private int    amount;
    private double price;
    private long timestamp;


    private static Connection createConnection() {
        Connection c = null;
        if(Itemex.database_type.equals("mariadb")) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://" + Itemex.db_hostname + ":" + Itemex.db_port + "/" + Itemex.db_name;
                c = DriverManager.getConnection(url, Itemex.db_username, Itemex.db_passwd);
            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        else {
            try {
                Class.forName("org.sqlite.JDBC");
                Plugin plugin = Bukkit.getPluginManager().getPlugin("Itemex");
                if (plugin != null) {
                    String dbPath = new File(plugin.getDataFolder(), "itemex.db").getAbsolutePath();
                    c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                } else {
                    System.err.println("Could not find plugin Itemex");
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }





        return c;
    }



    public sqliteDb(ItemexCommand.Order sellorder) { //constructor
        uuid = sellorder.uuid;
        itemid = sellorder.itemid;
        ordertype = sellorder.ordertype;
        amount = sellorder.amount;
        price = sellorder.price;
        timestamp = Instant.now().getEpochSecond();
    }

    public int createSellOrder() {
        return insertIntoDB("SELLORDERS");
    }

    public int createBuyOrder() {
        return insertIntoDB("BUYORDERS");
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
                System.exit(0);
            }
        }

    } // end createDatabaseIfNotExists


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
                System.exit(0);
            }
        }

    } // end createDatabaseIfNotExists




    private int insertIntoDB(String table_name) {
        //System.out.println("# DEBUG: insertIntoDB: itemid: " + this.itemid);
        Connection c = null;
        Statement stmt = null;
        int insertedId = -1;
        boolean buy_or_sellorder = true; // buyorder
        if(table_name.equalsIgnoreCase("SELLORDERS"))
            buy_or_sellorder = false; // sellorder

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO " + table_name + " (player_uuid, itemid, ordertype, amount, price, timestamp) " +
                        "VALUES ('" + this.uuid + "', '" + this.itemid + "', '" + this.ordertype + "', '" + this.amount + "','" + this.price + "',  '" + this.timestamp + "' );";

                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    insertedId = generatedKeys.getInt(1);

                    // charge a listing fee
                    OfflinePlayer o_player =  Bukkit.getOfflinePlayer(UUID.fromString(this.uuid));
                    if(buy_or_sellorder)
                        if(Itemex.buy_listing_fee > 0.0)
                            econ.withdrawPlayer(o_player, Itemex.buy_listing_fee);  // substract amount from player

                        else
                        if(Itemex.sell_listing_fee > 0.0)
                            econ.withdrawPlayer(o_player, Itemex.sell_listing_fee);  // substract amount from player
                }

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
                return -1;
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        loadBestOrdersToRam(this.itemid, true);

        return insertedId;
    }



    public static boolean player_settings(String p_uuid, String settings, boolean player_join) {
        Connection c = null;
        PreparedStatement pstmt = null;
        String sql;
        String withdraw_threshold = settings;
        UUID uuid = UUID.fromString(p_uuid);
        String username = Bukkit.getOfflinePlayer(uuid).getName();

        c = createConnection();
        if (c != null) {
            try {
                if(Itemex.database_type.equals("sqlite"))
                    sql = "INSERT OR IGNORE INTO SETTINGS (player_uuid, set1, withdraw_threshold) VALUES (?, ?, ?);";
                else
                    sql = "INSERT IGNORE INTO SETTINGS (player_uuid, set1, withdraw_threshold) VALUES (?, ?, ?);";

                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, p_uuid);
                pstmt.setString(2, username);
                pstmt.setString(3, "0");
                pstmt.executeUpdate();

                // Whenever the player joins the server, set1 will be updated with the player's current name.
                sql = "UPDATE SETTINGS SET set1 = ? WHERE player_uuid = ?;";
                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, p_uuid);
                pstmt.executeUpdate();

                if(!player_join) {
                    sql = "UPDATE SETTINGS SET withdraw_threshold = ? WHERE player_uuid = ?;";
                    pstmt = c.prepareStatement(sql);
                    pstmt.setString(1, settings);
                    pstmt.setString(2, p_uuid);
                    pstmt.executeUpdate();
                }

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
                return false;
            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }



    public static List<String> getAllUsernamesFromSettings() {
        List<String> usernames = new ArrayList<>();
        Connection c = null;
        PreparedStatement pstmt = null;
        String sql;

        c = createConnection();
        if (c != null) {
            try {
                sql = "SELECT set1 FROM SETTINGS;";
                pstmt = c.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String username = rs.getString("set1");
                    usernames.add(username);
                }

            } catch (SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
                return null;
            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return usernames;
    }





    public static String get_player_settings(String p_uuid) {
        Connection c = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String withdraw_threshold = null;

        c = createConnection();
        if (c != null) {
            try {
                String sql = "SELECT withdraw_threshold FROM SETTINGS WHERE player_uuid = ?;";
                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, p_uuid);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    withdraw_threshold = rs.getString("withdraw_threshold");
                }
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (pstmt != null) {
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return withdraw_threshold;
    }







    public static boolean insertFullfilledOrders(String seller_uuid, String buyer_uuid, String itemid, int amount, double price) {
        Connection c = null;
        Statement stmt = null;

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO FULFILLEDORDERS (seller_uuid, buyer_uuid, itemid, amount, price, timestamp) " +
                        "VALUES ('" + seller_uuid + "', '" + buyer_uuid + "', '" + itemid + "', '" + amount + "','" + price + "',  '" + Instant.now().getEpochSecond() + "' );";

                stmt.executeUpdate(sql);
                stmt.close();
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
                return false;
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // insert last price to ram
        Itemex.getPlugin().mtop.get(itemid).last_price = price;
        return true;
    }



    public static boolean insertPayout(String player_uuid, String itemid, int amount) {
        Connection c = null;
        Statement stmt = null;

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO PAYOUTS (player_uuid,  itemid, amount, timestamp) " +
                        "VALUES ('" + player_uuid + "', '" + itemid + "', '" + amount + "', '" + Instant.now().getEpochSecond() + "' );";

                stmt.executeUpdate(sql);
                stmt.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
                return false;
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }



    public static boolean updatePayout(String player_uuid, String item_json, int amount) {
        //System.out.println("at update Payout json: " + item_json);
        Connection c = null;
        Statement stmt = null;

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                while (amount > 0) {
                    String selectSql = "SELECT id, amount FROM PAYOUTS WHERE player_uuid = '" + player_uuid + "' AND itemid = '" + item_json + "' ORDER BY id ASC LIMIT 1";
                    ResultSet rs = stmt.executeQuery(selectSql);

                    if (!rs.next()) {
                        break; // exit while loop if no more items found
                    }

                    int id = rs.getInt("id");
                    int currentAmount = rs.getInt("amount");

                    if (amount >= currentAmount) {
                        String deleteSql = "DELETE FROM PAYOUTS WHERE id = " + id;
                        stmt.executeUpdate(deleteSql);
                        amount -= currentAmount;
                    } else {
                        int remainingAmount = currentAmount - amount;
                        String updateSql = "UPDATE PAYOUTS SET amount = " + remainingAmount + " WHERE id = " + id;
                        stmt.executeUpdate(updateSql);
                        amount = 0;
                    }

                    rs.close();
                }

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return amount == 0;
    }







    public static Payout[] getPayout(String player_uuid) {
        HashMap<String, Integer> itemCounts = new HashMap<>();
        Connection c = null;
        Statement stmt = null;

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();
                String sql = "SELECT * FROM PAYOUTS WHERE player_uuid = '" + player_uuid + "' ORDER by timestamp";

                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String itemId = rs.getString("itemid");
                    int amount = rs.getInt("amount");

                    // Add amount to the current total for this itemId
                    itemCounts.put(itemId, itemCounts.getOrDefault(itemId, 0) + amount);
                }

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Now convert the map into a Payout array
        Payout[] buffer = new Payout[itemCounts.size()];
        int index = 0;
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            buffer[index++] = new Payout(null, entry.getKey(), entry.getValue());
        }

        return buffer;

    } // end getPayout





    public static OrderBuffer[] getOrdersOfPlayer(String player_uuid, String itemid, boolean buy_or_sell, int page){
        OrderBuffer[] buffer = new OrderBuffer[MAX_BUFFER];
        Connection c = null;
        Statement stmt = null;
        String sql;
        int row_counter = 0;

        String itemid_string = "";
        if(!itemid.equals("*"))
            itemid_string = "AND itemid = '" + itemid + "'";

        //proof if buy or sell
        if(buy_or_sell) // is buy
            sql = "SELECT * FROM BUYORDERS WHERE player_uuid = '" + player_uuid + "' " + itemid_string + " ORDER by timestamp ASC";
        else
            sql = "SELECT * FROM SELLORDERS WHERE player_uuid = '" + player_uuid + "' " + itemid_string + " ORDER by timestamp ASC";

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                stmt.executeUpdate(sql);
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );
                    row_counter++;
                }

            } catch ( Exception e ) {
                System.err.println( "at getOrdersOfPlayer: " + e);
                System.exit(0);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return buffer;
    }



    public static OrderBuffer getOrder(String ID, boolean buy_or_sell){
        OrderBuffer buffer = null;
        Connection c = null;
        Statement stmt = null;
        String sql;

        //proof if buy or sell
        if(buy_or_sell) // is buy
            sql = "SELECT * FROM BUYORDERS WHERE id = '" + ID + "'";
        else
            sql = "SELECT * FROM SELLORDERS WHERE id = '" + ID + "'";

        c = createConnection();
        if (c != null) {
            try {
                stmt = c.createStatement();

                stmt.executeUpdate(sql);
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    buffer = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );
                }

            } catch ( Exception e ) {
                System.err.println( "at getOrder: " + e);
                System.exit(0);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return buffer;
    }


    /*

    public static void loadAllBestOrdersToRam(boolean update){

        int amount = 0;
        // GET CATEGORIES FROM CONFIG
        for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
            String[] category = cat.split(":", 0);
            // GET ITEMS
            for (String itemid : CategoryFile.get().getStringList("categories." + category[0])) {
                loadBestOrdersToRam(itemid, update);
                amount++;
            }
        }
        getLogger().info("\u001B[32m" + amount +" items loaded into RAM!\u001B[37m == (" + amount*8+ " orders)");
    }
    */

    public static void loadAllBestOrdersToRam(boolean update){
        int amount = 0;

        try (Connection c = createConnection()) {
            // GET CATEGORIES FROM CONFIG
            for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                String[] category = cat.split(":", 0);
                // GET ITEMS
                for (String itemid : CategoryFile.get().getStringList("categories." + category[0])) {
                    loadBestOrdersToRam(itemid, update, c);  // Pass the connection here
                    amount++;
                }
            }
            getLogger().info("\u001B[32m" + amount +" items loaded into RAM!\u001B[37m == (" + amount*8+ " orders)");
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


    public static void loadBestOrdersToRam(String item, boolean update){
        loadBestOrdersToRam(item, update, null);
    }

    public static void loadBestOrdersToRam(String item, boolean update, Connection providedConnection){
        double[] top_buy_price = new double[5];
        double[] top_sell_price = new double[5];
        int[] top_buy_amount = new int[5];
        int[] top_sell_amount = new int[5];
        int row_counter = 0;

        boolean shouldCloseConnection = (providedConnection == null);
        Connection c = providedConnection != null ? providedConnection : createConnection();

        // SELLORDERS
        try {
            PreparedStatement stmt = c.prepareStatement("SELECT * FROM SELLORDERS WHERE itemid = ? ORDER by price ASC LIMIT 20");
            stmt.setString(1, item);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double temp_price = rs.getDouble("price");
                int temp_amount = rs.getInt("amount");

                if( row_counter > 0 && temp_price == top_sell_price[row_counter-1]) {
                    top_sell_amount[row_counter-1] = top_sell_amount[row_counter-1] + temp_amount;
                } else {
                    top_sell_price[row_counter] = temp_price;
                    top_sell_amount[row_counter] = temp_amount;
                    row_counter++;
                }
                if(row_counter > 3)
                    break;
            }

        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            if (shouldCloseConnection) {
                try { c.close(); } catch (Exception ignored) {}
            }
            System.exit(0);
        }

        row_counter = 0;

        // BUYORDERS
        try {
            PreparedStatement stmt = c.prepareStatement("SELECT * FROM BUYORDERS WHERE itemid = ? ORDER by price DESC LIMIT 20");
            stmt.setString(1, item);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double temp_price = rs.getDouble("price");
                int temp_amount = rs.getInt("amount");

                if( row_counter > 0 && temp_price == top_buy_price[row_counter-1]) {
                    top_buy_amount[row_counter-1] = top_buy_amount[row_counter-1] + temp_amount;
                } else {
                    top_buy_price[row_counter] = temp_price;
                    top_buy_amount[row_counter] = temp_amount;
                    row_counter++;
                }
                if(row_counter > 3)
                    break;
            }

        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            if (shouldCloseConnection) {
                try { c.close(); } catch (Exception ignored) {}
            }
            System.exit(0);
        }
        if(top_buy_price != null) {
            boolean match = false;

            String[] trades = get_last_trades(item, "4");
            double[] last_trade_price = new double[trades.length];
            int[] last_trade_timestamp = new int[trades.length];

            for (int i = 0; i < trades.length; i++) {
                String[] parts = trades[i].split(":");
                last_trade_timestamp[i] = Integer.parseInt(parts[0]);
                last_trade_price[i] = Double.parseDouble(parts[1]);
            }

            // If item entry not exists
            if( Itemex.getPlugin().mtop.get(item) == null) {
                TopOrders topo = new TopOrders(item, top_buy_price, top_sell_price, top_buy_amount, top_sell_amount, last_trade_price, last_trade_timestamp);
                Itemex.getPlugin().mtop.put(item, topo);
            }
            else {
                Itemex.getPlugin().mtop.get(item).update_topOrders(item, top_buy_price, top_sell_price, top_buy_amount, top_sell_amount, last_trade_price, last_trade_timestamp);
            }

            match = Itemex.getPlugin().mtop.get(item).find_order_match();

            if(match && update) {
                fulfillOrder(item);
            }
        }
    }

    /*
    public static void loadBestOrdersToRam(String item, boolean update){
        double[] top_buy_price = new double[5];
        double[] top_sell_price = new double[5];
        int[] top_buy_amount = new int[5];
        int[] top_sell_amount = new int[5];
        int row_counter = 0;

        // SELLORDERS
        try (Connection c = createConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT * FROM SELLORDERS WHERE itemid = ? ORDER by price ASC LIMIT 20");
            stmt.setString(1, item);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double temp_price = rs.getDouble("price");
                int temp_amount = rs.getInt("amount");

                if( row_counter > 0 && temp_price == top_sell_price[row_counter-1]) {
                    top_sell_amount[row_counter-1] = top_sell_amount[row_counter-1] + temp_amount;
                }
                else {
                    top_sell_price[row_counter] = temp_price;
                    top_sell_amount[row_counter] = temp_amount;
                    row_counter++;
                }
                if(row_counter > 3)
                    break;
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        row_counter = 0;

        // BUYORDERS
        try (Connection c = createConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT * FROM BUYORDERS WHERE itemid = ? ORDER by price DESC LIMIT 20");
            stmt.setString(1, item);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double temp_price = rs.getDouble("price");
                int temp_amount = rs.getInt("amount");

                if( row_counter > 0 && temp_price == top_buy_price[row_counter-1]) {
                    top_buy_amount[row_counter-1] = top_buy_amount[row_counter-1] + temp_amount;
                }
                else {
                    top_buy_price[row_counter] = temp_price;
                    top_buy_amount[row_counter] = temp_amount;
                    row_counter++;
                }
                if(row_counter > 3)
                    break;
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if(top_buy_price != null) {
            boolean match = false;

            String[] trades = get_last_trades(item, "4");
            double[] last_trade_price = new double[trades.length];
            int[] last_trade_timestamp = new int[trades.length];

            for (int i = 0; i < trades.length; i++) {
                String[] parts = trades[i].split(":");
                last_trade_timestamp[i] = Integer.parseInt(parts[0]);
                last_trade_price[i] = Double.parseDouble(parts[1]);
            }

            // If item entry not exists
            if( Itemex.getPlugin().mtop.get(item) == null) {
                TopOrders topo = new TopOrders(item, top_buy_price, top_sell_price, top_buy_amount, top_sell_amount, last_trade_price, last_trade_timestamp);
                Itemex.getPlugin().mtop.put(item, topo);
            }
            else {
                Itemex.getPlugin().mtop.get(item).update_topOrders(item, top_buy_price, top_sell_price, top_buy_amount, top_sell_amount, last_trade_price, last_trade_timestamp);
            }

            match = Itemex.getPlugin().mtop.get(item).find_order_match();

            if(match && update) {
                fulfillOrder(item);
            }
        }
    }

     */





    public static String[] get_last_trades(String item_json, String max_entries) {
        Connection conn = null;
        Statement stmt = null;
        List<String> trades = new ArrayList<String>();

        try {
            conn = createConnection();

            stmt = conn.createStatement();
            String sql = "SELECT * FROM FULFILLEDORDERS WHERE itemid = '" + item_json + "' ORDER by timestamp ASC LIMIT " + max_entries;
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                double temp_price = rs.getDouble("price");
                String temp_timestamp = rs.getString("timestamp");
                trades.add(temp_timestamp + ":" + temp_price);
            }

            rs.close();
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        while (trades.size() < 4) {
            trades.add("0:0");
        }

        return trades.toArray(new String[0]);
    }




    public static ArrayList<OrderBuffer> selectAll(String table, String itemid){
        ArrayList<OrderBuffer> buffer = new ArrayList<>();

        String sql = null;
        if(table.equals("SELLORDERS")) {
            sql = "SELECT * FROM SELLORDERS WHERE itemid = '" + itemid + "' ORDER by price ASC";
        }
        else if(table.equals("BUYORDERS")) {
            sql = "SELECT * FROM BUYORDERS WHERE itemid = '" + itemid + "' ORDER by price DESC";
        }

        try (Connection conn = createConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                buffer.add(new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp")));
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return buffer;
    }



    public static boolean updateOrder(String table_name, int ID, int amount, double price, String ordertype, String itemid) {
        //System.out.println("# DEBUG - updateOrder");
        Connection c = null;
        Statement stmt = null;
        int update_status = 0;
        String sql;

        if(ordertype.contains("admin")) {}

        else {
            try {
                c = createConnection();
                stmt = c.createStatement();
                if(price > 0)
                     sql = "UPDATE " + table_name + " SET ordertype = '" + ordertype + "',  amount = " + amount +  ", price = " + price + " WHERE id = " + ID;
                else { // chest shop
                     sql = "UPDATE " + table_name + " SET amount = " + amount + " WHERE id = " + ID;
                }


                update_status = stmt.executeUpdate(sql);
                stmt.close();

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
                return false;
            }
            loadBestOrdersToRam(itemid, false);
            if(update_status == 1)
                return true;
            else
                return false;
        }

        return true;
    } // end insertIntoDB




    public static boolean fulfillOrder(String itemid) {
        ArrayList<OrderBuffer> sellorders = selectAll("SELLORDERS", itemid);
        ArrayList<OrderBuffer> buyorders = selectAll("BUYORDERS", itemid);
        boolean normal_buyorder = true;
        boolean normal_sellorder = true;

        // if admin orders is enabled
        if(Itemex.admin_function) {
            OrderBuffer admin_buy_order = new OrderBuffer(-1, "", itemid, "buy:limit:admin", 1, Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[4], Instant.now().getEpochSecond());
            OrderBuffer admin_sell_order = new OrderBuffer(-1, "", itemid, "sell:limit:admin", 1, Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[4], Instant.now().getEpochSecond());
            sellorders.add( admin_sell_order );
            buyorders.add( admin_buy_order );
        }

        for (OrderBuffer se : sellorders) {
            //System.out.println("SELLORDER: " + se.ordertype + " [" + se.amount + "] $" + se.price);

            for (OrderBuffer be : buyorders) {

                //System.out.println("BUYORDER: " + itemid + " " + be.ordertype + " [" + be.amount + "] $" + be.price);
                //System.out.println("SELLORDER: " + itemid + " " + se.ordertype + " [" + se.amount + "] $" + se.price);

                // if orders are limit or market
                String[] buy_limit_or_market = be.ordertype.split(":");
                String[] sell_limit_or_market = se.ordertype.split(":");

                // if order == market -> adjust price to best market orders
                if(sell_limit_or_market[1].equals("market"))
                    se.price = be.price;   // set the best price

                if(buy_limit_or_market[1].equals("market"))
                    be.price = se.price;  // set the best price

                if( buy_limit_or_market.length > 2)
                    if(buy_limit_or_market[2].equals("chest") || buy_limit_or_market[2].equals("admin") )
                        normal_buyorder = false;

                if( sell_limit_or_market.length > 2)
                    if(sell_limit_or_market[2].equals("chest") || sell_limit_or_market[2].equals("admin"))
                        normal_sellorder = false;


                if (be.price >= se.price && be.amount !=0 && se.amount !=0) { // match found
                    //System.out.println("MATCH AT: be: " + be.id +" ["+ be.amount +"] + se: "+ se.id + "["+ se.amount +"]");

                    // LIMIT ORDER OR MARKET ORDER (Marketorder adjusted aboth)
                    if(se.price <= be.price && se.amount != 0 && be.amount !=0)  { // if sell amount < buy amount than close sell order + update buy order


                        if(se.amount < be.amount) {
                            //System.out.println("# DEBUG: at fulfill order (if sell amount < buy amount than close sell order + update buy order)");
                            if(withdraw(se.uuid, be.uuid, se.itemid, se.amount, se.price, be.ordertype, se.ordertype) ){ //if buyer have enough money than true
                                be.amount = be.amount - se.amount;
                                if(be.ordertype.equals("buy:market"))
                                    be.price = Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0];
                                updateOrder("BUYORDERS", be.id, be.amount, be.price, be.ordertype, itemid);
                                se.amount = 0; //close sell order
                                if(normal_sellorder)
                                    closeOrder("SELLORDERS", se.id, itemid, se.ordertype);
                                else
                                    updateOrder("SELLORDERS", se.id, 0, se.price, se.ordertype, itemid);

                            }
                            else { // remove order if buyer don't have enough money
                                be.amount = 0; //close buy order
                                if(normal_buyorder)
                                    closeOrder("BUYORDERS", be.id, itemid, be.ordertype);
                                else
                                    updateOrder("BUYORDERS", be.id, 0, be.price, be.ordertype, itemid);
                            }
                        }


                        else if(se.amount > be.amount) { // if sell amount > buy amount than update sell order + close buy order
                            //System.out.println("# DEBUG: at fulfill order (if sell amount > buy amount than update sell order + close buy order)");
                            if(withdraw(se.uuid, be.uuid, se.itemid, be.amount, se.price, be.ordertype, se.ordertype) ){ //if buyer have enough money than true
                                se.amount = se.amount - be.amount;
                                if(be.ordertype.equals("sell:market"))
                                    se.price = Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[3];
                                updateOrder("SELLORDERS", se.id, se.amount, se.price, se.ordertype, itemid);
                                be.amount = 0; //close buy order
                                if(normal_buyorder)
                                    closeOrder("BUYORDERS", be.id, itemid, be.ordertype);
                                else
                                    updateOrder("BUYORDERS", be.id, 0, be.price, be.ordertype, itemid);
                            }
                            else { // remove order if buyer don't have enough money
                                be.amount = 0; //close buy order
                                if(normal_buyorder)
                                    closeOrder("BUYORDERS", be.id, itemid, be.ordertype);
                                else
                                    updateOrder("BUYORDERS", be.id, 0, be.price, be.ordertype, itemid);
                            }
                        }

                        // if sell amount == buy amount than close buy and sell order
                        else if(se.amount == be.amount) {
                            //System.out.println("# DEBUG: at fulfill order (if sell amount == buy amount than close buy and sell order)");
                            if(withdraw(se.uuid, be.uuid, se.itemid, be.amount, se.price, be.ordertype, se.ordertype) ){ //if buyer have enough money than true
                                be.amount = 0;
                                if(normal_buyorder)
                                    closeOrder("BUYORDERS", be.id, itemid, be.ordertype);
                                else
                                    updateOrder("BUYORDERS", be.id, 0, be.price, be.ordertype, itemid);
                                if(normal_sellorder)
                                    closeOrder("SELLORDERS", se.id, itemid, se.ordertype);
                                else
                                    updateOrder("SELLORDERS", se.id, 0, se.price, se.ordertype, itemid);

                            }
                            else { // remove order if buyer don't have enough money
                                be.amount = 0; //close buy order
                                if(normal_buyorder)
                                    closeOrder("BUYORDERS", be.id, itemid, be.ordertype);
                                else
                                    updateOrder("BUYORDERS", be.id, 0, be.price, be.ordertype, itemid);
                            }
                        }
                    }
                } // end if price matches
            } // end buyorders loop
        } // end sellorders loop
        return true;
    }






    public static boolean withdraw(String seller_uuid, String buyer_uuid, String itemid, int amount, double price, String be_ordertype, String se_ordertype) {
        System.out.println("# DEBUG: AT WITHDRAW itemid: " + itemid + ":" + be_ordertype + ":" + se_ordertype);
        System.out.println("# DEBUG - seller_uuid: " + seller_uuid);
        System.out.println("# DEBUG: - buyer_uuid: " + buyer_uuid);

        double sub_total = price * amount;
        double buyer_total = sub_total + (sub_total/100*Itemex.broker_fee_buyer);
        double seller_total = sub_total - (sub_total/100*Itemex.broker_fee_seller);

        OfflinePlayer o_seller = Bukkit.getOfflinePlayer(UUID.fromString(seller_uuid));
        OfflinePlayer o_buyer = Bukkit.getOfflinePlayer(UUID.fromString(buyer_uuid));
        Player seller = Bukkit.getPlayer(UUID.fromString(seller_uuid));
        Player buyer = Bukkit.getPlayer(UUID.fromString(buyer_uuid));

        double buyer_balance = econ.getBalance(o_buyer);


        if(be_ordertype.contains("admin")) { // buyorder is admin
            econ.depositPlayer(o_seller, seller_total);         // give money to seller
            if(seller != null)
                seller.sendMessage("" + Itemex.language.getString("sellorder_C") + ChatColor.GREEN+ Itemex.language.getString("sq_fulfilled") + ChatColor.WHITE + Itemex.language.getString("sq_you_sold") + " [" + amount + "] "  + ItemexCommand.get_meta(itemid) + Itemex.language.getString("sq_for") + ChatColor.GREEN + " " + format_price( seller_total ) );

            insertFullfilledOrders(seller_uuid, "admin", itemid, amount, price); // Insert Fullfilled order into db
            return true;
        }
        else if(se_ordertype.contains("admin")) { // sellorder is admin
            if( buyer_total < buyer_balance) {
                econ.withdrawPlayer(o_buyer, buyer_total);          // subtract money from buyer
                if(buyer == null) {
                    insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
                }
                else {
                    insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
                    buyer.sendMessage("" + Itemex.language.get("buyorder_C") + ChatColor.GREEN+ Itemex.language.getString("sq_fulfilled") + ChatColor.WHITE + " " + Itemex.language.getString("sq_you_got") + " [" + amount + "] "  + ItemexCommand.get_meta(itemid) + Itemex.language.getString("sq_for") + ChatColor.RED + " " + format_price( buyer_total ) );
                    TextComponent message = new TextComponent("\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("sq_you_can_with") + " /ix withdraw " + get_meta(itemid) + " " + amount);
                    message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + get_meta(itemid) + " " + amount));
                    buyer.spigot().sendMessage(message);
                }
            }
            else {
                buyer.sendMessage(ChatColor.RED+ Itemex.language.getString("not_enough_money") + ChatColor.WHITE + Itemex.language.getString("sq_you_need") + ChatColor.GREEN + format_price( buyer_total ) + ChatColor.WHITE + Itemex.language.getString("sq_but_you_only_have") + ChatColor.RED + " " + format_price( buyer_balance ));
                return false;
            }

            insertFullfilledOrders("admin", buyer_uuid, itemid, amount, price); // Insert Fullfilled order into db
            return true;
        }

        else if(be_ordertype.contains("chest")) { // buyorder chest
            System.out.println("IS A CHEST: " + be_ordertype);
            String[] parts = be_ordertype.split(":");
            OrderBuffer temp = getOrder(parts[3], false);
            updateOrder("SELLORDERS", Integer.parseInt(parts[3]),temp.amount + amount, temp.price, temp.ordertype, temp.itemid);

            if(buyer != null)
                buyer.sendMessage(ChatColor.GREEN + "Your ChestShop bought: " + ChatColor.WHITE + amount + " [" + get_meta(itemid) + "] " + Itemex.language.getString("sq_for") + " " + format_price(price));
        }

        else if(se_ordertype.contains("chest")) { // sellorder chest
            System.out.println("IS A CHEST: " + se_ordertype);
            String[] parts = se_ordertype.split(":");
            OrderBuffer temp = getOrder(parts[3], true);
            updateOrder("BUYORDERS", Integer.parseInt(parts[3]),temp.amount + amount, temp.price, temp.ordertype, temp.itemid);

            if(buyer != null)
                buyer.sendMessage(ChatColor.GREEN + "Your ChestShop bought: " + ChatColor.WHITE + amount + " [" + get_meta(itemid) + "] " + Itemex.language.getString("sq_for") + " " + format_price(price));
        }


        // NORMAL ORDERS

        else if( buyer_total <= buyer_balance || be_ordertype.equals("refund")) {                     // check if buyer have enough money
            //System.out.println("# DEBUG: Player have enough money" );
            econ.withdrawPlayer(o_buyer, buyer_total);          // subtract money from buyer
            econ.depositPlayer(o_seller, seller_total);         // give money to seller

            insertFullfilledOrders(seller_uuid, buyer_uuid, itemid, amount, price); // Insert Fullfilled order into db

            if(seller_uuid.equals(buyer_uuid) && !be_ordertype.contains("chest")) {   // REFUND IF PLAYER CLOSES ORDER
                //System.out.println("# DEBUG: Own order");
                if(seller != null) {
                    seller.sendMessage(Itemex.language.getString("sq_closed_succ"));
                    TextComponent message = new TextComponent("\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("sq_you_can_with") + " /ix withdraw " + get_meta(itemid) + " " + amount);
                    message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + get_meta(itemid) + " " + amount));
                    buyer.spigot().sendMessage(message);
                }
                insertPayout(seller_uuid, itemid, amount); // Insert item payout into db
                return true;
            }
            if(!be_ordertype.contains("chest")) { // send the item to payouts == normal buyorder
                //System.out.println("# DEBUG: withdraw the buy to user -> not insert to ChestShop order");
                if(buyer == null) {
                    //System.out.println("--DEBUG: BUYER IS OFFLINE!");
                    insertPayout(buyer_uuid, itemid, amount);

                }
                else {
                    int withdraw_threshold = Integer.parseInt(get_player_settings(buyer_uuid));

                    // DIRECT PAYOUT
                    String[] results =  ItemexCommand.getFreeInventory(buyer, itemid).split(":");;
                    int player_max_items = Integer.parseInt(results[2]);

                    if(amount > withdraw_threshold) {
                        //buyer.sendMessage("1 player_max_items: " + player_max_items + " amount: " + amount);
                        insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
                    }

                    else if(amount <= withdraw_threshold) {
                        //buyer.sendMessage("2 player_max_items: " + player_max_items + " amount: " + amount);
                        ItemStack item2 = constructItem(itemid, amount);
                        buyer.getInventory().addItem(item2);
                    }

                    else {
                        // payout directly amount = withdraw_threshold
                        buyer.sendMessage("error");
                    }

                    buyer.sendMessage("" + Itemex.language.get("buyorder_C") + ChatColor.GREEN+ Itemex.language.getString("sq_fulfilled") + ChatColor.WHITE + " " + Itemex.language.getString("sq_you_got") + " [" + amount + "] "  + ItemexCommand.get_meta(itemid) + Itemex.language.getString("sq_for") + ChatColor.RED + " " + format_price( buyer_total ) );
                    TextComponent message = new TextComponent("\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("sq_you_can_with") + " /ix withdraw " + get_meta(itemid) + " " + amount);
                    message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + get_meta(itemid) + " " + amount));
                    buyer.spigot().sendMessage(message);
                }
            }




            if(seller == null) {
                //System.out.println("--DEBUG: SELLER IS OFFLINE!");
            }
            else {
                seller.sendMessage("" + Itemex.language.getString("sellorder_C") + ChatColor.GREEN+ Itemex.language.getString("sq_fulfilled") + ChatColor.WHITE + Itemex.language.getString("sq_you_sold") + " [" + amount + "] "  + ItemexCommand.get_meta(itemid) + Itemex.language.getString("sq_for") + ChatColor.GREEN + " " + format_price( seller_total ) );
            }


            return true;
        } // end enough money
        else {
            buyer.sendMessage(ChatColor.RED+ Itemex.language.getString("not_enough_money") + ChatColor.WHITE + Itemex.language.getString("sq_you_need") + ChatColor.GREEN + format_price( buyer_total ) + ChatColor.WHITE + Itemex.language.getString("sq_but_you_only_have") + ChatColor.RED + " " + format_price( buyer_balance ));
            return false;
        }
        return true;
    } // end withdraw



    public static boolean closeOrder(String table_name, int ID, String itemid, String ordertype) {
        Connection c = null;
        Statement stmt = null;

        if(ordertype.contains("chest")) {}
        else if(ordertype.contains("admin")) {}
        else {
            try {
                c = createConnection();
                stmt = c.createStatement();
                String sql = "DELETE FROM " + table_name + " WHERE id = " + ID;

                stmt.executeUpdate(sql);
                stmt.close();

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
                return false;
            }
        }
        loadBestOrdersToRam(itemid, false);
        return true;
    } // end closeOrder



    public static boolean PlayercloseOrder(String player_uuid, String table_name, int ID) {
        Connection c = null;
        Statement stmt = null;
        int row_affected = 0;
        int refund_amount = 0;
        String refund_item_id = "";
        double refund_price = 0;
        String itemid = "";
        String se_ordertype = null, be_ordertype;

        // get the refund_amount of order
        if(table_name.equals("SELLORDERS")) {
            try {
                c = createConnection();
                stmt = c.createStatement();
                String sql = "SELECT * FROM SELLORDERS WHERE id = '" + ID + "'";

                stmt.executeUpdate(sql);
                ResultSet rs    = stmt.executeQuery(sql);

                while (rs.next()) {
                    refund_amount = rs.getInt("amount");
                    refund_item_id = rs.getString("itemid");
                    refund_price = rs.getDouble("price");
                    itemid = rs.getString("itemid");
                    se_ordertype = rs.getString("itemid");
                }
                stmt.close();

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }

        // delete the order
        try {
            c = createConnection();
            stmt = c.createStatement();
            String sql = "DELETE FROM " + table_name + " WHERE id = " + ID + " AND player_uuid = '" + player_uuid + "'";

            row_affected = stmt.executeUpdate(sql);
            stmt.close();


        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        loadBestOrdersToRam(itemid, true);
        if(row_affected == 1) {
            // refund
            if(table_name.equals("SELLORDERS")) {
                if(refund_amount != 0) {
                    if (withdraw(player_uuid , player_uuid, refund_item_id, refund_amount, refund_price,  "refund", "")) {
                        return true;
                    }
                    else return false;
                }
                else return true;
            }
            return true;
        }

        return false;
    } // end closeOrder



    static public String getLastPrice(String itemid) {
        Statement stmt = null;
        String sql;
        Connection c = null;
        String lastPrice = "0";

        sql = "SELECT * FROM FULFILLEDORDERS WHERE itemid = '" + itemid + "' ORDER by timestamp DESC LIMIT 1";

        try {
            c = createConnection();
            stmt = c.createStatement();

            stmt.executeUpdate(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String tmp = "";
                if(rs.getString("seller_uuid").contains("admin"))
                    tmp = "admin_seller";
                else if(rs.getString("buyer_uuid").contains("admin"))
                    tmp = "admin_buyer";

                lastPrice = rs.getString("price") + ":" + tmp;
            }
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( "- ERROR: at getLastPrice: " + e);
            System.exit(0);
            return "0";
        }

        return lastPrice;
    }

    public static List<String> getAllFulfilledOrders(String itemid) {
        PreparedStatement stmt = null;
        Connection c = null;
        List<String> result = new ArrayList<>();

        String sql = "SELECT price, timestamp FROM FULFILLEDORDERS WHERE itemid = ? ORDER BY timestamp DESC";

        try {
            c = createConnection();
            stmt = c.prepareStatement(sql);

            stmt.setString(1, itemid);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String price = rs.getString("price");
                String timestamp = rs.getString("timestamp");

                result.add(price + ":" + timestamp);
            }

        } catch (Exception e) {
            System.err.println("- ERROR: at getAllFulfilledOrders: " + e);
            System.exit(0);
            return Collections.emptyList();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    static public boolean check_if_db_is_JSON() {
        Connection c = null;
        Statement stmt = null;
        String sql = "SELECT * FROM SELLORDERS ORDER BY timestamp ASC LIMIT 1";
        boolean is_json_format = false;

        try {
            c = createConnection();
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (rs.getString("itemid").startsWith("[{")) {
                    is_json_format = true;
                    break;
                }
            }
            stmt.close();

        } catch (Exception e) {
            System.err.println("at check_if_db_is_JSON: " + e);
            System.exit(0);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return is_json_format;
    }

    public static void updateDB_from_STRING_to_JSON(String tableName, String fieldName) {
        Connection c = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;

        try {
            c = createConnection();

            // Select all entries from the table
            String selectSql = "SELECT id, " + fieldName + " FROM " + tableName;
            selectStmt = c.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();

            // Prepare the update statement
            String updateSql = "UPDATE " + tableName + " SET " + fieldName + " = ? WHERE id = ?";
            updateStmt = c.prepareStatement(updateSql);

            // Loop through the result set and update each row
            while (rs.next()) {
                int id = rs.getInt("id");
                String oldItemId = rs.getString(fieldName);

                // Convert to the new format
                String newItemId = "[{\"itemid\":\"" + oldItemId + "\"}]";

                // Set parameters and execute the update statement
                updateStmt.setString(1, newItemId);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
            }

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        } finally {
            try {
                if (selectStmt != null) {
                    selectStmt.close();
                }
                if (updateStmt != null) {
                    updateStmt.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    public static boolean insert_sellNotification(String player_uuid, String itemid, double price, int amount) {
        System.out.println("AT INSERTPAYOUTinsert_sellNotification");
        Connection c = null;
        Statement stmt = null;

        try {
            c = createConnection();

            stmt = c.createStatement();

            String sql = "INSERT INTO SELL_NOTIFICATION (player_uuid, itemid, amount, price, timestamp) " +
                    "VALUES ('" + player_uuid + "', '" + itemid + "', '" + amount + "', '" + price + "', '" + Instant.now().getEpochSecond() + "' );";

            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        return true;
    } // end insert_sellNotification


    public static List<Map<String, Object>> get_sellNotification(String playerUUID) {
        Connection c = null;
        Statement stmt = null;
        List<Map<String, Object>> sellNotifications = new ArrayList<>();

        try {
            c = createConnection();

            stmt = c.createStatement();

            String sql = "SELECT * FROM SELL_NOTIFICATION WHERE player_uuid = '"+playerUUID+"'";

            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                sellNotifications.add(row);
            }
            rs.close();
            stmt.executeUpdate("DELETE FROM SELL_NOTIFICATION WHERE player_uuid = '"+playerUUID+"'");

        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return Collections.emptyList();
        }

        return sellNotifications;
    }





    public static class OrderBuffer{
        public int id;
        public String uuid;
        public String itemid;
        public String ordertype;
        public int amount;
        public double price;
        public long timestamp;

        public OrderBuffer(int id, String uuid, String itemid, String ordertype, int amount, double price, long timestamp)
        {
            this.id = id;
            this.uuid = uuid;
            this.itemid = itemid;
            this.ordertype = ordertype;
            this.amount = amount;
            this.price = price;
            this. timestamp = timestamp;
        }
    }



    public static class Payout{
        public String id;
        public int amount;
        public String itemid;

        Payout(String id, String itemid, int amount) {
            this.id = id;
            this.amount = amount;
            this.itemid = itemid;
        }
    }



}