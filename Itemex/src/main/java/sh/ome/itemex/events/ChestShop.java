package sh.ome.itemex.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.commands.ix_command;
import sh.ome.itemex.functions.sqliteDb;
import static org.bukkit.Bukkit.getLogger;
import static sh.ome.itemex.commands.commands.get_meta;
import static sh.ome.itemex.commands.commands.identify_item;

public class ChestShop implements Listener {
    /*

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
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
                String temp[] = get_content(contents).split(":"); // slot_counter + ":" + full_item_slots + ":" + stackable_amount + ":" + first_item_json + ":" + first_item_amount;
                String first_item_from_chest = temp[3] + ":" + temp[4]; //item json
                getLogger().info("# DEBUG ixc: " + get_content(contents));


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
                    ix_command.Order sell_order = new ix_command.Order();
                    sell_order.amount = Integer.parseInt(temp[5]);
                    sell_order.uuid = event.getPlayer().getUniqueId().toString();
                    sell_order.itemid = first_item_from_chest;
                    sell_order.ordertype = "sell:limit:chest";
                    sell_order.price = Double.parseDouble( event.getLine(1).split(":")[1] );

                    sqliteDb db_sell_order = new sqliteDb(sell_order);
                    int sellorder_id = db_sell_order.createSellOrder();
                    if( sellorder_id == -1) {
                        id_line = id_line + "S:" + -1;
                    }
                    else
                        id_line = id_line + "S:" + sellorder_id;


                    // CREATE BUY ORDER
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
                    int buyorder_id = db_buy_order.createBuyOrder();
                    if( buyorder_id == -1) {
                        id_line = id_line + "B:" + -1;
                    }
                    else {
                        id_line = id_line + "B:" + buyorder_id;
                        if(!sqliteDb.updateOrder("SELLORDERS", sellorder_id, sell_order.amount,  sell_order.price, "buy:limit:chest:" + buyorder_id, first_item_from_chest)) {
                            getLogger().info("ERROR - updating sellorder: " + sellorder_id + " at ChestShop!");
                            block.breakNaturally();
                        }
                    }

                    event.setLine(0, ChatColor.GREEN + "[ixc] " + ChatColor.RESET + get_meta(first_item_from_chest));
                    event.setLine(1, ChatColor.RED + "BUY: " + ChatColor.WHITE + event.getLine(1));
                    event.setLine(2, ChatColor.GREEN + "SELL: " + ChatColor.WHITE + event.getLine(2));
                    event.setLine(3, ChatColor.BLACK + id_line );
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + Itemex.language.getString("chestshop_instruction"));
                    event.getBlock().breakNaturally();
                }


            }
            else // remove sign
                event.getBlock().breakNaturally();
        }



        // Check if the first line contains a registred shop

    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            Block block = chest.getBlock();
            if (hasIXCSign(block)) {
                ItemStack[] contents = chest.getInventory().getContents();
                //print_content("single", contents);
                getLogger().info("# DEBUG (onInventoryOpen): " + get_content(contents));
            }
        } else if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            Chest left = (Chest) doubleChest.getLeftSide();
            Chest right = (Chest) doubleChest.getRightSide();
            if (hasIXCSign(left.getBlock()) || hasIXCSign(right.getBlock())) {
                ItemStack[] contents = doubleChest.getInventory().getContents();
                //print_content("double", contents);
                getLogger().info("# DEBUG (onInventoryOpen): " + get_content(contents));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Block block = null;
        ItemStack[] contents = new ItemStack[0];
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
            if(hasIXCSign(right.getBlock())) {
                block = right.getBlock();
                contents = doubleChest.getInventory().getContents();
                f_inventroy_close(contents, event, block);
            }
            else
                return;
        } // end double chest

    }

    private boolean f_inventroy_close(ItemStack[] contents, InventoryCloseEvent event, Block block) {
        String cheast_content[] = get_content(contents).split(":");
        String all_lines[] = get_all_lines(block);
        sendOrdersizeToPlayer(cheast_content, event);
        update_dbs(cheast_content, all_lines);
        return true;
    }

    private boolean update_dbs(String[] cheast_content, String[] all_lines) {
        int sellorder_id = Integer.parseInt( all_lines[3].split(":")[1].replace("B", "") );
        int buyorder_id = Integer.parseInt( all_lines[3].split(":")[2] );
        String item_json = cheast_content[3] + ":" + cheast_content[4];
        int amount = Integer.parseInt( cheast_content[5] );
        int totalSlots = Integer.parseInt(cheast_content[0]);
        int max_stacks = Integer.parseInt(cheast_content[2]);
        int full_item_slots = Integer.parseInt(cheast_content[1]);

        boolean s_status = sqliteDb.updateOrder("SELLORDERS", sellorder_id, amount, -1, "", item_json); //-1 means no change in price
        boolean b_status = sqliteDb.updateOrder("BUYORDERS", buyorder_id, ((totalSlots * max_stacks) + max_stacks - (amount + full_item_slots * max_stacks)-64), -1, "", item_json);

        getLogger().info("status: " + s_status + ":" + b_status);
        return true;
    }



    private void sendOrdersizeToPlayer(String[] temp, InventoryCloseEvent event) { // temp = slot_counter + ":" + full_item_slots + ":" + stackable_amount + ":" + first_item_json + ":" + first_item_amount;
        int totalSlots = Integer.parseInt(temp[0]);
        int max_stacks = Integer.parseInt(temp[2]);
        int amount = Integer.parseInt(temp[5]);
        int full_item_slots = Integer.parseInt(temp[1]);
        event.getPlayer().sendMessage("Buy Order amount: " + ((totalSlots * max_stacks) + max_stacks - (amount + full_item_slots * max_stacks)-64));
        event.getPlayer().sendMessage("Sell Order amount: " + amount);
    }




    @EventHandler
    public void onHopperMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof Hopper || event.getDestination().getHolder() instanceof Hopper) {
            // Hier kannst du die gewünschten Aktionen durchführen, wenn ein Gegenstand von oder in einen Trichter verschoben wird.
            // Du kannst auf das Quell- und Zielinventar über event.getSource() und event.getDestination() zugreifen.
        }
    }




    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
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


    private Chest getConnectedChest(Chest chest) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = chest.getBlock().getRelative(face);

            if (relativeBlock.getType().equals(chest.getBlock().getType())) {
                return (Chest) relativeBlock.getState();
            }
        }

        return null;
    }


    private String get_content(ItemStack[] contents) {
        // Print the contents of the chest
        int slot_counter = 0;
        int empty_slots = 0;
        int full_item_slots = 0;
        int first_item_amount = 0;
        int stackable_amount = 0;
        String first_item_json = "";

        for (ItemStack item : contents) {
            if (item != null) {
                if(item.getType().toString().equals("AIR"))
                    empty_slots++;
                else {
                    // set the itemid
                    if(first_item_amount == 0) {
                        first_item_json = identify_item(item);
                        first_item_amount = item.getAmount();
                        stackable_amount = item.getType().getMaxStackSize();
                    }

                    else if(identify_item(item).equals(first_item_json))
                        first_item_amount = first_item_amount + item.getAmount();
                    else
                        full_item_slots++;
                }
            }
            slot_counter++;
        }
        return slot_counter + ":" + full_item_slots + ":" + stackable_amount + ":" + first_item_json + ":" + first_item_amount;
    }




    private boolean hasIXCSign(Block block) {
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN }) {
            Block relativeBlock = block.getRelative(face);
            if (relativeBlock.getState() instanceof Sign) {
                Sign sign = (Sign) relativeBlock.getState();
                if (sign.getLine(0).contains("[ixc]")) {
                    getLogger().info("SIGN FOUND");
                    return true;
                }
            }
        }
        return false;
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



*/
}
