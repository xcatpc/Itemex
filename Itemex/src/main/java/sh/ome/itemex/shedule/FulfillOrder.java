package sh.ome.itemex.shedule;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.sqliteDb;

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

        // load buy and sell orders into RAM
        sqliteDb.OrderBuffer[] sellorders = sqliteDb.selectAll("SELLORDERS");
        sqliteDb.OrderBuffer[] buyorders = sqliteDb.selectAll("BUYORDERS");


        // FIND AND FULFILL ORDERS
        for(int x=0; x<1000000-1; x++) { //max 1b entries
            if(sellorders[x] == null) { break;}
            else {
                for(int y=0; y<1000000-1; y++) {
                    if(buyorders[y] == null) { break;}
                    else {
                        if(sellorders[x].itemid.equals( buyorders[y].itemid )) { //check if itemid matches

                            // if orders are limit or market
                            String[] buy_limit_or_market = buyorders[y].ordertype.split(":", 2);
                            String[] sell_limit_or_market = sellorders[x].ordertype.split(":", 2);

                            // if order == market -> adjust price to best market orders
                            if(sell_limit_or_market[1].equals("market"))
                                sellorders[x].price = buyorders[y].price;   // set the best price

                            if(buy_limit_or_market[1].equals("market"))
                                buyorders[y].price =  sellorders[x].price;  // set the best price

                            // ADMIN ORDER if admin order enabled AND (sell-order OR buy-order is an admin order)
                            // same as limit, but each single amount fullfilled price in- or decreases to spread. Default amount of spread =
                            if( Itemex.admin_function && (sell_limit_or_market[1].equals("admin") || buy_limit_or_market[1].equals("admin")) ) { // sell and buy order cannot be the same time!
                                if(sell_limit_or_market[1].equals("admin")) {   // if sell order is admin
                                    System.out.println("Sellorder = admin" + sellorders[x].id + " " + sellorders[x].amount + " " + sellorders[x].price);
                                }

                                if(buy_limit_or_market[1].equals("admin")) {    // if buy order is admin
                                    System.out.println("Buyorder = admin" + buyorders[y].id + " " + buyorders[y].amount + " " + buyorders[y].price);
                                }
                            }
                            // ------ TESTING END



                            // LIMIT ORDER OR MARKET ORDER (Marketorder adjusted aboth)
                            else if(sellorders[x].price <= buyorders[y].price && sellorders[x].amount != 0 && buyorders[y].amount !=0)  { // if sell amount < buy amount than close sell order + update buy order

                                if(sellorders[x].amount < buyorders[y].amount) {  //if buyer have enough money than true
                                    if(withdraw(sellorders[x].uuid, buyorders[y].uuid, sellorders[x].itemid, sellorders[x].amount, sellorders[x].price) ){
                                        buyorders[y].amount = buyorders[y].amount - sellorders[x].amount;
                                        sqliteDb.updateOrder("BUYORDERS", buyorders[y].id, buyorders[y].amount, buyorders[y].price, "buy:limit");
                                        //System.out.println("sellorder_price <= buyorder_price");
                                        sellorders[x].amount = 0; //close sell order
                                        sqliteDb.closeOrder("SELLORDERS", sellorders[x].id);
                                    }
                                    else { // remove order if buyer don't have enough money
                                        buyorders[y].amount = 0; //close buy order
                                        sqliteDb.closeOrder("BUYORDERS", buyorders[y].id);
                                        //System.out.println("-Debug: <= Buyer don't have enough money, buyorder with the id: " + buyorders[y].id + " removed!");
                                    }
                                }


                                else if(sellorders[x].amount > buyorders[y].amount) { // if sell amount > buy amount than update sell order + close buy order
                                    if(withdraw(sellorders[x].uuid, buyorders[y].uuid, sellorders[x].itemid, buyorders[y].amount, sellorders[x].price) ){ //if buyer have enough money than true
                                        sellorders[x].amount = sellorders[x].amount - buyorders[y].amount;
                                        sqliteDb.updateOrder("SELLORDERS", sellorders[x].id, sellorders[x].amount, buyorders[y].price, "sell:limit");
                                        //System.out.println("sellorder_price > buyorder_price");
                                        buyorders[y].amount = 0; //close buy order
                                        sqliteDb.closeOrder("BUYORDERS", buyorders[y].id);
                                    }
                                    else { // remove order if buyer don't have enough money
                                        buyorders[y].amount = 0; //close buy order
                                        sqliteDb.closeOrder("BUYORDERS", buyorders[y].id);
                                        //System.out.println("-Debug: > Buyer don't have enough money, buyorder with the id: " + buyorders[y].id + " removed!");
                                    }
                                }

                                // if sell amount == buy amount than close buy and sell order
                                else if(sellorders[x].amount == buyorders[y].amount) {   //System.out.println("Sell amount == buyorder amount 3");
                                    if(withdraw(sellorders[x].uuid, buyorders[y].uuid, sellorders[x].itemid, buyorders[y].amount, sellorders[x].price) ){ //if buyer have enough money than true
                                        buyorders[y].amount = 0;
                                        sqliteDb.closeOrder("BUYORDERS", buyorders[y].id);
                                        //System.out.println("sellorder_price == buyorder_price");
                                        sellorders[x].amount = 0; //close sell order
                                        sqliteDb.closeOrder("SELLORDERS", sellorders[x].id);
                                    }
                                    else { // remove order if buyer don't have enough money
                                        buyorders[y].amount = 0; //close buy order
                                        sqliteDb.closeOrder("BUYORDERS", buyorders[y].id);
                                        //System.out.println("-Debug: == Buyer don't have enough money, buyorder with the id: " + buyorders[y].id + " removed!");
                                    }
                                }
                            }




                        } // end item match
                    } // end buyorders[y] != null
                } // end y 0 -> 1000000
            } // end sellorders[x] != null
        } // end x 0 -> 100000
    } // end fulfill method





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


}
