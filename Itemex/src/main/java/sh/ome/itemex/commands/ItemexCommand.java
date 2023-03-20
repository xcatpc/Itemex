package sh.ome.itemex.commands;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.files.CategoryFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class ItemexCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {



        if( command.getName().equalsIgnoreCase("ix")) {
            String reply_command = "";

            if(strings.length == 0) {
                Player p = null;
                if(sender instanceof Player) {
                    reply_command = reply_command + print_help(true);
                    p = (Player) sender;
                    generateGUI(p, 1);
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
                    //Order buyorder = new Order();
                    int item_counter=0;

                    if(strings.length == 1 || strings.length == 2) { // /ix buy given itemID or whatisinmyrighthand
                        reply_command = "\n\n\n";
                        // check if something is in right hand
                        sqliteDb.OrderBuffer[] orders;
                        String itemid;

                        if(strings.length == 1)
                           itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                        else
                            itemid = strings[1].toUpperCase();

                        orders = sqliteDb.getBestOrders( itemid );

                        // check if there is a sell order with enough amount (1)
                        int last_sell_order=-1;
                        for(int x=0; x<=7; x++) {
                            String[] ordertype;
                            //if(orders[0] == null)
                                //reply_command = reply_command + "There are no buy or sell orders.\nYou can create one with: /ix buy <itemname> <amount> limit <price>";
                            if(orders[x] == null) {
                                break;
                            }
                            else {
                                ordertype = orders[x].ordertype.split(":", 2);
                            }

                            if(ordertype[0].equals("sell")) {
                                last_sell_order = x;
                            }

                        }
                        if(last_sell_order == -1) { // no (last) sell orders
                            TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) There are no sell orders to buy. \nYou can create a buy order with: /ix buy " + itemid + " 1 limit");
                            message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" 1 limit "));
                            p.spigot().sendMessage(message);
                        }
                        else {
                            //create buy order
                            //reply_command = reply_command + create_buy_order(p, itemid, orders[last_sell_order].price, 1);    // replaced with create order
                            reply_command = reply_command + create_order(p, itemid, 0, 1, "buy", "market");
                        }

                    } // end ix buy

                    else if(strings.length == 5 || strings.length == 6) { // /ix buy <itemid> <amount> limit <price>
                        float price = parseFloat(strings[4]);
                        int amount = 0;
                        boolean buy_order_ok = true;

                        //proof amount
                        if(strings[2].equals("max"))
                            amount = item_counter;
                        else
                            amount = parseInt(strings[2]);
                        if(amount <= 0)
                            buy_order_ok = false;

                        //proof market or limit
                        if(!strings[3].equals("limit") && !strings[3].equals("market")) {
                            reply_command = reply_command + "Wrong market option: " + strings[3] + " only limit and market accepted!";
                            buy_order_ok = false;
                        }
                        if(strings[3].equals("limit")) {
                            if(price <= 0) {
                                reply_command = reply_command + "Price is not allowed lower than 0 at limit! Price:" + price;
                                buy_order_ok = false;
                            }
                        }

                        if(buy_order_ok)
                            reply_command = reply_command + create_order(p, strings[1], parseFloat(strings[4]), amount, "buy", strings[3]);


                    }
                    else {
                        reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + "Wrong command: use: /ix buy *<itemname> *<limit> *<price> *<amount> | * == optional";
                    }
                } // end buy





                else if(strings[0].equals("sell") ) {
                    Order sellorder = new Order();
                    boolean is_damaged_or_enchantment = true;

                    if(strings.length == 1 || strings.length == 2) { // /ix sell
                        reply_command = "\n\n\n";
                        String itemid;
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
                            sqliteDb.OrderBuffer[] orders;
                            orders = sqliteDb.getBestOrders( itemid );
                            // check if there is a buy order with enough amount (1)
                            int first_buy_order=-1;
                            for(int x=0; x<=7; x++) {
                                String[] ordertype;
                                if(orders[0] == null) {
                                    TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) There are no buy orders to sell. \nYou can create a sell order with: /ix sell " + itemid + " 1 limit ");
                                    message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" 1 limit "));
                                    p.spigot().sendMessage(message);
                                }

                                if(orders[x] == null) {
                                    break;
                                }
                                else {
                                    ordertype = orders[x].ordertype.split(":", 2);
                                }

                                if(ordertype[0].equals("buy")) {
                                    if( first_buy_order == -1)
                                        first_buy_order = x;
                                    reply_command = reply_command + ChatColor.GREEN + ordertype[0] + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                                }
                                else {
                                    reply_command = reply_command + ChatColor.RED + ordertype[0] + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                                }

                            }
                            if(first_buy_order == -1) { // no (first) sell orders
                                //reply_command = reply_command + "\nThere are no buy orders to sell. You can create a sell order with: /ix sell <itemname> <amount> limit <price>";
                            }
                            else {
                               // create_sell_order(p, itemid, 1, orders[first_buy_order].price); //replaced with create order
                                reply_command = reply_command + create_order(p, itemid, 0,1, "sell", "market");
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

                    else if(strings.length == 5 || strings.length == 6) { // /ix sell <itemid> <amount> limit <price>
                        float price = parseFloat(strings[4]);
                        int amount = 0;
                        boolean sell_order_ok = true;
                        //System.out.println("ix sell limit: " + price);

                        if( p.getInventory().getItemInMainHand().getDurability() != 0 ) {    // GET DAMAGE 0 = no damage
                            reply_command = reply_command + "You can't sell used items";
                            is_damaged_or_enchantment = false;
                        }

                        else if( p.getInventory().getItemInMainHand().getEnchantments().size() != 0 ) {    // if there is an enchantment on the item
                            is_damaged_or_enchantment = false;
                            reply_command = reply_command + "You can't sell enchantment items (right now)";
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
                                //reply_command = reply_command + " " + item.getAmount() + "x" + item.getType() + "\n";
                                item_counter = item_counter + item.getAmount();
                                item_found = true;
                            }
                        }

                        // proof amount
                        if(strings[2].equals("max"))
                            sellorder.amount = item_counter;
                        else
                            sellorder.amount = parseInt(strings[2]);

                        if( strings.equals("market") )
                            sellorder.amount = 0;
                        else
                            if(sellorder.amount <=0) {
                                reply_command = reply_command + "Price is not allowed lower than 0 at limit! Price:" + price;
                                sell_order_ok = false;
                            }
                            else {
                                if(!is_damaged_or_enchantment) {}
                                else if(item_found) {
                                    if(sell_order_ok)
                                        reply_command = reply_command + create_order( p, strings[1], parseFloat(strings[4]), sellorder.amount, "sell", strings[3] );
                                }
                                else
                                    reply_command = reply_command + "No given items found in your inventory. Please check the correct name with: /ix whatIsInMyRightHand";
                            }
                    } // end ix sell limit

                    else {
                        reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + "Wrong command: use: /ix sell <itemname> <amount> limit <price>";
                    }
                } // end sell




                else if(strings[0].equals("price") ) {
                    reply_command = "\n\n\n------------------------\n";

                    // get entries from db
                    sqliteDb.OrderBuffer[] orders = new sqliteDb.OrderBuffer[0];

                    if(strings.length == 1) { // /ix price
                        orders = sqliteDb.getBestOrders( p.getInventory().getItemInHand().getType().toString().toUpperCase() );
                        reply_command = reply_command + "Prices of the ITEM: " + p.getInventory().getItemInHand().getType().toString() + "\n";
                    }
                    else  if(strings.length == 2) { // /ix price <item id>
                        orders = sqliteDb.getBestOrders( strings[1].toUpperCase() );
                        reply_command = reply_command + "Prices of the ITEM: " + strings[1].toUpperCase() + "\n";
                    }

                    reply_command = reply_command + "------------------------\n";
                    reply_command = reply_command + "ORDERTYPE - ITEMID - AMOUNT - PRICE\n";


                    for(int x=0; x<=7; x++) {
                        String[] ordertype;
                        if(orders[x] == null)
                            break;
                        else
                            ordertype = orders[x].ordertype.split(":", 2);

                        if(ordertype[0].equals("sell"))
                            reply_command = reply_command + ChatColor.RED + ordertype[0] + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                        else
                            reply_command = reply_command + ChatColor.GREEN + ordertype[0] + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                    }
                    reply_command = reply_command + "-----------\n";

                } // end price



                else if(strings[0].equals("whatIsInMyRightHand") ) {
                    reply_command = "ITEMID: " + p.getInventory().getItemInHand().getType().toString();

                    // GET DAMAGE 0 = no damage
                    reply_command = reply_command + " \nDurability: " + p.getInventory().getItemInMainHand().getDurability() + "\n";

                    // CHECK if ITEM HAS ENCHANTMENTS
                    reply_command = reply_command + "Number of Enchantments: " + p.getInventory().getItemInMainHand().getEnchantments().size();


                }



                else if(strings[0].equals("order") ) {
                    int orderid = -1;

                    if(strings.length == 1) {
                        reply_command = "/ix order <close | edit | list>";
                    }
                    else if( strings[1].equals("close") ) {
                        if( strings[2].equals("buyorders") || strings[2].equals("sellorders") ) {
                            try {
                                orderid = Integer.parseInt(strings[3]);;
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
                    generateGUI(p, 1);

                }


                else if(strings[0].equals("gui_new") ) {
                    GUI.generateGUI(p, 0);

                    /*
                    for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                        p.sendMessage(cat);
                    }

                     */

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
        return true;
    } //  end onCommand









    static class Order
    {
        public String uuid;
        public String itemid;
        public String ordertype;
        public int    amount;
        public float price;
    };

    public static String create_order(Player p, String itemid, float price, int amount, String buy_or_sell, String market_option) {
        String reply_command = "";

        Order order = new Order();
        // create sell order (RAM)
        order.amount = amount;
        order.uuid = p.getUniqueId().toString();
        order.itemid = itemid;
        order.ordertype = buy_or_sell + ":" + market_option;
        order.price = price;

        sqliteDb db_order = new sqliteDb(order);

        //System.out.println("AT create_order, buy_or_sell: " + buy_or_sell);

        if(buy_or_sell.equals("sell")) {
            if( db_order.createSellOrder() )
                reply_command = ChatColor.RED + "SELLORDER " + ChatColor.WHITE + ChatColor.BOLD+  "[" + amount + "] " + itemid.toUpperCase() + ChatColor.WHITE + " created!";
            else
                reply_command = "ERROR! Sellorder NOT created!";
            p.getInventory().removeItem(new ItemStack(Material.getMaterial(itemid.toUpperCase()), amount));
        }
        else if(buy_or_sell.equals("buy")) {
            if( db_order.createBuyOrder() )
                reply_command =  ChatColor.GREEN + "BUYORDER " + ChatColor.WHITE + "created! " + ChatColor.BOLD + "[1] " + itemid + ChatColor.WHITE ;
            else
                reply_command = "ERROR! Buyorder NOT created!";
        }
        return reply_command;
    }



    public void generateGUI(Player p, int count) {
        Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.BLACK +  "ITEMEX");
        String item_count = Integer.toString(count);

        // BACK left
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("previous page");
        ArrayList<String> back_lore = new ArrayList<>();
        back_lore.add(ChatColor.GOLD + "1");
        backMeta.setLore(back_lore);
        back.setItemMeta(backMeta);

        // NEXT right
        ItemStack next = new ItemStack(Material.SPRUCE_DOOR);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName("next page");
        ArrayList<String> next_lore = new ArrayList<>();
        next_lore.add(ChatColor.GOLD + "1");
        nextMeta.setLore(next_lore);
        next.setItemMeta(nextMeta);

        // Plus 1 right
        ItemStack plus = new ItemStack(Material.TORCH);
        ItemMeta plusMeta = next.getItemMeta();
        plusMeta.setDisplayName(ChatColor.WHITE + "add 1");
        ArrayList<String> plus_lore = new ArrayList<>();
        plus_lore.add(ChatColor.WHITE + "1");
        plusMeta.setLore(plus_lore);
        plus.setItemMeta(plusMeta);

        // Plus 2 right
        ItemStack plus2 = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta plus2Meta = next.getItemMeta();
        plus2Meta.setDisplayName(ChatColor.WHITE + "add 16");
        ArrayList<String> plus2_lore = new ArrayList<>();
        plus2_lore.add(ChatColor.WHITE + "1");
        plus2Meta.setLore(plus2_lore);
        plus2.setItemMeta(plus2Meta);

        // Plus 3 right
        ItemStack plus3 = new ItemStack(Material.SOUL_TORCH);
        ItemMeta plus3Meta = next.getItemMeta();
        plus3Meta.setDisplayName(ChatColor.WHITE + "add 64");
        ArrayList<String> plus3_lore = new ArrayList<>();
        plus3_lore.add(ChatColor.WHITE + "1");
        plus3Meta.setLore(plus3_lore);
        plus3.setItemMeta(plus3Meta);

        // Minus 1 left
        ItemStack minus = new ItemStack(Material.TORCH);
        ItemMeta minusMeta = next.getItemMeta();
        minusMeta.setDisplayName(ChatColor.WHITE + "sub 1");
        ArrayList<String> minus_lore = new ArrayList<>();
        minus_lore.add(ChatColor.WHITE + "1");
        minusMeta.setLore(minus_lore);
        minus.setItemMeta(minusMeta);

        // Minus 2 left
        ItemStack minus2 = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta minus2Meta = next.getItemMeta();
        minus2Meta.setDisplayName(ChatColor.WHITE + "sub 16");
        ArrayList<String> minus2_lore = new ArrayList<>();
        minus2_lore.add(ChatColor.WHITE + "1");
        minus2Meta.setLore(minus2_lore);
        minus2.setItemMeta(minus2Meta);

        // Minus 3 left
        ItemStack minus3 = new ItemStack(Material.SOUL_TORCH);
        ItemMeta minus3Meta = next.getItemMeta();
        minus3Meta.setDisplayName(ChatColor.WHITE + "sub 64");
        ArrayList<String> minus3_lore = new ArrayList<>();
        minus3_lore.add(ChatColor.WHITE + "1");
        minus3Meta.setLore(minus3_lore);
        minus3.setItemMeta(minus3Meta);

        // Item Information
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "Shows amount:");
        ArrayList<String> item_lore = new ArrayList<>();
        item_lore.add(ChatColor.GREEN + item_count);
        itemMeta.setLore(item_lore);
        item.setItemMeta(itemMeta);

        int x = 8;
        for (Material material : Material.values()) {
            //System.out.println(x + " " + material.isBlock());


            ItemStack temp = new ItemStack(material);

            if(temp.getItemMeta() != null) {
                sqliteDb.OrderBuffer[] toporders = sqliteDb.getBestOrders( material.name() );
                String s_toporder[] = new String[8];
                for(int xx =0; xx<=7; xx++) {
                    if(toporders[xx] != null) {
                        String ordertype[] = toporders[xx].ordertype.split(":", 0);
                        String format_price = String.format("%.02f", toporders[xx].price);
                        if(ordertype[0].equals("sell"))
                            s_toporder[xx] = ChatColor.RED + "[" +  toporders[xx].amount +"] $" + format_price;
                        else
                            s_toporder[xx] = ChatColor.GREEN + "[" +  toporders[xx].amount +"] $" + format_price;
                    }

                    else
                        s_toporder[xx] = ChatColor.DARK_GRAY + "-";
                }

                ItemMeta tempMeta = temp.getItemMeta();
                //tempMeta.setDisplayName(material.name());
                ArrayList<String> temp_lore = new ArrayList<>();
                temp_lore.add(ChatColor.DARK_GRAY + "[amount] <price>");
                temp_lore.add(s_toporder[0]);
                temp_lore.add(s_toporder[1]);
                temp_lore.add(s_toporder[2]);
                temp_lore.add(s_toporder[3]);

                temp_lore.add(s_toporder[4]);
                temp_lore.add(s_toporder[5]);
                temp_lore.add(s_toporder[6]);
                temp_lore.add(s_toporder[7]);

                temp_lore.add(ChatColor.RED + "sellorders" +  ChatColor.WHITE + " | " + ChatColor.GREEN + "buyorders");
                tempMeta.setLore(temp_lore);
                temp.setItemMeta(tempMeta);

                inv.setItem(x, temp);
            }


            x++;
            if(x >= 54) { break; }
        } // material values end

        inv.setItem(0, back);
        inv.setItem(8, next);

        inv.setItem(5, plus);
        inv.setItem(6, plus2);
        inv.setItem(7, plus3);

        inv.setItem(3, minus);
        inv.setItem(2, minus2);
        inv.setItem(1, minus3);

        inv.setItem(4, item);

        p.openInventory(inv);
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
                "\n.\nThis version is in alpha, if you have any problems or suggestions please write me to xcatpc@proton.me" + white;
        reply_command = reply_command + "\n";
        return reply_command;
    } // end print_help
}