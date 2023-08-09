package sh.ome.itemex.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.Itemex;

import java.util.ArrayList;

public class limit_setprice_GUI {

    static int price = 1;

    public static void generateGUI(Player p, String title, String itemname, int amount) {

        //int price = 0;

        Inventory inv = Bukkit.createInventory(null, 2*9, ChatColor.BLACK +  title);

        // Plus 1
        ItemStack plus1 = new ItemStack(Material.PAPER, 1);
        ItemMeta plus1Meta = plus1.getItemMeta();
        plus1Meta.setDisplayName(ChatColor.DARK_GREEN + "add 1");
        ArrayList<String> plus1_lore = new ArrayList<>();
        plus1_lore.add(ChatColor.GOLD + "[" + price +"]");
        //plus1_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ad_1_m"));
        plus1Meta.setLore(plus1_lore);
        plus1.setItemMeta(plus1Meta);

        // Plus 10
        ItemStack plus10 = new ItemStack(Material.PAPER, 10);
        ItemMeta plus10Meta = plus10.getItemMeta();
        plus10Meta.setDisplayName(ChatColor.DARK_GREEN + "add 10");
        ArrayList<String> plus10_lore = new ArrayList<>();
        plus10_lore.add(ChatColor.GOLD + "[" + price +"]");
        //plus10_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ad_64_m"));
        plus10Meta.setLore(plus10_lore);
        plus10.setItemMeta(plus10Meta);

        // Plus 100
        ItemStack plus100 = new ItemStack(Material.PAPER, 100);
        ItemMeta plus100Meta = plus10.getItemMeta();
        plus100Meta.setDisplayName(ChatColor.DARK_GREEN + "add 100");
        ArrayList<String> plus100_lore = new ArrayList<>();
        plus100_lore.add(ChatColor.GOLD + "[" + price +"]");
        //plus10_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ad_64_m"));
        plus100Meta.setLore(plus100_lore);
        plus100.setItemMeta(plus100Meta);

        // comma left
        ItemStack plus1000 = new ItemStack(Material.BOOK, 1);
        ItemMeta plus1000Meta = plus10.getItemMeta();
        plus1000Meta.setDisplayName(ChatColor.DARK_GREEN + "comma left");
        ArrayList<String> plus1000_lore = new ArrayList<>();
        plus1000_lore.add(ChatColor.GOLD + "[" + price +"]");
        //plus10_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_ad_64_m"));
        plus1000Meta.setLore(plus1000_lore);
        plus1000.setItemMeta(plus1000Meta);

        // CANCLE ORDER
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "CANCLE");
        ArrayList<String> close_lore = new ArrayList<>();
        close_lore.add(ChatColor.WHITE + "Click here to cancle the order");
        closeMeta.setLore(close_lore);
        close.setItemMeta(closeMeta);

        // CONFIRM ORDER
        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "CONFIRM");
        ArrayList<String> confirm_lore = new ArrayList<>();
        confirm_lore.add(ChatColor.WHITE + "Click here to confirm the order");
        confirmMeta.setLore(confirm_lore);
        confirm.setItemMeta(confirmMeta);

        // BACK
        ItemStack back = new ItemStack(Material.CROSSBOW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GOLD + "Back");
        ArrayList<String> back_lore = new ArrayList<>();
        back_lore.add(ChatColor.WHITE + "Click here to go back");
        backMeta.setLore(back_lore);
        back.setItemMeta(backMeta);


        // Minus 1
        ItemStack minus1 = new ItemStack(Material.PAPER, 1);
        ItemMeta minus1Meta = minus1.getItemMeta();
        minus1Meta.setDisplayName(ChatColor.DARK_RED + "sub 1");
        ArrayList<String> minus1_lore = new ArrayList<>();
        minus1_lore.add(ChatColor.GOLD + "[" + price +"]");
        //minus1_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_sub_1_m"));
        minus1Meta.setLore(minus1_lore);
        minus1.setItemMeta(minus1Meta);

        // Minus 10
        ItemStack minus10 = new ItemStack(Material.PAPER, 10);
        ItemMeta minus10Meta = minus10.getItemMeta();
        minus10Meta.setDisplayName(ChatColor.DARK_RED + "sub 10");
        ArrayList<String> minus10_lore = new ArrayList<>();
        minus10_lore.add(ChatColor.GOLD + "[" + price +"]");
        //minus10_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_sub_64_m"));
        minus10Meta.setLore(minus10_lore);
        minus10.setItemMeta(minus10Meta);

        // Minus 100
        ItemStack minus100 = new ItemStack(Material.PAPER, 100);
        ItemMeta minus100Meta = minus100.getItemMeta();
        minus100Meta.setDisplayName(ChatColor.DARK_RED + "sub 100");
        ArrayList<String> minus100_lore = new ArrayList<>();
        minus100_lore.add(ChatColor.GOLD + "[" + price +"]");
        //minus100_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_sub_64_m"));
        minus100Meta.setLore(minus100_lore);
        minus100.setItemMeta(minus100Meta);

        // comma right
        ItemStack minus1000 = new ItemStack(Material.BOOK, 1);
        ItemMeta minus1000Meta = minus1000.getItemMeta();
        minus1000Meta.setDisplayName(ChatColor.DARK_RED + "comma right");
        ArrayList<String> minus1000_lore = new ArrayList<>();
        minus1000_lore.add(ChatColor.GOLD + "[" + price +"]");
        //minus1000_lore.add(ChatColor.DARK_GRAY + Itemex.language.getString("cs_sub_64_m"));
        minus1000Meta.setLore(minus1000_lore);
        minus1000.setItemMeta(minus1000Meta);

        // ITEMID
        ItemStack item = ItemexCommand.constructItem( ItemexCommand.get_json_from_meta(itemname), amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + itemname);
        ArrayList<String> item_lore = new ArrayList<>();
        item_lore.add(ChatColor.DARK_GRAY + "Amount: [" + amount + "]");
        item_lore.add(ChatColor.DARK_GRAY + "Price: 0");
        itemMeta.setLore(item_lore);
        item.setItemMeta(itemMeta);


        inv.setItem(5, plus1);
        inv.setItem(6, plus10);
        inv.setItem(7, plus100);
        inv.setItem(8, plus1000);
        inv.setItem(4, item);
        inv.setItem(3, minus1);
        inv.setItem(2, minus10);
        inv.setItem(1, minus100);
        inv.setItem(0, minus1000);

        inv.setItem(9, close);
        inv.setItem(10, close);
        inv.setItem(11, close);
        inv.setItem(12, close);
        inv.setItem(13, confirm);
        inv.setItem(14, confirm);
        inv.setItem(15, confirm);
        inv.setItem(16, confirm);
        inv.setItem(17, back);

        p.openInventory(inv);

    }
}
