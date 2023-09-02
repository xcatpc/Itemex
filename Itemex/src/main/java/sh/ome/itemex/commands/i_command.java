package sh.ome.itemex.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.ome.itemex.GUI.i_GUI;

import static sh.ome.itemex.commands.commands.print_help;

public class i_command implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("i")) {
            Player p = (Player) sender;
            p.sendMessage("i used");

            if (args[0].equals("help")) {
                p.sendMessage("help output");
            }
            else if (args[0].equals("gui")) {
                i_GUI.generateGUI(p, "ITEMEX - Buy Item", 0, 0);
            }
        }
        return false;
    }
}
