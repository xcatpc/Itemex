package sh.ome.itemex.commands;
import com.google.gson.Gson;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import sh.ome.itemex.GUI.ix_GUI;
import sh.ome.itemex.GUI.sendGUI;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.functions.sqliteDb;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static org.bukkit.Bukkit.getLogger;
import static sh.ome.itemex.commands.commands.*;

public class ix_command implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (Itemex.itemex_stats) {
            // CommandUsageCounts
            String full_command = String.join(" ", strings);
            if (!Itemex.commandUsageCounts.containsKey(full_command))
                Itemex.commandUsageCounts.put(full_command, 1);
            else {
                int count = Itemex.commandUsageCounts.get(full_command);
                Itemex.commandUsageCounts.put(full_command, count + 1);
            }

            int totalCommandsEntered = 0;
            for (int count : Itemex.commandUsageCounts.values())
                totalCommandsEntered += count;
            if (totalCommandsEntered > 20)
                // clear the existing map object
                Itemex.commandUsageCounts.clear();
            // CommandUsageCounts END
        }


        if (command.getName().equalsIgnoreCase("ix")) {
            String reply_command = "";

            if (strings.length == 0) {
                Player p = null;
                if (sender instanceof Player) {
                    reply_command = reply_command + print_help(true);
                    p = (Player) sender;
                    //GUI.generateGUI(p, "ITEMEX - Market Orders", 0, 0);
                } else
                    reply_command = reply_command + print_help(false);

            } else {  // more than 1 parameter
                Player p = null;
                if (sender instanceof Player) {
                    p = (Player) sender;
                    if (!p.hasPermission("itemex.command.ix")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                } else if (sender instanceof ConsoleCommandSender) {
                    getLogger().info("SERVER TERMINAL");
                    getLogger().info("Players Name: " + strings[strings.length - 1]);
                    //p = Bukkit.getPlayerExact( strings[strings.length-1] ); // NAME
                    //OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString( strings[strings.length-1] ));
                    //getLogger().info("Name: " + op.getName());
                    getLogger().info("COMMANDS VIA THE SERVER TERMINAL ARE NOT SUPPORTED!");
                    return true;
                } else if (sender instanceof BlockCommandSender) {
                    OfflinePlayer op;
                    if (strings.length == 4) {
                        op = Bukkit.getOfflinePlayer(UUID.fromString(strings[strings.length - 1]));
                    } else if (strings.length == 6) {
                        op = Bukkit.getOfflinePlayer(UUID.fromString(strings[strings.length - 1]));
                    }
                    return true;
                }


                if (strings[0].equals("help")) {
                    if (sender instanceof Player)
                        reply_command = reply_command + print_help(true);
                    else
                        reply_command = reply_command + print_help(false);
                }

                else if (strings[0].equals("buy")) {
                    int item_counter = 0;
                    String itemid;
                    String item_json = "";

                    if (!p.hasPermission("itemex.command.ix.buy")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    if (strings.length == 1) { // /ix buy given itemID or whatisinmyrighthand
                        reply_command = "\n\n\n";
                        // check if something is in right hand


                        if (strings.length == 1) {
                            itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                            item_json = identify_item(p.getInventory().getItemInHand());
                        }

                        else
                            itemid = strings[1].toUpperCase();

                        if (itemid == "AIR") {
                            p.sendMessage(Itemex.language.getString("message_cant_buy_nothing"));
                            return false;
                        }

                        // check if there is a sell order with enough amount (1)
                        if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0] == 0) {
                            TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("buy_no_sellorders_to_buy") + ChatColor.WHITE + itemid + "\n.\n" + ChatColor.BLUE + Itemex.language.getString("buy_click_here_create") + ChatColor.GREEN + Itemex.language.getString("buy_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix buy " + itemid + " 1 limit");
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + get_meta(item_json) + " 1 limit "));
                            p.spigot().sendMessage(message);
                        } else {
                            //create buy order
                            p.sendMessage(Itemex.language.getString("buy_best_sellorder_price") + Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0]);
                            reply_command = reply_command + create_order(p, item_json, Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0], 1, "buy", "market");
                        }

                    } // end ix buy

                    else if (strings.length == 4 || strings.length == 5) { // /ix buy <itemid> <amount> limit <price>
                        //p.sendMessage("strlen: " + strings.length);
                        double price = 1;
                        int amount = 1;
                        boolean buy_order_ok = true;
                        itemid = strings[1];
                        item_json = get_json_from_meta(itemid);
                        //p.sendMessage(itemid);
                        //p.sendMessage(item_json);

                        //proof amount
                        if (strings[2].equals("max"))
                            amount = item_counter;
                        else {
                            try {
                                amount = Integer.parseInt(strings[2]);
                            } catch (NumberFormatException e) {
                                buy_order_ok = false;
                                reply_command = reply_command + "amount have to be an integer";
                                amount = 0;
                            }
                        }
                        if (amount <= 0) {
                            buy_order_ok = false;
                            reply_command = reply_command + Itemex.language.getString("buy_price_cant_be_0");
                        }


                        //proof if item is constructible:
                        ItemStack item_test = constructItem(item_json, 1);
                        if(item_test.getType().toString().equalsIgnoreCase("AIR"))
                            buy_order_ok = false;

                        if (strings[3].equals("market")) {
                            if(Itemex.getPlugin().mtop.get(item_json) == null) {
                                buy_order_ok = false;
                                price = 0;
                                return false;
                            }
                            else
                                price = Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0];
                            if (price <= 0)
                                buy_order_ok = false;
                        } else if(strings.length == 5) {
                            price = parseFloat(strings[4]);
                        }




                        //proof market or limit
                        if (!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + Itemex.language.getString("buy_wrong_market_option") + "(" + strings[3] + ")" + Itemex.language.get("buy_only_market_or_limit_accepted");
                            buy_order_ok = false;
                        }
                        if (strings[3].equals("limit") && strings.length == 5) {
                            if (price <= 0) {
                                reply_command = reply_command + Itemex.language.get("buy_price_not_allowed_lower_than_0") + price;
                                buy_order_ok = false;
                            }
                        }

                        if (buy_order_ok && price >= 0 && strings[3].equals("limit")) {
                            reply_command = reply_command + create_order(p, item_json, price, amount, "buy", strings[3]);
                        } else if (strings[3].equals("market")) {
                            if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0] == 0) {
                                TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("buy_no_sellorders_to_buy") + ChatColor.WHITE + itemid + "\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("buy_you_can_create") + ChatColor.GREEN + Itemex.language.getString("buy_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix buy " + itemid + " " + amount + " limit");
                                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + get_meta(item_json) + " " + amount + " limit "));
                                p.spigot().sendMessage(message);
                            } else {
                                //create buy order
                                p.sendMessage(Itemex.language.getString("buy_best_sellorder_price") + Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0]);
                                if (strings.length >= 5 && strings[4].equals("confirm")) {
                                    reply_command = reply_command + create_order(p, item_json, Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0], amount, "buy", "market");
                                } else {
                                    TopOrders topo = Itemex.getPlugin().mtop.get(item_json);
                                    List<sh.ome.itemex.RAM.Order> sell_orders = topo.get_top_sell(Itemex.admin_function);
                                    for (sh.ome.itemex.RAM.Order order : sell_orders) {
                                        double sprice = order.getPrice();
                                        int samount = order.getAmount();
                                        boolean isAdmin = order.isAdmin();

                                        if (amount == 0)
                                            reply_command = reply_command + ChatColor.DARK_RED + Itemex.language.getString("sellorder") + ChatColor.DARK_GRAY + itemid + "  [" + samount + "] " + format_price(sprice) + "\n";
                                        else if (isAdmin)
                                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price(sprice) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + "\n";
                                        else
                                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price(sprice) + "\n";
                                    }


                                    reply_command = reply_command + "-----------------------------\n";
                                    p.sendMessage(reply_command);
                                    reply_command = "";
                                    TextComponent message = new TextComponent(ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("confirm_order_price"));
                                    message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid + " " + amount + " market confirm "));
                                    p.spigot().sendMessage(message);
                                }
                            }
                        } else {
                            //getLogger().info(buy_order_ok);
                        }

                    } else {
                        //reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /ix buy *<itemname> *<limit> *<price> *<amount> | * == optional";
                    }
                } // end buy


                else if (strings[0].equals("sell")) {
                    String item_json ="";
                    if (!p.hasPermission("itemex.command.ix.sell")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    Order sellorder = new Order();
                    boolean isnt_damaged = true;
                    boolean item_supported = true;
                    String itemid;

                    if (strings.length == 1 || strings.length == 2) { // /ix sell
                        if (strings.length == 1) { // itemid is what player has in hand
                            item_json = identify_item(p.getInventory().getItemInHand());
                            itemid = get_itemid(item_json);
                            if(itemid.contains("PAINTING") || itemid.contains("GOAT_HORN") || itemid.contains("SUSPICIOUS_STEW")|| itemid.equals("more_than_one_enchantment_not_supported"))
                                item_supported = false;
                        } else {
                            boolean item_found = false;
                            // check if player have the amount of items provided at the parameter
                            int item_counter = 0;
                            for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                                if (item != null && identify_item(item).equals(get_json_from_meta(strings[1]))) {  //   strings[1].equalsIgnoreCase(item.getType().toString())) { //searching only for items with the given ID from command
                                    reply_command = reply_command + " " + item.getAmount() + "x" + item.getType() + "\n";
                                    item_counter = item_counter + item.getAmount();
                                    item_found = true;
                                    item_json = identify_item(item);
                                }
                            }
                            if (item_found) {
                                itemid = strings[1].toUpperCase();
                            }
                            else
                                itemid = "AIR";
                        }

                        if (p.getInventory().getItemInMainHand().getDurability() != 0)    // GET DAMAGE 0 = no damage
                            isnt_damaged = false;

                        if (!itemid.equals("AIR") && isnt_damaged && item_supported) {
                            // if item not in mtop create empty one
                            if (Itemex.getPlugin().mtop.get(item_json) == null) {
                                TopOrders topo = new TopOrders();
                                Itemex.getPlugin().mtop.put(item_json, topo);
                            }
                            // check if there is a buy order with enough amount (1)
                            if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(item_json).get_top_buyorder_prices()[0] == 0) {
                                TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("sell_no_buyorders_to_sell") + ChatColor.WHITE + itemid + "\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("buy_you_can_create") + ChatColor.GREEN + Itemex.language.getString("sell_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix sell " + itemid + " 1 limit ");
                                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + get_meta(item_json) + " 1 limit "));
                                p.spigot().sendMessage(message);
                            } else {
                                // create_sell_order(p, itemid, 1, orders[first_buy_order].price); //replaced with create order
                                reply_command = reply_command + create_order(p, item_json, Itemex.getPlugin().mtop.get(item_json).get_top_buyorder_prices()[3], 1, "sell", "market");
                            }
                        } else {
                            if (isnt_damaged)
                                reply_command = Itemex.language.getString("cant_sell_enchantments");
                            else if (strings.length == 1)
                                reply_command = Itemex.language.getString("noting_in_right_hand");
                            else if (item_supported)
                                reply_command = "NOT IMPLEMENTED";
                            else
                                reply_command = Itemex.language.getString("item_not_in_inventory");
                        }
                    }


                    else if (strings.length == 4 || strings.length == 5) { // /ix sell <itemid> <amount> limit <price>
                        double price = 0;
                        boolean sell_order_ok = true;
                        boolean item_found = false;
                        itemid = strings[1];

                        // check if player have the amount of items provided at the parameter
                        int item_counter = 0;
                        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                            if (item != null && identify_item(item).equals( get_json_from_meta(strings[1]) )) { // search if json match from item in inventory with command
                                item_counter = item_counter + item.getAmount();
                                item_json = identify_item(item);
                                item_found = true;
                            }
                        }

                        //p.sendMessage("user amount: " + strings[2]);
                        //p.sendMessage("found amount: " + item_counter);

                        if(itemid.contains("PAINTING") || itemid.contains("GOAT_HORN") || itemid.contains("SUSPICIOUS_STEW") || itemid.equals("more_than_one_enchantment_not_supported"))
                            item_supported = false;

                        if (strings[3].equals("market") && item_found == true) {
                            price = Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0];
                            if (price <= 0)
                                sell_order_ok = false;
                        } else if (item_found == true){
                            price = parseFloat(strings[4]);
                        }

                        // proof price
                        if (!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + Itemex.language.getString("buy_wrong_market_option") + strings[3] + Itemex.language.getString("buy_only_market_or_limit_accepted");
                            sell_order_ok = false;
                        }
                        if (strings[3].equals("limit")) {
                            if (price <= 0) {
                                reply_command = reply_command + Itemex.language.getString("buy_price_not_allowed_lower_than_0") + price;
                                sell_order_ok = false;
                            }
                        }


                        //reply_command = "/ix sell <itemname> <amount> limit <price> " + p.getName();

                        // proof amount
                        if (strings[2].equals("max"))
                            sellorder.amount = item_counter;
                        else
                            sellorder.amount = parseInt(strings[2]);

                        // proof inventory amount >= given amount
                        if (item_counter < sellorder.amount)
                            item_found = false;
                        else
                            item_found = true;

                        if (sellorder.amount <= 0 && strings[3].equals("limit")) {
                            reply_command = reply_command + Itemex.language.getString("buy_price_not_allowed_lower_than_0") + price;
                            sell_order_ok = false;
                        } else {
                            if (!isnt_damaged || !item_supported) {
                                p.sendMessage("damaged or not supported item");
                            } else if (item_found) {
                                if (sell_order_ok && price >= 0 && strings[3].equals("limit"))
                                    reply_command = reply_command + create_order(p, item_json, price, sellorder.amount, "sell", strings[3]);
                                else {
                                    //getLogger().info("# DEBUG SELL market order");
                                    if (!Itemex.admin_function && Itemex.getPlugin().mtop.get(item_json).get_top_buyorder_prices()[0] == 0) {
                                        TextComponent message = new TextComponent(ChatColor.RED + Itemex.language.getString("sell_no_buyorders_to_sell") + ChatColor.WHITE + itemid + "\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("buy_you_can_create") + ChatColor.RED + Itemex.language.getString("sell_order") + ChatColor.BLUE + Itemex.language.getString("with") + ": /ix sell " + itemid + " 1 limit ");
                                        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + get_meta(item_json) + " " + sellorder.amount + " limit "));
                                        p.spigot().sendMessage(message);
                                    } else {
                                        //create buy order
                                        p.sendMessage(Itemex.language.getString("buy_best_sellorder_price") + Itemex.getPlugin().mtop.get(item_json).get_top_sellorder_prices()[0]);
                                        if (strings.length >= 5 && strings[4].equals("confirm")) {
                                            reply_command = reply_command + create_order(p, item_json, Itemex.getPlugin().mtop.get(item_json).get_top_buyorder_prices()[3], sellorder.amount, "sell", "market");
                                        } else {

                                            TopOrders topo = Itemex.getPlugin().mtop.get(item_json);
                                            List<sh.ome.itemex.RAM.Order> buy_orders = topo.get_top_buy(Itemex.admin_function);
                                            for (sh.ome.itemex.RAM.Order order : buy_orders) {
                                                double sprice = order.getPrice();
                                                int samount = order.getAmount();
                                                boolean isAdmin = order.isAdmin();

                                                if (samount == 0)
                                                    reply_command = reply_command + ChatColor.DARK_GREEN + Itemex.language.getString("buyorder") + ChatColor.DARK_GRAY + itemid + "  [" + samount + "] " + format_price(sprice) + "\n";
                                                else if (isAdmin)
                                                    reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price(sprice) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + "\n";
                                                else
                                                    reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + samount + "] " + format_price(sprice) + "\n";
                                            }

                                            reply_command = reply_command + "-----------------------------\n";
                                            p.sendMessage(reply_command);
                                            reply_command = "";
                                            TextComponent message = new TextComponent(ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("confirm_order_price"));
                                            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid + " " + sellorder.amount + " market confirm "));
                                            p.spigot().sendMessage(message);
                                        }
                                    }
                                }
                            } else
                                reply_command = reply_command + ChatColor.RED + Itemex.language.getString("no_given_items") + ChatColor.GOLD + strings[1] + ChatColor.RED + Itemex.language.getString("found_in_inventory") + "\n" + ChatColor.WHITE + Itemex.language.getString("check_correct_name") + " /ix whatIsInMyRightHand";
                        }
                    } // end ix sell limit

                    else {
                        //reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /ix sell <itemname> <amount> limit <price>";
                    }
                } // end sell


                else if (strings[0].equals("price")) {
                    if (!p.hasPermission("itemex.command.price")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    reply_command = "\n\n\n-----------------------------\n";
                    TopOrders topo = null;
                    String itemid = null;
                    String item_json = "";

                    if (strings.length == 1) { // /ix price
                        item_json = identify_item(p.getInventory().getItemInHand());
                        itemid = get_itemid(item_json);


                    } else if (strings.length >= 2) { // /ix price <item id>
                        itemid = strings[1];
                        item_json = get_json_from_meta( itemid );
                        itemid = get_itemid(item_json);
                    }

                    //p.sendMessage(item_json);

                    if (itemid.equals("AIR")) {
                        reply_command = Itemex.language.getString("price_no_item");
                        //reply_command = "You only have AIR in your hand!";
                    } else if (strings.length <= 2){
                        topo = Itemex.getPlugin().mtop.get(item_json);
                        reply_command = reply_command + Itemex.language.getString("price_of_item") + ChatColor.GOLD +  get_meta(item_json)+ ChatColor.WHITE + "\n";
                        reply_command = reply_command + "-----------------------------\n";
                        reply_command = reply_command + Itemex.language.getString("ordertype_itemid_amount_price") + "\n";

                        if(topo == null) {
                            reply_command = Itemex.language.getString("wrong_itemname");
                            p.sendMessage(reply_command);
                            return false;
                        }

                        List<sh.ome.itemex.RAM.Order> sell_orders = topo.get_top_sell(Itemex.admin_function);
                        int sellorder_counter = 0;
                        String sell_temp = "";
                        for (sh.ome.itemex.RAM.Order order : sell_orders) {
                            double price = order.getPrice();
                            int amount = order.getAmount();
                            boolean isAdmin = order.isAdmin();

                            if(sellorder_counter == 3)
                                sell_temp = ChatColor.DARK_GREEN + Itemex.language.getString("cs_best_to_buy") + ChatColor.RESET;
                            else sell_temp = "";

                            if (amount == 0)
                                reply_command = reply_command + ChatColor.DARK_RED + Itemex.language.getString("sellorder") + ChatColor.DARK_GRAY + itemid + "  [" + amount + "] " + format_price(price) + sell_temp + "\n";
                            else if (isAdmin)
                                reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price(price) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + sell_temp + "\n";
                            else
                                reply_command = reply_command + ChatColor.RED + Itemex.language.getString("sellorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price(price) + sell_temp + "\n";

                            sellorder_counter++;
                        }

                        List<sh.ome.itemex.RAM.Order> buy_orders = topo.get_top_buy(Itemex.admin_function);
                        int buyorder_counter = 0;
                        String buy_temp = "";
                        for (sh.ome.itemex.RAM.Order order : buy_orders) {
                            double price = order.getPrice();
                            int amount = order.getAmount();
                            boolean isAdmin = order.isAdmin();

                            if(buyorder_counter == 0)
                                buy_temp = ChatColor.DARK_RED + Itemex.language.getString("cs_best_to_sell") + ChatColor.RESET;
                            else
                                buy_temp = "";

                            if (amount == 0)
                                reply_command = reply_command + ChatColor.DARK_GREEN + Itemex.language.getString("buyorder") + ChatColor.DARK_GRAY + itemid + "  [" + amount + "] " + format_price(price) + buy_temp + "\n";
                            else if (isAdmin)
                                reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price(price) + ChatColor.YELLOW + " [admin]" + ChatColor.WHITE + buy_temp + "\n";
                            else
                                reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("buyorder") + ChatColor.WHITE + itemid + "  [" + amount + "] " + format_price(price) + buy_temp + "\n";


                            buyorder_counter++;
                        }

                        //print last trades
                        reply_command = reply_command + ChatColor.WHITE + "-----------------------------\n";
                        reply_command += "Last Trades: \n";



                        for (int x=0; x<=3; x++) {
                            double price = topo.get_last_trade_price()[x];
                            int timestamp = topo.get_last_timestamp()[x];

                            String date;
                            if(timestamp != 0) {
                                Instant instant = Instant.ofEpochSecond(timestamp);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
                                date = formatter.format(instant);
                            }
                            else
                                date = "-";

                            reply_command = reply_command +  ChatColor.DARK_AQUA + date + " - " + ChatColor.YELLOW + format_price( price ) + "\n";
                        }



                        reply_command = reply_command + ChatColor.WHITE + "-----------------------------\n";
                    }
                    else if(strings[2].contains("history")) {
                        String max_entries;
                        if (strings.length > 3)
                            max_entries = strings[3];
                        else
                            max_entries = "1";

                        String[] trades = sqliteDb.get_last_trades(item_json, max_entries);

                        for (String trade : trades) {
                            String date;
                            String[] parts = trade.split(":");
                            double price;
                            int timestamp;

                            if(parts.length == 2) {
                                try {
                                    price = Double.parseDouble(parts[1]);
                                    timestamp = Integer.parseInt(parts[0]);
                                } catch (NumberFormatException e) {
                                    continue;
                                }

                                if(timestamp != 0) {
                                    Instant instant = Instant.ofEpochSecond(timestamp);
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
                                    date = formatter.format(instant);
                                } else {
                                    date = "-";
                                    break;
                                }

                                reply_command = reply_command +  ChatColor.DARK_AQUA + date + " - " + ChatColor.YELLOW + format_price(price) + "\n";
                            } else {
                                //getLogger().info("Trade string does not contain both price and timestamp.");
                            }
                        }

                    }


                } // end price





                else if (strings[0].equals("send")) {

                    if (!p.hasPermission("itemex.command.ix.send")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                    //p.sendMessage("len: " + strings.length);

                    if(strings.length <2) {
                        return true;
                    }
                    else if(strings.length == 2) {
                        //OfflinePlayer receiver = Bukkit.getOfflinePlayer(strings[1]);
                        sendGUI.generateGUI(p, "IX Item Send to: " + strings[1]);
                    }
                    else if(strings.length > 3){
                        String receiver = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                        Player receiverplayer = Bukkit.getPlayer(strings[1]);

                        String itemid = strings[2];
                        String amount = strings[3];

                        String item_json = get_json_from_meta(itemid);
                        boolean item_found = false;

                        // check if player have the amount of items provided at the parameter
                        int item_counter = 0;
                        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                            if (item != null && identify_item(item).equals( item_json )) { // search if json match from item in inventory with command
                                item_counter = item_counter + item.getAmount();
                                item_json = identify_item(item);
                                item_found = true;
                            }
                        }

                        // Removing the items if found
                        if (item_found && item_counter >= Integer.parseInt(amount)) {
                            int amountToRemove = Integer.parseInt(amount);
                            Inventory inv = p.getInventory();
                            for (int i = 0; i < inv.getSize(); i++) {
                                ItemStack item = inv.getItem(i);
                                if (item != null && identify_item(item).equals(item_json)) {
                                    int newAmount = item.getAmount() - amountToRemove;
                                    if (newAmount > 0) {
                                        item.setAmount(newAmount);
                                        break;
                                    } else {
                                        amountToRemove -= item.getAmount();
                                        inv.setItem(i, null);
                                    }
                                    if (amountToRemove == 0) break;
                                }
                            }
                            sqliteDb.insertPayout(receiver.toString(), item_json, Integer.parseInt(amount)); // Insert item payout into db
                            p.sendMessage("Item: " + ChatColor.GOLD +  itemid + " [" + amount + "] " + ChatColor.GREEN + "sent to " + strings[1]);
                            if(receiverplayer != null)
                                receiverplayer.sendMessage(p.getName() + " sent you " + ChatColor.GOLD + itemid + " [" + amount + "] " + ChatColor.RESET + "(take a look in your vault)");
                        } else {
                            p.sendMessage(ChatColor.RED + "You do not have enough of the specified item to send.");
                        }
                    }

                } // end send





                else if (strings[0].equals("stats")) {
                    if (!p.hasPermission("itemex.command.ix.stats")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                    // p.sendMessage("len: " + strings.length);
                    int page = 1;
                    int itemsPerPage = 50;
                    int startIndex = (page - 1) * itemsPerPage;

                    if (strings.length == 1) {
                        reply_command = reply_command + "wrong command! Usage: /ix stats <buyorders | sellorders | tradingvolume> <* pagenumber> - // (*=optional)";
                    }

                    else if (strings.length == 3) {
                        try {
                            page = Integer.parseInt(strings[2]);
                        } catch (NumberFormatException e) {
                            p.sendMessage(ChatColor.RED + "Invalid page number.");
                            return true;
                        }
                    }

                    if(strings.length >= 2 && strings[1].equalsIgnoreCase("tradingvolume")) {
                        ArrayList<sqliteDb.ItemVolume> items = sqliteDb.getItemsSortedByVolume();
                        Collections.reverse(items);
                        for (int i = startIndex; i < startIndex + itemsPerPage && i < items.size(); i++) {
                            sqliteDb.ItemVolume item = items.get(i);
                            reply_command = reply_command + ChatColor.GREEN + get_meta(item.itemid) + ChatColor.RESET + " volume: " + ChatColor.GOLD + format_price(item.volume) + "\n";
                        }
                    } else if (strings[1].equalsIgnoreCase("buyorders") || strings[1].equalsIgnoreCase("sellorders")) {
                        ArrayList<sqliteDb.OrderBuffer> orders = sqliteDb.getOrdersSortedByPrice(strings[1].toUpperCase());
                        for (int i = startIndex; i < startIndex + itemsPerPage && i < orders.size(); i++) {
                            sqliteDb.OrderBuffer order = orders.get(i);
                            reply_command = reply_command + ChatColor.GREEN + get_meta(order.itemid) + ChatColor.RESET + " price: " + ChatColor.GOLD + format_price(order.price) + ChatColor.RESET + " amount: " + ChatColor.GOLD + order.amount + "\n";
                        }
                    }

                    else if (strings[1].equalsIgnoreCase("mylastpurchases") || strings[1].equalsIgnoreCase("mylastsells")) {
                        String item_color = ChatColor.GREEN.toString();

                        if (strings.length == 3) {
                            try {
                                page = Integer.parseInt(strings[2]);
                            } catch (NumberFormatException e) {
                                p.sendMessage(ChatColor.RED + "Invalid page number.");
                                return true;
                            }
                        }

                        boolean buy_or_sell = strings[1].equalsIgnoreCase("mylastpurchases");
                        if (!buy_or_sell) {
                            item_color = ChatColor.RED.toString();
                        }

                        List<String> orders = sqliteDb.getFulfilledOrdersFromPlayer(p.getUniqueId().toString(), buy_or_sell);

                        startIndex = (page - 1) * itemsPerPage;
                        int endIndex = Math.min(startIndex + itemsPerPage, orders.size());

                        for (int i = endIndex - 1; i >= startIndex && i >= 0; i--) {
                            String order = orders.get(i);
                            String[] orderDetails = order.split(":");
                            String itemID = orderDetails[0] + ":" + orderDetails[1];
                            String amount = orderDetails[2];
                            double price = Double.parseDouble(orderDetails[3]);
                            String timestamp = orderDetails[4];

                            Instant instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
                            String date = formatter.format(instant);

                            reply_command = reply_command + item_color + get_meta(itemID) + ChatColor.GOLD + " [" + amount + "] " + ChatColor.RESET + format_price(price) + " " + ChatColor.DARK_GRAY + date + "\n";
                        }
                    }


                    else {
                        reply_command = reply_command + "Wrong command: /ix stats <tradingvolume | buyorders | sellorders> [page number]";
                    }
                } // end stats








                else if (strings[0].equals("whatIsInMyRightHand")) {

                    if (!p.hasPermission("itemex.command.ix.whatIsInMyRightHand")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    ItemStack item = p.getInventory().getItemInMainHand();
                    Gson gson = new Gson();
                    String json = identify_item(item);
                    List<Map<String, Object>> list = gson.fromJson(json, List.class);
                    int enc_counter = 0;

                    for (Map<String, Object> map : list) {
                        if (map.containsKey("itemid")) {
                            reply_command = "\n-----------------------------\n";
                            reply_command = reply_command + ChatColor.DARK_GREEN + "ITEMID: " + ChatColor.WHITE + get_meta(json) + "\n";
                            reply_command = reply_command + ChatColor.DARK_GRAY + json + ChatColor.RESET + "\n";
                            reply_command = reply_command + "-----------------------------\n";
                        }
                        if(map.containsKey("enc"))
                            enc_counter++;
                    }

                    if(enc_counter > 1)
                        reply_command = reply_command + "MORE THAN ONE ENC NOT SUPPORTED\n";

                }



                else if (strings[0].equals("order")) {
                    int orderid = -1;

                    if (strings.length == 1) {
                        reply_command = "/ix order <close | edit | list>";
                    } else if (strings[1].equals("close")) {
                        if (!p.hasPermission("itemex.command.ix.order.close")) {
                            p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                            return true;
                        }

                        if (strings.length <= 2) {
                            reply_command = reply_command + ChatColor.YELLOW + Itemex.language.getString("you_can_only_use_buy_or_sellorders") + ChatColor.RESET;
                        }
                        else if (strings.length <= 3) {
                            reply_command = reply_command + ChatColor.YELLOW + "You have to provide the id number: " + ChatColor.RESET + "\n/ix order <list | close> <buyorders | sellorders> <ID Number>";
                        }

                        else if (strings[2].equals("buyorders") || strings[2].equals("sellorders")) {
                            try {
                                orderid = parseInt(strings[3]);
                            } catch (NumberFormatException e) {
                                reply_command = Itemex.language.getString("orderid_must_be_number") + "\n";
                            }
                            if (orderid != -1) {
                                if (sqliteDb.PlayercloseOrder(p.getUniqueId().toString(), strings[2].toUpperCase(), orderid))
                                    reply_command = reply_command + ChatColor.GREEN + Itemex.language.getString("order_del_success") + ChatColor.RESET;
                                else
                                    reply_command = reply_command + ChatColor.RED + Itemex.language.getString("order_not_del_success") + ChatColor.RESET;
                            }
                        } else
                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("only_buy_or_sellorders") + ChatColor.RESET;
                    }



                    else if (strings[1].equals("edit")) {
                        if (!p.hasPermission("itemex.command.ix.order.edit")) {
                            p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                            return true;
                        }
                        if(strings[2] != null && ( strings[2].equalsIgnoreCase("BUYORDERS") || strings[2].equalsIgnoreCase("SELLORDERS"))) {

                            int orderId = -1;
                            double price = -1.0;
                            int amount = -1;

                            try {
                                if (strings.length > 5) {
                                    if (strings[3] != null && !strings[3].isEmpty()) {
                                        if (strings[3].matches("\\d+")) {
                                            orderId = Integer.parseInt(strings[3]);
                                        } else {
                                            reply_command = reply_command + ChatColor.RED + " Error: OrderID isn't a valide number";
                                        }
                                    }

                                    if (strings[4] != null && !strings[4].isEmpty()) {
                                        if (strings[4].matches("[0-9]*\\.?[0-9]*")) {
                                            price = Double.parseDouble(strings[4]);
                                        } else {
                                            reply_command = reply_command + ChatColor.RED + " Error: Price isn't a valid floating number.";
                                        }
                                    }

                                    if (strings[5] != null && !strings[5].isEmpty()) {
                                        if (strings[5].matches("\\d+")) {
                                            amount = Integer.parseInt(strings[5]);
                                        } else {
                                            reply_command = reply_command + ChatColor.RED + " Error: Amount isn't a valide number.";
                                        }
                                    }
                                }
                            } catch (NumberFormatException e) {
                                reply_command = reply_command + ChatColor.RED + " Error by converting the numbers " + e.getMessage();
                            }

                            if (orderId != -1 && price != -1.0 && amount != -1) { // if no error

                                // get the existing order
                                boolean buy_or_sell_order = false;
                                if(strings[2].equalsIgnoreCase("BUYORDERS"))
                                    buy_or_sell_order = true;

                                sqliteDb.OrderBuffer old_order = sqliteDb.getOrder(strings[3], buy_or_sell_order); //true = buy ; false = sell

                                if(amount > old_order.amount) {
                                    // search if player have enought in inventory
                                    String parts[] = getFreeInventory(p, old_order.itemid).split(":");
                                    int inv_item_amount = Integer.parseInt( parts[1] );
                                    //p.sendMessage("amount: "+ amount + " old_order_amount: " + old_order.amount + " inv_item_amount: " + inv_item_amount);
                                    if( (amount - old_order.amount) <= inv_item_amount) { // enough in inv
                                        //remove from inv

                                        if (sqliteDb.updateOrder(strings[2].toUpperCase(), old_order.id, amount, price, old_order.ordertype, old_order.itemid)) {
                                            int amountToRemove = amount - old_order.amount;
                                            Inventory inv = p.getInventory();
                                            for (int i = 0; i < inv.getSize(); i++) {
                                                ItemStack item = inv.getItem(i);
                                                if (item != null && identify_item(item).equals(old_order.itemid)) {
                                                    int newAmount = item.getAmount() - amountToRemove;
                                                    if (newAmount > 0) {
                                                        item.setAmount(newAmount);
                                                        break;
                                                    } else {
                                                        amountToRemove -= item.getAmount();
                                                        inv.setItem(i, null);
                                                    }
                                                    if (amountToRemove == 0) break;
                                                }
                                            }
                                            reply_command = reply_command + ChatColor.GREEN + "Order changed! [" + old_order.itemid + " : " + amount + "]";
                                        }

                                    }
                                    else {
                                        reply_command = reply_command + ChatColor.RED + " " + Itemex.language.getString("item_not_in_inventory");
                                    }

                                }

                                else if(amount < old_order.amount) {
                                    // transfer difference to vault
                                    if(sqliteDb.updateOrder(strings[2].toUpperCase(), old_order.id, amount, price, old_order.ordertype, old_order.itemid)) {
                                        sqliteDb.insertPayout(p.getUniqueId().toString(), old_order.itemid, old_order.amount-amount); // Insert item payout into db
                                        reply_command = reply_command + ChatColor.GREEN + "Order changed! [" + old_order.itemid + " : " + amount + "]";
                                        TextComponent message = new TextComponent("\n" + ChatColor.BLUE + ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("sq_you_can_with") + " /ix withdraw " + get_meta(old_order.itemid) + " " + (old_order.amount-amount));
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix withdraw " + get_meta(old_order.itemid) + " " + (old_order.amount-amount) ));
                                        p.spigot().sendMessage(message);
                                    }
                                }

                                else {
                                    if(sqliteDb.updateOrder(strings[2].toUpperCase(), old_order.id, amount, price, old_order.ordertype, old_order.itemid)) {
                                        reply_command = reply_command + ChatColor.GREEN + "Order changed! [" + old_order.itemid + " : " + amount + "]";
                                    }
                                }


                            }

                        }
                        else
                            reply_command = reply_command + Itemex.language.getString("only_buy_or_sellorders");
                    } // end order edit




                    else if (strings[1].equals("list")) {
                        if (strings.length <= 2) {
                            reply_command = reply_command + ChatColor.RED + Itemex.language.getString("you_can_only_use_buy_or_sellorders") + ChatColor.RESET;
                        } else if (strings[2].equals("buyorders") || strings[2].equals("sellorders")) {
                            boolean buy_or_sell;
                            String item_json = "*";
                            if (strings.length == 4)
                                item_json = get_json_from_meta(strings[3]);

                            String color = ChatColor.RED.toString();

                            if (strings[2].equals("buyorders")) {
                                buy_or_sell = true;
                                color = ChatColor.GREEN.toString();
                            } else
                                buy_or_sell = false;

                            sqliteDb.OrderBuffer[] list = sqliteDb.getOrdersOfPlayer(p.getUniqueId().toString(), item_json, buy_or_sell, 1); //true = buy ; false = sell
                            reply_command = reply_command + Itemex.language.getString("list_all_orders_and_categories");
                            for (int i = 0; i < 100; i++) { // 100 is max
                                if (list[i] == null) {
                                    break;
                                } else {
                                    reply_command = reply_command + color + list[i].id + " " + ChatColor.WHITE + get_itemid(list[i].itemid) + " " + list[i].amount + " " + color + format_price( list[i].price ) + " " + list[i].ordertype + "\n" + ChatColor.WHITE;
                                }
                            } // end for
                            //reply_command = reply_command + "\n" + Itemex.language.getString("and_more") + " /ix order list <sellorders | buyorders> <itemid>";

                        } else
                            reply_command = reply_command + ChatColor.YELLOW + Itemex.language.getString("you_can_only_use_buy_or_sellorders") + ChatColor.RESET;

                    } // end of list

                } // end order



                else if (strings[0].equals("withdraw")) {
                    if (!p.hasPermission("itemex.command.ix.withdraw")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                    // /ix withdraw list
                    if (strings.length == 1 || (strings.length == 2) && (strings[1].equals("list") || strings[1].equals("_list"))) {
                        sqliteDb.Payout[] payouts = sqliteDb.getPayout(p.getUniqueId().toString());
                        reply_command = reply_command + "\n\n" + Itemex.language.getString("only_following_items") + "\n";

                        for (int i = 0; i < payouts.length; i++) {
                            if (payouts[i] == null) { //skip empty entries
                                if (i == 0)
                                    reply_command = reply_command + "\n\n" + Itemex.language.getString("nothing_to_withdraw");
                                break;
                            }
                            reply_command = reply_command + "\n [" + payouts[i].amount + "] " + ChatColor.GREEN + get_meta(payouts[i].itemid) + ChatColor.WHITE + " \n";
                        }
                    }

                    // /ix withdraw <itemid> <amount>
                    else if (strings.length == 3) {
                        p.sendMessage( withdraw(strings[1], strings[2], p) ); // itemid, amount, Player
                    }

                    else {
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /help";
                    }
                } // end withdraw


                else if (strings[0].equals("deposit")) {
                    if (!p.hasPermission("itemex.command.ix.deposit")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    if (strings.length == 2) {
                        if(strings[1].equals("all")) {
                            ItemStack[] items = p.getInventory().getContents();
                            for (ItemStack item : items) {
                                if (item != null && !item.getType().equals(Material.AIR) && item.getDurability() == 0) {
                                    deposit(get_meta( identify_item(item)), String.valueOf(item.getAmount()), p);
                                }
                            }

                        }
                    }

                    // /ix deposit <itemid> <amount>
                    else if (strings.length == 3) {
                        boolean is_integer = true;
                        try {
                            Integer.parseInt(strings[2]);
                        } catch (NumberFormatException e) {
                            is_integer = false;
                        }
                        if(strings[2].equalsIgnoreCase("max"))
                            is_integer = true;

                        if(is_integer) {
                            boolean status = deposit(strings[1], strings[2], p);
                            if(status)
                                p.sendMessage( ChatColor.GREEN + "deposited!");
                            else
                                p.sendMessage( ChatColor.RED + "error at deposit: Item not found or not enough amount of it!");
                        }
                        else
                        {
                            p.sendMessage("The amount have to be an integer");
                        }


                    }

                    else {
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /help";
                    }
                } // end deposit



                else if (strings[0].equals("gui")) {
                    if (!p.hasPermission("itemex.command.ix.gui")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }
                    ix_GUI.generateGUI(p, "IX - Market Orders", 0, 0);
                }


                else if (strings[0].equals("setting")) {
                    if (!p.hasPermission("itemex.command.ix.setting")) {
                        p.sendMessage(ChatColor.RED + Itemex.language.getString("message_no_permission"));
                        return true;
                    }

                    if (strings.length == 3 && strings[1].equalsIgnoreCase("withdraw_threshold")) {
                        // proof if strings[2] is an integer
                        boolean is_integer = true;
                        try {
                            Integer.parseInt(strings[2]);
                        } catch (NumberFormatException e) {
                            is_integer = false;
                        }

                        if(is_integer) {
                            boolean status = sqliteDb.player_settings(p.getUniqueId().toString(), strings[2], false);
                            if(status)
                                p.sendMessage("withdraw_threshold set to: " + ChatColor.GREEN + strings[2]);
                            else
                                p.sendMessage("Error at DB. Please send a email to: xcatpc@proton.me to fix the bug.");
                        }
                        else
                            p.sendMessage("The amount have to be an integer");


                    }
                    else {
                        reply_command = reply_command + Itemex.language.getString("wrong_command") + " /help";
                    }
                }


                // hidden commands
                else if (strings[0].equals("update_ram")) { // /ix update_ram <itemid>
                    if (strings.length != 2) {
                        p.sendMessage(ChatColor.RED + " you have to give one argument: ITEMID");
                    } else {
                        getLogger().info("ITEMID: " + strings[1].toUpperCase());
                        sqliteDb.loadBestOrdersToRam(strings[1].toUpperCase(), true);
                        p.sendMessage(ChatColor.GREEN + "Top Order for Item: " + strings[1].toUpperCase() + " updated in RAM!");
                    }
                } else if (strings[0].equals("command_usage")) {
                    for (Map.Entry<String, Integer> entry : Itemex.commandUsageCounts.entrySet()) {
                        String commandName = entry.getKey();
                        int usageCount = entry.getValue();
                        getLogger().info(commandName + ": " + usageCount);
                    }
                } else if (strings[0].equals("get_uuid")) {
                    p.sendMessage(p.getUniqueId().toString());
                    getLogger().info(p.getUniqueId().toString());
                }
                else if (strings[0].equals("construct_item")) {
                    ItemStack item = constructItem(strings[1], Integer.parseInt(strings[2]));
                    p.getInventory().addItem(item);
                }
                else if (strings[0].equals("map")) {
                    // Create a new map view.
                    MapView mapView = Bukkit.createMap(p.getWorld());

                    // Remove all existing renderers.
                    for (MapRenderer renderer : mapView.getRenderers()) {
                        mapView.removeRenderer(renderer);
                    }

                    // Add our custom renderer.
                    mapView.addRenderer(new MapRenderer() {
                        @Override
                        public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                            // This will only be called once per player, when they first view the map.

                            // Draw axes lines.
                            byte axisColor = MapPalette.matchColor(java.awt.Color.WHITE);
                            for (int x = 0; x < 128; x++) {
                                mapCanvas.setPixel(x, 118, axisColor); // Horizontal axis
                            }
                            for (int y = 0; y < 128; y++) {
                                mapCanvas.setPixel(10, y, axisColor); // Vertical axis
                            }
                        }
                    });

                    // Create a new map item.
                    ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
                    MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
                    mapMeta.setMapView(mapView);
                    mapItem.setItemMeta(mapMeta);

                    // Give the map to the player.
                    p.getInventory().addItem(mapItem);
                }
                else if (strings[0].equals("space")) {
                    if (strings.length != 2) {
                        p.sendMessage(ChatColor.RED + " you have to set one argument: ITEMID");
                    } else {
                        String item_json = get_json_from_meta(strings[1]);
                        String parts[] = getFreeInventory(p, item_json).split(":");
                        int max_item_count = Integer.parseInt(parts[1]);
                        int max_items = Integer.parseInt(parts[2]);
                        p.sendMessage("max_item_count: " + max_item_count);
                        p.sendMessage("max_items: " + max_items);
                    }


                }













                else
                    reply_command = "Option not found!: " + strings[0] + "\n use /ix help";

            } // end else more than 1 args


            // Send reply to p
            if (sender instanceof Player) {
                Player p = (Player) sender;
                Economy economy = Itemex.getEconomy();

                if (p.hasPermission("itemex.ix")) {
                    //p.sendMessage(ChatColor.GREEN + "The player has a balance of: " + economy.getBalance(p));
                    p.sendMessage(reply_command);
                } else {
                    p.sendMessage(reply_command);
                    //p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command!");
                }
            } else if (sender instanceof ConsoleCommandSender) {
                //getLogger().info("This is from the server command box");
                getLogger().info(reply_command);
            } else if (sender instanceof BlockCommandSender) {
                //getLogger().info("This is from the command block");
                getLogger().info(reply_command);
            }
        }
        commands.checkAndSendUsageCounts();
        return true;
    } //  end onCommand


    public static class Order {
        public String uuid;
        public String itemid;
        public String ordertype;
        public int amount;
        public double price;
    }

    public static boolean deposit(String itemid, String amount, Player p) {
        String reply_command = "";
        String item_json = "";
        int i_amount = 0;
        if(!amount.equals("max"))
            i_amount = Integer.parseInt(amount);
        boolean item_found = false;
        // check if player have the amount of items provided at the parameter
        int item_counter = 0;
        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
            if(item != null && !item.getType().toString().equals("AIR")) {
                //p.sendMessage(identify_item(item));
                p.sendMessage(itemid + ChatColor.GREEN + " deposited." + ChatColor.RESET);
            }
            if (item != null && identify_item(item).equals(get_json_from_meta(itemid)) && item.getDurability() == 0) { //searching only for items with the given ID from command
                reply_command = reply_command + " " + item.getAmount() + "x" + item.getType() + "\n";
                item_counter = item_counter + item.getAmount();
                item_found = true;
                item_json = identify_item(item);
            }
        }
        if(amount.equals("max"))
            i_amount = item_counter;

        // if amount is higher than item in inventory
        if(i_amount > item_counter) {
            return false;
        }
        else if (item_found && i_amount > 0) {
            int amountToRemove = i_amount;
            Inventory inv = p.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && identify_item(item).equals(item_json)) {
                    int newAmount = item.getAmount() - amountToRemove;
                    if (newAmount > 0) {
                        item.setAmount(newAmount);
                        break;
                    } else {
                        amountToRemove -= item.getAmount();
                        inv.setItem(i, null);
                    }
                    if (amountToRemove == 0) break;
                }
            }
            // add item to payout
            sqliteDb.insertPayout(p.getUniqueId().toString(), item_json, i_amount); // Insert item payout into db
            return true;
        }


        else
            return false;

    }

    public static String withdraw(String itemid, String amount, Player p) {
        //getLogger().info("# DEBUG - at withdraw");
        String item_json = get_json_from_meta(itemid);
        String reply = "";
        int item_amount = 0;
        int available_items = 0;

        sqliteDb.Payout[] payouts = sqliteDb.getPayout(p.getUniqueId().toString());
        for (sqliteDb.Payout payout : payouts) {
            if(payout.itemid.equals(item_json))
                available_items = payout.amount;
        }

        if (amount.equals("max")) {
            item_amount = available_items;
        } else {
            item_amount = parseInt(amount);
            if (item_amount > 9 * 5 * 64) {
                item_amount = 9 * 5 * 64;
            }
        }

        // proof if player have enough space
        String parts[] = getFreeInventory(p, item_json).split(":");
        int max_item_count = Integer.parseInt(parts[1]);
        int max_items = Integer.parseInt(parts[2]);

        if(max_items < item_amount) {
            reply = Itemex.language.getString("no_space") + " you can withdraw max: " + max_items + "(max_items)";
            item_amount = max_items; // Adjust the item_amount to the max items the player can hold
        }
        else if(available_items < item_amount) {
            reply = Itemex.language.getString("no_space") + " you can withdraw max: " + available_items + "(available_items)";
            item_amount = available_items; // Adjust the item_amount to the available items
        }
        for (sqliteDb.Payout payout : payouts) {
            if(payout.itemid.equals(item_json)) {
                sqliteDb.updatePayout(p.getUniqueId().toString(), item_json, item_amount);
                for(int i = 0; i < item_amount; i++) {
                    p.getPlayer().getInventory().addItem( constructItem(item_json, 1)); // give buyer good from payout
                }
            }
        }
        return reply;
    }







}