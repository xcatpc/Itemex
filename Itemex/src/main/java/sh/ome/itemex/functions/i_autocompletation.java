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
                List<String> options = Arrays.asList("gui");
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


/*


else if(command.getName().toString().equals("i") && ( args[0].equals("buy") || args[0].equals("sell") )) {
                if(args.length == 2) {
                    List<String> materialNames = new ArrayList<>(Material.values().length);
                    if( args[0].equals("buy") ) {
                        // GET CATEGORIES FROM CONFIG
                        for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                            String[] category = cat.split(":", 0);
                            // GET ITEMS
                            for (String item_json : CategoryFile.get().getStringList("categories." + category[0])) {
                                String itemName = get_meta(item_json);
                                if (args.length > 2 && itemName.toLowerCase().contains(args[1].toLowerCase())) { // Check without case sensitivity
                                    materialNames.add(itemName);

                                } else if (args.length == 2) { // if no filter is provided, add all items
                                    materialNames.add(itemName);
                                }
                            }
                        }
                        return materialNames;
                    }

                    else if( args[0].equals("sell") ) {
                        // ADD ONLY MATERIAL WHAT PLAYER HAVE IN INVENTORY
                        Player p = Bukkit.getPlayer(sender.getName());
                        materialNames.add("- list of what you have in inventory");
                        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                            if(item != null && !item.getType().toString().equals("AIR") ) { // add all items if not AIR
                                String json = identify_item(item);
                                String meta = get_meta(json);
                                if(!meta.equals("more_than_one_enchantment_not_supported")) {
                                    if (args.length > 1 && meta.toLowerCase().contains(args[1].toLowerCase())) { // Check without case sensitivity
                                        materialNames.add(meta);
                                    } else if (args.length == 1) { // if no filter is provided, add all items
                                        materialNames.add(meta);
                                    }
                                }
                            }
                        }
                        return materialNames;
                    } // end sell
                }


                else if(args.length == 3) {
                    List<String> options = null;
                    if( args[0].equals("buy") )
                        options = Arrays.asList("0_set_amount", "1", "16", "32", "64", "1024", "2048");
                    else {
                        String temp[] = getFreeInventory(Bukkit.getPlayer(sender.getName()), get_json_from_meta(args[1])).split(":");
                        options = Arrays.asList(temp[1]);
                    }
                    return options;
                }



                else if(args.length == 4) {
                    double best_order;
                    if(args[0].equals("sell"))
                        best_order = Itemex.getPlugin().mtop.get( get_json_from_meta(args[1]) ).get_top_sellorder_prices()[0];
                    else
                        best_order = Itemex.getPlugin().mtop.get( get_json_from_meta(args[1]) ).get_top_buyorder_prices()[0];

                    List<String> options = Arrays.asList(Double.toString(best_order), "0_best_marketprice");
                    return options;

                }
                else if(args.length == 5)
                    return Arrays.asList("");
            } // end if buy or sell



 */
