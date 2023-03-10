//TODO
// if buyer have not enough money then close order
// new orders must be sort down (Because old orders should be fulfilled first if price is equal)

package sh.ome.itemex.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.sql.*;
import java.util.UUID;

import static sh.ome.itemex.Itemex.econ;

public class FulfillOrder {

    private String uuid;
    private String itemid;
    private String ordertype; //sell 0, buy 1
    private int    amount;
    private float price;
    private long timestamp;

    public FulfillOrder() throws SQLException {
        //System.out.println("TICK #");
        // load buy and sell orders into RAM
        DbEntryBuffer[] sellorders = selectAll("SELLORDERS");
        DbEntryBuffer[] buyorders = selectAll("BUYORDERS");

        // FIND AND FULFILL ORDERS
        for(int x=0; x<1000000-1; x++) {
            if(sellorders[x] == null) { break;}
            else {
                for(int y=0; y<1000000-1; y++) {
                    if(buyorders[y] == null) { break;}
                    else {
                        if(sellorders[x].itemid.equals( buyorders[y].itemid )) { //check if itemid matches
                            if(sellorders[x].price <= buyorders[y].price && sellorders[x].amount != 0 && buyorders[y].amount !=0)  { // if sell amount < buy amount than close sell order + update buy order
                                if(sellorders[x].amount < buyorders[y].amount) {  //if buyer have enough money than true
                                    if(withdraw(sellorders[x].uuid, buyorders[y].uuid, sellorders[x].itemid, sellorders[x].amount, sellorders[x].price) ){
                                        buyorders[y].amount = buyorders[y].amount - sellorders[x].amount;
                                        updateOrder("BUYORDERS", buyorders[y].id, buyorders[y].amount);
                                        sellorders[x].amount = 0; //close sell order
                                        closeOrder("SELLORDERS", sellorders[x].id);
                                    }
                                }

                                if(sellorders[x].amount > buyorders[y].amount) { // if sell amount > buy amount than update sell order + close buy order
                                    if(withdraw(sellorders[x].uuid, buyorders[y].uuid, sellorders[x].itemid, buyorders[y].amount, sellorders[x].price) ){//if buyer have enough money than true
                                        sellorders[x].amount = sellorders[x].amount - buyorders[y].amount;
                                        updateOrder("SELLORDERS", sellorders[x].id, sellorders[x].amount);
                                        buyorders[y].amount = 0; //close buy order
                                        closeOrder("BUYORDERS", buyorders[y].id);
                                    }
                                }

                                // if sell amount == buy amount than close buy and sell order
                                if(sellorders[x].amount == buyorders[y].amount) {   //System.out.println("Sell amount == buyorder amount 3");
                                    if(withdraw(sellorders[x].uuid, buyorders[y].uuid, sellorders[x].itemid, buyorders[y].amount, sellorders[x].price) ){
                                        buyorders[y].amount = 0;
                                        closeOrder("BUYORDERS", buyorders[y].id);
                                        sellorders[x].amount = 0; //close sell order
                                        closeOrder("SELLORDERS", sellorders[x].id);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:itemex.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return conn;
    }

    //withdraw money and items to the players. Writes an entry into the payout db
    private boolean withdraw(String seller_uuid, String buyer_uuid, String itemid, int amount, double price) {
        //System.out.println("AT WITHDRAW");
        OfflinePlayer o_seller =  Bukkit.getOfflinePlayer(UUID.fromString(seller_uuid));
        OfflinePlayer o_buyer =  Bukkit.getOfflinePlayer(UUID.fromString(buyer_uuid));
        Player seller = Bukkit.getPlayer(UUID.fromString(seller_uuid));
        Player buyer = Bukkit.getPlayer(UUID.fromString(buyer_uuid));
        double total = price * amount;
        double buyer_balance = econ.getBalance(o_buyer);

        if( total < buyer_balance ) {                    // check if buyer have enough money
            econ.withdrawPlayer(o_buyer, total);         // subtract money from buyer
            econ.depositPlayer(o_seller, total);         // give money to seller

            sqliteDb.insertFullfilledOrders(seller_uuid, buyer_uuid, itemid, amount, price); // Insert Fullfilled order into db

            if(seller == null) {
                //System.out.println("--DEBUG: SELLER IS OFFLINE!");
                sqliteDb.insertPayout(seller_uuid, itemid, amount); // Insert payout into db
            }
            else {
                seller.sendMessage("SELL ORDER" + ChatColor.GREEN+ " FULFILLED!" + ChatColor.WHITE + " You sold [" + amount + "] "  + itemid + " for" + ChatColor.GREEN + " $"  + total);
            }

            if(buyer == null) {
                //System.out.println("--DEBUG: BUYER IS OFFLINE!");
                sqliteDb.insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
            }
            else {
                sqliteDb.insertPayout(buyer_uuid, itemid, amount); // Insert item payout into db
                buyer.sendMessage("BUY ORDER" + ChatColor.GREEN+ " FULFILLED!" + ChatColor.WHITE + " You got [" + amount + "] "  + itemid + " for" + ChatColor.RED + " $"  + total);
                //buyer.sendMessage("You can withdraw with: /ix withdraw " + itemid +" " + amount);
                TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) You can withdraw with: /ix withdraw " + itemid +" " + amount);
                message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + itemid +" " + amount));
                buyer.spigot().sendMessage(message);
                //message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/heal"));
            }
            return true;
        } // end enough money
        else {
            return false;
        }
    } // end withdraw



    public DbEntryBuffer[] selectAll(String table){
        DbEntryBuffer[] buffer = new DbEntryBuffer[1000000];
        String sql = null;
        if(table.equals("SELLORDERS")) {
            sql = "SELECT * FROM SELLORDERS ORDER by itemid ASC, price";
        }
        else if(table.equals("BUYORDERS")) {
            sql = "SELECT * FROM BUYORDERS ORDER by itemid ASC, price DESC";
        }

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            int row_counter = 0;
            while (rs.next()) {
                buffer[row_counter] = new DbEntryBuffer(rs.getInt("id"), rs.getString("player_uuid"), rs.getString("itemid"), rs.getString("ordertype"),rs.getInt("amount"), rs.getFloat("price"), rs.getLong("timestamp") );
                row_counter++;
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        return buffer;
    }


    private boolean updateOrder(String table_name, int ID, int amount) {
        //System.out.println("AT updateORDER: " + table_name + " " +ID + " " + amount);
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
            stmt = c.createStatement();
            String sql = "UPDATE " + table_name + " SET amount = " + amount +  " WHERE id = " + ID;

            stmt.executeUpdate(sql);
            stmt.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
            return false;
        }
        return true;
    } // end insertIntoDB


    private boolean closeOrder(String table_name, int ID) {
        //System.out.println("AT closeOrder: " + table_name + " " + ID);
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:itemex.db");
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






    static class DbEntryBuffer{
        public int id;
        public String uuid;
        public String itemid;
        public String ordertype; //sell 0, buy 1
        public int amount;
        public float price;
        public long timestamp;

        DbEntryBuffer(int id, String uuid, String itemid, String ordertype, int amount, float price, long timestamp)
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
}
