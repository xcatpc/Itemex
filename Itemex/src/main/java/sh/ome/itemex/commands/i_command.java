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

            if(args.length == 0)
                i_GUI.generateGUI(p, "ITEMEX - BUY ITEM", 0, 0);

            else {
                if (args[0].equals("gui")) {
                    i_GUI.generateGUI(p, "ITEMEX - BUY ITEM", 0, 0);
                }
                else {
                    p.sendMessage("/i This command is not implemented!");
                }
            }


        }
        return false;
    }
}
