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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.functions.sqliteDb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

                    if(strings.length == 1 || strings.length == 2) { // /ix buy given itemID or whatisinmyrighthand
                        reply_command = "\n\n\n";
                        // check if something is in right hand


                        if(strings.length == 1)
                            itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                        else
                            itemid = strings[1].toUpperCase();

                        if(itemid == "AIR") {
                            p.sendMessage("You can't buy nothing (AIR)");
                            return false;
                        }

                        // check if there is a sell order with enough amount (1)
                        if (Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0] == 0) {
                            TextComponent message = new TextComponent(ChatColor.RED + "There are no sell orders to buy! \n" + ChatColor.BLUE + " -> (CLICK HERE) You can create a buy order with: /ix buy " + itemid + " 1 limit");
                            message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" 1 limit "));
                            p.spigot().sendMessage(message);
                        }
                        else {
                            //create buy order
                            p.sendMessage("Best sellorderprice: " + Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0]);
                            reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0], 1, "buy", "market");
                        }

                    } // end ix buy

                    else if(strings.length >= 4 && strings.length <= 6) { // /ix buy <itemid> <amount> limit <price>
                        float price;
                        int amount = 0;
                        boolean buy_order_ok = true;
                        itemid = strings[1].toUpperCase();

                        if(strings.length == 4) {
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
                            reply_command = reply_command + "Price can't be 0";
                        }


                        //proof market or limit
                        if(!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + "Wrong market option: (" + strings[3] + ") only limit and market accepted!";
                            buy_order_ok = false;
                        }
                        if(strings[3].equals("limit")) {
                            if(price <= 0) {
                                reply_command = reply_command + "Price is not allowed lower than 0 at limit! Price:" + price;
                                buy_order_ok = false;
                            }
                        }

                        if(buy_order_ok && price >= 0 && strings[3].equals("limit")) {
                            reply_command = reply_command + create_order(p, strings[1], price, amount, "buy", strings[3]);
                        }

                        else if(buy_order_ok && strings[3].equals("market")) {
                            if (Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0] == 0) {
                                TextComponent message = new TextComponent(ChatColor.RED + "There are no sell orders to buy! \n" + ChatColor.BLUE + " -> (CLICK HERE) You can create a buy order with: /ix buy " + itemid + " " + amount + " limit");
                                message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" " + amount + " limit "));
                                p.spigot().sendMessage(message);
                            }
                            else {
                                //create buy order
                                p.sendMessage("Best sellorderprice: " + Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0]);
                                reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0], amount, "buy", "market");
                            }
                        }

                    }
                    else {
                        reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + "Wrong command: use: /ix buy *<itemname> *<limit> *<price> *<amount> | * == optional";
                    }
                } // end buy




                else if(strings[0].equals("sell") ) {
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
                            if (Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[0] == 0) {
                                TextComponent message = new TextComponent(ChatColor.RED + "There are no buy orders to sell! \n" + ChatColor.BLUE + " -> (CLICK HERE) You can create a sell order with: /ix sell " + itemid + " 1 limit ");
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
                                reply_command = "You can't sell used items or with enchantment! (at the moment)";
                            else if(strings.length == 1)
                                reply_command = "You have nothing in your right hand!";
                            else
                                reply_command = "You don't have this item in your inventory!";
                        }
                    }

                    else if(strings.length >= 4 && strings.length <= 6) { // /ix sell <itemid> <amount> limit <price>
                        float price;
                        boolean sell_order_ok = true;
                        itemid = strings[1].toUpperCase();

                        if(strings.length == 4) {
                            price = Itemex.getPlugin().mtop.get( itemid ).get_top_sellorder_prices()[0];
                            if(price <= 0)
                                sell_order_ok = false;
                        }
                        else {
                            price = parseFloat(strings[4]);
                        }


                        // proof price
                        if(!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + "Wrong market option: " + strings[3] + " only limit and market accepted!";
                            sell_order_ok = false;
                        }
                        if(strings[3].equals("limit")) {
                            if(price <= 0) {
                                reply_command = reply_command + "Price is not allowed lower than 0 at limit! Price:" + price;
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
                            reply_command = reply_command + "Price is not allowed lower than 0 at limit! Price:" + price;
                            sell_order_ok = false;
                        }
                        else {
                            if(!is_damaged_or_enchantment) {
                                System.out.println("# DEBUG - Damaged or enchantment");
                            }
                            else if(item_found) {
                                if(sell_order_ok && price >= 0 && strings[3].equals("limit"))
                                    reply_command = reply_command + create_order( p, itemid, price, sellorder.amount, "sell", strings[3] );
                                else {

                                    if (Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[0] == 0) {
                                        TextComponent message = new TextComponent(ChatColor.RED + "There are no buy orders to sell! \n" + ChatColor.BLUE + " -> (CLICK HERE) You can create a sell order with: /ix sell " + itemid + " 1 limit ");
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" 1 limit "));
                                        p.spigot().sendMessage(message);
                                    }
                                    else {
                                        // create_sell_order(p, itemid, 1, orders[first_buy_order].price); //replaced with create order
                                        reply_command = reply_command + create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[3],1, "sell", "market");
                                    }
                                }
                            }
                            else
                                reply_command = reply_command + ChatColor.RED + "No given items: " + ChatColor.GOLD + strings[1] + ChatColor.RED + " found in your inventory!\n" + ChatColor.WHITE +"Please check the correct name with: /ix whatIsInMyRightHand";
                        }
                    } // end ix sell limit

                    else {
                        reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + "Wrong command: use: /ix sell <itemname> <amount> limit <price>";
                    }
                } // end sell





                else if(strings[0].equals("price") ) {
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
                        reply_command = "You only have AIR in your hand!";
                    }
                    else {
                        topo = Itemex.getPlugin().mtop.get( itemid );
                        reply_command = reply_command + "Prices of the ITEM: " + ChatColor.GOLD +  itemid + ChatColor.WHITE + "\n";
                        reply_command = reply_command + "-----------------------------\n";
                        reply_command = reply_command + "ORDERTYPE - ITEMID - AMOUNT - PRICE\n";


                        for(int x=3; x>=0; x--){
                            if(topo.get_sellorder_amount()[x] == 0)
                                reply_command = reply_command + ChatColor.DARK_RED + "sellorder  " + ChatColor.DARK_GRAY + itemid + "  " + topo.get_sellorder_amount()[x] +  "  $ " + topo.get_top_sellorder_prices()[x] + "\n";
                            else
                                reply_command = reply_command + ChatColor.RED + "sellorder  " + ChatColor.WHITE + itemid + "  " + topo.get_sellorder_amount()[x] +  "  $ " + topo.get_top_sellorder_prices()[x] + "\n";
                        }
                        for(int x=0; x<=3; x++){
                            if(topo.get_buyorder_amount()[x] == 0)
                                reply_command = reply_command + ChatColor.DARK_GREEN + "buyorder  " + ChatColor.DARK_GRAY + itemid + "  " + topo.get_buyorder_amount()[x] +  "  $ " + topo.get_top_buyorder_prices()[x] + "\n";
                            else
                                reply_command = reply_command + ChatColor.GREEN + "buyorder  " + ChatColor.WHITE + itemid + "  " + topo.get_buyorder_amount()[x] +  "  $ " + topo.get_top_buyorder_prices()[x] + "\n";
                        }
                        reply_command = reply_command + "-----------------------------\n";
                    }



                } // end price




                else if(strings[0].equals("whatIsInMyRightHand") ) {
                    ItemStack item = p.getInventory().getItemInMainHand();
                    reply_command = "ITEMID: " + item.getType() + "\n";

                    // GET DAMAGE 0 = no damage
                    if(item.getDurability() != 0)
                        reply_command = reply_command + "Durability: " + item.getDurability() + "\n";

                    // CHECK if ITEM HAS ENCHANTMENTS
                    if (item.getItemMeta().hasEnchants()) {
                        reply_command = reply_command + "Number of Enchantments: " + item.getEnchantments().size() + "\n";
                        Map enc = item.getEnchantments();
                        reply_command = reply_command + "Enchantments: " + item.getEnchantments() + " size: " + enc.size();
                    }


                    // CHECK POTION
                    if (item.getType() == Material.POTION) {
                        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                        reply_command = reply_command + "POTION: " + potionMeta.getBasePotionData().getType() + " ";
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
                        if( strings[2].equals("buyorders") || strings[2].equals("sellorders") ) {
                            try {
                                orderid = parseInt(strings[3]);;
                            } catch (NumberFormatException e) {
                                reply_command = "OrderID must be a number!\n";
                            }
                            if(orderid != -1) {
                                if( sqliteDb.PlayercloseOrder(p.getUniqueId().toString(), strings[2].toUpperCase(), orderid) )
                                    reply_command = reply_command + ChatColor.GREEN + "Order deleted sucessfully!" + ChatColor.RESET;
                                else
                                    reply_command = reply_command + ChatColor.RED + "Order NOT deleted sucessfully!" + ChatColor.RESET;
                            }
                        }
                        else
                            reply_command = reply_command + ChatColor.RED + "only buyorder or sellorder are accepted!" + ChatColor.RESET;;
                    }

                    else if( strings[1].equals("edit") ) {

                        reply_command = reply_command + "not implemented. If you need it. Write me to xcatpc@proton.me";
                        //sqliteDb.updateOrder("tabelname", 14, 13, (float)4055.49, "sell:limit");
                    }

                    else if( strings[1].equals("list") ) {
                        if( strings.length <= 2) {
                            reply_command = reply_command + ChatColor.RED + "You can only use buyorders or sellorders!" + ChatColor.RESET;
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
                            reply_command = reply_command + ".\nList of all your ORDERS: \nORDER ID- ITEMID - AMOUNT - PRICE - ORDERTYPE\n";
                            for(int i=0; i<100; i++){ // 100 is max
                                if(list[i] == null) {
                                    break;
                                }
                                else {
                                    reply_command = reply_command +  color + list[i].id + " " +ChatColor.WHITE +list[i].itemid + " " + list[i].amount + " " + color + list[i].price + " " + list[i].ordertype + "\n" + ChatColor.WHITE;
                                }
                            } // end for
                            reply_command = reply_command + "\nand more.. please use /ix order list <sellorders | buyorders> <itemid>";

                        }
                        else
                            reply_command = reply_command + ChatColor.RED + "You can only use buyorders or sellorders!" + ChatColor.RESET;

                    } // end of list

                } // end order




                else if(strings[0].equals("withdraw") ) {
                    if(strings.length == 1 || (strings.length == 2) && ( strings[1].equals("list") || strings[1].equals("_list") ) ){ // /ix withdraw list
                        //reply_command = reply_command + " /ix withdraw list \n";
                        sqliteDb.Payout[] payouts = sqliteDb.getPayout(p.getUniqueId().toString());
                        reply_command = reply_command + "\n\nYou can withdraw following items: [Amount] ItemID\n";

                        for (int i = 0; i < payouts.length; i++) {
                            if(payouts[i] == null) { //skip empty entries
                                if(i == 0)
                                    reply_command = reply_command + "\n\nThere are no items to withdraw!";
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
                                reply_command = reply_command + free_space + "\nPayout [" + x + "/" + payouts[i].amount + "] " + ChatColor.GOLD + payouts[i].itemid + ChatColor.WHITE +  " not enough space! \n.\n";
                            }
                            else {
                                reply_command = reply_command + free_space + "\nPayout [" + x + "/" + payouts[i].amount + "] " + ChatColor.GREEN + payouts[i].itemid + ChatColor.WHITE +  " \n.\n";
                            }

                            //update db (subtract x)
                            if(x != 0) {        // x == the payout item amount
                                sqliteDb.updatePayout(p.getUniqueId().toString(), payouts[i].id, payouts[i].itemid, payouts[i].amount-x ); //update the amount
                            }
                        }

                    }
                    else {
                        reply_command = reply_command + "Wrong syntax. Please look at: /help";
                    }
                }



                else if(strings[0].equals("gui") ) {
                    GUI.generateGUI(p, "ITEMEX - Market Orders", 0, 0);
                }



                else if(strings[0].equals("extractitems") ) {

                    int x=0;
                    for (Material material : Material.values()) {
                        System.out.println(":" + x + ":" +material.toString() + ":" + material.isBlock() + ":" + material.isItem() + ":" + material.isBurnable() + ":" + material.isFuel() + ":" + material.isInteractable() + ":" + material.isSolid() + ":" + material.isFlammable() + ":" + material.isOccluding() + ":" + material.isRecord());
                        x++;
                    }
                    //p.sendMessage("amount + price: " + Itemex.getPlugin().top[0].get_buyorder_amount() + " + " + Itemex.getPlugin().top[0].get_top_buyorder_prices() );
                }

                else if(strings[0].equals("test") ) {
                    System.out.println("ITEMID: " + strings[1].toUpperCase());
                    sqliteDb.loadBestOrdersToRam(strings[1].toUpperCase(), true);
                }

                else if(strings[0].equals("test2")) {
                    for (Map.Entry<String, Integer> entry : Itemex.commandUsageCounts.entrySet()) {
                        String commandName = entry.getKey();
                        int usageCount = entry.getValue();
                        System.out.println(commandName + ": " + usageCount);
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
                    p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command!");
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
        public float price;
    };

    public static String create_order(Player p, String itemid, float price, int amount, String buy_or_sell, String market_option) {
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
            if( db_order.createSellOrder() )
                reply_command = ChatColor.RED + "SELLORDER " + ChatColor.WHITE + ChatColor.BOLD+  "[" + amount + "] " + itemid.toUpperCase() + ChatColor.WHITE + " created!";
            else
                reply_command = "ERROR! Sellorder NOT created!";
            p.getInventory().removeItem(new ItemStack(Material.getMaterial(itemid.toUpperCase()), amount));
        }
        else if(buy_or_sell.equals("buy")) {
            double buyer_balance = econ.getBalance(p);
            if( (amount * price) < buyer_balance ) {
                if( db_order.createBuyOrder() )
                    reply_command =  ChatColor.GREEN + "BUYORDER " + ChatColor.WHITE + "created! " + ChatColor.BOLD + "[" + amount + "] " + itemid + ChatColor.WHITE ;
                else
                    reply_command = "ERROR! Buyorder NOT created!";
            }
            else {  //not enough money
                reply_command = ChatColor.RED+ "NOT ENOUGH MONEY!" + ChatColor.WHITE + " You got need " + ChatColor.GREEN +"$" + (amount * price) + ChatColor.WHITE + " but you only have " + ChatColor.RED + " $"  + buyer_balance;
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
        reply_command = reply_command + "USAGE: \n" + green + "/ix buy " + dark_gray + "| buy what is in right hand on market price " +
                "\n" + green + "/ix gui " + dark_gray + "| Graphical User Interface\n." +

                "\n" + green + "/ix sell " + dark_gray + "| sell what is in right hand on market price" +
                "\n" + green + "/ix price " + dark_gray + "| prints the current buy and sell orders" +
                "\n" + green + "/ix price <itemid> " + dark_gray + "| prints the current buy and sell orders\n." +

                "\n" + green + "/ix buy <itemname> <amount> <limit | market> <price> " + dark_gray + "| create buy order" +
                "\n" + green + "/ix sell <itemname> <amount> <limit | market> <price> " + dark_gray + "| create sell order\n." +

                "\n" + green + "/ix order list <buyordery | sellorders> *<item id>" + dark_gray + "| list all own buy- and sellorders" +
                "\n" + green + "/ix order close <buyordery | sellorders> <order id> " + dark_gray + "| list all your available payouts\n." +

                "\n" + green + "/ix withdraw list " + dark_gray+ "| list all your available payouts" +
                "\n" + green + "/ix withdraw <itemname> <amount> " + dark_gray + "| withdraw " + dark_purple +
                "\n.\nThis version is in beta, if you have any problems or suggestions please write me to" + white +" xcatpc@proton.me " + dark_purple + "or join us on discord: " + white + "https://discord.gg/rKEwQjpmXj" + white;
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




}