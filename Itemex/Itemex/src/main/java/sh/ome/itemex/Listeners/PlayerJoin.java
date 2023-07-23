package sh.ome.itemex.Listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.ItemexCommand;
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
            e.getPlayer().sendMessage("BUY ORDER" + ChatColor.GREEN + Itemex.language.getString("sq_fulfilled") + ChatColor.WHITE + " " + Itemex.language.getString("sq_you_got") + " [" + payouts[i].amount + "] "  + ItemexCommand.get_meta(payouts[i].itemid) );
            TextComponent message = new TextComponent(ChatColor.MAGIC + "X" + ChatColor.BLUE + "-> (" + ChatColor.GOLD + Itemex.language.getString("click_here") + ChatColor.BLUE + ") " + Itemex.language.getString("sq_you_can_with") + " /ix withdraw " + ItemexCommand.get_meta(payouts[i].itemid) +" " + payouts[i].amount);
            message.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ix withdraw " + ItemexCommand.get_meta(payouts[i].itemid) +" " + payouts[i].amount));
            e.getPlayer().spigot().sendMessage(message);
        }
    }
}
