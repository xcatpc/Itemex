package sh.ome.itemex.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.files.CategoryFile;

import java.util.ArrayList;

public class GUI {
    public static void generateGUI(Player p, String menu_type, int slot, int page) {
        //p.sendMessage("AT GENERATEGUI: " + menu_type);

        // -- MENU START -------------------------------------------------------

        // CREATE INVENTORY
        Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.BLACK +  menu_type);

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

        // Market orders
        ItemStack marketorders = new ItemStack(Material.COMPASS);
        ItemMeta marketordersMeta = marketorders.getItemMeta();
        marketordersMeta.setDisplayName(ChatColor.GOLD + Itemex.language.getString("cs_mo"));
        ArrayList<String> marketorders_lore = new ArrayList<>();
        marketorders_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_mo_m1"));
        marketorders_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_mo_m2"));
        marketorders_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_mo_m3"));
        marketordersMeta.setLore(marketorders_lore);
        marketorders.setItemMeta(marketordersMeta);

        // Limit orders
        ItemStack limitorders = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta limitordersMeta = limitorders.getItemMeta();
        limitordersMeta.setDisplayName(ChatColor.DARK_GREEN + Itemex.language.getString("cs_lo"));
        ArrayList<String> limitorders_lore = new ArrayList<>();
        limitorders_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_lo_m1"));
        limitorders_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_lo_m2"));
        limitorders_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_lo_m3"));
        limitordersMeta.setLore(limitorders_lore);
        limitorders.setItemMeta(limitordersMeta);

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
        right_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_click_r_m"));
        //right_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
        //right_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
        rightMeta.setLore(right_lore);
        right.setItemMeta(rightMeta);
        //LEFT
        ItemStack left = new ItemStack(Material.CROSSBOW);
        ItemMeta leftMeta = left.getItemMeta();
        leftMeta.setDisplayName(ChatColor.WHITE + Itemex.language.getString("cs_click_l"));
        ArrayList<String> left_lore = new ArrayList<>();
        left_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_click_l_m"));
        //left_lore.add(ChatColor.DARK_GRAY + "Withdraw your items once -");
        //left_lore.add(ChatColor.DARK_GRAY + "your buy order is fulfilled");
        leftMeta.setLore(left_lore);
        left.setItemMeta(leftMeta);


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
            category_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_click_cat"));
            category_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ycbasei"));
            categoryMeta.setLore(category_lore);
            category[cat_gui_type].setItemMeta(categoryMeta);
        }



        // PLACEHOLDER
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        // print placeholder:
        for(int inv_gui_type = 0; inv_gui_type <= 6*9-1; inv_gui_type++)
            inv.setItem(inv_gui_type, placeholder);


        // -- MENU END -------------------------------------------------------

            if(menu_type.contains("ITEMEX - Market Orders")) {


                    inv.setItem(9, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    // market activated
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



            } // END Market Orders


        inv.setItem(0, marketorders);
        inv.setItem(1, limitorders);
        inv.setItem(2, orderbook);
        inv.setItem(3, fastsell);

        inv.setItem(6, withdraw);
        inv.setItem(7, help);
        inv.setItem(8, close);

        p.openInventory(inv);
    }
}



