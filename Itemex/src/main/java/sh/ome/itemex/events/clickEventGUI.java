package sh.ome.itemex.events;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.commands.sqliteDb;

import java.util.ArrayList;

import static java.lang.Float.parseFloat;

public class clickEventGUI implements Listener {
    @EventHandler
    public void clickEvent(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if(e.getView().getTitle().equalsIgnoreCase(ChatColor.BLACK + "ITEMEX")) {
            e.setCancelled(true);
            String s_amount = e.getView().getItem(4).getItemMeta().getLore().get(0);        // get the amount of slot 4
            int amount = Integer.parseInt( s_amount.substring(2) );                    // removes the color of the text
            String s_page = e.getView().getItem(0).getItemMeta().getLore().get(0);          // get the amount of slot 0 (PREV)
            int page = Integer.parseInt( s_page.substring(2) );                        // removes the color of the text

            // if user click on slot nr 4 (Item to buy or sell)
            if(e.getSlot() == 4) {
                String itemid = e.getCurrentItem().getType().toString();
                String[] price = get_price( itemid );

                if( e.getClick().isLeftClick() ) {
                    if( !price[1].equals("-") ) {
                        float s_price = parseFloat(price[1]);
                        ItemexCommand.create_order(p, itemid, s_price, amount, "buy", "market");
                        p.sendMessage("BUY " + amount + " " + itemid +" price: " + s_price );      // price[1] == best sell order
                    }
                    else {
                        // send message to create limit buy order
                        TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) There are no sell orders to buy. \nYou can create a buy order with: /ix buy " + itemid + " "+ amount +" limit ");
                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix buy " + itemid +" "+ amount +" limit "));
                        p.spigot().sendMessage(message);
                        e.getView().close();
                    }


                }
                else if ( e.getClick().isRightClick()) {
                    boolean item_found = false;
                    int item_counter=0;

                    // check if player have the amount of items provided by gui
                    for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
                        if(item != null && itemid.equalsIgnoreCase(item.getType().toString())) { //searching only for items with the given ID from command
                            item_counter = item_counter + item.getAmount();
                            item_found = true;
                        }
                    }

                    if( !price[0].equals("-") ) {
                        float b_price = parseFloat(price[0]);

                        if( item_found && amount <= item_counter) {
                            ItemexCommand.create_order(p, itemid, b_price, amount, "sell", "market");
                            p.sendMessage("SELL " + amount + " " + itemid +" price: " + b_price );     // price[0] == best buy order
                        }
                        else {
                            p.sendMessage("Not enough items in inventory!");
                        }
                    }
                    else {
                        // send message to create limit sell order
                        TextComponent message = new TextComponent(ChatColor.BLUE + "-> (CLICK HERE) There are no buy orders to sell. \nYou can create a sell order with: /ix sell " + itemid + " "+ amount + " limit ");
                        message.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ix sell " + itemid +" "+ amount +" limit "));
                        p.spigot().sendMessage(message);
                        e.getView().close();
                    }



                }
                else if( e.getClick().toString().equalsIgnoreCase("SWAP_OFFHAND")) {
                    p.sendMessage("Swap offhand");
                    //remove it or block it
                }
                else{
                    //p.sendMessage(e.getClick().toString());
                }

            }

            // if player clicks on items (not the menu)
            else if(e.getSlot() > 8) {
                if( e.getClick().toString().equalsIgnoreCase("SWAP_OFFHAND")) {
                    p.sendMessage("Swap offhand");
                    //remove it or block it
                }
                else{
                    String r_value[] = new String[2];
                    r_value = get_price( e.getCurrentItem().getType().toString() );
                    update_item(amount, p, e, e.getCurrentItem().getType(), 4, e.getCurrentItem().getType().toString(), r_value[0] + " ", r_value[1]);

                    update_item(amount, p, e, Material.SOUL_TORCH, 1, "sub 64", "", "");
                    update_item(amount, p, e, Material.REDSTONE_TORCH, 2, "sub 16", "", "");
                    update_item(amount, p, e, Material.TORCH, 3, "sub 1","", "");
                    update_item(amount, p, e, Material.TORCH, 5, "add 1", "", "");
                    update_item(amount, p, e, Material.REDSTONE_TORCH, 6, "add 16", "", "");
                    update_item(amount, p, e, Material.SOUL_TORCH, 7, "add 64", "", "");

                    //p.sendMessage(e.getClick().toString());
                }
            }

            // Handle clicks
            switch(e.getSlot()) {
                case 5:
                    amount = amount + 1;
                    break;
                case 6:
                    amount = amount + 16;
                    break;
                case 7:
                    amount = amount + 64;
                    break;
                case 3:
                    amount = amount - 1;
                    break;
                case 2:
                    amount = amount - 16;
                    break;
                case 1:
                    amount = amount - 64;
                    break;
                case 8: // next
                    page++;
                    update_item(page, p, e, Material.SPRUCE_DOOR, 8, "next page", "", "");
                    update_item(page, p, e, Material.OAK_DOOR, 0, "previous page", "", "");
                    scroll_page(page, e.getInventory());
                    break;
                case 0: // prev
                    page--;
                    if(page <= 1)    // if page is negative set to 0
                        page = 1;
                    update_item(page, p, e, Material.SPRUCE_DOOR, 8, "next page", "", "");
                    update_item(page, p, e, Material.OAK_DOOR, 0, "previous page", "", "");
                    scroll_page(page, e.getInventory());
                    break;
            }

            //if player clicks on menu
            if(e.getSlot() < 8) {

                String r_value[] = new String[2];
                r_value = get_price( e.getInventory().getItem(4).getType().toString() );
                
                update_item(amount, p, e, e.getInventory().getItem(4).getType(), 4, e.getInventory().getItem(4).getType().toString(), r_value[0], r_value[1]);

                update_item(amount, p, e, Material.SOUL_TORCH, 1, "sub 64","", "");
                update_item(amount, p, e, Material.REDSTONE_TORCH, 2, "sub 16", "", "");
                update_item(amount, p, e, Material.TORCH, 3, "sub 1", "", "");
                update_item(amount, p, e, Material.TORCH, 5, "add 1", "", "");
                update_item(amount, p, e, Material.REDSTONE_TORCH, 6, "add 16", "", "");
                update_item(amount, p, e, Material.SOUL_TORCH, 7, "add 64", "", "");
            }

        }

    } // end clickevent function

    public void update_item(int amount, Player p, InventoryClickEvent e, Material m, int slot, String text, String buy, String sell) {
        // update paper (AMOUNT)
        if(amount < 1)  // if amount is negativ set to 1
            amount = 1;
        //p.sendMessage("Amount: " + amount);
        String temp_s_amount = Integer.toString(amount);

        ItemStack item = new ItemStack(m);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + text);
        ArrayList<String> item_lore = new ArrayList<>();
        item_lore.add(ChatColor.WHITE + temp_s_amount);
        if(!buy.equals("") || !sell.equals("")) {
            // get sum of market price
            item_lore.add(ChatColor.GREEN + "(left) BUY: " + amount );
            item_lore.add(ChatColor.RED + "(right) SELL: " + amount);
        }

        itemMeta.setLore(item_lore);
        item.setItemMeta(itemMeta);
        e.getInventory().setItem(slot, item);
    } // end update_item


    private void scroll_page(int page, Inventory inv) {
        int x=0;
        int max_stack = 5*9;

        for (Material material : Material.values()) {

            ItemStack temp = new ItemStack(material);

            if(x >= (page*max_stack - max_stack)) {
                if(temp.getItemMeta() != null) {
                    sqliteDb.OrderBuffer[] toporders = sqliteDb.getBestOrders( material.name() );
                    String s_toporder[] = new String[8];
                    for(int xx =0; xx<=7; xx++) {
                        if(toporders[xx] != null) {
                            String ordertype[] = toporders[xx].ordertype.split(":", 0);
                            String format_price = String.format("%.02f", toporders[xx].price);
                            if(ordertype[0].equals("sell"))
                                s_toporder[xx] = ChatColor.RED + "[" +  toporders[xx].amount +"] $" + format_price;
                            else
                                s_toporder[xx] = ChatColor.GREEN + "[" +  toporders[xx].amount +"] $" + format_price;
                        }

                        else
                            s_toporder[xx] = ChatColor.DARK_GRAY + "-";
                    }

                    ItemMeta tempMeta = temp.getItemMeta();
                    //tempMeta.setDisplayName(material.name());
                    ArrayList<String> temp_lore = new ArrayList<>();
                    temp_lore.add(ChatColor.DARK_GRAY + "[amount] <price>");
                    temp_lore.add(s_toporder[0]);
                    temp_lore.add(s_toporder[1]);
                    temp_lore.add(s_toporder[2]);
                    temp_lore.add(s_toporder[3]);

                    temp_lore.add(s_toporder[4]);
                    temp_lore.add(s_toporder[5]);
                    temp_lore.add(s_toporder[6]);
                    temp_lore.add(s_toporder[7]);

                    temp_lore.add(ChatColor.RED + "sellorders" +  ChatColor.WHITE + " | " + ChatColor.GREEN + "buyorders");
                    tempMeta.setLore(temp_lore);
                    temp.setItemMeta(tempMeta);

                   // inv.setItem(x, temp);
                    inv.setItem(( x+max_stack-page*max_stack+9), temp);
                }
                //inv.setItem(( x+max_stack-page*max_stack+9), new ItemStack(material));

            }
            if(x >= page*max_stack-1)
                break;
            x++;

        } // Material values end
    } // end scroll_page


    private String[] get_price(String item){
        //System.out.println("At get_price: " + item);
        String r_value[];
        r_value = new String[2];
        String buy = "-";
        String sell = "-";
        String[] ordertype;
        sqliteDb.OrderBuffer[] orders = new sqliteDb.OrderBuffer[0];
        orders = sqliteDb.getBestOrders( item );
        for(int x=0; x<=7; x++) {

            if(orders[x] == null) {
                break;
            }
            ordertype = orders[x].ordertype.split(":", 2);

            if(ordertype[0].equals("sell")) {
                //System.out.println(orders[x].ordertype + " " + orders[x].price);
                sell = String.valueOf(orders[x].price);

            }
            else if(ordertype[0].equals("buy")) {
                //System.out.println(orders[x].ordertype + " " + orders[x].price);
                if(buy.equals("-")) {
                    buy = String.valueOf(orders[x].price);
                    //System.out.println("B: " + buy);
                }
            }
        }
        r_value[0] = buy;
        r_value[1] = sell;
        return r_value;
    }



    private String[] get_price_of(String item, int amount){
        String r_value[];
        r_value = new String[2];
        return r_value;
    }

}
