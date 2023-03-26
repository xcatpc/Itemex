package sh.ome.itemex.Listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sh.ome.itemex.functions.sqliteDb;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //System.out.println("# DEBUG - PLAYER JOINED THE SERVER");
        sqliteDb.Payout[] payouts;
        payouts= sqliteDb.getPayout(e.getPlayer().getUniqueId().toString());

        for (int i = 0; i < payouts.length; i++) {
            if(payouts[i] == null) { //skip empty entries
                break;
            }
            e.getPlayer().sendMessage("BUY ORDER" + ChatColor.GREEN+ " FULFILLED!" + ChatColor.WHITE + " You got [" + payouts[i].amount + "] "  + payouts[i].itemid );
            TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) You can withdraw with: /ix withdraw " + payouts[i].itemid +" " + payouts[i].amount);
            message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + payouts[i].itemid +" " + payouts[i].amount));
            e.getPlayer().spigot().sendMessage(message);
        }
    }
}
