package sh.ome.itemex.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.ix_command;
import sh.ome.itemex.functions.sqliteDb;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getLogger;
import static sh.ome.itemex.commands.commands.*;

public class ChestShop implements Listener {

    private Map<String, String> chestOwners = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && clickedBlock.getState() instanceof Sign) {
            Sign sign = (Sign) clickedBlock.getState();
            String[] lines = sign.getLines();
            if(lines[0].contains("§a[ixc]")) {
                // if owner not in ram - get it from db
                if (!chestOwners.containsKey(lines[3])) {
                    String buyorder_id = lines[3].split(":")[2];
                    sqliteDb.OrderBuffer ob = sqliteDb.getOrder(buyorder_id, true);
                    chestOwners.put(lines[3], ob.uuid);
                }

                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if(chestOwners.get(lines[3]).equals(player.getUniqueId().toString())) //if owner
                        player.sendMessage("You can delete your ChestShop by breaking the sign!");
                    else
                        event.setCancelled(true);

                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if(chestOwners.get(lines[3]).equals(player.getUniqueId().toString())) { //if owner
                        player.sendMessage("You are the owner.\nYou control the buyorder by putting in other items as placeholder.");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if(Itemex.chestshop) {
            Block block = event.getBlock();

            // Check if the first line on the sign is [ixc]
            if (event.getLine(0).equals("[ixc]")) {

                // Check the attached block to the sign
                Block attachedBlock = null;

                // Check for each possible attached block face
                for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN }) {
                    if (block.getRelative(face).getType() == Material.CHEST || block.getRelative(face).getType() == Material.TRAPPED_CHEST) {
                        attachedBlock = block.getRelative(face);
                        break;
                    }
                }

                if (attachedBlock != null) {
                    Chest chest = (Chest) attachedBlock.getState();
                    ItemStack[] contents = chest.getInventory().getContents();
                    String item_json ="";
                    for (ItemStack item : contents) {
                        if(item != null) {
                            item_json = identify_item(item);
                        }
                    }
                    String temp[] = get_content(contents, item_json).split(":");
                    String first_item_from_chest = temp[3] + ":" + temp[4]; //item json


                    // if no items inside the chest
                    if(temp[2].equals("0")) {
                        event.getPlayer().sendMessage("You have to put some item into the chest");
                        event.getBlock().breakNaturally();
                        return;
                    }

                    // Check if the sign is set up correctly
                    if (event.getLine(1).contains("S:") && event.getLine(2).contains("B:")) {
                        String id_line = "";

                        // CREATE SELL ORDER
                        //getLogger().info("# DEBUG: create chestshop SELL ORDER");
                        ix_command.Order sell_order = new ix_command.Order();
                        sell_order.amount = 0; // block sell order first
                        sell_order.uuid = event.getPlayer().getUniqueId().toString();
                        sell_order.itemid = first_item_from_chest;
                        sell_order.ordertype = "sell:limit:chest";
                        sell_order.price = Double.parseDouble( event.getLine(1).split(":")[1] );

                        sqliteDb db_sell_order = new sqliteDb(sell_order);
                        sell_order.amount = Integer.parseInt(temp[5]);
                        int sellorder_id = db_sell_order.createSellOrder(false);
                        if( sellorder_id == -1) {
                            id_line = id_line + "S:" + -1;
                        }
                        else
                            id_line = id_line + "S:" + sellorder_id;


                        // CREATE BUY ORDER
                        //getLogger().info("# DEBUG: create chestshop BUY ORDER");
                        ix_command.Order buy_order = new ix_command.Order();

                        int totalSlots = Integer.parseInt(temp[0]);
                        int max_stacks = Integer.parseInt(temp[2]);
                        int amount = Integer.parseInt(temp[5]);
                        int full_item_slots = Integer.parseInt(temp[1]);

                        buy_order.amount = ((totalSlots * max_stacks) + max_stacks - (amount + full_item_slots * max_stacks));
                        buy_order.uuid = event.getPlayer().getUniqueId().toString();
                        buy_order.itemid = first_item_from_chest;
                        buy_order.ordertype = "buy:limit:chest:" + sellorder_id;
                        buy_order.price = Double.parseDouble( event.getLine(2).split(":")[1] );

                        sqliteDb db_buy_order = new sqliteDb(buy_order);
                        int buyorder_id = db_buy_order.createBuyOrder(false);
                        if( buyorder_id == -1) {
                            id_line = id_line + "B:" + -1;
                        }
                        else {
                            id_line = id_line + "B:" + buyorder_id;
                            // UPDATE SELL ORDER
                            //getLogger().info("# DEBUG: update chestshop SELL ORDER");
                            if(!sqliteDb.updateOrder("SELLORDERS", sellorder_id, sell_order.amount,  sell_order.price, "sell:limit:chest:" + buyorder_id, first_item_from_chest)) {
                                //getLogger().info("ERROR - updating sellorder: " + sellorder_id + " at ChestShop!");
                                block.breakNaturally();
                            }
                            else
                                sqliteDb.loadBestOrdersToRam(buy_order.itemid, true); // check if there is a matching order on the market
                        }

                        event.setLine(0, ChatColor.GREEN + "[ixc] " + ChatColor.RESET + get_meta(first_item_from_chest));
                        event.setLine(1, ChatColor.RED + "BUY: " + ChatColor.WHITE + event.getLine(1));
                        event.setLine(2, ChatColor.GREEN + "SELL: " + ChatColor.WHITE + event.getLine(2));
                        event.setLine(3, ChatColor.BLACK + id_line );
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + Itemex.language.getString("t"));
                        event.getBlock().breakNaturally();
                    }
                }
                else // remove sign
                    event.getBlock().breakNaturally();
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(Itemex.chestshop) {

            InventoryHolder holder = event.getInventory().getHolder();
            if (holder instanceof Chest) {
                Chest chest = (Chest) holder;
                if (hasIXCSign(chest.getBlock())) {
                    String[] lines = getIXCSign(chest.getBlock());
                    // if owner not in ram - get it from db
                    if (!chestOwners.containsKey(lines[3])) {
                        String buyorder_id = lines[3].split(":")[2];
                        sqliteDb.OrderBuffer ob = sqliteDb.getOrder(buyorder_id, true);
                        chestOwners.put(lines[3], ob.uuid);
                    }
                    removeItemsFromChest(chest.getInventory(), lines[0]); // remove all listed items from chest
                    if(chestOwners.get(lines[3]).equals(event.getPlayer().getUniqueId().toString())) { // if owner
                        insertItemsFromDB(chest.getInventory(), lines[3]);
                        blockChestOrder(lines[3]);
                    }
                    else {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Itemex.language.getString("chestshop_not_your_chest"));
                    }

                }
            }

            else if (holder instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) holder;
                Chest left = (Chest) doubleChest.getLeftSide();
                Chest right = (Chest) doubleChest.getRightSide();
                String[] lines;
                if (hasIXCSign(left.getBlock()) || hasIXCSign(right.getBlock())) {
                    if(hasIXCSign(left.getBlock()))
                        lines = getIXCSign(left.getBlock());
                    else
                        lines = getIXCSign(right.getBlock());

                    // if owner not in ram - get it from db
                    if (!chestOwners.containsKey(lines[3])) {
                        String buyorder_id = lines[3].split(":")[2];
                        sqliteDb.OrderBuffer ob = sqliteDb.getOrder(buyorder_id, true);
                        chestOwners.put(lines[3], ob.uuid);
                    }

                    removeItemsFromChest(doubleChest.getInventory(), lines[0]); // remove all listed items from chest
                    if(chestOwners.get(lines[3]).equals(event.getPlayer().getUniqueId().toString())) {  // if owner
                        insertItemsFromDB(doubleChest.getInventory(), lines[3]);
                        blockChestOrder(lines[3]);
                    }
                    else {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Itemex.language.getString("chestshop_not_your_chest"));
                    }

                }
            }
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(Itemex.chestshop) {
            InventoryHolder holder = event.getInventory().getHolder();
            Block block;
            ItemStack[] contents;
            if (holder instanceof Chest) {
                Chest chest = (Chest) holder;
                block = chest.getBlock();
                if (hasIXCSign(block)) {
                    contents = chest.getInventory().getContents();
                    f_inventroy_close(contents, event, block);
                }
            } else if (holder instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) holder;
                Chest left = (Chest) doubleChest.getLeftSide();
                Chest right = (Chest) doubleChest.getRightSide();

                if (hasIXCSign(left.getBlock())) {
                    block = left.getBlock();
                    contents = doubleChest.getInventory().getContents();
                    f_inventroy_close(contents, event, block);
                }
                else if(hasIXCSign(right.getBlock())) {
                    block = right.getBlock();
                    contents = doubleChest.getInventory().getContents();
                    f_inventroy_close(contents, event, block);
                }
                else
                    return;
            } // end double chest
        }
    }




    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(Itemex.chestshop) {
            Block block = event.getBlock();
            Material blockType = block.getType();

            if (blockType.equals(Material.CHEST) || blockType.equals(Material.TRAPPED_CHEST)) {
                Chest chest = (Chest) block.getState();
                Chest connectedChest = getConnectedChest(chest);

                if (hasIXCSign(block) || (connectedChest != null && hasIXCSign(connectedChest.getBlock()))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You have to remove the sign first!");
                }
            }
            else if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).contains("[ixc]")) {
                    String orderIds = sign.getLine(3); // S:123B:456
                    int sellorder_id = Integer.parseInt( orderIds.split(":")[1].replace("B", "") );
                    int buyorder_id = Integer.parseInt( orderIds.split(":")[2] );

                    // now delete the orders from the database
                    sqliteDb.closeOrder("SELLORDERS", sellorder_id,"" , "");
                    sqliteDb.closeOrder("BUYORDERS", buyorder_id, "", "");

                    event.getPlayer().sendMessage(ChatColor.GREEN + "ChestShop orders deleted.");
                }
            }
        }

    }



    @EventHandler
    public void onHopperMoveItem(InventoryMoveItemEvent event) {
        if (Itemex.chestshop && (event.getSource().getHolder() instanceof Hopper || event.getDestination().getHolder() instanceof Hopper)) {

            // Hier kannst du die gewünschten Aktionen durchführen, wenn ein Gegenstand von oder in einen Trichter verschoben wird.
            // Du kannst auf das Quell- und Zielinventar über event.getSource() und event.getDestination() zugreifen.
        }
    }












    private void removeItemsFromChest(Inventory inventory, String content) {
        String prefixToRemove = "§a[ixc] §r";
        String result = content.replace(prefixToRemove, "");
        String item_json = get_json_from_meta(result);
        //System.out.println("DEBUG REMOVE: " + item_json);
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if(item != null && identify_item(item).equals( item_json)) {
                contents[i] = null; // set slot to null to remove item
            }
        }
        inventory.setContents(contents);
    }

    private void insertItemsFromDB(Inventory inv, String id_line) {
        String[] tmp = id_line.split(":");
        sqliteDb.OrderBuffer sell_ob = sqliteDb.getOrder(tmp[1].replace("B", ""), false);
        //getLogger().info("# DEBUG: insertItemsFromDB: sell_ob.amount= " + sell_ob.amount);

        if(sell_ob != null) {
            ItemStack item = constructItem(sell_ob.itemid, sell_ob.amount);
            inv.addItem(item);
        }
    }

    private void blockChestOrder(String id_line) {
        String[] tmp = id_line.split(":");
        //getLogger().info("S: " + tmp[1].replace("B", ""));
        //getLogger().info("B: " + tmp[2]);
        //sqliteDb.OrderBuffer sell_ob = sqliteDb.getOrder(tmp[1].replace("B", ""), false);
        //getLogger().info("# DEBUG: insertItemsFromDB: sell_ob.amount= " + sell_ob.amount);

        sqliteDb.blockOrder("SELLORDERS", Integer.parseInt(tmp[1].replace("B", "")));
        sqliteDb.blockOrder("BUYORDERS", Integer.parseInt(tmp[2]));

    }

    private boolean f_inventroy_close(ItemStack[] contents, InventoryCloseEvent event, Block block) {
        String[] lines = getIXCSign(block);
        if(chestOwners.get(lines[3]).equals(event.getPlayer().getUniqueId().toString())) { // if owner
            String cheast_content[] = get_content(contents, get_all_lines(block)[0]).split(":");
            update_dbs(cheast_content, lines, (Player) event.getPlayer());
        }
        return true;
    }

    private boolean update_dbs(String[] cheast_content, String[] all_lines, Player p) {
        int sellorder_id = Integer.parseInt( all_lines[3].split(":")[1].replace("B", "") );
        int buyorder_id = Integer.parseInt( all_lines[3].split(":")[2] );

        String result = all_lines[0].replace("§a[ixc] §r", "");
        String item_json = get_json_from_meta(result);

        // cheast_content=  slot_counter + ":" + occupied_slot + ":" + stackable_amount + ":" + item_json + ":" + first_item_amount;
        int totalSlots = Integer.parseInt(cheast_content[0]);
        int occupiedSlots = Integer.parseInt(cheast_content[1]);
        int max_stacks = Integer.parseInt(cheast_content[2]);
        int amount = Integer.parseInt( cheast_content[5]);
        int buyorder_amount = (totalSlots-occupiedSlots) * max_stacks - amount;

        boolean s_status = sqliteDb.updateOrder("SELLORDERS", sellorder_id, amount, -1, "", item_json); //-1 means no change in price
        boolean b_status = sqliteDb.updateOrder("BUYORDERS", buyorder_id, buyorder_amount, -1, "", item_json);

        //getLogger().info("# DEBUG - status: " + s_status + ":" + b_status);
        p.sendMessage(ChatColor.DARK_RED + Itemex.language.getString("buyorder") + ChatColor.RESET + "[" + buyorder_amount + "]");
        p.sendMessage(ChatColor.DARK_GREEN + Itemex.language.getString("sellorder") + ChatColor.RESET + "[" + amount + "]");
        sqliteDb.loadBestOrdersToRam(item_json, true);
        return true;
    }




    private Chest getConnectedChest(Chest chest) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = chest.getBlock().getRelative(face);

            if (relativeBlock.getType().equals(chest.getBlock().getType())) {
                return (Chest) relativeBlock.getState();
            }
        }

        return null;
    }


    private String get_content(ItemStack[] contents, String item_json_raw) {
        // Print the contents of the chest
        int slot_counter = 0;
        int first_item_amount = 0;
        int occupied_slot = 0;
        String prefixToRemove = "§a[ixc] §r";
        String item_json;

        String result = item_json_raw.replace(prefixToRemove, "");
        if(result.contains("[{\"itemid\":"))
            item_json = result;
        else
            item_json = get_json_from_meta(result);
        int stackable_amount = constructItem(item_json, 1).getMaxStackSize();


        for (ItemStack item : contents) {
            if (item != null) {
                if(identify_item(item).equals(item_json)) {
                    first_item_amount = first_item_amount + item.getAmount();
                }
                else
                    occupied_slot++;
            }
            slot_counter++;
        }
        return slot_counter + ":" + occupied_slot + ":" + stackable_amount + ":" + item_json + ":" + first_item_amount;
    }




    private boolean hasIXCSign(Block block) {
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN }) {
            Block relativeBlock = block.getRelative(face);
            if (relativeBlock.getState() instanceof Sign) {
                Sign sign = (Sign) relativeBlock.getState();
                if (sign.getLine(0).contains("[ixc]")) {
                    //getLogger().info("SIGN FOUND");
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getIXCSign(Block chestBlock) {
        BlockFace[] adjacentFaces = new BlockFace[]{
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : adjacentFaces) {
            Block relativeBlock = chestBlock.getRelative(face);

            if (relativeBlock.getState() instanceof Sign) {
                Sign sign = (Sign) relativeBlock.getState();

                if (sign.getLine(0).contains("[ixc]")) {
                    //System.out.println("ixc sign found at getIXCSign");
                    //System.out.println("line3: " + sign.getLine(3));
                    return sign.getLines();
                }
            }
        }
        return null; // Wenn kein passendes Schild gefunden wurde
    }



    private String[] get_all_lines(Block block) {
        String[] tmp = new String[4];
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN }) {
            Block relativeBlock = block.getRelative(face);
            if (relativeBlock.getState() instanceof Sign) {
                Sign sign = (Sign) relativeBlock.getState();
                for(int i=0; i<=3; i++)
                    tmp[i] = sign.getLine(i);
                }
            }
        return tmp;
    }



}
