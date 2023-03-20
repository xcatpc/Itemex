package sh.ome.itemex.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sh.ome.itemex.commands.sqliteDb;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //System.out.println("# DEBUG - PLAYER JOINED THE SERVER");
        sqliteDb.Payout[] payouts;
        payouts = new sqliteDb.Payout[1];
        payouts= sqliteDb.getPayout(e.getPlayer().getUniqueId().toString());


        for (int i = 0; i < payouts.length; i++) {
            if(payouts[i] == null) { //skip empty entries
                break;
            }
            e.getPlayer().sendMessage("BUY ORDER" + ChatColor.GREEN+ " FULFILLED!" + ChatColor.WHITE + " You got [" + payouts[i].amount + "] "  + payouts[i].itemid );
            e.getPlayer().sendMessage("You can" + ChatColor.GREEN+ " withdraw" + ChatColor.WHITE + " with the command: " + ChatColor.GREEN + "/ix withdraw " + payouts[i].itemid + " " + payouts[i].amount  );
        }
    }
}
