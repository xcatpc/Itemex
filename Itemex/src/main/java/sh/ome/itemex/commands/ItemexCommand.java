package sh.ome.itemex.commands;
import com.google.gson.Gson;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.Order;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.functions.sqliteDb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static sh.ome.itemex.Itemex.econ;

public class ItemexCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (Itemex.itemex_stats) {
            // CommandUsageCounts
            String full_command = String.join(" ", strings);
            if (!Itemex.commandUsageCounts.containsKey( full_command ))
                Itemex.commandUsageCounts.put( full_command , 1);
            else {
                int count = Itemex.commandUsageCounts.get( full_command );
                Itemex.commandUsageCounts.put( full_command , count + 1);
            }

            int totalCommandsEntered = 0;
            for (int count : Itemex.commandUsageCounts.values())
                totalCommandsEntered += count;
            if (totalCommandsEntered > 20)
                // clear the existing map object
                Itemex.commandUsageCounts.clear();
            // CommandUsageCounts END
        }



        if( command.getName().equalsIgnoreCase("ix")) {
            String reply_command = "";

            if(strings.length == 0) {
                Player p = null;
                if(sender instanceof Player) {
                    reply_command = reply_command + print_help(true);
                    p = (Player) sender;
                    GUI.generateGUI(p, "ITEMEX - Market Orders", 0, 0);
                }
                else
                    reply_command = reply_command + print_help(false);

            }
            else {  // more than 1 parameter
                Player p = null;
                if(sender instanceof Player) {
                    p = (Player) sender;
                    if (!p.hasPermission("itemex.command.ix")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                }
                else if( sender instanceof ConsoleCommandSender) {
                    System.out.println("SERVER TERMINAL");
                    System.out.println("Players Name: " + strings[strings.length-1]);
                    //p = Bukkit.getPlayerExact( strings[strings.length-1] ); // NAME
                    //OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString( strings[strings.length-1] ));
                    //System.out.println("Name: " + op.getName());
                    System.out.println("Return (break);");
                    return true;
                }
                else if( sender instanceof BlockCommandSender) {
                    OfflinePlayer op;
                    if(strings.length == 4) {
                        op = Bukkit.getOfflinePlayer(UUID.fromString( strings[strings.length-1] ));
                    }
                    else if(strings.length == 6) {
                        op = Bukkit.getOfflinePlayer(UUID.fromString( strings[strings.length-1] ));
                    }
                    return true;
                }





                if(strings[0].equals("help")) {
                    if(sender instanceof Player)
                        reply_command = reply_command + print_help(true);
                    else
                        reply_command = reply_command + print_help(false);
                }





                else if(strings[0].equals("buy") ) {
                    int item_counter=0;
                    String itemid;

                    if (!p.hasPermission("itemex.command.ix.buy")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    if(strings.length == 1 || strings.length == 2) { // /ix buy given itemID or whatisinmyrighthand
                        reply_command = "\n\n\n";
                        // check if something is in right hand


                        if(strings.length == 1)
                            itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                        else
                            itemid = strings[1].toUpperCase();

                        if(itemid == "AIR") {
                            p.sendMessage( Itemex.language.getString("message_cant_buy_nothing"));
                            return false;
                        }

                        // check if there is a sell order with enough amount (1)
                        if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0] == 0) {
                            TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("buy_no_sellorders_to_buy") + ChatColor.WHITE + itemid + "\n.\n" + ChatColor.BLUE + Itemex.language.getString("buy_click_here_create") + ChatColor.GREEN + Itemex.language.getString("buy_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix buy " + itemid + " 1 limit");
                            message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" 1 limit "));
                            p.spigot().sendMessage(message);
                        }
                        else {
                            //create buy order
                            p.sendMessage(Itemex.language.getString("buy_best_sellorder_price") + Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0]);
                            reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0], 1, "buy", "market");
                        }

                    } // end ix buy

                    else if(strings.length >= 4 && strings.length <= 6) { // /ix buy <itemid> <amount> limit <price>
                        double price;
                        int amount = 0;
                        boolean buy_order_ok = true;
                        itemid = strings[1].toUpperCase();

                        if( strings[3].equals("market") ) {
                            price = Itemex.getPlugin().mtop.get( itemid ).get_top_sellorder_prices()[0];
                            if(price <= 0)
                                buy_order_ok = false;
                        }
                        else {
                            price = parseFloat(strings[4]);
                        }

                        //proof amount
                        if(strings[2].equals("max"))
                            amount = item_counter;
                        else
                            amount = parseInt(strings[2]);
                        if(amount <= 0) {
                            buy_order_ok = false;
                            reply_command = reply_command + Itemex.language.getString("buy_price_cant_be_0");
                        }


                        //proof market or limit
                        if(!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + Itemex.language.getString("buy_wrong_market_option") + "(" + strings[3] + ")" + Itemex.language.get("buy_only_market_or_limit_accepted");
                            buy_order_ok = false;
                        }
                        if(strings[3].equals("limit")) {
                            if(price <= 0) {
                                reply_command = reply_command + Itemex.language.get("buy_price_not_allowed_lower_than_0") + price;
                                buy_order_ok = false;
                            }
                        }

                        if(buy_order_ok && price >= 0 && strings[3].equals("limit")) {
                            reply_command = reply_command + create_order(p, strings[1], price, amount, "buy", strings[3]);
                        }

                        else if(strings[3].equals("market")) {
                            if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0] == 0) {
                                TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("buy_no_sellorders_to_buy") + ChatColor.WHITE + itemid + "\n.\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("buy_you_can_create") + ChatColor.GREEN + Itemex.language.getString("buy_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix buy " + itemid + " " + amount + " limit");
                                message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" " + amount + " limit "));
                                p.spigot().sendMessage(message);
                            }
                            else {
                                //create buy order
                                p.sendMessage(Itemex.language.getString("buy_best_sellorder_price") + Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0]);
                                if( strings.length >= 5 && strings[4].equals("confirm") ) {
                                    reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0], amount, "buy", "market");
                                }
                                else {
                                    TopOrders topo = Itemex.getPlugin().mtop.get(itemid);
                                    List<sh.ome.itemex.RAM.Order> sell_orders = topo.get_top_sell(Itemex.admin_function);
                                    for (sh.ome.itemex.RAM.Order order : sell_orders) {
                                        double sprice = order.getPrice();
                                        int samount = order.getAmount();
                                        boolean isAdmin = order.isAdmin();

                                        if(amount == 0)
                                            reply_command = reply_command + ChatColor.DARK_RED + Itemex.language.getString("sellorder") + ChatColor.DARK_GRAY + itemid + "  [" + samount + "] " + format_price( sprice ) + "\n";
                                        else if(isAdmin)
                                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price( sprice ) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + "\n";
                                        else
                                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price( sprice ) + "\n";
                                    }


                                    reply_command = reply_command + "-----------------------------\n";
                                    p.sendMessage(reply_command);
                                    reply_command = "";
                                    TextComponent message = new TextComponent(ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("confirm_order_price"));
                                    message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" " + amount + " market confirm "));
                                    p.spigot().sendMessage(message);
                                }
                            }
                        }
                        else {
                            //System.out.println(buy_order_ok);
                        }

                    }
                    else {
                        //reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /ix buy *<itemname> *<limit> *<price> *<amount> | * == optional";
                    }
                } // end buy




                else if(strings[0].equals("sell") ) {

                    if (!p.hasPermission("itemex.command.ix.sell")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    Order sellorder = new Order();
                    boolean is_damaged_or_enchantment = true;
                    String itemid;

                    if(strings.length == 1 || strings.length == 2) { // /ix sell

                        if(strings.length == 1) { // itemid is what player has in hand
                            itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                        }

                        else {
                            boolean item_found = false;
                            // check if player have the amount of items provided at the parameter
                            int item_counter=0;
                            for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                                if(item != null && strings[1].equalsIgnoreCase(item.getType().toString())) { //searching only for items with the given ID from command
                                    reply_command = reply_command + " " + item.getAmount() + "x" + item.getType() + "\n";
                                    item_counter = item_counter + item.getAmount();
                                    item_found = true;
                                }
                            }
                            if(item_found)
                                itemid = strings[1].toUpperCase();
                            else
                                itemid = "AIR";
                        }

                        if( p.getInventory().getItemInMainHand().getDurability() != 0 )    // GET DAMAGE 0 = no damage
                            is_damaged_or_enchantment = false;

                        else if( p.getInventory().getItemInMainHand().getEnchantments().size() != 0 )      // if there is an enchantment on the item
                            is_damaged_or_enchantment = false;



                        if(!itemid.equals("AIR") && is_damaged_or_enchantment) {
                            // check if there is a buy order with enough amount (1)
                            if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[0] == 0) {
                                TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("sell_no_buyorders_to_sell") + ChatColor.WHITE + itemid + "\n.\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("buy_you_can_create") + ChatColor.GREEN + Itemex.language.getString("sell_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix sell " + itemid + " 1 limit ");
                                message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" 1 limit "));
                                p.spigot().sendMessage(message);
                            }

                            else {
                               // create_sell_order(p, itemid, 1, orders[first_buy_order].price); //replaced with create order
                                reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[3],1, "sell", "market");
                            }
                        }
                        else {
                            if(is_damaged_or_enchantment)
                                reply_command = Itemex.language.getString("cant_sell_enchantments");
                            else if(strings.length == 1)
                                reply_command = Itemex.language.getString("noting_in_right_hand");
                            else
                                reply_command = Itemex.language.getString("item_not_in_inventory");
                        }
                    }

                    else if(strings.length >= 4 && strings.length <= 6) { // /ix sell <itemid> <amount> limit <price>
                        double price;
                        boolean sell_order_ok = true;
                        itemid = strings[1].toUpperCase();

                        if( strings[3].equals("market") ) {
                            price = Itemex.getPlugin().mtop.get( itemid ).get_top_sellorder_prices()[0];
                            if(price <= 0)
                                sell_order_ok = false;
                        }
                        else {
                            price = parseFloat(strings[4]);
                        }


                        // proof price
                        if(!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + Itemex.language.getString("buy_wrong_market_option") + strings[3] + Itemex.language.getString("buy_only_market_or_limit_accepted");
                            sell_order_ok = false;
                        }
                        if(strings[3].equals("limit")) {
                            if(price <= 0) {
                                reply_command = reply_command + Itemex.language.getString("buy_price_not_allowed_lower_than_0") + price;
                                sell_order_ok = false;
                            }
                        }

                        boolean item_found = false;
                        //reply_command = "/ix sell <itemname> <amount> limit <price> " + p.getName();

                        // check if player have the amount of items provided at the parameter
                        int item_counter=0;
                        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                            if(item != null && strings[1].equalsIgnoreCase(item.getType().toString())) { //searching only for items with the given ID from command
                                item_counter = item_counter + item.getAmount();
                            }
                        }

                        // proof amount
                        if(strings[2].equals("max"))
                            sellorder.amount = item_counter;
                        else
                            sellorder.amount = parseInt(strings[2]);

                        // proof inventory amount >= given amount
                        if( item_counter >= sellorder.amount)
                            item_found = true;

                        if(sellorder.amount <=0 && strings[3].equals("limit")) {
                            reply_command = reply_command + Itemex.language.getString("buy_price_not_allowed_lower_than_0") + price;
                            sell_order_ok = false;
                        }
                        else {
                            if(!is_damaged_or_enchantment) {
                                //System.out.println("# DEBUG - Damaged or enchantment");
                            }
                            else if(item_found) {
                                if(sell_order_ok && price >= 0 && strings[3].equals("limit"))
                                    reply_command = reply_command + create_order( p, itemid, price, sellorder.amount, "sell", strings[3] );
                                else {
                                    //System.out.println("# DEBUG SELL market order");
                                    if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[0] == 0) {
                                        TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("sell_no_buyorders_to_sell") + ChatColor.WHITE + itemid + "\n.\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("buy_you_can_create") + ChatColor.RED + Itemex.language.getString("sell_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix sell " + itemid + " 1 limit ");
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid + " " + sellorder.amount + " limit "));
                                        p.spigot().sendMessage(message);
                                    }
                                    else {
                                        //create buy order
                                        p.sendMessage(Itemex.language.getString("buy_best_sellorder_price") + Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0]);
                                        if( strings.length >= 5 && strings[4].equals("confirm") ) {
                                            reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[3],sellorder.amount, "sell", "market");
                                        }
                                        else {

                                            TopOrders topo = Itemex.getPlugin().mtop.get(itemid);
                                            List<sh.ome.itemex.RAM.Order> buy_orders = topo.get_top_buy(Itemex.admin_function);
                                            for (sh.ome.itemex.RAM.Order order : buy_orders) {
                                                double sprice = order.getPrice();
                                                int samount = order.getAmount();
                                                boolean isAdmin = order.isAdmin();

                                                if(samount == 0)
                                                    reply_command = reply_command + ChatColor.DARK_GREEN + Itemex.language.getString("buyorder") + ChatColor.DARK_GRAY + itemid + "  [" + samount + "] " + format_price( sprice ) + "\n";
                                                else if(isAdmin)
                                                    reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price( sprice ) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + "\n";
                                                else
                                                    reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price( sprice ) + "\n";
                                            }

                                            reply_command = reply_command + "-----------------------------\n";
                                            p.sendMessage(reply_command);
                                            reply_command = "";
                                            TextComponent message = new TextComponent(ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("confirm_order_price"));
                                            message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" " + sellorder.amount + " market confirm "));
                                            p.spigot().sendMessage(message);
                                        }
                                    }
                                }
                            }
                            else
                                reply_command = reply_command + ChatColor.RED + Itemex.language.getString("no_given_items") + ChatColor.GOLD + strings[1] + ChatColor.RED + Itemex.language.getString("found_in_inventory") + "\n" + ChatColor.WHITE + Itemex.language.getString("check_correct_name") + " /ix whatIsInMyRightHand";
                        }
                    } // end ix sell limit

                    else {
                        //reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /ix sell <itemname> <amount> limit <price>";
                    }
                } // end sell





                else if(strings[0].equals("price") ) {
                    if (!p.hasPermission("itemex.command.price")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }


                    reply_command = "\n\n\n-----------------------------\n";
                    TopOrders topo = null;
                    String itemid = null;

                    if(strings.length == 1) { // /ix price
                        itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                    }
                    else if(strings.length == 2) { // /ix price <item id>
                        itemid = strings[1].toUpperCase();

                    }

                    if(itemid.equals("AIR")) {
                        reply_command = Itemex.language.getString("price_no_item");
                        //reply_command = "You only have AIR in your hand!";
                    }
                    else {
                        topo = Itemex.getPlugin().mtop.get( itemid );
                        reply_command = reply_command + Itemex.language.getString("price_of_item") + ChatColor.GOLD +  itemid + ChatColor.WHITE + "\n";
                        reply_command = reply_command + "-----------------------------\n";
                        reply_command = reply_command + Itemex.language.getString("ordertype_itemid_amount_price") + "\n";


                        List<sh.ome.itemex.RAM.Order> sell_orders = topo.get_top_sell(Itemex.admin_function);
                        for (sh.ome.itemex.RAM.Order order : sell_orders) {
                            double price = order.getPrice();
                            int amount = order.getAmount();
                            boolean isAdmin = order.isAdmin();

                            if(amount == 0)
                                reply_command = reply_command + ChatColor.DARK_RED + Itemex.language.getString("sellorder") + ChatColor.DARK_GRAY + itemid + "  [" + amount + "] " + format_price( price ) + "\n";
                            else if(isAdmin)
                                reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price( price ) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + "\n";
                            else
                                reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price( price ) + "\n";
                        }

                        List<sh.ome.itemex.RAM.Order> buy_orders = topo.get_top_buy(Itemex.admin_function);
                        for (sh.ome.itemex.RAM.Order order : buy_orders) {
                            double price = order.getPrice();
                            int amount = order.getAmount();
                            boolean isAdmin = order.isAdmin();

                            if(amount == 0)
                                reply_command = reply_command + ChatColor.DARK_GREEN + Itemex.language.getString("buyorder") + ChatColor.DARK_GRAY + itemid + "  [" + amount + "] " + format_price( price ) + "\n";
                            else if(isAdmin)
                                reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price( price ) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + "\n";
                            else
                                reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price( price ) + "\n";
                        }

                        reply_command = reply_command + ChatColor.WHITE +  "-----------------------------\n";
                    }



                } // end price




                else if(strings[0].equals("whatIsInMyRightHand") ) {

                    if (!p.hasPermission("itemex.command.ix.whatIsInMyRightHand")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    ItemStack item = p.getInventory().getItemInMainHand();
                    reply_command = "ITEMID: " + item.getType() + "\n";

                    // GET DAMAGE 0 = no damage
                    if(item.getDurability() != 0)
                        reply_command = reply_command + Itemex.language.getString("durability") + item.getDurability() + "\n";

                    // CHECK if ITEM HAS ENCHANTMENTS
                    if (item.getItemMeta().hasEnchants()) {
                        reply_command = reply_command + Itemex.language.getString("nr_of_enchant") + item.getEnchantments().size() + "\n";
                        Map enc = item.getEnchantments();
                        reply_command = reply_command + Itemex.language.getString("enchantments") + item.getEnchantments() + Itemex.language.getString("size") + enc.size();
                    }


                    // CHECK POTION
                    if (item.getType() == Material.POTION) {
                        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                        reply_command = reply_command + Itemex.language.getString("potion") + potionMeta.getBasePotionData().getType() + " ";
                        // DURATION ?
                        PotionMeta meta = (PotionMeta) item.getItemMeta();
                        List po_effects = meta.getCustomEffects();
                        //System.out.println("IS EMPTY: " + po_effects.isEmpty());
                        //PotionEffect effect = meta.getCustomEffects()
                        reply_command = reply_command + "\nisExtendedt: " + ((PotionMeta) item.getItemMeta()).getBasePotionData().isExtended() + "\n";
                        reply_command = reply_command + "isUpgraded: " + ((PotionMeta) item.getItemMeta()).getBasePotionData().isUpgraded();
                    }

                    // ENCHANTED_BOOK
                    if (item.getType() == Material.ENCHANTED_BOOK) {
                        reply_command = reply_command + "IS ENCHANTED_BOOK\n";
                        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                        Map<Enchantment, Integer> enchantments = meta.getStoredEnchants();

                        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                            Enchantment enchantment = entry.getKey();
                            int level = entry.getValue();
                            // Do something with the enchantment and level
                            reply_command = reply_command + enchantment.getName() + "; Level: " + level;
                        }
                    }



                }



                else if(strings[0].equals("order") ) {
                    int orderid = -1;

                    if(strings.length == 1) {
                        reply_command = "/ix order <close | edit | list>";
                    }
                    else if( strings[1].equals("close") ) {
                        if (!p.hasPermission("itemex.command.ix.order.close")) {
                            p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                            return true;
                        }

                        if( strings[2].equals("buyorders") || strings[2].equals("sellorders") ) {
                            try {
                                orderid = parseInt(strings[3]);;
                            } catch (NumberFormatException e) {
                                reply_command = Itemex.language.getString("orderid_must_be_number") + "\n";
                            }
                            if(orderid != -1) {
                                if( sqliteDb.PlayercloseOrder(p.getUniqueId().toString(), strings[2].toUpperCase(), orderid) )
                                    reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("order_del_success") + ChatColor.RESET;
                                else
                                    reply_command = reply_command + ChatColor.RED + Itemex.language.getString("order_not_del_success") + ChatColor.RESET;
                            }
                        }
                        else
                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("only_buy_or_sellorders") + ChatColor.RESET;;
                    }

                    else if( strings[1].equals("edit") ) {
                        if (!p.hasPermission("itemex.command.ix.order.edit")) {
                            p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                            return true;
                        }
                        reply_command = reply_command + "not implemented. If you need it. Write me to xcatpc@proton.me";
                        //sqliteDb.updateOrder("tabelname", 14, 13, (float)4055.49, "sell:limit");
                    }

                    else if( strings[1].equals("list") ) {
                        if( strings.length <= 2) {
                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("you_can_only_use_buy_or_sellorders") + ChatColor.RESET;
                        }
                        else if( strings[2].equals("buyorders") || strings[2].equals("sellorders") ) {
                            boolean buy_or_sell;
                            String item_id = "*";
                            if( strings.length == 4 )
                                item_id = strings[3].toUpperCase();
                            //reply_command = item_id;

                            String color = ChatColor.RED.toString();

                            if( strings[2].equals("buyorders") ) {
                                buy_or_sell = true;
                                color = ChatColor.GREEN.toString();
                            }

                            else
                                buy_or_sell = false;

                            sqliteDb.OrderBuffer[] list = sqliteDb.getOrdersOfPlayer(p.getUniqueId().toString(), item_id, buy_or_sell, 1); //true = buy ; false = sell
                            reply_command = reply_command + Itemex.language.getString("list_all_orders_and_categories");
                            for(int i=0; i<100; i++){ // 100 is max
                                if(list[i] == null) {
                                    break;
                                }
                                else {
                                    reply_command = reply_command +  color + list[i].id + " " +ChatColor.WHITE +list[i].itemid + " " + list[i].amount + " " + color + list[i].price + " " + list[i].ordertype + "\n" + ChatColor.WHITE;
                                }
                            } // end for
                            reply_command = reply_command + "\n" + Itemex.language.getString("and_more") + " /ix order list <sellorders | buyorders> <itemid>";

                        }
                        else
                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("you_can_only_use_buy_or_sellorders") + ChatColor.RESET;

                    } // end of list

                } // end order




                else if(strings[0].equals("withdraw") ) {

                    if (!p.hasPermission("itemex.command.ix.withdraw")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    if(strings.length == 1 || (strings.length == 2) && ( strings[1].equals("list") || strings[1].equals("_list") ) ){ // /ix withdraw list
                        //reply_command = reply_command + " /ix withdraw list \n";
                        sqliteDb.Payout[] payouts = sqliteDb.getPayout(p.getUniqueId().toString());
                        reply_command = reply_command + "\n\n" + Itemex.language.getString("only_following_items") + "\n";

                        for (int i = 0; i < payouts.length; i++) {
                            if(payouts[i] == null) { //skip empty entries
                                if(i == 0)
                                    reply_command = reply_command + "\n\n" + Itemex.language.getString("nothing_to_withdraw");
                                break;
                            }
                            reply_command = reply_command + "\n [" + payouts[i].amount + "] " + ChatColor.GREEN + payouts[i].itemid + ChatColor.WHITE +  " \n";
                        }
                        //reply_command = reply_command + "You can withdraw with /ix withdraw <itemid> <amount>";
                    }
                    else if(strings.length == 3) { // /ix withdraw <itemid> <amount>
                        reply_command = reply_command + "/ix withdraw <itemID:" + strings[1] +"> <amount:" + strings[2]+"> \n\n";
                        sqliteDb.Payout[] payouts = sqliteDb.getPayout(p.getUniqueId().toString());
                        int item_amount = 0;

                        if(strings[2].equals("max")) {
                            item_amount = 9*5*64;
                        }
                        else {
                            item_amount = parseInt( strings[2] );
                            if( item_amount > 9*5*64) {
                                item_amount = 9*5*64;
                            }
                        }

                        // proof if player have enough space
                        int empty_slots = 0;
                        int this_item_count = 0;
                        int this_item_stacks = 0;
                        int free_space = 0;

                        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                            if(item == null) { // if stack is empty
                                empty_slots++;
                            }
                            else if(item.getType().toString().equals(strings[1].toUpperCase())) {
                                this_item_count = this_item_count + item.getAmount();
                                this_item_stacks++;
                            }
                        }
                        empty_slots = empty_slots - 5;      // subtract amour and left hand slots

                        // send amount
                        for (int i = 0; i < payouts.length; i++) {
                            if(payouts[i] == null) { //skip empty entries
                                break;
                            }
                            // check how many stacks the given item has (eg. diamond = 64; egg = 16)
                            int max_stack = Material.getMaterial(payouts[i].itemid).getMaxStackSize();
                            int max_items = empty_slots * max_stack;


                            free_space = this_item_stacks * max_stack - this_item_count;
                            free_space = free_space + max_items;


                            // send items with amount of free_space to inventory
                            int x = 0;
                            while(x <= payouts[i].amount-1) {
                                if(free_space >= 1) {
                                    //reply_command = reply_command + "\nFree space: " + free_space + "\n";
                                    p.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial( payouts[i].itemid.toUpperCase() ))); // give buyer good from payout
                                    x++;
                                    this_item_count++;
                                    free_space--;
                                }
                                else {
                                    break;
                                }
                            }

                            if(payouts[i].amount > x) {
                                reply_command = reply_command + free_space + "\n" + Itemex.language.getString("payout") + " [" + x + "/" + payouts[i].amount + "] " + ChatColor.GOLD + payouts[i].itemid + ChatColor.WHITE +  " " + Itemex.language.getString("no_space") + " \n.\n";
                            }
                            else {
                                reply_command = reply_command + free_space + "\n" + Itemex.language.getString("payout") + " [" + x + "/" + payouts[i].amount + "] " + ChatColor.GREEN + payouts[i].itemid + ChatColor.WHITE +  " \n.\n";
                            }

                            //update db (subtract x)
                            if(x != 0) {        // x == the payout item amount
                                sqliteDb.updatePayout(p.getUniqueId().toString(), payouts[i].id, payouts[i].itemid, payouts[i].amount-x ); //update the amount
                            }
                        }

                    }
                    else {
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /help";
                    }
                }



                else if(strings[0].equals("gui") ) {
                    if (!p.hasPermission("itemex.command.ix.gui")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                    GUI.generateGUI(p, "ITEMEX - Market Orders", 0, 0);
                }




                //stealth

                else if(strings[0].equals("showmyuuid") ) {
                    p.sendMessage(p.getUniqueId().toString());
                }


                else if(strings[0].equals("extractitems") ) {

                    int x=0;
                    for (Material material : Material.values()) {
                        System.out.println(":" + x + ":" +material.toString() + ":" + material.isBlock() + ":" + material.isItem() + ":" + material.isBurnable() + ":" + material.isFuel() + ":" + material.isInteractable() + ":" + material.isSolid() + ":" + material.isFlammable() + ":" + material.isOccluding() + ":" + material.isRecord());
                        x++;
                    }
                    //p.sendMessage("amount + price: " + Itemex.getPlugin().top[0].get_buyorder_amount() + " + " + Itemex.getPlugin().top[0].get_top_buyorder_prices() );
                }

                else if(strings[0].equals("update_ram") ) { // /ix update_ram <itemid>
                    if(strings.length != 2) {
                        p.sendMessage(ChatColor.RED + " you have to give one argument: ITEMID");
                    }
                    else {
                        System.out.println("ITEMID: " + strings[1].toUpperCase());
                        sqliteDb.loadBestOrdersToRam(strings[1].toUpperCase(), true);
                        p.sendMessage(ChatColor.GREEN + "Top Order for Item: " + strings[1].toUpperCase() + " updated in RAM!");
                    }
                }

                else if(strings[0].equals("command_usage")) {
                    for (Map.Entry<String, Integer> entry : Itemex.commandUsageCounts.entrySet()) {
                        String commandName = entry.getKey();
                        int usageCount = entry.getValue();
                        System.out.println(commandName + ": " + usageCount);
                    }
                }




                else if(strings[0].equals("check_item")) {
                    ItemStack item = p.getInventory().getItemInHand();
                    String itemid = item.getType().toString().toUpperCase();
                    p.sendMessage("ITEMID: " + itemid);

                    ItemMeta itemMeta = item.getItemMeta();

                    // List all enchantments of the item, including enchanted books
                    if (itemMeta instanceof EnchantmentStorageMeta) {
                        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemMeta;
                        if (esm.hasStoredEnchants()) {
                            for (Map.Entry<Enchantment, Integer> enchantment : esm.getStoredEnchants().entrySet()) {
                                p.sendMessage("Stored Enchantment: " + enchantment.getKey().getKey().getKey() + ", Level: " + enchantment.getValue());
                            }
                        } else {
                            p.sendMessage("No stored enchantments.");
                        }
                    } else {
                        Map<Enchantment, Integer> enchantments = item.getEnchantments();
                        if (enchantments.isEmpty()) {
                            p.sendMessage("No enchantments.");
                        } else {
                            for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                                p.sendMessage("Enchantment: " + enchantment.getKey().getKey().getKey() + ", Level: " + enchantment.getValue());
                            }
                        }
                    }

                    // List all effects of the item, in case it's a potion
                    if (itemMeta instanceof PotionMeta) {
                        PotionMeta potionMeta = (PotionMeta) itemMeta;

                        // Base potion effect
                        PotionData baseData = potionMeta.getBasePotionData();
                        p.sendMessage("Base Potion: " + baseData.getType().name() + ", Extended: " + baseData.isExtended() + ", Upgraded: " + baseData.isUpgraded());

                        // Custom potion effects
                        if (potionMeta.hasCustomEffects()) {
                            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                                p.sendMessage("Effect: " + effect.getType().getName() + ", Duration: " + effect.getDuration() + ", Amplifier: " + effect.getAmplifier());
                            }
                        } else {
                            p.sendMessage("No additional potion effects.");
                        }
                    }
                }










                else
                    reply_command = "Option not found!: " + strings[0] + "\n use /ix help";

            } // end else more than 1 args



            // Send reply to p
            if(sender instanceof Player) {
                Player p = (Player) sender;
                Economy economy = Itemex.getEconomy();

                if(p.hasPermission("itemex.ix")) {
                    //p.sendMessage(ChatColor.GREEN + "The player has a balance of: " + economy.getBalance(p));
                    p.sendMessage(reply_command);
                }
                else {
                    p.sendMessage(reply_command);
                    //p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command!");
                }
            }
            else if( sender instanceof ConsoleCommandSender) {
                //System.out.println("This is from the server command box");
                System.out.println(reply_command);
            }
            else if( sender instanceof BlockCommandSender) {
                //System.out.println("This is from the command block");
                System.out.println(reply_command);
            }
        }
        checkAndSendUsageCounts();
        return true;
    } //  end onCommand









    public static class Order
    {
        public String uuid;
        public String itemid;
        public String ordertype;
        public int    amount;
        public double price;
    };

    public static String create_order(Player p, String itemid, double price, int amount, String buy_or_sell, String market_option) {
        //System.out.println("# DEBUG AT: create_order: " + amount);
        String reply_command = "";

        Order order = new Order();
        // create sell order (RAM)
        order.amount = amount;
        order.uuid = p.getUniqueId().toString();
        order.itemid = itemid;
        order.ordertype = buy_or_sell + ":" + market_option;
        order.price = price;

        sqliteDb db_order = new sqliteDb(order);

        if(buy_or_sell.equals("sell")) {
            if( db_order.createSellOrder() != -1 )
                reply_command = ChatColor.RED + Itemex.language.getString("sellorder_C") + ChatColor.WHITE + ChatColor.BOLD+  "[" + amount + "] " + itemid.toUpperCase() + ChatColor.WHITE + " " + Itemex.language.getString("created");
            else
                reply_command = Itemex.language.getString("er_sello_ncreated");
            p.getInventory().removeItem(new ItemStack(Material.getMaterial(itemid.toUpperCase()), amount));
        }
        else if(buy_or_sell.equals("buy")) {
            double buyer_balance = econ.getBalance(p);
            if( (amount * price) < buyer_balance ) {
                if( db_order.createBuyOrder() != -1)
                    reply_command =  ChatColor.GREEN + Itemex.language.getString("buyorder_C") + ChatColor.WHITE + Itemex.language.getString("created") + ChatColor.BOLD + " [" + amount + "] " + itemid + ChatColor.WHITE ;
                else
                    reply_command = Itemex.language.getString("erbuyo_ncreated");
            }
            else {  //not enough money
                reply_command = ChatColor.RED+ Itemex.language.getString("not_enough_money") + ChatColor.WHITE + Itemex.language.getString("you_need") + ChatColor.GREEN + format_price( (amount * price) ) + ChatColor.WHITE + Itemex.language.getString("but_you_only_have") + ChatColor.RED + " " + format_price( buyer_balance );
            }


        }
        return reply_command;
    }


    private String print_help(boolean player_or_console) {
        String gold;
        String white;
        String green;
        String dark_gray;
        String dark_purple;

        if(player_or_console) {
            gold = ChatColor.GOLD.toString();
            white = ChatColor.WHITE.toString();
            green = ChatColor.GREEN.toString();
            dark_gray = ChatColor.DARK_GRAY.toString();
            dark_purple = ChatColor.DARK_PURPLE.toString();
        }
        else {
            gold = "";
            white = "";
            green = "";
            dark_gray = "";
            dark_purple = "";
        }

        String reply_command = "\n";
        reply_command = reply_command + gold + "ix = ITEMEX = Item Exchange v" + Itemex.version + white + "\n.\n";
        reply_command = reply_command + "USAGE: \n" + green + "/ix buy " + dark_gray + "| " + Itemex.language.getString("help_ix_buy") +
                "\n" + green + "/ix gui " + dark_gray + "| " + Itemex.language.getString("help_gui") +

                "\n" + green + "/ix sell " + dark_gray + "| " + Itemex.language.getString("help_ix_sell") +
                "\n" + green + "/ix price " + dark_gray + "| " + Itemex.language.getString("help_price") +
                "\n" + green + "/ix price <itemid> " + dark_gray + "| " + Itemex.language.getString("help_price_id") +

                "\n" + green + "/ix buy <itemname> <amount> <limit | market> <price> " + dark_gray + "| " + Itemex.language.getString("help_cbo") +
                "\n" + green + "/ix sell <itemname> <amount> <limit | market> <price> " + dark_gray + "| " + Itemex.language.getString("help_cso") + "\n." +

                "\n" + green + "/ix order list <buyordery | sellorders> *<item id>" + dark_gray + "| " + Itemex.language.getString("help_order_list") +
                "\n" + green + "/ix order close <buyordery | sellorders> <order id> " + dark_gray + "| " + Itemex.language.getString("help_order_close") +

                "\n" + green + "/ix whatIsInMyRightHand" + dark_gray+ "| " + Itemex.language.getString("help_wiimrh") +

                "\n" + green + "/ix withdraw list " + dark_gray+ "| " + Itemex.language.getString("help_with_list") +
                "\n" + green + "/ix withdraw <itemname> <amount> " + dark_gray + "| " + Itemex.language.getString("help_withdraw") + dark_purple +
                "\n.\n" + Itemex.language.getString("help_message") + white +" xcatpc@proton.me " + dark_purple + Itemex.language.getString("help_or_join_disc") + white + "https://discord.gg/rKEwQjpmXj" + white;
        reply_command = reply_command + "\n";
        return reply_command;
    } // end print_help






    public void checkAndSendUsageCounts() {
        int totalCommandsEntered = 0;

        // Sum up the usage counts for each command
        for (int count : Itemex.commandUsageCounts.values()) {
            totalCommandsEntered += count;
        }

        if (totalCommandsEntered > 19) {
            // create a new thread to handle the HTTP request
            Thread httpRequestThread = new Thread(() -> {
                try {
                    URL url = new URL(Itemex.server_url + "/itemex");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);

                    // create map to hold counts and id
                    Map<String, Object> data = new HashMap<>();
                    data.put("com", Itemex.commandUsageCounts);
                    data.put("id", Itemex.server_id);

                    // convert map to JSON string and write to output stream
                    String json = new Gson().toJson(data);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    // check response code and close connection
                    int responseCode = con.getResponseCode();
                    con.disconnect();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // start the thread
            httpRequestThread.start();

        }
    }




    public static String format_price(double price) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(Itemex.thousand_separator); // Thousands separator
        symbols.setDecimalSeparator(Itemex.decimal_separator); // Decimal separator

        StringBuilder pattern = new StringBuilder("###,##0.");
        for (int i = 0; i < Itemex.decimals; i++) {
            pattern.append("0");
        }

        DecimalFormat df = new DecimalFormat(pattern.toString(), symbols);
        String formattedValue = df.format(price);

        switch(Itemex.unitLocation.toLowerCase()) {
            case "right":
                return formattedValue + Itemex.currencySymbol; // Currency symbol at the end
            case "left":
                return Itemex.currencySymbol + formattedValue; // Currency symbol at the beginning
            default:
                return formattedValue; // No currency symbol if unitLocation is not "right" or "left"
        }
    }

}