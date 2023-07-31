package sh.ome.itemex.functions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.files.CategoryFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sh.ome.itemex.commands.ItemexCommand.get_itemid;
import static sh.ome.itemex.commands.ItemexCommand.get_meta;
import static sh.ome.itemex.functions.sqliteDb.getPayout;

public class commandAutoComplete implements TabCompleter {

    private static final int MAX_BUFFER = 1000000000;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> options = Arrays.asList("price", "buy", "sell", "whatIsInMyRightHand", "withdraw", "deposit", "gui", "order", "setting");
            List<String> filteredOptions = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(input))
                    filteredOptions.add(option);
            }
            return filteredOptions;
        }

        if(args[0].equals("buy") || args[0].equals("sell")) {

            if(args.length == 2) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                if( args[0].equals("buy") ) {
                    // GET CATEGORIES FROM CONFIG
                    for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                        String[] category = cat.split(":", 0);
                        // GET ITEMS
                        for (String item_json : CategoryFile.get().getStringList("categories." + category[0])) {
                            String itemName = ItemexCommand.get_meta(item_json);
                            if (args.length > 1 && itemName.toLowerCase().contains(args[1].toLowerCase())) { // Check without case sensitivity
                                    materialNames.add(itemName);

                            } else if (args.length == 1) { // if no filter is provided, add all items
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
                            String json = ItemexCommand.identify_item(item);
                            String meta = ItemexCommand.get_meta(json);
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
                    options = Arrays.asList("0_set amount", "1", "16", "32", "64", "1024", "2048");
                else
                    options = Arrays.asList("0_set amount", "1", "16", "32", "64", "1024", "max");

                return options;
            }
            else if(args.length == 4) {
                List<String> options = Arrays.asList("limit", "market");
                return options;
            }
            else if(args.length == 5) {
                List<String> options = Arrays.asList("0_set price","10", "20", "30", "500", "5000", "1000000");
                if( args[3].equals("limit") )
                    return options;
                else {
                    List<String> options2 = Arrays.asList("preview","confirm");
                    return options2;
                }
            }
            else if(args.length == 6)
                return Arrays.asList("");


        } // end if buy or sell
        else if(args[0].equals("withdraw")) {
            if(args.length == 2) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                materialNames.add("_list");

                Player player = (Player) sender;
                String uuidString = player.getUniqueId().toString();
                sqliteDb.Payout[] buffer = getPayout( uuidString );

                String filter = args[1].toLowerCase(); // The second argument is the filter

                for (int i = 0; i < buffer.length; i++) {
                    if (buffer[i] != null) {
                        String itemName = get_itemid( buffer[i].itemid );
                        if(itemName.toLowerCase().contains(filter)) {
                            materialNames.add(get_meta(buffer[i].itemid)+ " " + buffer[i].amount);
                        }
                    }
                }
                return materialNames;
            }
            else if(args.length == 3 && (args[1].equals("_list") || args[1].equals("list")) ) {
                return  Arrays.asList("");
            }
            else if(args.length == 3) {
                List<String> options = Arrays.asList("max", "1", "16", "64", "512", "4096");
                return options;
            }
            if(args.length == 4)
                return Arrays.asList("");
        } // end withdraw

        else if(args[0].equals("deposit")) {
            if(args.length == 2) {
                // ADD ONLY MATERIAL WHAT PLAYER HAVE IN INVENTORY
                List<String> materialNames = new ArrayList<>(Material.values().length);
                Player p = Bukkit.getPlayer(sender.getName());
                materialNames.add("- list of what you have in inventory");
                for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                    if (item != null && !item.getType().toString().equals("AIR")) { // add all items if not AIR
                        String json = ItemexCommand.identify_item(item);
                        String meta = ItemexCommand.get_meta(json);
                        if (!meta.equals("more_than_one_enchantment_not_supported")) {
                            if (args.length > 1 && meta.toLowerCase().contains(args[1].toLowerCase())) { // Check without case sensitivity
                                materialNames.add(meta);
                            } else if (args.length == 1) { // if no filter is provided, add all items
                                materialNames.add(meta);
                            }
                        }
                    }
                }
                return materialNames;
            }
            else if(args.length == 3) {
                List<String> options = Arrays.asList("max", "1");
                return options;
            }
            else {
                List<String> options = Arrays.asList("");
                return options;
            }

        } // end deposit

        else if(args[0].equals("setting")) {
            if(args.length == 2) {
                List<String> options = Arrays.asList("withdraw_threshold");
                return options;
            }
            else if(args.length == 3 && args[1].equals("withdraw_threshold")) {
                List<String> options = Arrays.asList("0", "16", "64");
                return options;
            }
            else {
                List<String> options = Arrays.asList("");
                return options;
            }

        } // end setting


        else if(args[0].equals("price")) {
            if(args.length == 2) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                materialNames.add("- let empty for what you hold in hand");
                // GET CATEGORIES FROM CONFIG
                for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                    String[] category = cat.split(":", 0);
                    // GET ITEMS
                    for (String item_json : CategoryFile.get().getStringList("categories." + category[0])) {
                        String itemName = ItemexCommand.get_meta(item_json);
                        if (args.length > 1 && itemName.toLowerCase().contains(args[1].toLowerCase())) { // Check without case sensitivity
                            materialNames.add(itemName);

                        } else if (args.length == 1) { // if no filter is provided, add all items
                            materialNames.add(itemName);
                        }
                    }
                }
                return materialNames;
            }
            else if(args.length == 3) {
                List<String> options = Arrays.asList("-", "history");
                return options;
            }
            else if(args.length == 4) {
                List<String> options = Arrays.asList("0_max_entries", "1", "5", "10", "25");
                return options;
            }
            else if(args.length == 5) {
                List<String> options = Arrays.asList("");
                return options;
            }
        } // end price

        else if(args[0].equals("order")) {
            List<String> options = Arrays.asList("list", "close", "edit");
            if(args.length == 2) {
                return options;
            }
            else if(args.length == 3) {
                List<String> order_options = Arrays.asList("buyorders", "sellorders");
                return order_options;
            }

            else if(args.length == 4 && ( args[1].equals("edit") || args[1].equals("close")) ) {
                List<String> edit_options = Arrays.asList("<order id>");
                return edit_options;
            }

            else if(args.length == 4 && args[1].equals("list")) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                materialNames.add("- let empty for what you hold in hand");
                // GET CATEGORIES FROM CONFIG
                for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                    String[] category = cat.split(":", 0);
                    // GET ITEMS
                    for (String item_json : CategoryFile.get().getStringList("categories." + category[0])) {
                        String itemName = ItemexCommand.get_meta(item_json);
                        // Check without case sensitivity
                        if (args.length > 3 && itemName.toLowerCase().contains(args[3].toLowerCase())) {
                            materialNames.add(itemName);
                        } else if (args.length == 2) { // if no filter is provided, add all items
                            materialNames.add(itemName);
                        }
                    }
                }
                return materialNames;
            }

            else if(args.length == 5 && args[1].equals("edit")) {
                List<String> options_5 = Arrays.asList("0_set_new_price", "1", "100", "1000", "1000");
                return options_5;
            }
            else if(args.length == 6 && args[1].equals("edit")) {
                List<String> options_6 = Arrays.asList("0_set_new_amount", "1", "16", "64", "512");
                return options_6;
            }
            else if(args.length == 7 && args[1].equals("edit")) {
                return Arrays.asList("");
            }
            else if(args.length == 5)
                return Arrays.asList("");


        } //end order


        else if(args[0].equals("gui"))
            return Arrays.asList("");
        else if(args[0].equals("whatIsInMyRightHand"))
            return Arrays.asList("");


            return null;
    }
}
