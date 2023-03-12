package sh.ome.itemex.commands;
import java.sql.*;
import java.time.Instant;

public class sqliteDb {
    private String uuid;
    private String itemid;
    private String ordertype;
    private int    amount;
    private float price;
    private long timestamp;

    public sqliteDb(ItemexCommand.Order sellorder) { //constructor
        uuid = sellorder.uuid;
        itemid = sellorder.itemid.toUpperCase();
        ordertype = sellorder.ordertype;
        amount = sellorder.amount;
        price = sellorder.price;
        timestamp = Instant.now().getEpochSecond();
    }

    public boolean createSellOrder() {
        return insertIntoDB("SELLORDERS");
    }

    public boolean createBuyOrder() {
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


    private boolean insertIntoDB(String table_name) {
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();

            String sql = "INSERT INTO " + table_name + " (player_uuid, itemid, ordertype, amount, price, timestamp) " +
                    "VALUES ('" + this.uuid + "', '" + this.itemid + "', '" + this.ordertype + "', '" + this.amount + "','" + this.price + "',  '" + this.timestamp + "' );";

            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        return true;
    } // end insertIntoDB


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
        System.out.println("UUID: " + player_uuid);
        Payout[] buffer = new Payout[1000000];
        System.out.println("UUID2: " + player_uuid);

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
            System.out.println("ERROR at getPayou()");
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return buffer;

    } // end getPayout



    public static OrderBuffer[] getOrdersOfPlayer(String player_uuid, String itemid, boolean buy_or_sell, int page){
        OrderBuffer[] buffer = new OrderBuffer[100];
        Connection c = null;
        Statement stmt = null;
        String sql;
        int row_counter = 0;

        //proof if buy or sell
        if(buy_or_sell) // is buy
            sql = "SELECT * FROM BUYORDERS WHERE player_uuid = '" + player_uuid + "' ORDER by timestamp ASC LIMIT 100";
        else
            sql = "SELECT * FROM SELLORDERS WHERE player_uuid = '" + player_uuid + "' ORDER by timestamp ASC LIMIT 100";

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();

            stmt.executeUpdate(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getFloat("price"), rs.getLong("timestamp") );
                row_counter++;
            }
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( "at getOrdersOfPlayer: " + e);
            System.exit(0);
        }

        return buffer;
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
                buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getFloat("price"), rs.getLong("timestamp") );
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
                buffer[row_counter] = new OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getFloat("price"), rs.getLong("timestamp") );
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


    public static sqliteDb.OrderBuffer[] selectAll(String table){
        sqliteDb.OrderBuffer[] buffer = new sqliteDb.OrderBuffer[1000000];
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
                buffer[row_counter] = new sqliteDb.OrderBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getFloat("price"), rs.getLong("timestamp") );
                row_counter++;
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return buffer;
    }


    public static boolean updateOrder(String table_name, int ID, int amount, float price, String ordertype) {
        //System.out.println("AT updateORDER: " + table_name + " " +ID + " " + amount + " " + price + " " + ordertype);
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./plugins/Itemex/itemex.db");
            stmt = c.createStatement();
            String sql = "UPDATE " + table_name + " SET ordertype = '" + ordertype + "',  amount = " + amount +  ", price = " + price + " WHERE id = " + ID;

            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        return true;
    } // end insertIntoDB



    public static boolean closeOrder(String table_name, int ID) {
        //System.out.println("AT closeOrder: " + table_name + " " + ID);
        Connection c = null;
        Statement stmt = null;

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
        return true;
    } // end closeOrder









    public static class OrderBuffer{
        public int id;
        public String uuid;
        public String itemid;
        public String ordertype;
        public int amount;
        public float price;
        public long timestamp;

        public OrderBuffer(int id, String uuid, String itemid, String ordertype, int amount, float price, long timestamp)
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
