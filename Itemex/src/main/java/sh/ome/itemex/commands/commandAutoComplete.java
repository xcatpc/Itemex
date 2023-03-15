package sh.ome.itemex.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class commandAutoComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            List<String> options = Arrays.asList("price", "buy", "sell", "whatIsInMyRightHand", "withdraw", "gui", "order");
            return options;
        }
        if(args[0].equals("buy") || args[0].equals("sell")) {

            if(args.length == 2) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                if( args[0].equals("buy") ) {
                    for (Material mat : Material.values()) {
                        materialNames.add(mat.name().toLowerCase());
                    }
                    return materialNames;
                }
                else if( args[0].equals("sell") ) {
                    // ADD ONLY MATERIAL WHAT PLAYER HAVE IN INVENTORY
                    Player p = Bukkit.getPlayer(sender.getName());
                    materialNames.add("- list of what you have in inventory");
                    for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                        if(item != null && !item.getType().toString().equals("AIR") ) { // add all items if not AIR
                            materialNames.add(item.getType().toString());
                        }
                    }
                    return materialNames;
                } // end sell
            }

            else if(args.length == 3) {
                List<String> options = null;
                if( args[0].equals("buy") )
                    options = Arrays.asList("1", "16", "32", "64", "1024", "2048");
                else
                    options = Arrays.asList("1", "16", "32", "64", "1024", "max");

                return options;
            }
            else if(args.length == 4) {
                List<String> options = Arrays.asList("limit", "market");
                return options;
            }
            else if(args.length == 5) {
                List<String> options = Arrays.asList("10", "20", "30", "500", "5000", "1000000");
                return options;
            }
            else if(args.length == 6)
                return Arrays.asList("");


        } // end if buy or sell
        else if(args[0].equals("withdraw")) {
            if(args.length == 2) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                materialNames.add("_list");
                for (Material mat : Material.values()) {
                    materialNames.add(mat.name().toLowerCase());
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

        else if(args[0].equals("price")) {
            if(args.length == 2) {
                List<String> materialNames = new ArrayList<>(Material.values().length);
                materialNames.add("- let empty for what you hold in hand");
                for (Material mat : Material.values()) {
                    materialNames.add(mat.name().toLowerCase());
                }
                return materialNames;
            }
            else if(args.length == 3) {
                List<String> options = Arrays.asList("");
                return options;
            }
        } // end price

        else if(args[0].equals("order")) {
            List<String> options = Arrays.asList("list", "close");
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
                materialNames.add("- let empty for all");
                for (Material mat : Material.values()) {
                    materialNames.add(mat.name().toLowerCase());
                }
                return materialNames;
            }
            else if(args.length == 5)
                return  Arrays.asList("");


        } //end order


        else if(args[0].equals("gui"))
            return Arrays.asList("");
        else if(args[0].equals("whatIsInMyRightHand"))
            return Arrays.asList("");


            return null;
    }
}
