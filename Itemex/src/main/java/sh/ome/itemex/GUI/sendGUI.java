package sh.ome.itemex.GUI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.Itemex;

import java.util.ArrayList;

public class sendGUI {


    public static void generateGUI(Player p, String title) {

        //int price = 0;

        Inventory inv = Bukkit.createInventory(null, 1*9, ChatColor.BLACK +  title);

        // CLOSE WINDOW
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + Itemex.language.getString("cs_close"));
        ArrayList<String> close_lore = new ArrayList<>();
        close_lore.add(ChatColor.WHITE + Itemex.language.getString("cs_close_m"));
        closeMeta.setLore(close_lore);
        close.setItemMeta(closeMeta);

        // SEND ITEM
        ItemStack send = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta sendMeta = send.getItemMeta();
        sendMeta.setDisplayName(ChatColor.GREEN + "Send");
        ArrayList<String> send_lore = new ArrayList<>();
        send_lore.add(ChatColor.WHITE + "click here to send items.");
        sendMeta.setLore(send_lore);
        send.setItemMeta(sendMeta);


        inv.setItem(7, send);
        inv.setItem(8, close);

        p.openInventory(inv);

    }
}
