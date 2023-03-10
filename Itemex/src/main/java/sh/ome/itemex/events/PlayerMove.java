package sh.ome.itemex.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if(p.hasPermission("Itemex.move")) {
            event.setCancelled(true);
        }
    }
}
