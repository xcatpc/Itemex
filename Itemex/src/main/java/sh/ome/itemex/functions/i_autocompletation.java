package sh.ome.itemex.functions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class i_autocompletation implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        // DO NOT USE THIS CLASS!!!!!!!!! Its a placeholder

        if (command.getName().toString().equals("i")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> options = Arrays.asList("price", "buy", "sell", "stats", "whatIsInMyRightHand", "withdraw", "deposit", "gui", "order", "setting", "send");
                List<String> filteredOptions = new ArrayList<>();
                for (String option : options) {
                    if (option.startsWith(input))
                        filteredOptions.add(option);
                }
                return filteredOptions;
            }
        }




        return null;
    }
}
