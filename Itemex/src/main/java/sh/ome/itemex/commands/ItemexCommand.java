package sh.ome.itemex.commands;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.functions.sqliteDb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static sh.ome.itemex.Itemex.econ;

public class ItemexCommand implements CommandExecutor {

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
                    System.out.println("SERVER TERMINAL");
                    System.out.println("Players Name: " + strings[strings.length - 1]);
                    //p = Bukkit.getPlayerExact( strings[strings.length-1] ); // NAME
                    //OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString( strings[strings.length-1] ));
                    //System.out.println("Name: " + op.getName());
                    System.out.println("COMMANDS VIA THE SERVER TERMINAL ARE NOT SUPPORTED!");
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
                            //System.out.println(buy_order_ok);
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
                                    //System.out.println("# DEBUG SELL market order");
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

                    p.sendMessage(item_json);

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
                                //System.out.println("Trade string does not contain both price and timestamp.");
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
                                receiverplayer.sendMessage(p.getName() + " send you " + ChatColor.GOLD + itemid + " [" + amount + "] (look in your vault: ix /gui");
                        } else {
                            p.sendMessage(ChatColor.RED + "You do not have enough of the specified item to send.");
                        }
                    }

                } // end send




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
                    GUI.generateGUI(p, "ITEMEX - Market Orders", 0, 0);
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
                        System.out.println("ITEMID: " + strings[1].toUpperCase());
                        sqliteDb.loadBestOrdersToRam(strings[1].toUpperCase(), true);
                        p.sendMessage(ChatColor.GREEN + "Top Order for Item: " + strings[1].toUpperCase() + " updated in RAM!");
                    }
                } else if (strings[0].equals("command_usage")) {
                    for (Map.Entry<String, Integer> entry : Itemex.commandUsageCounts.entrySet()) {
                        String commandName = entry.getKey();
                        int usageCount = entry.getValue();
                        System.out.println(commandName + ": " + usageCount);
                    }
                } else if (strings[0].equals("get_uuid")) {
                    p.sendMessage(p.getUniqueId().toString());
                    System.out.println(p.getUniqueId().toString());
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
                //System.out.println("This is from the server command box");
                System.out.println(reply_command);
            } else if (sender instanceof BlockCommandSender) {
                //System.out.println("This is from the command block");
                System.out.println(reply_command);
            }
        }
        checkAndSendUsageCounts();
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
                p.sendMessage(identify_item(item));
                p.sendMessage(itemid);
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
        //System.out.println("# DEBUG - at withdraw");
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
            reply = Itemex.language.getString("no_space") + " you can withdraw max: " + max_items;
            item_amount = max_items; // Adjust the item_amount to the max items the player can hold
        }
        else if(available_items < item_amount) {
            reply = Itemex.language.getString("no_space") + " you can withdraw max: " + available_items;
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





    public static String create_order(Player p, String item_json, double price, int amount, String buy_or_sell, String market_option) {
        //System.out.println("# DEBUG AT: create_order: " + amount + " item_json: " + item_json);
        String itemid = get_itemid(item_json);
        String reply_command = "";

        Order order = new Order();
        // create sell order (RAM)
        order.amount = amount;
        order.uuid = p.getUniqueId().toString();
        order.itemid = item_json;
        order.ordertype = buy_or_sell + ":" + market_option;
        order.price = price;

        sqliteDb db_order = new sqliteDb(order);

        if (buy_or_sell.equals("sell")) {
            if (db_order.createSellOrder() != -1) {
                reply_command = ChatColor.RED + Itemex.language.getString("sellorder_C") + ChatColor.WHITE + ChatColor.BOLD + "[" + amount + "] " + itemid.toUpperCase() + ChatColor.WHITE + " " + Itemex.language.getString("created");
            } else {
                reply_command = Itemex.language.getString("er_sello_ncreated");
            }

            //p.sendMessage("item_json: " + item_json);
            //p.sendMessage("amount: " + amount);

            int amountToRemove = amount;
            Inventory inv = p.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && identify_item(item).equals(item_json)) {
                    int newAmount = item.getAmount() - amountToRemove;
                    if (newAmount > 0) {
                        item.setAmount(newAmount);
                        break;
                    } else {
                        inv.setItem(i, null);
                        amountToRemove -= item.getAmount();
                    }
                    if (amountToRemove == 0) break;
                }
            }
        }


        else if (buy_or_sell.equals("buy")) {
            double buyer_balance = econ.getBalance(p);
            if ((amount * price) < buyer_balance) {
                if (db_order.createBuyOrder() != -1)
                    reply_command = ChatColor.GREEN + Itemex.language.getString("buyorder_C") + ChatColor.WHITE + Itemex.language.getString("created") + ChatColor.BOLD + " [" + amount + "] " + itemid + ChatColor.WHITE;
                else
                    reply_command = Itemex.language.getString("erbuyo_ncreated");
            } else {  //not enough money
                reply_command = ChatColor.RED + Itemex.language.getString("not_enough_money") + ChatColor.WHITE + Itemex.language.getString("you_need") + ChatColor.GREEN + format_price((amount * price)) + ChatColor.WHITE + Itemex.language.getString("but_you_only_have") + ChatColor.RED + " " + format_price(buyer_balance);
            }
        }
        return reply_command;
    }

    public static String get_itemid(String json) {
        //System.out.println("# DEBUG (get_itemid): " + json);
        Gson gson = new Gson();
        List<Map<String, Object>> list = gson.fromJson(json, List.class);
        int enc_counter = 0;
        String itemid ="";
        for (Map<String, Object> map : list) {
            if (map.containsKey("itemid")) {
                itemid = map.get("itemid").toString();
            }
            if(map.containsKey("enc"))
                enc_counter++;
        }
        if(enc_counter > 1)
            return "more_than_one_enchantment_not_supported";
        return itemid;
    }

    public static String get_json_from_meta(String meta) {
        String[] elements = meta.split(":");

        if (elements.length == 1) {
            // Normal item
            return "[{\"itemid\":\"" + elements[0].toUpperCase() +"\"}]";
        } else if (elements[0].equals("ENCHANTED_BOOK")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"enc\":\"" + elements[1] + "\",\"lev\":" + elements[2] + "}]";
        } else if (elements[0].equals("POTION") || elements[0].equals("SPLASH_POTION") || elements[0].equals("LINGERING_POTION") || elements[0].equals("TIPPED_ARROW")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"bp_ext\":" + elements[2] + ",\"bp_name\":\"" + elements[1] + "\",\"bp_upg\":" + elements[3] + "}]";
        } else if (elements[0].equals("GOAT_HORN")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"song\":\"" + elements[1] + "\"}]";
        } else if (elements[0].equals("SUSPICIOUS_STEW")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"stew_eff\":\"" + elements[1] + "\",\"stew_dur\":" + elements[2] + "}]";
        } else if (elements[0].equals("PAINTING")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"paint\":\"" + elements[1] + "\"}]";
        } else if (elements[0].equals("FIREWORK_ROCKET")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"f_dur\":" + elements[1] + "}]";
        } else {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() +"\"}]";
        }
    }


    public static String get_meta(String json) {
        Gson gson = new Gson();
        List<Map<String, Object>> list = gson.fromJson(json, List.class);
        int enc_counter = 0;
        String itemid = "";

        // enchanted book
        String enc = "";
        String lev = "";

        // tipped arrow or potion
        String bp_name = "";
        String bp_ext = "";
        String bp_upg = "";

        // goat horn
        String song = "";

        // SUSPICIOUS_STEW
        String stew_eff = "";
        //String stew_amp = "";
        String stew_dur = "";

        // painting
        String paint = "";

        // firework rocket
        String f_dur = "";

        for (Map<String, Object> map : list) {
            if (map.containsKey("itemid"))
                itemid = map.get("itemid").toString();

            if(map.containsKey("enc")) {
                enc = map.get("enc").toString();
                enc_counter++;
            }
            if(map.containsKey("lev")) {
                Double doubleValue = (Double) map.get("lev");
                lev = Integer.toString(doubleValue.intValue());
            }

            if(map.containsKey("bp_name"))
                bp_name = map.get("bp_name").toString();
            if(map.containsKey("bp_ext"))
                bp_ext = map.get("bp_ext").toString();
            if(map.containsKey("bp_upg"))
                bp_upg = map.get("bp_upg").toString();

            if(map.containsKey("song"))
                song = map.get("song").toString();

            if(map.containsKey("stew_eff"))
                stew_eff = map.get("stew_eff").toString();
            if(map.containsKey("stew_amp")) {
                //Double doubleValue = (Double) map.get("stew_amp");
                //stew_amp = Integer.toString(doubleValue.intValue());
            }
            if(map.containsKey("stew_dur")) {
                Double doubleValue = (Double) map.get("stew_dur");
                stew_dur = Integer.toString(doubleValue.intValue());
            }

            if(map.containsKey("paint"))
                paint = map.get("paint").toString();

            if(map.containsKey("f_dur")) {
                Double doubleValue = (Double) map.get("f_dur");
                f_dur = Integer.toString(doubleValue.intValue());
            }
        }

        if(enc_counter > 1)
            return "more_than_one_enchantment_not_supported";
        else if(itemid.equals("ENCHANTED_BOOK"))
            return itemid + ":" + enc + ":" + lev;
        else if(itemid.equals("POTION") || itemid.equals("SPLASH_POTION") || itemid.equals("LINGERING_POTION") || itemid.equals("TIPPED_ARROW"))
            return itemid + ":" + bp_name + ":" + bp_ext + ":" +  bp_upg;
        else if(itemid.equals("GOAT_HORN"))
            return itemid + ":" + song;
        else if(itemid.equals("SUSPICIOUS_STEW"))
            return itemid + ":" + stew_eff + ":" + stew_dur;
        else if(itemid.equals("PAINTING"))
            return itemid + ":" + paint;
        else if(itemid.equals("FIREWORK_ROCKET"))
            return itemid + ":" + f_dur;
        else
            return itemid;
    }


    public static String identify_item(ItemStack item) {
        if(item == null)
            return null;
        String itemid = item.getType().toString().toUpperCase();
        ItemMeta itemMeta = item.getItemMeta();

        List<Map<String, Object>> enchantmentList = new ArrayList<>(); // stores everything in this map
        Map<String, Object> temp = new HashMap<>();
        temp.put("itemid", itemid);
        if (item.getDurability() != 0)
            temp.put("dur", item.getDurability());
        enchantmentList.add(temp);


        if (itemMeta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemMeta;
            if (esm.hasStoredEnchants()) {
                for (Map.Entry<Enchantment, Integer> enchantment : esm.getStoredEnchants().entrySet()) {
                    Map<String, Object> enchantmentData = new HashMap<>();
                    enchantmentData.put("enc", enchantment.getKey().getKey().getKey());
                    enchantmentData.put("lev", enchantment.getValue());
                    enchantmentList.add(enchantmentData);
                }
            }
        } else {
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            if (!enchantments.isEmpty()) {
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                    Map<String, Object> enchantmentData = new HashMap<>();
                    enchantmentData.put("enc", enchantment.getKey().getKey().getKey());
                    enchantmentData.put("lev", enchantment.getValue());
                    enchantmentList.add(enchantmentData);
                }
            }
        }

        // Check if the item is a goat horn
        if (item.getType() == Material.GOAT_HORN) {
            ItemMeta meta = item.getItemMeta();

            int startIndex = meta.getAsString().indexOf("minecraft:") + "minecraft:".length();
            String instrumentName = meta.getAsString().substring(startIndex, meta.getAsString().length() - 2);
            Map<String, Object> enchantmentData = new HashMap<>();
            enchantmentData.put("song", instrumentName);
            enchantmentList.add(enchantmentData);
        }

        // Check if the item is a suspicious stew
        if (item.getType() == Material.SUSPICIOUS_STEW) {
            SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) item.getItemMeta();

            if (stewMeta.hasCustomEffects()) {
                for (PotionEffect effect : stewMeta.getCustomEffects()) {
                    Map<String, Object> effectData = new HashMap<>();
                    effectData.put("stew_eff", effect.getType().getName());
                    effectData.put("stew_dur", effect.getDuration());
                    //effectData.put("stew_amp", effect.getAmplifier());
                    enchantmentList.add(effectData);
                }
            }
        }


        // Check if the item is a painting
        if (item.getType() == Material.PAINTING) {
            ItemMeta meta = item.getItemMeta();
            int startIndex = meta.getAsString().indexOf("minecraft:") + "minecraft:".length();

            if (meta.getAsString().length() > startIndex + 3) { // Ensure the string is long enough
                String entityName = meta.getAsString().substring(startIndex, meta.getAsString().length() - 3);
                Map<String, Object> enchantmentData = new HashMap<>();
                enchantmentData.put("paint", entityName);
                enchantmentList.add(enchantmentData);
            } else {

            }
        }


        // List all properties of the item, in case it's a firework
        if (itemMeta instanceof FireworkMeta) {
            Map<String, Object> enchantmentData = new HashMap<>();

            FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;

            // Firework duration
            int duration = fireworkMeta.getPower();
            enchantmentData.put("f_dur", duration);

            // Firework effects
            if (fireworkMeta.hasEffects()) {
                for (FireworkEffect effect : fireworkMeta.getEffects()) {
                    enchantmentData.put("f_eff", effect.getType().name());
                    enchantmentData.put("f_fli", effect.hasFlicker());
                    enchantmentData.put("f_trl", effect.hasTrail());

                    // Display colors of the effect
                    for (Color color : effect.getColors()) {
                        enchantmentData.put("f_col", color.asRGB());
                    }

                    // Display fade colors of the effect
                    for (Color fadeColor : effect.getFadeColors()) {
                        enchantmentData.put("f_fco", fadeColor.asRGB());
                    }
                }
            }
            enchantmentList.add(enchantmentData);
        }

        // List all effects of the item, in case it's a potion
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            Map<String, Object> enchantmentData = new HashMap<>();

            // Base potion effect
            PotionData baseData = potionMeta.getBasePotionData();
            enchantmentData.put("bp_name", baseData.getType().name());
            enchantmentData.put("bp_ext", baseData.isExtended());
            enchantmentData.put("bp_upg", baseData.isUpgraded());

            // Custom potion effects
            if (potionMeta.hasCustomEffects()) {
                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                    enchantmentData.put("cp_eff", effect.getType().getName());
                }
            } else {
                //p.sendMessage("No additional potion effects.");
            }
            enchantmentList.add(enchantmentData);
        }

        // output
        if (!enchantmentList.isEmpty()) {
            Gson gson = new Gson();
            String json = gson.toJson(enchantmentList);
            //System.out.println("# DEBUG: (json) " + json);
            return json;
        }
        return "";
    } // end check_item





    public static ItemStack constructItem(String item_json, int amount) {
        Gson gson = new Gson();
        JsonArray array = gson.fromJson(item_json, JsonArray.class);
        ItemStack item = null;
        ItemMeta meta = null;

        String itemid = "";
        String bp_name = "";
        int stew_dur = 0;
        String stew_eff = "";
        String song = "";
        String paint = "";
        int f_dur = 0;

        boolean bp_ext = false;
        boolean bp_upg = false;

        // Map to store enchantments
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();

            // Access each key-value pair in the object
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (key.equals("itemid")) {
                    itemid = value.getAsString();
                    Material material = Material.getMaterial(itemid);
                    if (material == null) {
                        //System.out.println("Invalid Material: " + itemid);  // Log the invalid material
                        return new ItemStack(Material.AIR);
                    }
                    item = new ItemStack(material, amount);
                    meta = item.getItemMeta();
                }


                // Handle Potion properties
                else if(key.equals("bp_name") || key.equals("bp_ext") || key.equals("bp_upg")) {
                    if(key.equals("bp_name"))
                        bp_name = value.getAsString();
                    else if(key.equals("bp_ext"))
                        bp_ext = value.getAsBoolean();
                    else if(key.equals("bp_upg"))
                        bp_upg = value.getAsBoolean();
                }
                // Handle Enchantment for enchanted book
                else if (key.equals("enc")) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value.getAsString()));
                    if (enchantment != null) {
                        enchantments.put(enchantment, 1); // default level is 1
                    }
                } else if (key.equals("lev")) {
                    Enchantment lastEnchantment = enchantments.keySet().stream().reduce((first, second) -> second).orElse(null);
                    if (lastEnchantment != null) {
                        enchantments.put(lastEnchantment, value.getAsInt());
                    }
                }
                // ROCKET
                else if(key.equals("f_dur"))
                    f_dur = value.getAsInt();
                // goat_horn song
                else if(key.equals("song"))
                    song = value.getAsString();
                // SUSPICIOUS_STEW
                else if(key.equals("stew_eff"))
                    stew_eff = value.getAsString();
                else if(key.equals("stew_dur"))
                    stew_dur = value.getAsInt();
                // PAINTING
                else if(key.equals("paint"))
                    paint = value.getAsString();

            } // end elements loop
        }

        if (item != null && meta != null) {


            // POTION
            if (meta instanceof PotionMeta) {
                // This is a Potion, Splash Potion, Lingering Potion, or Tipped Arrow
                PotionMeta potionMeta = (PotionMeta) meta;
                // Determine the potion type
                PotionType potionType = PotionType.valueOf(bp_name);
                if (potionType != null) {
                    // Apply the potion type and modifiers
                    PotionData potionData = new PotionData(potionType, bp_ext, bp_upg);
                    potionMeta.setBasePotionData(potionData);
                }

                // Check the type of potion and modify the item
                if (itemid.equals("SPLASH_POTION")) {
                    item = new ItemStack(Material.SPLASH_POTION);
                } else if (itemid.equals("LINGERING_POTION")) {
                    item = new ItemStack(Material.LINGERING_POTION);
                } else if (itemid.equals("TIPPED_ARROW")) {
                    item = new ItemStack(Material.TIPPED_ARROW);
                } else {
                    item = new ItemStack(Material.POTION);
                }

                item.setItemMeta(potionMeta);
            }

            // FIREWORK_ROCKET
            else if (itemid.equals("FIREWORK_ROCKET")) {
                FireworkMeta fireworkMeta = (FireworkMeta) meta;
                fireworkMeta.setPower(f_dur);
                item.setItemMeta(fireworkMeta);
            }

            // GOAT HORN
            else if (itemid.equals("GOAT_HORN")) {
                // not implemented
                //System.out.println("Song: " + song);
                //System.out.println("GOAT_HORN NOT IMPLEMENTED!");
                return new ItemStack(Material.AIR);
            }

            // SUSPICIOUS_STEW
            else if (itemid.equals("SUSPICIOUS_STEW")) {
                SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) meta;
                PotionEffectType potionEffectType = PotionEffectType.getByName(stew_eff);
                if (potionEffectType != null) {
                    PotionEffect potionEffect = new PotionEffect(potionEffectType, stew_dur * 20, 0); // Duration is in ticks (20 ticks = 1 second)
                    stewMeta.addCustomEffect(potionEffect, true);
                    item.setItemMeta(stewMeta);
                }
            }

            // PAINTING
            else if (itemid.equals("PAINTING")) {
                // not implemented
                //System.out.println("paint: " + paint);
                //System.out.println("PAINTING NOT IMPLEMENTED!");
                return new ItemStack(Material.AIR);
            }

            // ENCHANTED BOOK
            else if (meta instanceof EnchantmentStorageMeta && !enchantments.isEmpty()) {
                // This is an Enchanted Book
                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    enchantmentStorageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
                item.setItemMeta(enchantmentStorageMeta);
            }

            else {
                item.setItemMeta(meta);
            }
        }

        return item;
    }





    public static String getFreeInventory(Player p, String item_json){
        //System.out.println("# DEBUG: at getFreeInventory: itemid: " + item_json);
        int empty_slots = 0;
        int this_item_count = 0;

        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
            if (item == null) { // if stack is empty
                empty_slots++;
            } else if (item != null && identify_item(item).equals(item_json)) {
                this_item_count = this_item_count + item.getAmount();
            }
        }
        empty_slots = empty_slots - 5;      // subtract amour and left hand slots
        // check how many stacks the given item has (eg. diamond = 64; egg = 16)
        int max_stack = Material.getMaterial(get_itemid(item_json)).getMaxStackSize();
        int max_items = empty_slots * max_stack;

        return empty_slots + ":" + this_item_count + ":" + max_items;
    }






    private String print_help(boolean player_or_console) {
        String gold, white, green, dark_gray, dark_purple, red, blue, dark_aqua, light_purple, yellow, underline;

        if(player_or_console) {
            gold = ChatColor.GOLD.toString();
            white = ChatColor.RESET.toString();
            green = ChatColor.GREEN.toString();
            red = ChatColor.RED.toString();
            dark_gray = ChatColor.DARK_GRAY.toString();
            dark_purple = ChatColor.DARK_PURPLE.toString();
            blue = ChatColor.BLUE.toString();
            dark_aqua = ChatColor.DARK_AQUA.toString();
            light_purple = ChatColor.LIGHT_PURPLE.toString();
            yellow = ChatColor.YELLOW.toString();
            underline = ChatColor.UNDERLINE.toString();
        }
        else {
            gold = "";
            white = "";
            green = "";
            red = "";
            dark_gray = "";
            dark_purple = "";
            blue = "";
            dark_aqua = "";
            light_purple = "";
            yellow = "";
            underline = "";
        }

        String reply_command = "\n";
        reply_command = reply_command + gold + "------------------------------------\nix = ITEMEX = Item Exchange v" + Itemex.version  + "\n------------------------------------\n.\n" + white;
        reply_command = reply_command + underline + "USAGE: " + "https://www.youtube.com/watch?v=053V13OrZWI\n." +
                "\n" + white + "/ix " + yellow + "help " + dark_gray + "| " + Itemex.language.getString("help_help") + "\n" +
                "\n" + white + "/ix " + gold + "gui " + dark_gray + "| " + Itemex.language.getString("help_gui") +

                "\n" + white + "/ix " + green + "buy " + dark_gray + "| " + Itemex.language.getString("help_ix_buy") +
                "\n" + white + "/ix " + red + "sell " + dark_gray + "| " + Itemex.language.getString("help_ix_sell") +
                "\n" + white + "/ix " + green + "buy " + white + "<itemname> <amount> <limit | market> <price> " + dark_gray + "| " + Itemex.language.getString("help_cbo") +
                "\n" + white + "/ix " + red + "sell " + white + "<itemname> <amount> <limit | market> <price> " + dark_gray + "| " + Itemex.language.getString("help_cso") + "\n." +

                "\n" + white + "/ix " + blue + "price " + dark_gray + "| " + Itemex.language.getString("help_price") +
                "\n" + white + "/ix " + blue + "price " + white + "<itemid> " + dark_gray + "| " + Itemex.language.getString("help_price_id") + "\n." +

                "\n" + white + "/ix " + light_purple + "order list " + white + "<buyordery | sellorders> *<item id>" + dark_gray + "| " + Itemex.language.getString("help_order_list") +
                "\n" + white + "/ix " + light_purple + "order close " + white + "<buyordery | sellorders> <order id> " + dark_gray + "| " + Itemex.language.getString("help_order_close") +

                "\n" + white + "/ix " + dark_aqua + "whatIsInMyRightHand" + dark_gray+ "| " + Itemex.language.getString("help_wiimrh") +

                "\n" + white + "/ix " + gold + "withdraw list " + dark_gray+ "| " + Itemex.language.getString("help_with_list") +
                "\n" + white + "/ix " + gold + "withdraw" + white + " <itemname> <amount> " + dark_gray + "| " + Itemex.language.getString("help_withdraw") + dark_purple +
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