package sh.ome.itemex.functions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.files.CategoryFile;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;
import static sh.ome.itemex.Itemex.econ;
import static sh.ome.itemex.commands.ItemexCommand.format_price;

public class sqliteDb {
    private static int MAX_BUFFER = 10000000; // 10 MIO
    private String uuid;
    private String itemid;
    private String ordertype;
    private int    amount;
    private double price;
    private long timestamp;



    public sqliteDb(ItemexCommand.Order sellorder) { //constructor
        uuid = sellorder.uuid;
        itemid = sellorder.itemid.toUpperCase();
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


    public static void createDBifNotExists() {
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
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

            stmt.executeUpdate(sql);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql4);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    } // end createDatabaseIfNotExists



    private int insertIntoDB(String table_name) {
        Connection c = null;
        Statement stmt = null;
        int insertedId = -1;
        boolean buy_or_sellorder = true; // buyorder
        if(table_name.equalsIgnoreCase("SELLORDERS"))
            buy_or_sellorder = false; // sellorder


        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
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

            stmt.close();
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

        loadBestOrdersToRam(this.itemid, true);

        return insertedId;
    }



    public static boolean insertFullfilledOrders(String seller_uuid, String buyer_uuid, String itemid, int amount, double price) {
        //System.out.println("AT insertFullfilledOrders");
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();

            String sql = "INSERT INTO FULFILLEDORDERS (seller_uuid, buyer_uuid, itemid, amount, price, timestamp) " +
                    "VALUES ('" + seller_uuid + "', '" + buyer_uuid + "', '" + itemid + "', '" + amount + "','" + price + "',  '" + Instant.now().getEpochSecond() + "' );";

            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        return true;
    } // end insertIntoDB


    public static boolean insertPayout(String player_uuid, String itemid, int amount) {
        //System.out.println("AT INSERTPAYOUT");
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();

            String sql = "INSERT INTO PAYOUTS (player_uuid,  itemid, amount, timestamp) " +
                    "VALUES ('" + player_uuid + "', '" + itemid + "', '" + amount + "', '" + Instant.now().getEpochSecond() + "' );";

            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        return true;
    } // end insertPayout



    public static boolean updatePayout(String player_uuid, String id, String itemid, int amount) {
        //System.out.println("AT UPDATE PAYOUT");
       // System.out.println(player_uuid + " " + id + " " + itemid + " " + amount);

        Connection c = null;
        Statement stmt = null;

        //if amount == 0 -> delete entry -> else update
        if(amount == 0) {
            //System.out.println("remove payout entry");
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
                stmt = c.createStatement();
                String sql = "DELETE FROM PAYOUTS WHERE id = " + id;

                stmt.executeUpdate(sql);
                stmt.close();

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
                return false;
            }
        } // end amount == 0

        else {  //update value
            //System.out.println("update payout entry");
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
                stmt = c.createStatement();
                String sql = "UPDATE PAYOUTS SET amount = " + amount +  " WHERE id = " + id;

                stmt.executeUpdate(sql);
                stmt.close();

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
                return false;
            }
        }
        return true;
    } // end updatePayout



    public static Payout[] getPayout(String player_uuid) {
        //System.out.println("# DEBUG - UUID: " + player_uuid);
        Payout[] buffer = new Payout[MAX_BUFFER];
        //System.out.println("# DEBUG - UUID2: " + player_uuid);

        int row_counter = 0;

        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "SELECT * FROM PAYOUTS WHERE player_uuid = '" + player_uuid + "' ORDER by timestamp";

            stmt.executeUpdate(sql);
            ResultSet rs    = stmt.executeQuery(sql);

            while (rs.next()) {
                buffer[row_counter] = new Payout( rs.getString("id"), rs.getString("itemid"), rs.getInt("amount") );
                row_counter++;
            }
            stmt.close();

        } catch ( Exception e ) {
            //System.out.println("ERROR at getPayou()");
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
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

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();

            stmt.executeUpdate(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );
                row_counter++;
            }
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( "at getOrdersOfPlayer: " + e);
            System.exit(0);
        }
        return buffer;
    }


    public static OrderBuffer getOrder(String ID, boolean buy_or_sell){
        OrderBuffer buffer = null;
        Connection c = null;
        Statement stmt = null;
        String sql;
        int row_counter = 0;

        //proof if buy or sell
        if(buy_or_sell) // is buy
            sql = "SELECT * FROM BUYORDERS WHERE id = '" + ID + "'";
        else
            sql = "SELECT * FROM SELLORDERS WHERE id = '" + ID + "'";

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();

            stmt.executeUpdate(sql);
            ResultSet rs = stmt.executeQuery(sql);
            buffer = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );


            stmt.close();

        } catch ( Exception e ) {
            System.err.println( "at getOrder: " + e);
            System.exit(0);
        }
        return buffer;
    }


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

    public static void loadBestOrdersToRam(String item, boolean update){
        double[] top_buy_price = new double[4];
        double[] top_sell_price = new double[4];
        int[] top_buy_amount = new int[4];
        int[] top_sell_amount = new int[4];
        int row_counter = 0;

        Connection c = null;
        Statement stmt = null;

        // SELLORDERS
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "SELECT * FROM SELLORDERS WHERE itemid = '" + item + "' ORDER by price ASC LIMIT 4";

            stmt.executeUpdate(sql);
            ResultSet rs    = stmt.executeQuery(sql);

            while (rs.next()) {
                top_sell_price[row_counter] = rs.getDouble("price");
                top_sell_amount[row_counter] = rs.getInt("amount");
                //System.out.println("# DEBUG - RROW_COUNTER SELL = " + row_counter + " " + rs.getString("ordertype"));
                row_counter++;

            }
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        row_counter = 0;

        //BUYORDERS
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "SELECT * FROM BUYORDERS WHERE itemid = '" + item + "' ORDER by price DESC LIMIT 4";

            stmt.executeUpdate(sql);
            ResultSet rs    = stmt.executeQuery(sql);

            while (rs.next()) {
                top_buy_price[row_counter] = rs.getDouble("price");
                top_buy_amount[row_counter] = rs.getInt("amount");
                //System.out.println("# DEBUG - ROW_COUNTER BUY= " + row_counter + " " + rs.getString("ordertype"));
                row_counter++;
            }
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if(top_buy_price != null) {
            TopOrders topo = new TopOrders(top_buy_price, top_sell_price, top_buy_amount, top_sell_amount);
            Itemex.getPlugin().mtop.put(item, topo);
            boolean match = topo.find_order_match();
            if(match && update) {
                //System.out.println("# DEBUG - at load best order to ram: item: " + item);
                fulfillOrder(item);
            }
            //System.out.println("GET: " + Itemex.getPlugin().mtop.get(item).get_top_sellorder_prices()[0]);
        }


    }


    public static OrderBuffer[] getBestOrders(String item){
        OrderBuffer[] buffer = new OrderBuffer[8];
        OrderBuffer[] temp = new OrderBuffer[4];
        int row_counter = 0;

        Connection c = null;
        Statement stmt = null;

        // SELLORDERS
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "SELECT * FROM SELLORDERS WHERE itemid = '" + item + "' ORDER by price ASC LIMIT 4";

            stmt.executeUpdate(sql);
            ResultSet rs    = stmt.executeQuery(sql);

            while (rs.next()) {
                buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );
                row_counter++;
            }
            //copy sells into temp
            for(int x=0; x<=3; x++)
                temp[x] = buffer[x];

            int y=0;
            for(int x = row_counter-1; x>=0; x--) {
                buffer[y] = temp[x];
                y++;
            }

            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        //BUYORDERS
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "SELECT * FROM BUYORDERS WHERE itemid = '" + item + "' ORDER by price DESC LIMIT 4";

            stmt.executeUpdate(sql);
            ResultSet rs    = stmt.executeQuery(sql);

            while (rs.next()) {
                buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );
                row_counter++;
            }
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return buffer;
    }



    private static Connection connect() {
        String url = "jdbc:sqlite:./plugins/Itemex/itemex.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return conn;
    }


    public static sqliteDb.OrderBuffer[] selectAllold(String table){
        sqliteDb.OrderBuffer[] buffer = new sqliteDb.OrderBuffer[MAX_BUFFER];
        String sql = null;
        if(table.equals("SELLORDERS")) {
            sql = "SELECT * FROM SELLORDERS ORDER by itemid ASC, price";
        }
        else if(table.equals("BUYORDERS")) {
            sql = "SELECT * FROM BUYORDERS ORDER by itemid ASC, price DESC";
        }

        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            int row_counter = 0;
            while (rs.next()) {
                buffer[row_counter] = new sqliteDb.OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp") );
                row_counter++;
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return buffer;
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

        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                //System.out.println("# DEBUG - id: " + rs.getInt("id") + " itemid: " + rs.getString("itemid") + " price: " + rs.getDouble("price"));
                buffer.add(new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getDouble("price"), rs.getLong("timestamp")));
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return buffer;
    }


    public static boolean updateOrder(String table_name, int ID, int amount, double price, String ordertype, String itemid) {
        //System.out.println("AT updateORDER: " + table_name + " " +ID + " " + amount + " " + price + " " + ordertype);
        Connection c = null;
        Statement stmt = null;
        int update_status = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "UPDATE " + table_name + " SET ordertype = '" + ordertype + "',  amount = " + amount +  ", price = " + price + " WHERE id = " + ID;

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
    } // end insertIntoDB



    public static boolean fulfillOrder(String itemid) { //ERROR INSIDE
        ArrayList<OrderBuffer> sellorders = selectAll("SELLORDERS", itemid);
        ArrayList<OrderBuffer> buyorders = selectAll("BUYORDERS", itemid);
        boolean normal_buyorder = true;
        boolean normal_sellorder = true;

        for (OrderBuffer se : sellorders) {
            //System.out.println("SELLORDER: " + se.ordertype + " [" + se.amount + "] $" + se.price);

            for (OrderBuffer be : buyorders) {
                //System.out.println("BUYORDER: " + itemid + " " + be.ordertype + " [" + be.amount + "] $" + be.price);
                //System.out.println("SELLORDER: " + itemid + " " + se.ordertype + " [" + se.amount + "] $" + se.price);

                // if orders are limit or market
                String[] buy_limit_or_market = be.ordertype.split(":");
                String[] sell_limit_or_market = se.ordertype.split(":");

                // if order == market -> adjust price to best market orders
                if(sell_limit_or_market[1].equals("market")) {
                    se.price = be.price;   // set the best price
                    // WHAT IF NOT be.price available or is 0 -> Remove order!
                }

                if(buy_limit_or_market[1].equals("market")) {
                    be.price = se.price;  // set the best price
                    // WHAT IF NOT se.price available or is 0 -> Remove order!
                }

                if( buy_limit_or_market.length > 2)
                    if(buy_limit_or_market[2].equals("chest")) {
                        normal_buyorder = false;
                    }

                if( sell_limit_or_market.length > 2)
                    if(sell_limit_or_market[2].equals("chest")) {
                        normal_sellorder = false;
                    }


                if (be.price >= se.price && be.amount !=0 && se.amount !=0) { // match found
                    //System.out.println("MATCH AT: be: " + be.id +" ["+ be.amount +"] + se: "+ se.id + "["+ se.amount +"]");

                    // ADMIN ORDER if admin order enabled AND (sell-order OR buy-order is an admin order)
                    // same as limit, but each single amount fullfilled price in- or decreases to spread. Default amount of spread =
                    if( Itemex.admin_function && (sell_limit_or_market[1].equals("admin") || buy_limit_or_market[1].equals("admin")) ) { // sell and buy order cannot be the same time!
                        //System.out.println("# DEBUG: at fulfill order (admin)");
                        if(sell_limit_or_market[1].equals("admin")) {   // if sell order is admin
                            //System.out.println("Sellorder = admin" + se.id + " " + se.amount + " " + se.price);
                        }

                        if(buy_limit_or_market[1].equals("admin")) {    // if buy order is admin
                            //System.out.println("Buyorder = admin" + be.id + " " + be.amount + " " + be.price);
                        }
                    }


                    // LIMIT ORDER OR MARKET ORDER (Marketorder adjusted aboth)
                    else if(se.price <= be.price && se.amount != 0 && be.amount !=0)  { // if sell amount < buy amount than close sell order + update buy order



                        if(se.amount < be.amount) {
                            //System.out.println("# DEBUG: at fulfill order (if sell amount < buy amount than close sell order + update buy order)");
                            if(withdraw(se.uuid, be.uuid, se.itemid, se.amount, se.price, normal_buyorder, be.ordertype) ){ //if buyer have enough money than true
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
                            if(withdraw(se.uuid, be.uuid, se.itemid, be.amount, se.price, normal_buyorder, be.ordertype) ){ //if buyer have enough money than true
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
                            if(withdraw(se.uuid, be.uuid, se.itemid, be.amount, se.price, normal_buyorder, be.ordertype) ){ //if buyer have enough money than true
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
        return false;
    }
    
    
    
    


    public static boolean withdraw(String seller_uuid, String buyer_uuid, String itemid, int amount, double price, boolean normal_buyorder, String be_ordertype) {
        //System.out.println("#DEBUG: AT WITHDRAW");
        OfflinePlayer o_seller =  Bukkit.getOfflinePlayer(UUID.fromString(seller_uuid));
        OfflinePlayer o_buyer =  Bukkit.getOfflinePlayer(UUID.fromString(buyer_uuid));
        Player seller = Bukkit.getPlayer(UUID.fromString(seller_uuid));
        Player buyer = Bukkit.getPlayer(UUID.fromString(buyer_uuid));

        double sub_total = price * amount;
        double buyer_total = sub_total + (sub_total/100*Itemex.broker_fee_buyer);
        double seller_total = sub_total - (sub_total/100*Itemex.broker_fee_seller);
        double buyer_balance = econ.getBalance(o_buyer);

        if( buyer_total < buyer_balance || be_ordertype.equals("refund")) {                     // check if buyer have enough money
            //System.out.println("# DEBUG: Player have enough money" );
            econ.withdrawPlayer(o_buyer, buyer_total);          // subtract money from buyer
            econ.depositPlayer(o_seller, seller_total);         // give money to seller

            //if ordertype contains "chest" ignore insertFullfilledOrders

            sqliteDb.insertFullfilledOrders(seller_uuid, buyer_uuid, itemid, amount, price); // Insert Fullfilled order into db

            if(seller_uuid.equals(buyer_uuid)) {   // REFUND IF PLAYER CLOSES ORDER
                //System.out.println("# DEBUG: Own order");
                if(seller != null) {
                    seller.sendMessage("SELLORDER CLOSED SUCESSFULLY");
                    TextComponent message = new TextComponent("\n.\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + "CLICK HERE" + ChatColor.BLUE + ") You can withdraw with: /ix withdraw " + itemid +" " + amount);
                    message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + itemid +" " + amount));
                    buyer.spigot().sendMessage(message);
                }
                insertPayout(seller_uuid, itemid, amount); // Insert item payout into db
                return true;
            }
            if(normal_buyorder) { // send the item to payouts
                //System.out.println("# DEBUG: withdraw the buy to user -> not insert to ChestShop order");
                if(buyer == null) {
                    //System.out.println("--DEBUG: BUYER IS OFFLINE!");
                    insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
                }
                else {
                    insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
                    buyer.sendMessage("BUY ORDER" + ChatColor.GREEN+ " FULFILLED!" + ChatColor.WHITE + " You got [" + amount + "] "  + itemid + " for" + ChatColor.RED + " " + format_price( buyer_total ) );
                    TextComponent message = new TextComponent("\n.\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + "CLICK HERE" + ChatColor.BLUE + ") You can withdraw with: /ix withdraw " + itemid +" " + amount);
                    message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + itemid +" " + amount));
                    buyer.spigot().sendMessage(message);
                }
            }
            else {  // send the item to chest shop order
                //System.out.println("# DEBUG: Not withdraw the buy to user -> insert to ChestShop order");
                //System.out.println(be_ordertype);
                String[] parts = be_ordertype.split(":");
                OrderBuffer temp = getOrder(parts[3], false);
                updateOrder("SELLORDERS", Integer.parseInt(parts[3]),temp.amount + amount, temp.price, temp.ordertype, temp.itemid);

                if(buyer != null)
                    buyer.sendMessage(ChatColor.GREEN + "Your ChestShop bought: " + ChatColor.WHITE + amount + " [" + itemid + "] for " + format_price(price));
            }

            if(seller == null) {
                //System.out.println("--DEBUG: SELLER IS OFFLINE!");
                //sqliteDb.insertPayout(seller_uuid, itemid, amount); // Insert payout into db
            }
            else {
                seller.sendMessage("SELL ORDER" + ChatColor.GREEN+ " FULFILLED!" + ChatColor.WHITE + " You sold [" + amount + "] "  + itemid + " for" + ChatColor.GREEN + " " + format_price( seller_total ) );
            }


            return true;
        } // end enough money
        else {
            buyer.sendMessage(ChatColor.RED+ "NOT ENOUGH MONEY!" + ChatColor.WHITE + " You got need " + ChatColor.GREEN + format_price( buyer_total ) + ChatColor.WHITE + " but you only have " + ChatColor.RED + " " + format_price( buyer_balance ));
            return false;
        }
    } // end withdraw



    public static boolean closeOrder(String table_name, int ID, String itemid, String ordertype) {
        //System.out.println("# DEBUG - AT closeOrder: " + table_name + " " + ID);
        Connection c = null;
        Statement stmt = null;
        
        if(ordertype.contains("chest")) {}
        else {
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
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
        //System.out.println("AT PlayercloseOrder: " + table_name + " " + ID + " Player_uid: "+ player_uuid);
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
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
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
                //System.out.println("REFUND AMOUNT: " + refund_amount);
                stmt.close();

            } catch ( Exception e ) {
                //System.out.println("ERROR at getPayou()");
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }

        // delete the order
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
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
                    if (withdraw(player_uuid , player_uuid, refund_item_id, refund_amount, refund_price, true, "refund") ) {
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
