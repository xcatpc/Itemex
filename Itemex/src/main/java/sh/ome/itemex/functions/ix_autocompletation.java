package sh.ome.itemex.functions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.ix_command;
import sh.ome.itemex.files.CategoryFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sh.ome.itemex.commands.commands.*;
import static sh.ome.itemex.functions.sqliteDb.getPayout;

public class ix_autocompletation implements TabCompleter {

    private static final int MAX_BUFFER = 1000000000;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().toString().equals("ix") || command.getName().toString().equals("i")) {

            if (args.length == 1) {
                String input = args[0].toLowerCase();
                List<String> options;
                if(command.getName().toString().equals("ix"))
                    options = Arrays.asList("price", "buy", "sell", "stats", "whatIsInMyRightHand", "withdraw", "deposit", "gui", "order", "setting", "send");
                else {
                    options = Arrays.asList("price", "buy", "sell", "gui");
                }
                List<String> filteredOptions = new ArrayList<>();
                for (String option : options) {
                    if (option.startsWith(input))
                        filteredOptions.add(option);
                }
                return filteredOptions;
            }

            if(command.getName().toString().equals("ix") && ( args[0].equals("buy") || args[0].equals("sell") )) {
                if(args.length == 2) {
                    List<String> materialNames = new ArrayList<>(Material.values().length);
                    if( args[0].equals("buy") ) {
                        // GET CATEGORIES FROM CONFIG
                        for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                            String[] category = cat.split(":", 0);
                            // GET ITEMS
                            for (String item_json : CategoryFile.get().getStringList("categories." + category[0])) {
                                String itemName = get_meta(item_json);
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
                    List<String> options = Arrays.asList("limit", "market");
                    return options;
                }
                else if(args.length == 5) {
                    double best_order;
                    if(args[0].equals("sell"))
                        best_order = Itemex.getPlugin().mtop.get( get_json_from_meta(args[1]) ).get_top_sellorder_prices()[0];
                    else
                        best_order = Itemex.getPlugin().mtop.get( get_json_from_meta(args[1]) ).get_top_buyorder_prices()[0];

                    List<String> options = Arrays.asList("0_set_price", Double.toString(best_order));
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
                    materialNames.add("all");
                    for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                        if (item != null && !item.getType().toString().equals("AIR")) { // add all items if not AIR
                            String json = identify_item(item);
                            String meta = get_meta(json);
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


            else if(args[0].equals("send")) {
                if(args.length == 2) {
                    List<String> options = new ArrayList<>();
                    List<String> usernames = sqliteDb.getAllUsernamesFromSettings();
                    if(usernames != null) {
                        String filter = args[1].toLowerCase();
                        for (String username : usernames) {
                            if (username != null && username.toLowerCase().startsWith(filter)) {
                                options.add(username);
                            }
                        }
                    }
                    return options;
                }


                else if(args.length == 3) {
                    // ADD ONLY MATERIAL WHAT PLAYER HAVE IN INVENTORY
                    List<String> materialNames = new ArrayList<>(Material.values().length);
                    Player p = Bukkit.getPlayer(sender.getName());
                    materialNames.add("- list of what you have in inventory");
                    for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                        if(item != null && !item.getType().toString().equals("AIR") ) { // add all items if not AIR
                            String json = identify_item(item);
                            String meta = get_meta(json);
                            if(!meta.equals("more_than_one_enchantment_not_supported")) {
                                if (args.length > 1 && meta.toLowerCase().contains(args[2].toLowerCase())) { // Check without case sensitivity
                                    materialNames.add(meta);
                                } else if (args.length == 1) { // if no filter is provided, add all items
                                    materialNames.add(meta);
                                }
                            }
                        }
                    }
                    return materialNames;
                }
                else if(args.length == 4 ) {
                    String temp[] = getFreeInventory(Bukkit.getPlayer(sender.getName()), get_json_from_meta(args[2])).split(":");
                    List<String>options = Arrays.asList(temp[1]);
                    return options;
                }
                else {
                    List<String> options = Arrays.asList("");
                    return options;
                }
            } // end send



            else if(args[0].equals("stats")) {
                if(args.length == 2) {
                    String input = args[1].toLowerCase();
                    List<String> options = Arrays.asList("buyorders", "sellorders", "tradingvolume", "mylastsells", "mylastpurchases");
                    List<String> filteredOptions = new ArrayList<>();
                    for (String option : options) {
                        if (option.startsWith(input))
                            filteredOptions.add(option);
                    }
                    return filteredOptions;
                }

                else if(args.length == 3) {
                    List<String> options = Arrays.asList("0_set_page", "1", "2", "3", "4", "5", "6", "7", "8");
                    return options;
                }


            }


            else if(args[0].equals("price")) {
                if(args.length == 2) {
                    List<String> materialNames = new ArrayList<>(Material.values().length);
                    materialNames.add("- let empty for what you hold in hand");
                    // GET CATEGORIES FROM CONFIG
                    for (String cat : CategoryFile.get().getStringList("categories.CATEGORY_NAMES")) {
                        String[] category = cat.split(":", 0);
                        // GET ITEMS
                        for (String item_json : CategoryFile.get().getStringList("categories." + category[0])) {
                            String itemName = get_meta(item_json);
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
                            String itemName = get_meta(item_json);
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

        }
        return null;
    }
}
