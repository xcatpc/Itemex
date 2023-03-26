package sh.ome.itemex.events;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.files.CategoryFile;

import java.util.ArrayList;

import static org.bukkit.Bukkit.getLogger;


public class ClickGUI implements Listener {
    int itemcounter = 0;
    int amount = 1;
    @EventHandler
    public void clickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();


        if(e.getView().getTitle().contains("ITEMEX")) {                          // If player click on Itemex GUI
            String menu_type = e.getView().getTitle().substring(2);
            //System.out.println("# DEBUG: at click EVENT: " + menu_type + " " + e.getSlot());
            e.setCancelled(true);
            if( e.getClick().isLeftClick() || e.getClick().isRightClick() ) {

                // SET THE MENU NAME
                // if click is on menu
                if(e.getSlot() >=0 && e.getSlot() <= 8) {
                    if (e.getSlot() == 0)
                        menu_type = "ITEMEX - Market Orders";
                    else if (e.getSlot() == 1)
                        menu_type = "ITEMEX - Limit Orders";
                    else if (e.getSlot() == 2) {
                        if(menu_type.contains("Orders:")) {
                            amount = amount - 64;
                            if (amount <= 0) {
                                 amount = 1;
                            }
                            p.sendMessage("sub 64: " + amount);
                        }
                        else
                            menu_type = "ITEMEX - Order Book";
                    }

                    else if (e.getSlot() == 3)
                        if(menu_type.contains("Orders:")) {
                            amount = amount - 1;
                            if (amount <= 0) {
                                amount = 1;
                            }
                            p.sendMessage("sub 1: " + amount);
                        }
                        else
                            menu_type = "ITEMEX - Fast Sell";
                    else if (e.getSlot() == 4) { // prev page
                        if(menu_type.contains(":")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[2]);
                            page = page - 1;
                            if(page == -1)
                                page = 0;
                            menu_type = subMenu[0] + ":" + subMenu[1] + ":" + page; // Category 1 page 0
                        }
                    }
                    else if (e.getSlot() == 5) {    // next page
                        if(menu_type.contains("Orders:")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[2]);
                            page = page + 1;
                            menu_type = subMenu[0] + ":" + subMenu[1] + ":" + page; // Category 1 page 0
                        }
                    }
                    else if (e.getSlot() == 6) {
                        if(menu_type.contains("Orders:")) {
                            amount = amount +1;
                            p.sendMessage("add 1: " + amount);
                        }
                        else
                            menu_type = "ITEMEX - Vault";
                    }

                    else if (e.getSlot() == 7) {
                        if(menu_type.contains("Orders:")) {
                            amount = amount + 64;
                            p.sendMessage("add 64: " + amount);
                        }
                        else {
                            menu_type = "ITEMEX - HELP";
                            e.getView().close();
                            p.sendMessage("No help video available. Pls be patient");
                        }

                    }
                    else if (e.getSlot() == 8) {
                        menu_type = "ITEMEX - CLOSE";
                        e.getView().close();
                    }
                    else
                        menu_type = e.getView().getTitle().substring(2);
                }
                else {  // rest of inventory
                    if(menu_type.contains( menu_type )) {
                        if(!menu_type.contains(":")) {
                            String subMenu[] = menu_type.split(":", 3);
                            if(e.getSlot() == 20) {  menu_type = subMenu[0] + ":0:0";  }                       // Category 0 page 0
                            else if(e.getSlot() == 21) {  menu_type = subMenu[0] + ":1:0";  }                  // Category 1 page 0
                            else if(e.getSlot() == 22) {  menu_type = subMenu[0] + ":2:0";  }                  // Category 2 page 0
                            else if(e.getSlot() == 23) {  menu_type = subMenu[0] + ":3:0";  }                  // Category 3 page 0
                            else if(e.getSlot() == 24) {  menu_type = subMenu[0] + ":4:0";  }                  // Category 4 page 0
                            else if(e.getSlot() == 29) {  menu_type = subMenu[0] + ":5:0";  }                  // Category 5 page 0
                            else if(e.getSlot() == 30) {  menu_type = subMenu[0] + ":6:0";  }                  // Category 6 page 0
                            else if(e.getSlot() == 31) {  menu_type = subMenu[0] + ":7:0";  }                  // Category 7 page 0
                            else if(e.getSlot() == 32) {  menu_type = subMenu[0] + ":8:0";  }                  // Category 8 page 0
                            else if(e.getSlot() == 33) {  menu_type = subMenu[0] + ":9:0";  }                  // Category 9 page 0
                        }
                        else { // GET ITEM TO BUY OR SELL
                            //System.out.println("# DEBUG: ITEM: " + e.getCurrentItem().getType() + " Click: " + e.getClick() + " Menutype: " + menu_type);
                            String itemid = e.getCurrentItem().getType().toString();

                            if( e.getClick().isRightClick()) {  // SELL
                                if(menu_type.contains( "Limit" )) {
                                    TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) to execute your SELL order " + itemid + " "+ amount + " limit <set your price>");
                                    message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" "+ amount +" limit "));
                                    p.spigot().sendMessage(message);
                                    e.getView().close();
                                    return;
                                }
                                else if(menu_type.contains( "Market" )) {
                                    if (Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[0] == 0) {
                                        p.sendMessage(ChatColor.RED + "There are no Orders to sell, but you can create one by:");
                                        TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) to execute your SELL order " + itemid + " "+ amount + " limit <set your price>");
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" "+ amount +" limit "));
                                        p.spigot().sendMessage(message);
                                        e.getView().close();
                                        return;
                                    }
                                    else {
                                        TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) to execute your SELL order " + itemid + " "+ amount + " market");
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" "+ amount +" market "));
                                        p.spigot().sendMessage(message);
                                        e.getView().close();
                                        return;
                                        //ItemexCommand.create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[3], amount, "sell", "market");
                                    }
                                }
                            }

                            else if( e.getClick().isLeftClick()) {  // BUY
                                if(menu_type.contains( "Limit" )) {
                                    TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) to execute your BUY order " + itemid + " "+ amount + " limit <set your price>");
                                    message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" "+ amount +" limit "));
                                    p.spigot().sendMessage(message);
                                    e.getView().close();
                                    return;
                                }
                                else if(menu_type.contains( "Market" )) {
                                    if (Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0] == 0) {
                                        p.sendMessage(ChatColor.RED + "There are no orders to buy, but you can create one by:");
                                        TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) to execute your BUY order " + itemid + " "+ amount + " limit <set your price>");
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" "+ amount +" limit "));
                                        p.spigot().sendMessage(message);
                                        e.getView().close();
                                        return;
                                    }
                                    else {
                                        TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) to execute your BUY order " + itemid + " "+ amount + " market ");
                                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" "+ amount +" market "));
                                        p.spigot().sendMessage(message);
                                        e.getView().close();
                                        return;
                                        //ItemexCommand.create_order(p, itemid, Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[0], amount, "buy", "market");
                                    }

                                }
                            }

                        }
                    }

                }



                // -- MENU START -------------------------------------------------------
                if( (e.getSlot() == 7 || e.getSlot() == 8) && !menu_type.contains("Orders:") ) {} // menu_type.contains("Orders:") for update add 64
                else {

                    // CREATE INVENTORY
                    Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.BLACK + menu_type);


                    // CLOSE WINDOW
                    ItemStack close = new ItemStack(Material.BARRIER);
                    ItemMeta closeMeta = close.getItemMeta();
                    closeMeta.setDisplayName(ChatColor.RED + "Close");
                    ArrayList<String> close_lore = new ArrayList<>();
                    close_lore.add(ChatColor.WHITE + "Click to close the window");
                    closeMeta.setLore(close_lore);
                    close.setItemMeta(closeMeta);

                    // HELP
                    ItemStack help = new ItemStack(Material.LIGHT);
                    ItemMeta helpMeta = help.getItemMeta();
                    helpMeta.setDisplayName(ChatColor.GRAY + "Help");
                    ArrayList<String> help_lore = new ArrayList<>();
                    help_lore.add(ChatColor.WHITE + "Choose a category below");
                    help_lore.add(ChatColor.DARK_GRAY + "- click = Video Tutorial Link");
                    helpMeta.setLore(help_lore);
                    help.setItemMeta(helpMeta);

                    // Market orders
                    ItemStack marketorders = new ItemStack(Material.COMPASS);
                    ItemMeta marketordersMeta = marketorders.getItemMeta();
                    marketordersMeta.setDisplayName(ChatColor.GOLD + "Market Orders");
                    ArrayList<String> marketorders_lore = new ArrayList<>();
                    marketorders_lore.add(ChatColor.WHITE + "Click to get to Market orders");
                    marketorders_lore.add(ChatColor.DARK_GRAY + "You can sell or buy to market prices. (fast)");
                    marketorders_lore.add(ChatColor.DARK_GRAY + "- Immediate order fulfillment, regardless of price");
                    marketordersMeta.setLore(marketorders_lore);
                    marketorders.setItemMeta(marketordersMeta);

                    // Limit orders
                    ItemStack limitorders = new ItemStack(Material.RECOVERY_COMPASS);
                    ItemMeta limitordersMeta = limitorders.getItemMeta();
                    limitordersMeta.setDisplayName(ChatColor.DARK_GREEN + "Limit Orders");
                    ArrayList<String> limitorders_lore = new ArrayList<>();
                    limitorders_lore.add(ChatColor.WHITE + "Click to get to Limit Orders");
                    limitorders_lore.add(ChatColor.DARK_GRAY + "You can sell or buy to fixed prices. (secure)");
                    limitorders_lore.add(ChatColor.DARK_GRAY + "- Order fulfilled at your price, or stays in book");
                    limitordersMeta.setLore(limitorders_lore);
                    limitorders.setItemMeta(limitordersMeta);

                    // Orderbook
                    ItemStack orderbook = new ItemStack(Material.CHEST);
                    ItemMeta orderbookMeta = orderbook.getItemMeta();
                    orderbookMeta.setDisplayName(ChatColor.DARK_PURPLE + "Order Book");
                    ArrayList<String> orderbook_lore = new ArrayList<>();
                    orderbook_lore.add(ChatColor.WHITE + "Click to get to all your Orders");
                    orderbook_lore.add(ChatColor.DARK_GRAY + "You will see all your buy- and sellorders");
                    orderbook_lore.add(ChatColor.DARK_GRAY + "- You can edit or remove it");
                    orderbookMeta.setLore(orderbook_lore);
                    orderbook.setItemMeta(orderbookMeta);

                    // Fastsell
                    ItemStack fastsell = new ItemStack(Material.CHEST_MINECART);
                    ItemMeta fastsellMeta = fastsell.getItemMeta();
                    fastsellMeta.setDisplayName(ChatColor.DARK_RED + "Fast Sell");
                    ArrayList<String> fastsell_lore = new ArrayList<>();
                    fastsell_lore.add(ChatColor.WHITE + "Click to sell quick");
                    fastsell_lore.add(ChatColor.DARK_GRAY + "Simply drop your items into");
                    fastsell_lore.add(ChatColor.DARK_GRAY + "- And sell automatically");
                    fastsellMeta.setLore(fastsell_lore);
                    fastsell.setItemMeta(fastsellMeta);

                    // WITHDRAW (VAULT)
                    ItemStack withdraw = new ItemStack(Material.ENDER_CHEST);
                    ItemMeta withdrawMeta = withdraw.getItemMeta();
                    withdrawMeta.setDisplayName(ChatColor.DARK_AQUA + "Vault");
                    ArrayList<String> withdraw_lore = new ArrayList<>();
                    withdraw_lore.add(ChatColor.WHITE + "Click for withdraw your items");
                    withdraw_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
                    withdraw_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
                    withdrawMeta.setLore(withdraw_lore);
                    withdraw.setItemMeta(withdrawMeta);

                    // SCROLLING (pages)
                    // RIGHT
                    ItemStack right = new ItemStack(Material.SPECTRAL_ARROW);
                    ItemMeta rightMeta = right.getItemMeta();
                    rightMeta.setDisplayName(ChatColor.WHITE + "Right");
                    ArrayList<String> right_lore = new ArrayList<>();
                    right_lore.add(ChatColor.DARK_GRAY + "Click for scroll to next page");
                    //right_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
                    //right_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
                    rightMeta.setLore(right_lore);
                    right.setItemMeta(rightMeta);
                    //LEFT
                    ItemStack left = new ItemStack(Material.CROSSBOW);
                    ItemMeta leftMeta = left.getItemMeta();
                    leftMeta.setDisplayName(ChatColor.WHITE + "Left");
                    ArrayList<String> left_lore = new ArrayList<>();
                    left_lore.add(ChatColor.DARK_GRAY + "Click for scroll to previous page");
                    //left_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
                    //left_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
                    leftMeta.setLore(left_lore);
                    left.setItemMeta(leftMeta);


                    // Plus 1 right
                    ItemStack plus1 = new ItemStack(Material.PAPER, 1);
                    ItemMeta plus1Meta = plus1.getItemMeta();
                    plus1Meta.setDisplayName(ChatColor.DARK_GREEN + "add 1");
                    ArrayList<String> plus1_lore = new ArrayList<>();
                    plus1_lore.add(ChatColor.GOLD + "[" + amount +"]");
                    plus1_lore.add(ChatColor.DARK_GRAY + "adds 1 pcs to item amount");
                    plus1Meta.setLore(plus1_lore);
                    plus1.setItemMeta(plus1Meta);

                    // Plus 64 right
                    ItemStack plus64 = new ItemStack(Material.PAPER, 64);
                    ItemMeta plus64Meta = plus64.getItemMeta();
                    plus64Meta.setDisplayName(ChatColor.DARK_GREEN + "add 64");
                    ArrayList<String> plus64_lore = new ArrayList<>();
                    plus64_lore.add(ChatColor.GOLD + "[" + amount +"]");
                    plus64_lore.add(ChatColor.DARK_GRAY + "adds 64 pcs to item amount");
                    plus64Meta.setLore(plus64_lore);
                    plus64.setItemMeta(plus64Meta);


                    // Minus 1 left
                    ItemStack minus1 = new ItemStack(Material.PAPER, 1);
                    ItemMeta minus1Meta = minus1.getItemMeta();
                    minus1Meta.setDisplayName(ChatColor.DARK_RED + "sub 1");
                    ArrayList<String> minus1_lore = new ArrayList<>();
                    minus1_lore.add(ChatColor.GOLD + "[" + amount +"]");
                    minus1_lore.add(ChatColor.DARK_GRAY + "subs 1 pcs from item amount");
                    minus1Meta.setLore(minus1_lore);
                    minus1.setItemMeta(minus1Meta);

                    // Minus 64 left
                    ItemStack minus64 = new ItemStack(Material.PAPER, 64);
                    ItemMeta minus64Meta = minus64.getItemMeta();
                    minus64Meta.setDisplayName(ChatColor.DARK_RED + "sub 64");
                    ArrayList<String> minus64_lore = new ArrayList<>();
                    minus64_lore.add(ChatColor.GOLD + "[" + amount +"]");
                    minus64_lore.add(ChatColor.DARK_GRAY + "subs 64 pcs from item amount");
                    minus64Meta.setLore(minus64_lore);
                    minus64.setItemMeta(minus64Meta);

                    // GET CATEGORIES FROM CONFIG
                    String[] cat_names = new String[10];
                    int cat_name_gui_type = 0;
                    for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                        cat_names[cat_name_gui_type] = cat;
                        cat_name_gui_type++;
                    }

                    ItemStack category[] = new ItemStack[15];
                    for(int cat_gui_type = 0; cat_gui_type <= 9; cat_gui_type++) {
                        //split
                        String cat_name[] = cat_names[cat_gui_type].split(":", 0);
                        category[cat_gui_type] = new ItemStack(Material.getMaterial( cat_name[1]));
                        ItemMeta categoryMeta = category[cat_gui_type].getItemMeta();
                        categoryMeta.setDisplayName(ChatColor.DARK_AQUA + cat_name[0]);
                        ArrayList<String> category_lore = new ArrayList<>();
                        category_lore.add(ChatColor.WHITE + "Click to choose a category");
                        category_lore.add(ChatColor.DARK_GRAY + "- you can buy and sell each item");
                        categoryMeta.setLore(category_lore);
                        category[cat_gui_type].setItemMeta(categoryMeta);
                    }


                    // PLACEHOLDER
                    ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

                    // print placeholder:
                    for(int inv_gui_type = 0; inv_gui_type <= 6*9-1; inv_gui_type++)
                        inv.setItem(inv_gui_type, placeholder);

                    inv.setItem(0, marketorders);
                    inv.setItem(1, limitorders);
                    inv.setItem(2, orderbook);
                    inv.setItem(3, fastsell);

                    inv.setItem(6, withdraw);
                    inv.setItem(7, help);
                    inv.setItem(8, close);


                    // -- MENU END -------------------------------------------------------
                    if( menu_type.contains("ITEMEX - Market Orders") || menu_type.contains("ITEMEX - Limit Orders")) {

                        if(!menu_type.contains(":")) {  // SHOW CATEGORY
                            if( menu_type.contains("ITEMEX - Market Orders") )
                                inv.setItem(9, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // market activated
                            else
                                inv.setItem(10, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // market activated

                            int begin_position = 20;
                            int cat_gui_type = 0 + begin_position;
                            for (int inv_gui_type = 0; inv_gui_type <= 6 * 9 - 1; inv_gui_type++) {
                                if (inv_gui_type >= 20 && inv_gui_type <= 24) {
                                    inv.setItem(cat_gui_type, category[cat_gui_type - 20]);
                                    cat_gui_type++;
                                } else if (inv_gui_type >= 29 && inv_gui_type <= 33) {
                                    inv.setItem(cat_gui_type + 4, category[cat_gui_type - 20]);
                                    cat_gui_type++;
                                }
                            }
                        }
                        else { // READ SUBMENU
                            int max_stack = 4*9;
                            int x=0;

                            String subMenu[] = menu_type.split(":", 3);
                            int cat_number = Integer.parseInt(subMenu[1]);
                            int page = Integer.parseInt(subMenu[2]) + 1;
                            //System.out.println("cat_number: " + cat_number + " page: " + page);
                            String cat_name[] =  cat_names[cat_number].split(":",2);

                            // print items
                            for (String itemid : CategoryFile.get().getStringList("categories." + cat_name[0])) {
                                if(x >= (page*max_stack - max_stack)) {

                                    //get total
                                    float buy_total = 0;
                                    float sell_total = 0;
                                    int rest_amount = amount;


                                    for(int bt=0; bt <= 3; bt++) {
                                        if(rest_amount <= Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[bt]) {
                                            buy_total = buy_total + rest_amount * Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[bt];
                                            break;
                                        }
                                        else { // is amount is larger -> next
                                            buy_total = buy_total + Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[bt] * Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[bt];
                                            rest_amount = rest_amount - Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[bt];
                                        }
                                    }

                                    rest_amount = amount;
                                    for(int st=0; st <= 3; st++) {
                                        if(rest_amount <= Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[st]) {
                                            sell_total = rest_amount * Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[st];
                                            break;
                                        }
                                        else {
                                            sell_total = sell_total + Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[st] * Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[st];
                                            rest_amount = rest_amount - Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[st];
                                        }
                                    }

                                    ItemStack item = new ItemStack(Material.getMaterial(itemid));
                                    ItemMeta itemMeta = item.getItemMeta();
                                    itemMeta.setDisplayName(ChatColor.WHITE + itemid + ChatColor.GOLD + " [" + amount +"]");
                                    ArrayList<String> item_lore = new ArrayList<>();

                                    item_lore.add(ChatColor.DARK_GRAY + "--------------------" );
                                    item_lore.add(ChatColor.WHITE + "Total Amounts: " + ChatColor.GOLD + "[" + amount +"]");
                                    item_lore.add(ChatColor.RED + "TOTAL SELL: $" + sell_total);
                                    item_lore.add(ChatColor.GREEN + "TOTAL BUY: $" + buy_total );
                                    item_lore.add(ChatColor.DARK_GRAY + "--------------------" );
                                    item_lore.add(ChatColor.WHITE + "(left) " + ChatColor.GREEN + "BUY " + ChatColor.WHITE +"| (right) " + ChatColor.RED + "SELL" );
                                    item_lore.add(ChatColor.DARK_GRAY + "--------------------" );
                                    item_lore.add(ChatColor.DARK_GRAY + "- Top Orders -");

                                    if(Itemex.getPlugin().mtop.get(itemid) == null) {
                                        for(int z=0; z <= 3; z++)
                                            item_lore.add(ChatColor.DARK_RED + "-");
                                        for(int z=0; z <= 3; z++)
                                            item_lore.add(ChatColor.DARK_GREEN + "-");
                                    }
                                    else {
                                        String best_to = "";
                                        for(int z=3; z >= 0; z--) {
                                            if(z == 0)
                                                best_to = ChatColor.DARK_GREEN + " best to buy";
                                            item_lore.add(ChatColor.DARK_RED + "[" + Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[z] +"] $" + Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[z] + best_to);

                                        }

                                        for(int z=0; z <= 3; z++) {
                                            if(z == 0)
                                                best_to = ChatColor.DARK_RED + " best to sell";
                                            else {
                                                best_to = "";
                                            }
                                            item_lore.add(ChatColor.DARK_GREEN + "[" + Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[z] +"] $" + Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[z] + best_to);
                                        }

                                    }


                                    item_lore.add(ChatColor.DARK_GRAY + "--------------------" );



                                    itemMeta.setLore(item_lore);
                                    item.setItemMeta(itemMeta);
                                    inv.setItem(x+max_stack-page*max_stack+18, item);

                                }

                                if(x >= page*max_stack-1)
                                    break;
                                x++;
                            }

                            // print scroll page
                            inv.setItem(4, left);
                            inv.setItem(5, right);

                            inv.setItem(2, minus64);
                            inv.setItem(3, minus1);

                            inv.setItem(6, plus1);
                            inv.setItem(7, plus64);

                        }


                    } // end ITEMEX - Market Orders
                    else
                        p.sendMessage("NOT IMPLEMENTED: " + menu_type);




                    p.openInventory(inv);
                }

            } // end right or left click
        }

        // SCAN ITEM IN CREATIVE FOR EXTRACTION OF ITEMS WITH CATEGORIES
        else {
            getLogger().info(":" + itemcounter + ":ITEM:" + p.getInventory().getItemInOffHand().getType());
            itemcounter++;
            if(itemcounter >= 9)
                itemcounter=0;
        }
        // END SCAN ITEMS

    }
}