package sh.ome.itemex.events;

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
import org.bukkit.scheduler.BukkitRunnable;
import sh.ome.itemex.Itemex;

import java.sql.SQLException;
import java.util.List;

public class SignShop implements Listener {
    private static final String METADATA_KEY = "shoptype";
    private static final String PRICE_KEY = "priceupdate";

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if(e.getLine(0).equalsIgnoreCase("[ix]")) {
            e.setLine(0, ChatColor.GREEN + "[ix]");
            e.setLine(2, ChatColor.WHITE + "<click>");
            e.setLine(3, ChatColor.WHITE + "<to enable>");
            e.getBlock().setMetadata(METADATA_KEY, new FixedMetadataValue(Itemex.getPlugin(), "null"));
            e.getBlock().setMetadata(PRICE_KEY, new FixedMetadataValue(Itemex.getPlugin(), false));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(e.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            if(sign.getLine(0).contains(ChatColor.GREEN + "[ix]")) {
                List<MetadataValue> metadataList = sign.getBlock().getMetadata(METADATA_KEY);
                if (metadataList.isEmpty()) {
                    String[] firstLineParts = sign.getLine(0).split(" ");
                    if (firstLineParts.length > 1) {
                        String shopType = firstLineParts[1].substring(2); // Entferne die Farbcodes
                        sign.getBlock().setMetadata(METADATA_KEY, new FixedMetadataValue(Itemex.getPlugin(), shopType));
                        sign.getBlock().setMetadata(PRICE_KEY, new FixedMetadataValue(Itemex.getPlugin(), false)); // Standardmäßig ist der Preis nicht aktiv
                        e.getPlayer().sendMessage("sign reloaded!");
                        return;
                    }
                }
                String shopType = sign.getBlock().getMetadata(METADATA_KEY).get(0).asString();
                boolean isPriceActive = sign.getBlock().getMetadata(PRICE_KEY).get(0).asBoolean();

                if (shopType.equals("null")) {
                    ItemStack heldItem = e.getPlayer().getItemInHand(); // Gegenstand in der Hand des Spielers
                    if (heldItem != null && heldItem.getType() != Material.AIR) {
                        sign.getBlock().setMetadata(METADATA_KEY, new FixedMetadataValue(Itemex.getPlugin(), heldItem.getType().toString()));
                        sign.setLine(0, ChatColor.GREEN + "[ix] " + ChatColor.GOLD + heldItem.getType().toString()); // Itemname auf das Schild setzen
                        sign.setLine(2, ChatColor.WHITE + heldItem.getType().toString()); // Itemname auf das Schild setzen
                        sign.setLine(3, ChatColor.WHITE + "<click>"); // Reset line 4
                        sign.update(); // Änderungen am Schild speichern
                        e.getPlayer().sendMessage(heldItem.getType() + " market-sign created successfully!");
                    }
                } else {
                    //e.getPlayer().sendMessage("ISSET: " + shopType);

                    if(isPriceActive){
                        // Auf das Schild klicken führt den Befehl aus
                        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                            e.setCancelled(true);
                            if (e.getPlayer().isSneaking()) {
                                e.getPlayer().performCommand("ix sell " + shopType + " 1 market");
                            }
                            else
                                e.getPlayer().performCommand("ix sell " + shopType + " 1 market confirm");

                        }
                        else if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                            if (e.getPlayer().isSneaking()) {
                                e.getPlayer().performCommand("ix buy " + shopType + " 1 market");
                                //e.getPlayer().sendMessage("LEFT + SHIFT");
                                //e.getClickedBlock().breakNaturally(); // Zerstöre das Schild
                            }
                            else {
                                e.getPlayer().performCommand("ix buy " + shopType + " 1 market confirm");
                                //e.getPlayer().sendMessage("LEFT");
                            }
                        }
                    } else {
                        double best_sellorder = Itemex.getPlugin().mtop.get(shopType).get_top_sellorder_prices()[0];
                        double best_buyorder = Itemex.getPlugin().mtop.get(shopType).get_top_buyorder_prices()[0];
                        sign.setLine(1, ChatColor.RED + "Sell: " + ChatColor.WHITE + best_buyorder); // Buy price moved to line 2
                        sign.setLine(2, ChatColor.GREEN + "Buy: " + ChatColor.WHITE + best_sellorder); // Sell price moved to line 3
                        sign.getBlock().setMetadata(PRICE_KEY, new FixedMetadataValue(Itemex.getPlugin(), true));
                        sign.update();

                        new BukkitRunnable() {
                            int countdown = 10; // 10 seconds countdown

                            @Override
                            public void run() {
                                if(countdown >= 0) {
                                    sign.setLine(3, ChatColor.WHITE + "Countdown: " + countdown); // Countdown
                                    sign.update();
                                    countdown--;
                                } else {
                                    sign.setLine(1, ChatColor.WHITE + " ");
                                    sign.setLine(2, ChatColor.WHITE + "<click>");
                                    sign.setLine(3, ChatColor.WHITE + "to enable");
                                    sign.update();
                                    sign.getBlock().setMetadata(PRICE_KEY, new FixedMetadataValue(Itemex.getPlugin(), false));
                                    sign.update();
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Itemex.getPlugin(), 0L, 20L); // 1-second tick interval for the countdown
                    }
                }
            }
        }
    }
}
