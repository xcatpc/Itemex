package sh.ome.itemex.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.files.CategoryFile;

import java.util.ArrayList;

public class GUI {
    public static void generateGUI(Player p, int count) {
        String GUI_name = null;
        switch (count){
            case 0:
                GUI_name = "ITEMEX - MARKET";
                break;
            case 1:
                GUI_name = "ITEMEX - LIMIT";
                break;
            case 2:
                GUI_name = "ITEMEX - ORDER BOOK";
                break;
            case 3:
                GUI_name = "ITEMEX - FAST SELL";
                break;
            case 6:
                GUI_name = "ITEMEX - VAULT";
                break;

            default:
                GUI_name = "ITEMEX - MARKET";
        }
        
        Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.BLACK +  GUI_name);
        

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



        // CATEGORIES
        String[] cat_names = new String[10];
        int cat_name_count = 0;
        for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
            cat_names[cat_name_count] = cat;
            cat_name_count++;
        }

        ItemStack category[] = new ItemStack[15];
        for(int cat_count = 0; cat_count <= 9; cat_count++) {
            //split
            String cat_name[] = cat_names[cat_count].split(":", 0);
            category[cat_count] = new ItemStack(Material.getMaterial( cat_name[1]));
            ItemMeta categoryMeta = category[cat_count].getItemMeta();
            categoryMeta.setDisplayName(ChatColor.DARK_AQUA + cat_name[0]);
            ArrayList<String> category_lore = new ArrayList<>();
            category_lore.add(ChatColor.WHITE + "Click to choose a category");
            category_lore.add(ChatColor.DARK_GRAY + "- you can buy and sell each item");
            categoryMeta.setLore(category_lore);
            category[cat_count].setItemMeta(categoryMeta);
        }



        // PLACEHOLDER
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);


        // print placeholder:
        for(int inv_count = 0; inv_count <= 6*9-1; inv_count++)
            inv.setItem(inv_count, placeholder);

        // print categories
        if(count <=1) {
            int begin_position = 20;
            int cat_count = 0 + begin_position;
            for(int inv_count = 0; inv_count <= 6*9-1; inv_count++) {
                if(inv_count >= 20 && inv_count <=24) {
                    inv.setItem(cat_count, category[cat_count-20]);
                    cat_count++;
                }
                else if(inv_count >= 29 && inv_count <=33) {
                    inv.setItem(cat_count+4, category[cat_count-20]);
                    cat_count++;
                }
            }
        }


        if(count == 0)
            inv.setItem(9, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));    //market activated
        else if(count == 1)
            inv.setItem(10, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));   //market limit
        else if(count == 2)
            inv.setItem(11, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));   //order book
        else if(count == 3)
            inv.setItem(12, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));   //fast sell
        else if(count == 6)
            inv.setItem(15, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));   //vault

        p.sendMessage("COUTN: " + count);

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



