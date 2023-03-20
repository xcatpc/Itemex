package sh.ome.itemex.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import sh.ome.itemex.commands.GUI;

public class ClickGUI implements Listener {
    @EventHandler
    public void clickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if(e.getView().getTitle().contains("ITEMEX")) {                          // If player click on Itemex GUI
            e.setCancelled(true);
            if( e.getClick().isLeftClick() || e.getClick().isRightClick() ) {
                p.sendMessage(e.getCurrentItem().getItemMeta().getDisplayName() + " Slot: " + e.getSlot());

                //click slot evaluation
                if(e.getSlot() == 8) {  //CLOSE
                    e.getView().close();
                }
                else if(e.getSlot() == 0) { //MARKET
                    GUI.generateGUI(p, 0);
                }
                else if(e.getSlot() == 1) { //LIMIT
                    GUI.generateGUI(p, 1);
                }
                else if(e.getSlot() == 2) { //ORDER BOOK
                    GUI.generateGUI(p, 2);
                }
                else if(e.getSlot() == 3) { //ORDER BOOK
                    GUI.generateGUI(p, 3);
                }
                else if(e.getSlot() == 6) { //VAULT
                    GUI.generateGUI(p, 6);
                }
                else if(e.getSlot() == 7) { //HELP
                    e.getView().close();
                    p.sendMessage("LINK: Not done yet, please be patient.");
                }


            } // end right or left click
        }
    }
}
