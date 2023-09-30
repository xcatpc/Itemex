package sh.ome.itemex.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.GUI.i_GUI;
import sh.ome.itemex.GUI.ix_GUI;
import sh.ome.itemex.GUI.setprice_GUI;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.ix_command;
import sh.ome.itemex.files.CategoryFile;
import sh.ome.itemex.functions.sqliteDb;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;
import static sh.ome.itemex.commands.commands.*;
import static sh.ome.itemex.functions.sqliteDb.getPayout;


public class i_ClickGUI implements Listener {
    int itemcounter = 0;
    int amount = 1;

    @EventHandler
    public void clickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getView().getTitle().contains("ITEMEX")) {                          // If player click on Itemex GUI
            if (e.getClickedInventory() == null) {
                return;
            }
            String menu_type = e.getView().getTitle().substring(2);
            //getLogger().info("# DEBUG: at click EVENT: " + menu_type + " " + e.getSlot() + " " + e.getClickedInventory().getType().toString());

            if ((e.getClick().isLeftClick() || e.getClick().isRightClick() || e.getClick().isKeyboardClick()) && e.getClickedInventory().getType().toString().equals("CHEST")) {
                e.setCancelled(true);

                // SET THE MENU NAME

                // if click is on menu
                if (e.getSlot() >= 0 && e.getSlot() <= 8) {
                    if (e.getSlot() == 0)
                        menu_type = "ITEMEX - BUY ITEM";
                    else if (e.getSlot() == 1)
                        menu_type = "ITEMEX - SELL ITEM";
                    else if (e.getSlot() == 2) {
                        if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            amount = amount - 64;
                            if (amount <= 0) {
                                amount = 1;
                            }
                            //p.sendMessage("sub 64: " + amount);
                        } else
                            menu_type = "ITEMEX - Order Book:S:0";
                    } else if (e.getSlot() == 3)
                        if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            amount = amount - 1;
                            if (amount <= 0) {
                                amount = 1;
                            }
                            //p.sendMessage("sub 1: " + amount);
                        } else
                            menu_type = "ITEMEX - Fast Sell";


                    else if (e.getSlot() == 4) { // prev page
                        if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[2]);
                            page = page - 1;
                            if (page == -1)
                                page = 0;
                            menu_type = subMenu[0] + ":" + subMenu[1] + ":" + page; // Category 1 page 0
                        } else if (menu_type.contains("Order Book")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[2]);
                            page = page - 1;
                            if (page == -1)
                                page = 0;
                            menu_type = subMenu[0] + ":" + subMenu[1] + ":" + page;
                        } else if (menu_type.contains("Vault")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[1]);
                            page = page - 1;
                            if (page == -1)
                                page = 0;
                            menu_type = subMenu[0] + ":" + page;
                        }
                    } else if (e.getSlot() == 5) {    // next page
                        if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[2]);
                            page = page + 1;
                            menu_type = subMenu[0] + ":" + subMenu[1] + ":" + page; // Category 1 page 0
                        } else if (menu_type.contains("Order Book")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[2]);
                            page = page + 1;
                            menu_type = subMenu[0] + ":" + subMenu[1] + ":" + page;
                        } else if (menu_type.contains("Vault")) {
                            String subMenu[] = menu_type.split(":", 3);
                            int page = Integer.parseInt(subMenu[1]);
                            page = page + 1;
                            menu_type = subMenu[0] + ":" + page;
                        }
                    } else if (e.getSlot() == 6) {
                        if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            amount = amount + 1;
                        }

                        else if (menu_type.contains("Order Book")) {
                            String subMenu[] = menu_type.split(":", 3);
                            menu_type = subMenu[0] + ":S:" + subMenu[2]; // Change :B: to :S:
                        } else
                            menu_type = "ITEMEX - Vault:0";
                    } else if (e.getSlot() == 7) {
                            if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            amount = amount + 64;
                            //p.sendMessage("add 64: " + amount);
                        } else if (menu_type.contains("Order Book")) {
                            String subMenu[] = menu_type.split(":", 3);
                            menu_type = subMenu[0] + ":B:" + subMenu[2]; // Change :B: to :S:
                        } else {
                            menu_type = "ITEMEX - HELP";
                            e.getView().close();
                            p.sendMessage("https://www.youtube.com/watch?v=C5-e7snYyns");
                        }

                    } else if (e.getSlot() == 8) {
                        menu_type = "ITEMEX - CLOSE";
                        e.getView().close();
                    } else
                        menu_type = e.getView().getTitle().substring(2);
                } else {  // rest of inventory
                    if (menu_type.contains(menu_type)) {
                        if ((menu_type.contains("BUY") || menu_type.contains("SELL")) && !menu_type.contains(":")) {
                            String subMenu[] = menu_type.split(":", 3);
                            if (e.getSlot() == 20) {
                                menu_type = subMenu[0] + ":0:0";
                            }                       // Category 0 page 0
                            else if (e.getSlot() == 21) {
                                menu_type = subMenu[0] + ":1:0";
                            }                  // Category 1 page 0
                            else if (e.getSlot() == 22) {
                                menu_type = subMenu[0] + ":2:0";
                            }                  // Category 2 page 0
                            else if (e.getSlot() == 23) {
                                menu_type = subMenu[0] + ":3:0";
                            }                  // Category 3 page 0
                            else if (e.getSlot() == 24) {
                                menu_type = subMenu[0] + ":4:0";
                            }                  // Category 4 page 0
                            else if (e.getSlot() == 29) {
                                menu_type = subMenu[0] + ":5:0";
                            }                  // Category 5 page 0
                            else if (e.getSlot() == 30) {
                                menu_type = subMenu[0] + ":6:0";
                            }                  // Category 6 page 0
                            else if (e.getSlot() == 31) {
                                menu_type = subMenu[0] + ":7:0";
                            }                  // Category 7 page 0
                            else if (e.getSlot() == 32) {
                                menu_type = subMenu[0] + ":8:0";
                            }                  // Category 8 page 0
                            else if (e.getSlot() == 33) {
                                menu_type = subMenu[0] + ":9:0";
                            }                  // Category 9 page 0
                        } else if (menu_type.contains("Order Book")) {
                            ItemStack item = e.getCurrentItem();
                            if (item == null) {
                                return;
                            }
                            String item_json = identify_item(item);
                            String orderID = "";
                            String buy_or_sell = "";
                            ItemMeta meta = item.getItemMeta();
                            boolean is_lore = false;

                            if (meta != null && meta.hasLore()) {
                                is_lore = true;
                                List<String> lore = meta.getLore();
                                for (String loreLine : lore) {
                                    if (loreLine.contains("ID:")) {
                                        String[] split = loreLine.split(":");
                                        if (split[1].equals("S"))
                                            buy_or_sell = "SELLORDERS";
                                        else
                                            buy_or_sell = "BUYORDERS";
                                        orderID = split[2];
                                    }
                                }
                            }

                            if (is_lore)
                                sqliteDb.PlayercloseOrder(p.getUniqueId().toString(), buy_or_sell, Integer.parseInt(orderID));

                            //e.getView().close();
                            //return;
                        } else if (menu_type.contains("Vault")) {
                            ItemStack item = e.getCurrentItem();
                            if (item == null)
                                return;
                            ItemMeta meta = item.getItemMeta();
                            String item_json = identify_item(item);
                            String customName = "";

                            if (meta != null && meta.hasDisplayName()) {
                                customName = meta.getDisplayName();
                            }

                            String amount = customName.replaceAll(".*\\[(\\d+)\\].*", "$1");
                            if (!amount.equals("") && !customName.equals("."))
                                ix_command.withdraw(get_meta(item_json), amount, p);

                        } else if (menu_type.contains("BUY ITEM:") || menu_type.contains("SELL ITEM:")) {
                            // GET ITEM TO BUY OR SELL
                            //getLogger().info("# DEBUG: ITEM: " + e.getCurrentItem().getType() + " Click: " + e.getClick() + " Menutype: " + menu_type);
                            ItemStack currentItem = e.getCurrentItem();
                            String item_json = identify_item(currentItem);
                            String itemid = "";
                            if (currentItem != null) {
                                itemid = currentItem.getType().toString();
                            } else
                                return;

                            if (menu_type.contains("BUY ITEM:")) {
                                setprice_GUI.generateGUI(p, "SET YOUR PRICE : SELL", itemid, amount, false);
                                return;
                            }
                            else if (menu_type.contains("SELL ITEM:")) {
                                setprice_GUI.generateGUI(p, "SET YOUR PRICE : BUY", itemid, amount, false);
                                return;
                            }
                        }
                    }
                }


                // -- MENU START -------------------------------------------------------
                if ((e.getSlot() == 7 || e.getSlot() == 8) && !menu_type.contains("Orders") && !menu_type.contains("Order Book")) {
                    //getLogger().info("CLICKED 7 or 8");
                } // menu_type.contains("Orders:") for update add 64

                else {
                    //getLogger().info("create inventory");

                    // CREATE INVENTORY
                    Inventory inv = Bukkit.createInventory(null, 6 * 9, ChatColor.BLACK + menu_type);

                    // CLOSE WINDOW
                    ItemStack close = new ItemStack(Material.BARRIER);
                    ItemMeta closeMeta = close.getItemMeta();
                    closeMeta.setDisplayName(ChatColor.RED + Itemex.language.getString("cs_close"));
                    ArrayList<String> close_lore = new ArrayList<>();
                    close_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_close_m"));
                    closeMeta.setLore(close_lore);
                    close.setItemMeta(closeMeta);

                    // HELP
                    ItemStack help = new ItemStack(Material.LIGHT);
                    ItemMeta helpMeta = help.getItemMeta();
                    helpMeta.setDisplayName(ChatColor.GRAY + Itemex.language.getString("cs_help"));
                    ArrayList<String> help_lore = new ArrayList<>();
                    help_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_help_m1"));
                    help_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_help_m2"));
                    helpMeta.setLore(help_lore);
                    help.setItemMeta(helpMeta);

                    // Marketplace i_ITEMEX
                    ItemStack marketplace_buy = new ItemStack(Material.WRITABLE_BOOK);
                    ItemMeta marketplace_buyMeta = marketplace_buy.getItemMeta();
                    marketplace_buyMeta.setDisplayName(ChatColor.GOLD + "Buy Item");
                    ArrayList<String> marketplace_buy_lore = new ArrayList<>();
                    marketplace_buy_lore.add(ChatColor.WHITE + "Click here to buy a item");
                    marketplace_buyMeta.setLore(marketplace_buy_lore);
                    marketplace_buy.setItemMeta(marketplace_buyMeta);

                    ItemStack marketplace_sell = new ItemStack(Material.BOOK);
                    ItemMeta marketplace_sellMeta = marketplace_sell.getItemMeta();
                    marketplace_sellMeta.setDisplayName(ChatColor.GOLD + "Sell Item");
                    ArrayList<String> marketplace_sell_lore = new ArrayList<>();
                    marketplace_sell_lore.add(ChatColor.WHITE + "Click here to sell a item");
                    marketplace_sellMeta.setLore(marketplace_sell_lore);
                    marketplace_sell.setItemMeta(marketplace_sellMeta);

                    // Orderbook
                    ItemStack orderbook = new ItemStack(Material.CHEST);
                    ItemMeta orderbookMeta = orderbook.getItemMeta();
                    orderbookMeta.setDisplayName(ChatColor.DARK_PURPLE + Itemex.language.getString("cs_ob"));
                    ArrayList<String> orderbook_lore = new ArrayList<>();
                    orderbook_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_ob_m1"));
                    orderbook_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ob_m2"));
                    orderbook_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ob_m3"));
                    orderbookMeta.setLore(orderbook_lore);
                    orderbook.setItemMeta(orderbookMeta);

                    // Fastsell
                    ItemStack fastsell = new ItemStack(Material.CHEST_MINECART);
                    ItemMeta fastsellMeta = fastsell.getItemMeta();
                    fastsellMeta.setDisplayName(ChatColor.DARK_RED + Itemex.language.getString("cs_fs"));
                    ArrayList<String> fastsell_lore = new ArrayList<>();
                    fastsell_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_fs_m1"));
                    fastsell_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_fs_m2"));
                    fastsell_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_fs_m3"));
                    fastsellMeta.setLore(fastsell_lore);
                    fastsell.setItemMeta(fastsellMeta);

                    // WITHDRAW (VAULT)
                    ItemStack withdraw = new ItemStack(Material.ENDER_CHEST);
                    ItemMeta withdrawMeta = withdraw.getItemMeta();
                    withdrawMeta.setDisplayName(ChatColor.DARK_AQUA + Itemex.language.getString("cs_va"));
                    ArrayList<String> withdraw_lore = new ArrayList<>();
                    withdraw_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_va_m1"));
                    withdraw_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_va_m2"));
                    withdraw_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_va_m3"));
                    withdrawMeta.setLore(withdraw_lore);
                    withdraw.setItemMeta(withdrawMeta);

                    // SCROLLING (pages)
                    // RIGHT
                    ItemStack right = new ItemStack(Material.SPECTRAL_ARROW);
                    ItemMeta rightMeta = right.getItemMeta();
                    rightMeta.setDisplayName(ChatColor.WHITE + Itemex.language.getString("cs_click_r"));
                    ArrayList<String> right_lore = new ArrayList<>();
                    right_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_click_r_m1"));
                    //right_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
                    //right_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
                    rightMeta.setLore(right_lore);
                    right.setItemMeta(rightMeta);
                    //LEFT
                    ItemStack left = new ItemStack(Material.CROSSBOW);
                    ItemMeta leftMeta = left.getItemMeta();
                    leftMeta.setDisplayName(ChatColor.WHITE + Itemex.language.getString("cs_click_l"));
                    ArrayList<String> left_lore = new ArrayList<>();
                    left_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_click_l_m1"));
                    //left_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
                    //left_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
                    leftMeta.setLore(left_lore);
                    left.setItemMeta(leftMeta);


                    // Plus 1 right
                    ItemStack plus1 = new ItemStack(Material.PAPER, 1);
                    ItemMeta plus1Meta = plus1.getItemMeta();
                    plus1Meta.setDisplayName(ChatColor.DARK_GREEN + Itemex.language.getString("cs_ad_1"));
                    ArrayList<String> plus1_lore = new ArrayList<>();
                    plus1_lore.add(ChatColor.GOLD + "[" + amount + "]");
                    plus1_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ad_1_m"));
                    plus1Meta.setLore(plus1_lore);
                    plus1.setItemMeta(plus1Meta);

                    // Plus 64 right
                    ItemStack plus64 = new ItemStack(Material.PAPER, 64);
                    ItemMeta plus64Meta = plus64.getItemMeta();
                    plus64Meta.setDisplayName(ChatColor.DARK_GREEN + Itemex.language.getString("cs_ad_64"));
                    ArrayList<String> plus64_lore = new ArrayList<>();
                    plus64_lore.add(ChatColor.GOLD + "[" + amount + "]");
                    plus64_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ad_64_m"));
                    plus64Meta.setLore(plus64_lore);
                    plus64.setItemMeta(plus64Meta);


                    // Minus 1 left
                    ItemStack minus1 = new ItemStack(Material.PAPER, 1);
                    ItemMeta minus1Meta = minus1.getItemMeta();
                    minus1Meta.setDisplayName(ChatColor.DARK_RED + Itemex.language.getString("cs_sub_1"));
                    ArrayList<String> minus1_lore = new ArrayList<>();
                    minus1_lore.add(ChatColor.GOLD + "[" + amount + "]");
                    minus1_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_sub_1_m"));
                    minus1Meta.setLore(minus1_lore);
                    minus1.setItemMeta(minus1Meta);

                    // Minus 64 left
                    ItemStack minus64 = new ItemStack(Material.PAPER, 64);
                    ItemMeta minus64Meta = minus64.getItemMeta();
                    minus64Meta.setDisplayName(ChatColor.DARK_RED + Itemex.language.getString("cs_sub_64"));
                    ArrayList<String> minus64_lore = new ArrayList<>();
                    minus64_lore.add(ChatColor.GOLD + "[" + amount + "]");
                    minus64_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_sub_64_m"));
                    minus64Meta.setLore(minus64_lore);
                    minus64.setItemMeta(minus64Meta);

                    // ORDERBOOK ---

                    // show show_sellorder
                    ItemStack show_sellorder = new ItemStack(Material.RED_BANNER, 1);
                    ItemMeta show_sellorder_meta = show_sellorder.getItemMeta();
                    show_sellorder_meta.setDisplayName(ChatColor.DARK_RED + "Show sell orders");
                    ArrayList<String> show_sellorder_lore = new ArrayList<>();
                    show_sellorder_lore.add(ChatColor.WHITE + "Prints all of your sellorders");
                    show_sellorder_lore.add(ChatColor.DARK_GRAY + "Just click to close the order");
                    show_sellorder_meta.setLore(show_sellorder_lore);
                    show_sellorder.setItemMeta(show_sellorder_meta);

                    // show show_buyorder
                    ItemStack show_buyorder = new ItemStack(Material.GREEN_BANNER, 1);
                    ItemMeta show_buyorder_meta = show_buyorder.getItemMeta();
                    show_buyorder_meta.setDisplayName(ChatColor.DARK_GREEN + "Show buy orders");
                    ArrayList<String> show_buyorder_lore = new ArrayList<>();
                    show_buyorder_lore.add(ChatColor.WHITE + "Prints all of your buyorders");
                    show_buyorder_lore.add(ChatColor.DARK_GRAY + "Just click to close the order");
                    show_buyorder_meta.setLore(show_buyorder_lore);
                    show_buyorder.setItemMeta(show_buyorder_meta);


                    // GET CATEGORIES FROM CONFIG
                    String[] cat_names = new String[10];
                    int cat_name_gui_type = 0;
                    for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                        cat_names[cat_name_gui_type] = cat;
                        cat_name_gui_type++;
                    }

                    ItemStack category[] = new ItemStack[15];
                    for (int cat_gui_type = 0; cat_gui_type <= 9; cat_gui_type++) {
                        //split
                        String cat_name[] = cat_names[cat_gui_type].split(":", 0);
                        category[cat_gui_type] = new ItemStack(Material.getMaterial(cat_name[1]));
                        ItemMeta categoryMeta = category[cat_gui_type].getItemMeta();
                        categoryMeta.setDisplayName(ChatColor.DARK_AQUA + cat_name[0]);
                        ArrayList<String> category_lore = new ArrayList<>();
                        category_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_click_cat"));
                        //category_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ycbasei"));
                        categoryMeta.setLore(category_lore);
                        category[cat_gui_type].setItemMeta(categoryMeta);
                    }


                    // PLACEHOLDER
                    ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                    ItemMeta placeholder_meta = placeholder.getItemMeta();
                    placeholder_meta.setDisplayName(".");
                    placeholder.setItemMeta(placeholder_meta);

                    // print placeholder:
                    for (int inv_gui_type = 0; inv_gui_type <= 6 * 9 - 1; inv_gui_type++)
                        inv.setItem(inv_gui_type, placeholder);


                    inv.setItem(0, marketplace_buy);
                    inv.setItem(1, marketplace_sell);
                    inv.setItem(2, orderbook);
                    //inv.setItem(3, fastsell);

                    inv.setItem(6, withdraw);
                    inv.setItem(7, help);
                    inv.setItem(8, close);


                    // -- MENU END -------------------------------------------------------
                    if (menu_type.contains("ITEMEX - BUY ITEM") || menu_type.contains("ITEMEX - SELL ITEM")) {
                        //getLogger().info("# DEBUG: ITEMEX - BUY OR SELL ITEM");

                        if (!menu_type.contains(":")) {  // SHOW CATEGORY
                            if (menu_type.contains("ITEMEX - BUY ITEM"))
                                inv.setItem(9, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // market activated
                            else if (menu_type.contains("ITEMEX - SELL ITEM"))
                                inv.setItem(10, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // limit activated

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
                        } else { // READ SUBMENU

                            if (menu_type.contains("ITEMEX - BUY ITEM"))
                                inv.setItem(9, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // market activated
                            else if (menu_type.contains("ITEMEX - SELL ITEM"))
                                inv.setItem(10, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // limit activated

                            int max_stack = 4 * 9;
                            int x = 0;

                            String subMenu[] = menu_type.split(":", 3);
                            int cat_number = Integer.parseInt(subMenu[1]);
                            int page = Integer.parseInt(subMenu[2]) + 1;
                            //getLogger().info("cat_number: " + cat_number + " page: " + page);
                            String cat_name[] = cat_names[cat_number].split(":", 2);

                            // print items
                            for (String itemid : CategoryFile.get().getStringList("categories." + cat_name[0])) {
                                if (x >= (page * max_stack - max_stack)) {

                                    //get total
                                    double buy_total = 0;
                                    double sell_total = 0;
                                    int rest_amount = amount;

                                    for (int bt = 0; bt <= 3; bt++) {
                                        if (rest_amount <= Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[bt]) {
                                            buy_total = buy_total + rest_amount * Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[bt];
                                            break;
                                        } else { // is amount is larger -> next
                                            buy_total = buy_total + Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[bt] * Itemex.getPlugin().mtop.get(itemid).get_top_sellorder_prices()[bt];
                                            rest_amount = rest_amount - Itemex.getPlugin().mtop.get(itemid).get_sellorder_amount()[bt];
                                        }
                                    }

                                    rest_amount = amount;
                                    for (int st = 0; st <= 3; st++) {
                                        if (rest_amount <= Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[st]) {
                                            sell_total = rest_amount * Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[st];
                                            break;
                                        } else {
                                            sell_total = sell_total + Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[st] * Itemex.getPlugin().mtop.get(itemid).get_top_buyorder_prices()[st];
                                            rest_amount = rest_amount - Itemex.getPlugin().mtop.get(itemid).get_buyorder_amount()[st];
                                        }
                                    }

                                    ItemStack item = constructItem(itemid, 1);

                                    if (item != null) {
                                        ItemMeta itemMeta = item.getItemMeta();
                                        if (itemMeta != null) {

                                            itemMeta.setDisplayName(ChatColor.WHITE + get_itemid(itemid) + ChatColor.GOLD + " [" + amount + "]");
                                            ArrayList<String> item_lore = new ArrayList<>();

                                            item_lore.add(ChatColor.DARK_GRAY + "--------------------");
                                            if (menu_type.contains("ITEMEX - BUY ITEM")) {
                                                item_lore.add(ChatColor.GREEN + Itemex.language.getString("cs_total_buy") + format_price(buy_total));
                                                item_lore.add(ChatColor.DARK_GRAY + "--------------------");
                                                item_lore.add(ChatColor.GRAY + "Just click to buy the item");
                                            }

                                            else {
                                                item_lore.add(ChatColor.RED + Itemex.language.getString("cs_total_sell") + format_price(sell_total));
                                                item_lore.add(ChatColor.DARK_GRAY + "--------------------");
                                                item_lore.add(ChatColor.GRAY + "Just click to sell the item");
                                            }

                                            //item_lore.add(ChatColor.GRAY + "You have: " + Itemex.econ.getBalance(p));

                                            itemMeta.setLore(item_lore);
                                            item.setItemMeta(itemMeta);
                                            inv.setItem(x + max_stack - page * max_stack + 18, item);
                                        } else {
                                            // Handle situation when itemMeta is null...
                                            ItemStack placeholder_item = new ItemStack(Material.getMaterial(get_itemid(itemid)), 1);
                                            ItemMeta placeholder_itemMeta = placeholder_item.getItemMeta();
                                            placeholder_itemMeta.setDisplayName(ChatColor.DARK_PURPLE + get_itemid(itemid) + " not supported!");
                                            placeholder_item.setItemMeta(placeholder_itemMeta);
                                            inv.setItem(x + max_stack - page * max_stack + 18, placeholder_item);
                                        }
                                    } else {
                                        // Handle situation when item is null...
                                        ItemStack placeholder_item = new ItemStack(Material.BARRIER);
                                        ItemMeta placeholder_itemMeta = placeholder_item.getItemMeta();
                                        placeholder_itemMeta.setDisplayName("Item not supported!");
                                        placeholder_item.setItemMeta(placeholder_itemMeta);
                                        inv.setItem(x + max_stack - page * max_stack + 18, placeholder_item);
                                    }


                                }

                                if (x >= page * max_stack - 1)
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
                    } // end IX - Limit- and Market Orders


                    else if (menu_type.contains("ITEMEX - Order Book")) {
                        String subMenu[] = menu_type.split(":", 3);
                        //getLogger().info("subMenu[2]: " + subMenu[2]);
                        boolean is_buy_not_sell = true;
                        String buy_sell_order_string = ChatColor.DARK_GREEN.toString();
                        int currentPage = Integer.parseInt(subMenu[2]); // Liest die aktuelle Seite aus dem Menu-Typen
                        int itemsPerPage = 36; // Setzt die Anzahl der Items pro Seite

                        if (subMenu.length >= 2) {
                            if (subMenu[1].equals("S")) {
                                inv.setItem(15, new ItemStack(Material.GREEN_STAINED_GLASS_PANE)); // show only sells activated
                                is_buy_not_sell = false;
                                buy_sell_order_string = ChatColor.DARK_RED.toString();
                            } else if (subMenu[1].equals("B")) {
                                inv.setItem(16, new ItemStack(Material.GREEN_STAINED_GLASS_PANE)); // show only buys activated
                                is_buy_not_sell = true;
                            }

                            sqliteDb.OrderBuffer[] list = sqliteDb.getOrdersOfPlayer(p.getUniqueId().toString(), "*", is_buy_not_sell, 1); //true = buy ; false = sell

                            // Beginnt am Index entsprechend der aktuellen Seite
                            for (int i = currentPage * itemsPerPage; i < list.length && i < (currentPage + 1) * itemsPerPage; i++) {
                                if (list[i] == null) {
                                    break;
                                } else {
                                    ItemStack item = constructItem(list[i].itemid, list[i].amount);
                                    ItemMeta item_meta = item.getItemMeta();
                                    item_meta.setDisplayName(ChatColor.GOLD + get_itemid(list[i].itemid) + ChatColor.RESET + " [" + list[i].amount + "]");
                                    ArrayList<String> item_lore = new ArrayList<>();
                                    item_lore.add(ChatColor.WHITE + "Price: " + buy_sell_order_string + format_price(list[i].price));

                                    item_lore.add(ChatColor.LIGHT_PURPLE + "(click to remove)");
                                    item_lore.add(ChatColor.DARK_GRAY + "ID:" + subMenu[1] + ":" + list[i].id);
                                    item_meta.setLore(item_lore);
                                    item.setItemMeta(item_meta);
                                    inv.setItem((i % itemsPerPage) + 18, item);
                                }
                            } // end for
                        } else
                            inv.setItem(15, new ItemStack(Material.GREEN_STAINED_GLASS_PANE)); // show only sells activated

                        inv.setItem(11, new ItemStack(Material.GREEN_STAINED_GLASS_PANE)); // order book activated
                        inv.setItem(4, left);
                        inv.setItem(5, right);
                        inv.setItem(6, show_sellorder); // show sell orders
                        inv.setItem(7, show_buyorder); // show buy orders
                    } else if (menu_type.contains("ITEMEX - Fast Sell")) {
                        inv.setItem(12, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));      // fast sell activated

                        // Drop windows
                        ItemStack drop = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
                        ItemMeta drop_meta = drop.getItemMeta();
                        drop_meta.setDisplayName(ChatColor.GOLD + "Drop your item to sell on market here");
                        drop.setItemMeta(drop_meta);

                        // SELL ITEM
                        ItemStack sell = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                        ItemMeta sellMeta = sell.getItemMeta();
                        sellMeta.setDisplayName(ChatColor.GREEN + "Sell");
                        ArrayList<String> sell_lore = new ArrayList<>();
                        sell_lore.add(ChatColor.WHITE + "click here to sell items.");
                        sellMeta.setLore(sell_lore);
                        sell.setItemMeta(sellMeta);


                        // confirm button
                        inv.setItem(53, sell);

                        inv.setItem(e.getSlot(), e.getCursor());

                        p.sendMessage("get_cursor: " + e.getCursor().getType().toString());
                        p.sendMessage("getSlot: " + e.getSlot());

                    } else if (menu_type.contains("ITEMEX - Vault")) {
                        //getLogger().info("# DEBUG: VAULT");
                        String subMenu[] = menu_type.split(":", 3);
                        //getLogger().info("subMenu[1]: " + subMenu[1]);
                        int currentPage = Integer.parseInt(subMenu[1]); // Liest die aktuelle Seite aus dem Menu-Typen
                        int itemsPerPage = 36; // Setzt die Anzahl der Items pro Seite

                        inv.setItem(15, new ItemStack(Material.GREEN_STAINED_GLASS_PANE)); // vault activated
                        inv.setItem(4, left);
                        inv.setItem(5, right);

                        sqliteDb.Payout[] buffer = getPayout(p.getUniqueId().toString());

                        // Beginnt am Index entsprechend der aktuellen Seite
                        for (int i = currentPage * itemsPerPage; i < buffer.length && i < (currentPage + 1) * itemsPerPage; i++) {
                            if (buffer[i] != null) {
                                ItemStack item = constructItem(buffer[i].itemid, buffer[i].amount);
                                ItemMeta item_meta = item.getItemMeta();
                                item_meta.setDisplayName(ChatColor.GOLD + get_itemid(buffer[i].itemid) + ChatColor.RESET + " [" + buffer[i].amount + "]");
                                ArrayList<String> item_lore = new ArrayList<>();
                                item_lore.add(ChatColor.LIGHT_PURPLE + "(click to withdraw)");
                                item_meta.setLore(item_lore);
                                item.setItemMeta(item_meta);
                                inv.setItem((i % itemsPerPage) + 18, item); // vault activated
                            }
                        }
                    }

                    p.openInventory(inv);
                }
            } // end right or left click
        } // END IX




        if(e.getView().getTitle().contains("SET YOUR PRICE")) {
            double d_limit_price;
            e.setCancelled(true);

            // get double from PLUS 1
            ItemStack plus1 = e.getView().getItem(5);
            String limit_price = "0";

            // get itemid
            ItemStack item = e.getView().getItem(4);

            limit_price = plus1.getItemMeta().getLore().get(0);
            limit_price = limit_price.replace("§6[", "");
            limit_price = limit_price.replace("]", "");
            d_limit_price = Double.parseDouble(limit_price);
            switch(e.getSlot()) {
                case 0:
                    d_limit_price *=10;
                    break;
                case 1:
                    d_limit_price -=100;
                    break;
                case 2:
                    d_limit_price -=10;
                    break;
                case 3:
                    d_limit_price -=1;
                    break;
                case 4:
                    // confirm
                    break;
                case 5:
                    d_limit_price +=1;
                    break;
                case 6:
                    d_limit_price +=10;
                    break;
                case 7:
                    d_limit_price +=100;
                    break;
                case 8:
                    d_limit_price /= 10;
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                    e.getView().close();
                    return;

                case 13:
                case 14:
                case 15:
                case 16:
                    //confirm
                    // proof if price changed to white at item:
                    ItemStack check_item = e.getView().getItem(4);
                    String tmp = check_item.getItemMeta().getLore().get(1);
                    //getLogger().info("# DEBUG: confirm " + tmp);
                    if(tmp.contains("§f")) { // price changed at item = limit order
                        ix_ClickGUI.create_limit_order(p, identify_item(item), e.getView().getTitle(), d_limit_price);
                    }
                    else { // market order
                        ix_ClickGUI.create_market_order(p, identify_item(item), e.getView().getTitle(), d_limit_price);
                    }

                    e.getView().close();
                    return;
                case 17:
                    //getLogger().info("Back");
                    e.getView().close();
                    i_GUI.generateGUI(p, "ITEMEX - BUY ITEM", 0, 0);
                    return;
                default:
                    return;
            }



            // if price lower than 0
            if(d_limit_price < 0) {
                d_limit_price = 1;
            }

            // PAINT NEW INV
            String title = e.getView().getTitle();
            Inventory inv = Bukkit.createInventory(null, 2*9, ChatColor.BLACK +  title);


            // ITEMID
            ItemMeta itemMeta = item.getItemMeta();
            ArrayList<String> item_lore = new ArrayList<>();
            item_lore.add(itemMeta.getLore().get(0)); // copy the amount
            item_lore.add(ChatColor.DARK_GRAY + "Price: " + ChatColor.WHITE + d_limit_price ); // copy the amount
            itemMeta.setLore(item_lore);
            item.setItemMeta(itemMeta);


            // Plus 1
            ItemMeta plus1Meta = plus1.getItemMeta();
            ArrayList<String> plus1_lore = new ArrayList<>();
            plus1_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            plus1Meta.setLore(plus1_lore);
            plus1.setItemMeta(plus1Meta);

            // Plus 10
            ItemStack plus10 = e.getView().getItem(6);
            ItemMeta plus10Meta = plus10.getItemMeta();
            ArrayList<String> plus10_lore = new ArrayList<>();
            plus10_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            plus10Meta.setLore(plus10_lore);
            plus10.setItemMeta(plus10Meta);

            // Plus 100
            ItemStack plus100 = e.getView().getItem(7);
            ItemMeta plus100Meta = plus100.getItemMeta();
            ArrayList<String> plus100_lore = new ArrayList<>();
            plus100_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            plus100Meta.setLore(plus100_lore);
            plus100.setItemMeta(plus100Meta);

            // comma left
            ItemStack plus1000 = e.getView().getItem(8);
            ItemMeta plus1000Meta = plus1000.getItemMeta();
            ArrayList<String> plus1000_lore = new ArrayList<>();
            plus1000_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            plus1000Meta.setLore(plus1000_lore);
            plus1000.setItemMeta(plus1000Meta);


            // Minus 1
            ItemStack minus1 = e.getView().getItem(3);
            ItemMeta minus1Meta = minus1.getItemMeta();
            ArrayList<String> minus1_lore = new ArrayList<>();
            minus1_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            minus1Meta.setLore(minus1_lore);
            minus1.setItemMeta(minus1Meta);

            // Minus 10
            ItemStack minus10 = e.getView().getItem(2);
            ItemMeta minus10Meta = minus10.getItemMeta();
            ArrayList<String> minus10_lore = new ArrayList<>();
            minus10_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            minus10Meta.setLore(minus10_lore);
            minus10.setItemMeta(minus10Meta);

            // Minus 100
            ItemStack minus100 = e.getView().getItem(1);
            ItemMeta minus100Meta = minus100.getItemMeta();
            ArrayList<String> minus100_lore = new ArrayList<>();
            minus100_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            minus100Meta.setLore(minus100_lore);
            minus100.setItemMeta(minus100Meta);

            // comma right
            ItemStack minus1000 = e.getView().getItem(0);
            ItemMeta minus1000Meta = minus1000.getItemMeta();
            ArrayList<String> minus1000_lore = new ArrayList<>();
            minus1000_lore.add(ChatColor.GOLD + "[" + d_limit_price +"]");
            minus1000Meta.setLore(minus1000_lore);
            minus1000.setItemMeta(minus1000Meta);

            inv.setItem(5, plus1);
            inv.setItem(6, plus10);
            inv.setItem(7, plus100);
            inv.setItem(8, plus1000);
            inv.setItem(3, minus1);
            inv.setItem(2, minus10);
            inv.setItem(1, minus100);
            inv.setItem(0, minus1000);

            inv.setItem(4, item);

            inv.setItem(9, e.getView().getItem(9));
            inv.setItem(10, e.getView().getItem(10));
            inv.setItem(11, e.getView().getItem(11));
            inv.setItem(12, e.getView().getItem(12));
            inv.setItem(13, e.getView().getItem(13));
            inv.setItem(14, e.getView().getItem(14));
            inv.setItem(15, e.getView().getItem(15));
            inv.setItem(16, e.getView().getItem(16));
            inv.setItem(17, e.getView().getItem(17));

            p.openInventory(inv);

        }


        if (e.getView().getTitle().contains("SEND ITEM TO:")) {

            String receiver = e.getView().getTitle().split(":")[1].replace(" ", "");
            Player receiverplayer = Bukkit.getPlayer(receiver);
            String rec_UUID = Bukkit.getOfflinePlayer(receiver).getUniqueId().toString();
            ItemStack items[] = new ItemStack[7];

            if (e.getSlot() == 7) {
                for (int x = 0; x <= 6; x++) {
                    items[x] = e.getView().getItem(x);
                    if (items[x] != null) {
                        sqliteDb.insertPayout(rec_UUID, identify_item(items[x]), items[x].getAmount());
                        e.getView().setItem(x, null);  // <- remove item
                        p.sendMessage("Item: " + ChatColor.GOLD +  items[x].getType() + " [" + items[x].getAmount() + "] " + ChatColor.GREEN + "sent to " + receiver);
                        if(receiverplayer != null)
                            receiverplayer.sendMessage(p.getName() + " send you " + items[x].getType() + " [" + items[x].getAmount() + "] (look in your vault: ix /gui");
                    }
                }
                e.setCancelled(true);
            }

            else if (e.getSlot() == 8) {
                for (int x = 0; x <= 6; x++) {
                    if (items[x] != null) {
                        p.getInventory().addItem(items[x]);
                        e.getView().setItem(x, null);
                    }
                }
                e.getView().close();
                return;
            }

            //p.sendMessage("Slot: " + e.getSlot());
        }



        // SCAN ITEM IN CREATIVE FOR EXTRACTION OF ITEMS WITH CATEGORIES
        else {
            //getLogger().info(":" + itemcounter + ":ITEM:" + ItemexCommand.identify_item(p.getInventory().getItemInOffHand()));
            itemcounter++;
            if(itemcounter >= 9)
                itemcounter=0;
        }
        // END SCAN ITEMS

    }



}