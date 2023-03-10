package sh.ome.itemex.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.Itemex;

import java.security.spec.RSAOtherPrimeInfo;
import java.util.ArrayList;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class ItemexCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if( command.getName().equalsIgnoreCase("ix")) {
            String reply_command = "";

            if(strings.length == 0) {
                reply_command = ChatColor.BOLD + "ix = ITEMEX = Item Exchange" + ChatColor.WHITE + "\n.\nusage: /ix <" + ChatColor.BLUE + "price" +  ChatColor.WHITE + "/" + ChatColor.GREEN + "buy" + ChatColor.WHITE + "/" + ChatColor.RED + "sell" + ChatColor.WHITE + "> *<itemname> *<amount> *<limit> *<price> ; see more information at " + ChatColor.BOLD + "/ix help" + ChatColor.WHITE;
            }
            else {
                Player p = null;
                if(sender instanceof Player) {
                    p = (Player) sender;
                    //System.out.println("USERID: " +p.getUniqueId());
                }
                else if( sender instanceof ConsoleCommandSender) {
                    //System.out.println("SERVER TERMINAL 1 ###############");
                        //System.out.println(strings[3]);
                        //p = Bukkit.getPlayer( UUID.fromString( strings[3] ) ); //UUID
                        p = Bukkit.getPlayerExact( strings[3] ); // NAME
                        //System.out.println("Name: " + p.getName());
                }
                else if( sender instanceof BlockCommandSender) {
                    if(strings.length == 4) {
                        p = Bukkit.getPlayer(strings[4]);
                    }
                    else if(strings.length == 6) {
                        p = Bukkit.getPlayer(strings[6]);
                    }
                }


                if(strings[0].equals("help")) {
                    reply_command = "\n";
                    //p.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command!");
                    reply_command = reply_command + ChatColor.GOLD + "ix = ITEMEX = Item Exchange v0.13" + ChatColor.WHITE + "\n.\n";
                    reply_command = reply_command + "USAGE: \n" + ChatColor.GREEN + "/ix buy " + ChatColor.DARK_GRAY + "| buy what is in right hand on market price " +
                            "\n" + ChatColor.GREEN + "/ix sell " + ChatColor.DARK_GRAY + "| sell what is in right hand on market price" +
                            "\n" + ChatColor.GREEN + "/ix price " + ChatColor.DARK_GRAY + "| prints the current buy and sell orders" +
                            "\n" + ChatColor.GREEN + "/ix price <itemid> " + ChatColor.DARK_GRAY + "| prints the current buy and sell orders\n#" +
                            //"\n - /ix buy <itemname> <amount>" +
                            //"\n - /ix sell <itemname> <amount>\n#" +
                            "\n" + ChatColor.GREEN + "/ix buy <itemname> <amount> limit <price> " + ChatColor.DARK_GRAY + "| create buy order" +
                            "\n" + ChatColor.GREEN + "/ix sell <itemname> <amount> limit <price> " + ChatColor.DARK_GRAY + "| create sell order\n#" +

                            "\n" + ChatColor.GREEN + "/ix withdraw list " + ChatColor.DARK_GRAY + "| list all your available payouts" +
                            "\n" + ChatColor.GREEN + "/ix withdraw <itemname> <amount> " + ChatColor.DARK_GRAY + "| withdraw " + ChatColor.DARK_PURPLE +
                            "\n.\nThis version is in alpha, if you have any problems or suggestions please write me to xcatpc@proton.me" + ChatColor.WHITE;
                    reply_command = reply_command + "\n";
                }

                else if(strings[0].equals("buy") ) {
                    //Order buyorder = new Order();
                    int item_counter=0;

                    if(strings.length == 1 || strings.length == 2) { // /ix buy
                        reply_command = "\n\n\n";
                        // check if something is in right hand
                        sqliteDb.OrderBuffer[] orders;
                        String itemid;

                        if(strings.length == 1)
                           itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                        else
                            itemid = strings[1].toUpperCase();

                        orders = sqliteDb.selectItem( itemid );

                        // check if there is a sell order with enough amount (1)
                        int last_sell_order=-1;
                        for(int x=0; x<=7; x++) {
                            if(orders[0] == null)
                                reply_command = reply_command + "There are no buy or sell orders.\nYou can create one with: /ix buy <itemname> <amount> limit <price>";
                            if(orders[x] == null) {
                                break;
                            }

                            else if(orders[x].ordertype.equals("sell")) {
                                last_sell_order = x;
                                //reply_command = reply_command + ChatColor.RED + orders[x].ordertype + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                            }
                            //else
                                //reply_command = reply_command + ChatColor.GREEN + orders[x].ordertype + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                        }
                        if(last_sell_order == -1) { // no (last) sell orders
                            reply_command = reply_command + "\nThere are no sell orders to buy. You can create a buy order with: /ix buy <itemname> <amount> limit <price>";
                        }
                        else {
                            //create buy order
                            reply_command = reply_command + create_buy_order(p, itemid, orders[last_sell_order].price, 1);
                        }

                    } // end ix buy


                    else if(strings.length == 5 || strings.length == 6) { // /ix buy limit <price> <itemname> <amount>
                        // amount
                        int i_amount = 0;
                        if(strings[2].equals("max")) {
                            i_amount = item_counter;
                        }
                        else {
                            i_amount = parseInt(strings[2]);
                        }
                        reply_command = reply_command + create_buy_order(p, strings[1], parseFloat(strings[4]), i_amount);
                    }
                    else {
                        reply_command = "argc count is: " + strings.length +"\n";
                        reply_command = reply_command + "Wrong command: use: /ix buy *<itemname> *<limit> *<price> *<amount> | * == optional";
                    }
                } // end buy
                else if(strings[0].equals("sell") ) {
                    Order sellorder = new Order();

                    if(strings.length == 1 || strings.length == 2) { // /ix sell
                        reply_command = "\n\n\n";
                        String itemid;
                        if(strings.length == 1) {
                            itemid = p.getInventory().getItemInHand().getType().toString().toUpperCase();
                        }

                        else {
                            //System.out.println("- ITEM in command: " + strings[1]);
                            boolean item_found = false;
                            //reply_command = "/ix sell <itemname> <amount> limit <price> " + p.getName();
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
                        if(!itemid.equals("AIR")) {
                            sqliteDb.OrderBuffer[] orders;
                            orders = sqliteDb.selectItem( itemid );
                            // check if there is a buy order with enough amount (1)
                            int first_buy_order=-1;
                            for(int x=0; x<=7; x++) {
                                if(orders[0] == null)
                                    reply_command = reply_command + "There are no buy or sell orders.\nYou can create one with: /ix sell <itemname> <amount> limit <price>";
                                if(orders[x] == null) {
                                    break;
                                }

                                if(orders[x].ordertype.equals("buy")) {
                                    if( first_buy_order == -1)
                                        first_buy_order = x;
                                    reply_command = reply_command + ChatColor.GREEN + orders[x].ordertype + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                                }
                                else {
                                    reply_command = reply_command + ChatColor.RED + orders[x].ordertype + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                                }

                            }
                            if(first_buy_order == -1) { // no (first) sell orders
                                reply_command = reply_command + "\nThere are no buy orders to sell. You can create a sell order with: /ix sell <itemname> <amount> limit <price>";
                            }
                            else {
                               create_sell_order(p, itemid, 1, orders[first_buy_order].price);
                            }
                        }
                        else {
                            if(strings.length == 1)
                                reply_command = "You have nothing in your right hand!";
                            else
                                reply_command = "You don't have this item in your inventory!";
                        }
                    }

                    else if(strings.length == 5 || strings.length == 6) { // /ix sell limit
                        boolean item_found = false;
                        //reply_command = "/ix sell <itemname> <amount> limit <price> " + p.getName();
                        // check if player have the amount of items provided at the parameter
                        int item_counter=0;
                        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                            if(item != null && strings[1].equalsIgnoreCase(item.getType().toString())) { //searching only for items with the given ID from command
                                reply_command = reply_command + " " + item.getAmount() + "x" + item.getType() + "\n";
                                item_counter = item_counter + item.getAmount();
                                item_found = true;
                            }
                        }
                        // amount
                        if(strings[2].equals("max")) { 
                            sellorder.amount = item_counter;
                        }
                        else {
                            sellorder.amount = parseInt(strings[2]);
                        }

                        if(item_found) {
                            /*
                            p.getInventory().removeItem(new ItemStack(Material.getMaterial(strings[1].toUpperCase()), sellorder.amount));

                            // create sell order (RAM)
                            sellorder.uuid = p.getUniqueId().toString();
                            sellorder.itemid = strings[1];
                            sellorder.ordertype = "sell"; // sell

                            sellorder.price = parseFloat(strings[4]);

                            // if no or rest => create sell order and store in db
                            sqliteDb db_sellorder = new sqliteDb(sellorder);

                             */
                            create_sell_order( p, strings[1], sellorder.amount, parseFloat(strings[4])) ;


                            /*
                            if( db_sellorder.createSellOrder() ) {
                                reply_command = "Sellorder created!";
                            }
                            else {
                                reply_command = "ERROR! Sellorder NOT created!";
                            }
                             */
                        }
                        else {
                            reply_command = "No given items found in your inventory. Please check the correct name with: /ix whatIsInMyRightHand";
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
                        orders = sqliteDb.selectItem( p.getInventory().getItemInHand().getType().toString().toUpperCase() );
                        reply_command = reply_command + "Prices of the ITEM: " + p.getInventory().getItemInHand().getType().toString() + "\n";
                    }
                    else  if(strings.length == 2) { // /ix price <item id>
                        orders = sqliteDb.selectItem( strings[1].toUpperCase() );
                        reply_command = reply_command + "Prices of the ITEM: " + strings[1].toUpperCase() + "\n";
                    }

                    reply_command = reply_command + "------------------------\n";
                    reply_command = reply_command + "ORDERTYPE - ITEMID - AMOUNT - PRICE\n";


                    for(int x=0; x<=7; x++) {
                        if(orders[x] == null)
                            break;
                        if(orders[x].ordertype.equals("sell"))
                            reply_command = reply_command + ChatColor.RED + orders[x].ordertype + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                        else
                            reply_command = reply_command + ChatColor.GREEN + orders[x].ordertype + "order  " + ChatColor.WHITE + orders[x].itemid + "  " + orders[x].amount + "  $ " + orders[x].price + "\n";
                    }
                    reply_command = reply_command + "-----------\n";

                } // end price
                else if(strings[0].equals("whatIsInMyRightHand") ) {
                    reply_command = p.getInventory().getItemInHand().getType().toString();
                }
                else if(strings[0].equals("withdraw") ) {
                    if(strings.length == 1) { // /ix withdraw list
                        reply_command = reply_command + " /ix withdraw list \n";
                        sqliteDb.Payout[] payouts = sqliteDb.getPayout(p.getUniqueId().toString());
                        reply_command = reply_command + "\n\nYou can withdraw following items: \n[Amount] ItemID";
                        for (int i = 0; i < payouts.length; i++) {
                            if(payouts[i] == null) { //skip empty entries
                                break;
                            }
                            reply_command = reply_command + "\n [" + payouts[i].amount + "] " + ChatColor.GREEN + payouts[i].itemid + ChatColor.WHITE +  " \n";
                        }
                        reply_command = reply_command + "You can withdraw with /ix withdraw <itemid> <amount>";
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

                            /*
                            reply_command = reply_command + "max_stack <itemid>: " + max_stack + "\n";
                            reply_command = reply_command + "max_items (emtpy stacks) : " + max_items + "\n";
                            reply_command = reply_command + "this_item_count: ("+ payouts[i].itemid +"): " + this_item_count + "\n";
                            reply_command = reply_command + "this_item_stacks: ("+ payouts[i].itemid +"): " + this_item_stacks + "\n";
                            reply_command = reply_command + "Empty space: " + free_space + "\n";
                            */

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


                else {
                    reply_command = "Option not found!: " + strings[0] + "\n use /ix help";
                }
            } // end else more than 1 args

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
                //System.out.println(reply_command);
            }
            else if( sender instanceof BlockCommandSender) {
                //System.out.println("This is from the command block");
                //System.out.println(reply_command);
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


    public static String create_buy_order(Player p, String itemid, float last_price, int amount) {
        Order buyorder = new Order();
        String reply_command;
        // create sell order (RAM)
        buyorder.amount = amount;  // always 1 with this command
        buyorder.uuid = p.getUniqueId().toString();
        buyorder.itemid = itemid;
        buyorder.ordertype = "buy"; // sell
        buyorder.price = last_price;

        // if no or rest => create buy order and store in db
        sqliteDb db_buyorder = new sqliteDb(buyorder);

        if( db_buyorder.createBuyOrder() ) {
            reply_command =  ChatColor.GREEN + "BUYORDER " + ChatColor.WHITE + "created! " + ChatColor.BOLD + "[1] " + itemid + ChatColor.WHITE ;
        }
        else {
            reply_command =  "ERROR! Buyorder NOT created!";
        }
        return reply_command;
    }


    public static String create_sell_order(Player p, String itemid, int amount, float price) {
        Order sellorder = new Order();
        String reply_command = "";
        reply_command = reply_command + "\nFIRST BUY ORDER: " + price;
        // create sell order (RAM)
        sellorder.amount = amount;
        sellorder.uuid = p.getUniqueId().toString();
        sellorder.itemid = itemid;
        sellorder.ordertype = "sell"; // sell
        sellorder.price = price;

        // if no or rest => create sell order and store in db
        sqliteDb db_sellorder = new sqliteDb(sellorder);

        if( db_sellorder.createSellOrder() ) {
            reply_command = ChatColor.RED + "SELLORDER " + ChatColor.WHITE + ChatColor.BOLD+  "[" + amount + "] " + itemid.toUpperCase() + ChatColor.WHITE + " created!";
        }
        else {
            reply_command = "ERROR! Sellorder NOT created!";
        }
        p.getInventory().removeItem(new ItemStack(Material.getMaterial(itemid.toUpperCase()), amount));

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
        //back_lore.add(ChatColor.BLUE + "uups");
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

        /*
        //only for test how many items are available
        int item_counter=0;
        for (Material material : Material.values()) {
            item_counter++;
        }
        float fields = 5*9-2;
        System.out.println("ITEMCOUNTER: " + item_counter + " GUI with 6*9-2 buttons= " + (float)item_counter / fields + " sites");

         */

        int x = 8;
        for (Material material : Material.values()) {
            //System.out.println(x + " " + material);

            inv.setItem(x, new ItemStack(material));

            x++;
            if(x >= 54) { break; }
        }

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
}