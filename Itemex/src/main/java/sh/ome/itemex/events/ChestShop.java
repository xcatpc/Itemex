package sh.ome.itemex.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.functions.sqliteDb;

import static sh.ome.itemex.functions.sqliteDb.updateOrder;

public class ChestShop implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getLine(0).contains("[ixc]")) {
            // Überprüfen, ob das Schild korrekt eingerichtet ist
            if (e.getLine(1).contains("S:") && e.getLine(2).contains("B:")) {
                e.setLine(0, ChatColor.GREEN + "[ixc]");
                e.setLine(1, ChatColor.RED + "BUY: " + ChatColor.WHITE + e.getLine(1));
                e.setLine(2, ChatColor.GREEN + "SELL: " + ChatColor.WHITE + e.getLine(2));
            } else {
                e.getPlayer().sendMessage(ChatColor.RED + "You can't open a limit order ChestShop because you have to follow the instructions:\nLINE 1: [ixc]\nLINE 2: S:<sellprice>\nLINE 3: B:<buyprice>");
                e.getBlock().breakNaturally();
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof Chest) {
            Player player = (Player) event.getPlayer();
            //player.sendMessage(ChatColor.GREEN + "You just opened a chest!");

            // remove all the content of the chest
            //inv.clear(); // removes all
            ItemStack main_item = new ItemStack(Material.GOLD_INGOT);
            inv.removeItem(main_item);

            // load sell chest orders from db

            // insert sell amount
            ItemStack goldBarren = new ItemStack(Material.GOLD_INGOT, 5);
            inv.addItem(goldBarren);

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            Block attachedBlock = chest.getBlock();
            updateSignIfAttachedChest(attachedBlock, (Player) e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            if (sign.getLine(0).contains(ChatColor.GREEN + "[ixc]")) {
                if (e.getAction().toString().contains("RIGHT")) {
                    e.getPlayer().sendMessage("RIGHT");
                } else if (e.getAction().toString().contains("LEFT")) {
                    e.getPlayer().sendMessage("LEFT");
                }
            }
        }
    }

    private void updateSignIfAttachedChest(Block block, Player p) {
        BlockState state = block.getState();
        String itemname = null;
        int totalAmount = 0;
        int freeSpace = 0;

        if (state instanceof Chest) {
            Chest chest = (Chest) state;
            Block signBlock = getSignBlockAttachedToChest(block);
            if (signBlock != null) {
                Sign sign = (Sign) signBlock.getState();
                if (sign.getLine(0).contains(ChatColor.GREEN + "[ixc]")) {
                    System.out.println("# DEBUG: LINE 0: " + sign.getLine(0));
                    Inventory inventory = chest.getInventory();
                    ItemStack[] contents = inventory.getContents();

                    for (ItemStack item : contents) {
                        if (item != null && !item.getType().toString().equals("AIR")) {
                            if(itemname == null) {
                                itemname = item.getType().toString();
                                System.out.println("# DEBUG: (first item of ixc) " + itemname);
                                sign.setLine(0, ChatColor.GREEN + "[ixc] " + ChatColor.GOLD + itemname);
                                sign.update();
                            }
                            if (item.getType().toString().equalsIgnoreCase(itemname)) {
                                totalAmount += item.getAmount();
                            }
                        }
                        //evaluate free space in stakes and items
                        else {
                            freeSpace++;
                        }
                    }

                    //if there are no items in the chest (itemname == null)
                    if( itemname == null)
                        itemname = sign.getLine(0).substring(10);

                    //create or update order
                    System.out.println("# DEBUG: " + itemname + " amount in chest: " + totalAmount);
                    System.out.println("# DEBUG: freespace slots: " + freeSpace);
                    p.sendMessage("Amount in chest: " + totalAmount);
                    p.sendMessage("Free slots: " + freeSpace);
                    Material item_material = Material.getMaterial(itemname.toUpperCase());
                    int stackSize = item_material.getMaxStackSize();
                    String[] buy_line = sign.getLine(1).split(":");
                    String[] sell_line = sign.getLine(2).split(":");

                    //update order
                    if( sign.getLine(3).contains("ID:") ) { // edit limit order
                        String[] parts = sign.getLine(3).split(":");
                        String buyorder_id = parts[2].substring(0, parts[2].length() - 1);
                        System.out.println("# DEBUG: BUYORDER ID: " + buyorder_id);
                        System.out.println("# DEBUG: SELLORDER ID: " + parts[3]);

                        if(!updateOrder("SELLORDERS", Integer.parseInt(parts[3]), totalAmount, Double.parseDouble( buy_line[2]), "buy:limit:chest:" + buyorder_id, itemname))
                            System.out.println("ERROR - updating sellorder: " + buyorder_id + " at ChestShop!");
                        if(!updateOrder("BUYORDERS", Integer.parseInt(buyorder_id), freeSpace * stackSize, Double.parseDouble( sell_line[2]), "buy:limit:chest:" + parts[3], itemname))
                            System.out.println("ERROR - updating buyorder: " + parts[3] + " at ChestShop!");

                    }
                    // creates new limit order
                    else {
                        //System.out.println("BUY_LINE: " + sign.getLine(1));
                        //System.out.println("SELL_LINE: " + sign.getLine(2));


                        String id_line = "ID:";
                        int buyorder_id = -1;
                        int sellorder_id = -1;

                        System.out.println("# DEBUG: buy_line[1]: " + buy_line[2]);
                        if(!buy_line[2].equals("0")) {
                            //create buy order: amount == free inventory space
                            ItemexCommand.Order order = new ItemexCommand.Order();
                            order.amount = freeSpace * stackSize;
                            order.uuid = p.getUniqueId().toString();
                            order.itemid = itemname;
                            order.ordertype = "buy:limit:chest";
                            order.price = Double.parseDouble( sell_line[2]);

                            sqliteDb db_order = new sqliteDb(order);
                            buyorder_id = db_order.createBuyOrder();
                            if( buyorder_id == -1) {
                                System.out.println("# DEBUG: order not created! failure at db operation");
                                id_line = id_line + "B:" + -1;
                            }
                            else
                                id_line = id_line + "B:" + buyorder_id;
                        }
                        else
                            id_line = id_line + "B:" + -1;


                        System.out.println("# DEBUG: sell_line[1]: " + sell_line[2]);
                        if(!sell_line[2].equals("0")) {
                            //create sell order: amount == full inventory items
                            ItemexCommand.Order order = new ItemexCommand.Order();

                            order.amount = totalAmount;
                            order.uuid = p.getUniqueId().toString();
                            order.itemid = itemname;
                            order.ordertype = "sell:limit:chest:" + buyorder_id;
                            order.price = Double.parseDouble( buy_line[2]);

                            sqliteDb db_order = new sqliteDb(order);
                            sellorder_id = db_order.createSellOrder();
                            if( sellorder_id == -1) {
                                System.out.println("# DEBUG: order not created! failure at db operation");
                                id_line = id_line + "S:" + -1;
                            }
                            else {
                                id_line = id_line + "S:" + sellorder_id;
                                if( buyorder_id != -1) {
                                    if(!updateOrder("BUYORDERS", buyorder_id, freeSpace * stackSize, Double.parseDouble( sell_line[2]), "buy:limit:chest:" + sellorder_id, itemname))
                                        System.out.println("ERROR - updating buyorder: " + buyorder_id + " at ChestShop!");
                                }
                            }
                        }
                        else {
                            id_line = id_line + "S:" + -1;
                            if(!updateOrder("BUYORDERS", buyorder_id, freeSpace * stackSize, Double.parseDouble( sell_line[2]), "buy:limit:chest:" + sellorder_id, itemname))
                                System.out.println("ERROR - updating buyorder: " + buyorder_id + " at ChestShop!");
                        }


                        sign.setLine(3, id_line); // ONLY FOR TEST
                        p.sendMessage("LINE SET");
                        sign.update();
                    }
                }
            }
        }
    }


    private Block getSignBlockAttachedToChest(Block chestBlock) {
        for (BlockFace face : BlockFace.values()) {
            Block attachedBlock = chestBlock.getRelative(face);
            BlockState state = attachedBlock.getState();
            if (state instanceof Sign) {
                return attachedBlock;
            }
        }
        return null;
    }
}
