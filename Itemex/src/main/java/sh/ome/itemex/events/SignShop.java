package sh.ome.itemex.events;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import sh.ome.itemex.Itemex;

import java.util.List;


public class SignShop implements Listener {


    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if(e.getLine(0).equalsIgnoreCase("[ix]")) {
            e.setLine(0, ChatColor.GREEN + "[ix]");
            e.setLine(2, ChatColor.WHITE + "<click>");
            e.getPlayer().setMetadata("shoptype", new FixedMetadataValue(Itemex.getPlugin(), "null"));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(e.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign)e.getClickedBlock().getState();
            if(sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[ix]")) {
                ItemStack heldItem = e.getPlayer().getItemInHand(); // Gegenstand in der Hand des Spielers
                if (heldItem != null && heldItem.getType() != Material.AIR) {
                    List<MetadataValue> metadata = e.getPlayer().getMetadata("shoptype");
                    if (metadata.isEmpty() || metadata.get(0).asString().equals("null")) {
                        e.getPlayer().setMetadata("shoptype", new FixedMetadataValue(Itemex.getPlugin(), heldItem.getType().toString()));
                        sign.setLine(2, ChatColor.WHITE + heldItem.getType().toString()); // Itemname auf das Schild setzen
                        sign.update(); // Änderungen am Schild speichern
                        e.getPlayer().sendMessage("SET: " + heldItem.getType());
                    }
                    else {
                        e.getPlayer().sendMessage("ISSET: " + e.getPlayer().getMetadata("shoptype").get(0).asString());

                        // Auf das Schild klicken führt den Befehl aus
                        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("RIGHT CLICK");
                            e.getPlayer().performCommand("ix sell " + heldItem.getType().toString() + " 1 market");
                        }
                        else if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                            e.getPlayer().sendMessage("LEFT CLICK");
                            e.getPlayer().performCommand("ix buy " + heldItem.getType().toString() + " 1 market");
                        }
                    }



                }
            }
        }
    }

}
