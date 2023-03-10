package sh.ome.itemex.commands;
import java.sql.*;
import java.time.Instant;

public class sqliteDb {
    private String uuid;
    private String itemid;
    private String ordertype; //sell 0, buy 1
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

    private boolean readFromDB() {
        return true;
    }

    public boolean refreshSqliteDB(sqliteDb data) {
        return true;
    }

    //get and set methods

    public String getItemid() {
        return this.itemid;
    }
    public String getOrdertype() {
        return this.ordertype;
    }
    public int getAmount() {
        return this.amount;
    }
    public long getTimestamp() {
        return this.timestamp;
    }

    public static void createDBifNotExists() {
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
                c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
                c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
        //System.out.println("AT GetPayout");
        Payout[] buffer = new Payout[1000000];
        int row_counter = 0;

        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return buffer;

    } // end getPayout



    public static OrderBuffer[] selectItem(String item){
        OrderBuffer[] buffer = new OrderBuffer[8];
        OrderBuffer[] temp = new OrderBuffer[4];
        int row_counter = 0;

        Connection c = null;
        Statement stmt = null;

        // SELLORDERS
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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


    public static class OrderBuffer{
        public int id;
        public String uuid;
        public String itemid;
        public String ordertype; //sell 0, buy 1
        public int amount;
        public float price;
        public long timestamp;

        OrderBuffer(int id, String uuid, String itemid, String ordertype, int amount, float price, long timestamp)
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
