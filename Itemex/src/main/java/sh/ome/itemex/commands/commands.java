package sh.ome.itemex.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.functions.sqliteDb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sh.ome.itemex.Itemex.econ;

public class commands {

    public static String get_itemid(String json) {
        //getLogger().info("# DEBUG (get_itemid): " + json);
        Gson gson = new Gson();
        List<Map<String, Object>> list = gson.fromJson(json, List.class);
        int enc_counter = 0;
        String itemid ="";
        for (Map<String, Object> map : list) {
            if (map.containsKey("itemid")) {
                itemid = map.get("itemid").toString();
            }
            if(map.containsKey("enc"))
                enc_counter++;
        }
        if(enc_counter > 1)
            return "more_than_one_enchantment_not_supported";
        return itemid;
    }

    public static String get_json_from_meta(String meta) {
        String[] elements = meta.split(":");

        if (elements.length == 1) {
            // Normal item
            return "[{\"itemid\":\"" + elements[0].toUpperCase() +"\"}]";
        } else if (elements[0].equals("ENCHANTED_BOOK")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"enc\":\"" + elements[1] + "\",\"lev\":" + elements[2] + "}]";
        } else if (elements[0].equals("POTION") || elements[0].equals("SPLASH_POTION") || elements[0].equals("LINGERING_POTION") || elements[0].equals("TIPPED_ARROW")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"bp_ext\":" + elements[2] + ",\"bp_name\":\"" + elements[1] + "\",\"bp_upg\":" + elements[3] + "}]";
        } else if (elements[0].equals("GOAT_HORN")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"song\":\"" + elements[1] + "\"}]";
        } else if (elements[0].equals("SUSPICIOUS_STEW")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"stew_eff\":\"" + elements[1] + "\",\"stew_dur\":" + elements[2] + "}]";
        } else if (elements[0].equals("PAINTING")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"paint\":\"" + elements[1] + "\"}]";
        } else if (elements[0].equals("FIREWORK_ROCKET")) {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() + "\"},{\"f_dur\":" + elements[1] + "}]";
        } else {
            return "[{\"itemid\":\"" + elements[0].toUpperCase() +"\"}]";
        }
    }


    public static String get_meta(String json) {
        Gson gson = new Gson();
        List<Map<String, Object>> list = gson.fromJson(json, List.class);
        int enc_counter = 0;
        String itemid = "";

        // enchanted book
        String enc = "";
        String lev = "";

        // tipped arrow or potion
        String bp_name = "";
        String bp_ext = "";
        String bp_upg = "";

        // goat horn
        String song = "";

        // SUSPICIOUS_STEW
        String stew_eff = "";
        //String stew_amp = "";
        String stew_dur = "";

        // painting
        String paint = "";

        // firework rocket
        String f_dur = "";

        for (Map<String, Object> map : list) {
            if (map.containsKey("itemid"))
                itemid = map.get("itemid").toString();

            if(map.containsKey("enc")) {
                enc = map.get("enc").toString();
                enc_counter++;
            }
            if(map.containsKey("lev")) {
                Double doubleValue = (Double) map.get("lev");
                lev = Integer.toString(doubleValue.intValue());
            }

            if(map.containsKey("bp_name"))
                bp_name = map.get("bp_name").toString();
            if(map.containsKey("bp_ext"))
                bp_ext = map.get("bp_ext").toString();
            if(map.containsKey("bp_upg"))
                bp_upg = map.get("bp_upg").toString();

            if(map.containsKey("song"))
                song = map.get("song").toString();

            if(map.containsKey("stew_eff"))
                stew_eff = map.get("stew_eff").toString();
            if(map.containsKey("stew_amp")) {
                //Double doubleValue = (Double) map.get("stew_amp");
                //stew_amp = Integer.toString(doubleValue.intValue());
            }
            if(map.containsKey("stew_dur")) {
                Double doubleValue = (Double) map.get("stew_dur");
                stew_dur = Integer.toString(doubleValue.intValue());
            }

            if(map.containsKey("paint"))
                paint = map.get("paint").toString();

            if(map.containsKey("f_dur")) {
                Double doubleValue = (Double) map.get("f_dur");
                f_dur = Integer.toString(doubleValue.intValue());
            }
        }

        if(enc_counter > 1)
            return "more_than_one_enchantment_not_supported";
        else if(itemid.equals("ENCHANTED_BOOK"))
            return itemid + ":" + enc + ":" + lev;
        else if(itemid.equals("POTION") || itemid.equals("SPLASH_POTION") || itemid.equals("LINGERING_POTION") || itemid.equals("TIPPED_ARROW"))
            return itemid + ":" + bp_name + ":" + bp_ext + ":" +  bp_upg;
        else if(itemid.equals("GOAT_HORN"))
            return itemid + ":" + song;
        else if(itemid.equals("SUSPICIOUS_STEW"))
            return itemid + ":" + stew_eff + ":" + stew_dur;
        else if(itemid.equals("PAINTING"))
            return itemid + ":" + paint;
        else if(itemid.equals("FIREWORK_ROCKET"))
            return itemid + ":" + f_dur;
        else
            return itemid;
    }


    public static String identify_item(ItemStack item) {
        if(item == null)
            return null;
        String itemid = item.getType().toString().toUpperCase();
        ItemMeta itemMeta = item.getItemMeta();

        List<Map<String, Object>> enchantmentList = new ArrayList<>(); // stores everything in this map
        Map<String, Object> temp = new HashMap<>();
        temp.put("itemid", itemid);
        if (item.getDurability() != 0)
            temp.put("dur", item.getDurability());
        enchantmentList.add(temp);


        if (itemMeta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemMeta;
            if (esm.hasStoredEnchants()) {
                for (Map.Entry<Enchantment, Integer> enchantment : esm.getStoredEnchants().entrySet()) {
                    Map<String, Object> enchantmentData = new HashMap<>();
                    enchantmentData.put("enc", enchantment.getKey().getKey().getKey());
                    enchantmentData.put("lev", enchantment.getValue());
                    enchantmentList.add(enchantmentData);
                }
            }
        } else {
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            if (!enchantments.isEmpty()) {
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                    Map<String, Object> enchantmentData = new HashMap<>();
                    enchantmentData.put("enc", enchantment.getKey().getKey().getKey());
                    enchantmentData.put("lev", enchantment.getValue());
                    enchantmentList.add(enchantmentData);
                }
            }
        }

        // Check if the item is a goat horn
        if (item.getType() == Material.GOAT_HORN) {
            ItemMeta meta = item.getItemMeta();

            int startIndex = meta.getAsString().indexOf("minecraft:") + "minecraft:".length();
            String instrumentName = meta.getAsString().substring(startIndex, meta.getAsString().length() - 2);
            Map<String, Object> enchantmentData = new HashMap<>();
            enchantmentData.put("song", instrumentName);
            enchantmentList.add(enchantmentData);
        }

        // Check if the item is a suspicious stew
        if (item.getType() == Material.SUSPICIOUS_STEW) {
            SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) item.getItemMeta();

            if (stewMeta.hasCustomEffects()) {
                for (PotionEffect effect : stewMeta.getCustomEffects()) {
                    Map<String, Object> effectData = new HashMap<>();
                    effectData.put("stew_eff", effect.getType().getName());
                    effectData.put("stew_dur", effect.getDuration());
                    //effectData.put("stew_amp", effect.getAmplifier());
                    enchantmentList.add(effectData);
                }
            }
        }


        // Check if the item is a painting
        if (item.getType() == Material.PAINTING) {
            ItemMeta meta = item.getItemMeta();
            int startIndex = meta.getAsString().indexOf("minecraft:") + "minecraft:".length();

            if (meta.getAsString().length() > startIndex + 3) { // Ensure the string is long enough
                String entityName = meta.getAsString().substring(startIndex, meta.getAsString().length() - 3);
                Map<String, Object> enchantmentData = new HashMap<>();
                enchantmentData.put("paint", entityName);
                enchantmentList.add(enchantmentData);
            } else {

            }
        }


        // List all properties of the item, in case it's a firework
        if (itemMeta instanceof FireworkMeta) {
            Map<String, Object> enchantmentData = new HashMap<>();

            FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;

            // Firework duration
            int duration = fireworkMeta.getPower();
            enchantmentData.put("f_dur", duration);

            // Firework effects
            if (fireworkMeta.hasEffects()) {
                for (FireworkEffect effect : fireworkMeta.getEffects()) {
                    enchantmentData.put("f_eff", effect.getType().name());
                    enchantmentData.put("f_fli", effect.hasFlicker());
                    enchantmentData.put("f_trl", effect.hasTrail());

                    // Display colors of the effect
                    for (Color color : effect.getColors()) {
                        enchantmentData.put("f_col", color.asRGB());
                    }

                    // Display fade colors of the effect
                    for (Color fadeColor : effect.getFadeColors()) {
                        enchantmentData.put("f_fco", fadeColor.asRGB());
                    }
                }
            }
            enchantmentList.add(enchantmentData);
        }

        // List all effects of the item, in case it's a potion
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            Map<String, Object> enchantmentData = new HashMap<>();

            // Base potion effect
            PotionData baseData = potionMeta.getBasePotionData();
            enchantmentData.put("bp_name", baseData.getType().name());
            enchantmentData.put("bp_ext", baseData.isExtended());
            enchantmentData.put("bp_upg", baseData.isUpgraded());

            // Custom potion effects
            if (potionMeta.hasCustomEffects()) {
                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                    enchantmentData.put("cp_eff", effect.getType().getName());
                }
            } else {
                //p.sendMessage("No additional potion effects.");
            }
            enchantmentList.add(enchantmentData);
        }

        // output
        if (!enchantmentList.isEmpty()) {
            Gson gson = new Gson();
            String json = gson.toJson(enchantmentList);
            //getLogger().info("# DEBUG: (json) " + json);
            return json;
        }
        return "";
    } // end check_item





    public static ItemStack constructItem(String item_json, int amount) {
        Gson gson = new Gson();
        JsonArray array = gson.fromJson(item_json, JsonArray.class);
        ItemStack item = null;
        ItemMeta meta = null;

        String itemid = "";
        String bp_name = "";
        int stew_dur = 0;
        String stew_eff = "";
        String song = "";
        String paint = "";
        int f_dur = 0;

        boolean bp_ext = false;
        boolean bp_upg = false;

        // Map to store enchantments
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();

            // Access each key-value pair in the object
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (key.equals("itemid")) {
                    itemid = value.getAsString();
                    Material material = Material.getMaterial(itemid);
                    if (material == null) {
                        //getLogger().info("Invalid Material: " + itemid);  // Log the invalid material
                        return new ItemStack(Material.AIR);
                    }
                    item = new ItemStack(material, amount);
                    meta = item.getItemMeta();
                }


                // Handle Potion properties
                else if(key.equals("bp_name") || key.equals("bp_ext") || key.equals("bp_upg")) {
                    if(key.equals("bp_name"))
                        bp_name = value.getAsString();
                    else if(key.equals("bp_ext"))
                        bp_ext = value.getAsBoolean();
                    else if(key.equals("bp_upg"))
                        bp_upg = value.getAsBoolean();
                }
                // Handle Enchantment for enchanted book
                else if (key.equals("enc")) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value.getAsString()));
                    if (enchantment != null) {
                        enchantments.put(enchantment, 1); // default level is 1
                    }
                } else if (key.equals("lev")) {
                    Enchantment lastEnchantment = enchantments.keySet().stream().reduce((first, second) -> second).orElse(null);
                    if (lastEnchantment != null) {
                        enchantments.put(lastEnchantment, value.getAsInt());
                    }
                }
                // ROCKET
                else if(key.equals("f_dur"))
                    f_dur = value.getAsInt();
                    // goat_horn song
                else if(key.equals("song"))
                    song = value.getAsString();
                    // SUSPICIOUS_STEW
                else if(key.equals("stew_eff"))
                    stew_eff = value.getAsString();
                else if(key.equals("stew_dur"))
                    stew_dur = value.getAsInt();
                    // PAINTING
                else if(key.equals("paint"))
                    paint = value.getAsString();

            } // end elements loop
        }

        if (item != null && meta != null) {


            // POTION
            if (meta instanceof PotionMeta) {
                // This is a Potion, Splash Potion, Lingering Potion, or Tipped Arrow
                PotionMeta potionMeta = (PotionMeta) meta;
                // Determine the potion type
                PotionType potionType = PotionType.valueOf(bp_name);
                if (potionType != null) {
                    // Apply the potion type and modifiers
                    PotionData potionData = new PotionData(potionType, bp_ext, bp_upg);
                    potionMeta.setBasePotionData(potionData);
                }

                // Check the type of potion and modify the item
                if (itemid.equals("SPLASH_POTION")) {
                    item = new ItemStack(Material.SPLASH_POTION);
                } else if (itemid.equals("LINGERING_POTION")) {
                    item = new ItemStack(Material.LINGERING_POTION);
                } else if (itemid.equals("TIPPED_ARROW")) {
                    item = new ItemStack(Material.TIPPED_ARROW);
                } else {
                    item = new ItemStack(Material.POTION);
                }

                item.setItemMeta(potionMeta);
            }

            // FIREWORK_ROCKET
            else if (itemid.equals("FIREWORK_ROCKET")) {
                FireworkMeta fireworkMeta = (FireworkMeta) meta;
                fireworkMeta.setPower(f_dur);
                item.setItemMeta(fireworkMeta);
            }

            // GOAT HORN
            else if (itemid.equals("GOAT_HORN")) {
                // not implemented
                //getLogger().info("Song: " + song);
                //getLogger().info("GOAT_HORN NOT IMPLEMENTED!");
                return new ItemStack(Material.AIR);
            }

            // SUSPICIOUS_STEW
            else if (itemid.equals("SUSPICIOUS_STEW")) {
                SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) meta;
                PotionEffectType potionEffectType = PotionEffectType.getByName(stew_eff);
                if (potionEffectType != null) {
                    PotionEffect potionEffect = new PotionEffect(potionEffectType, stew_dur * 20, 0); // Duration is in ticks (20 ticks = 1 second)
                    stewMeta.addCustomEffect(potionEffect, true);
                    item.setItemMeta(stewMeta);
                }
            }

            // PAINTING
            else if (itemid.equals("PAINTING")) {
                // not implemented
                //getLogger().info("paint: " + paint);
                //getLogger().info("PAINTING NOT IMPLEMENTED!");
                return new ItemStack(Material.AIR);
            }

            // ENCHANTED BOOK
            else if (meta instanceof EnchantmentStorageMeta && !enchantments.isEmpty()) {
                // This is an Enchanted Book
                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    enchantmentStorageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
                item.setItemMeta(enchantmentStorageMeta);
            }

            else {
                item.setItemMeta(meta);
            }
        }

        return item;
    }



    public static String getFreeInventory(Player p, String item_json){
        //getLogger().info("# DEBUG: at getFreeInventory: itemid: " + item_json);
        int empty_slots = 0;
        int this_item_count = 0;

        for (ItemStack item : p.getInventory().getContents()) { //check inventory of player
            if (item == null) { // if stack is empty
                empty_slots++;
            } else if (item != null && identify_item(item).equals(item_json)) {
                this_item_count = this_item_count + item.getAmount();
            }
        }
        empty_slots = empty_slots - 5;      // subtract amour and left hand slots
        // check how many stacks the given item has (eg. diamond = 64; egg = 16)
        int max_stack = Material.getMaterial(get_itemid(item_json)).getMaxStackSize();
        int max_items = empty_slots * max_stack;

        return empty_slots + ":" + this_item_count + ":" + max_items;
    }






    static String print_help(boolean player_or_console) {
        String gold, white, green, dark_gray, dark_purple, red, blue, dark_aqua, light_purple, yellow, underline;

        if(player_or_console) {
            gold = ChatColor.GOLD.toString();
            white = ChatColor.RESET.toString();
            green = ChatColor.GREEN.toString();
            red = ChatColor.RED.toString();
            dark_gray = ChatColor.DARK_GRAY.toString();
            dark_purple = ChatColor.DARK_PURPLE.toString();
            blue = ChatColor.BLUE.toString();
            dark_aqua = ChatColor.DARK_AQUA.toString();
            light_purple = ChatColor.LIGHT_PURPLE.toString();
            yellow = ChatColor.YELLOW.toString();
            underline = ChatColor.UNDERLINE.toString();
        }
        else {
            gold = "";
            white = "";
            green = "";
            red = "";
            dark_gray = "";
            dark_purple = "";
            blue = "";
            dark_aqua = "";
            light_purple = "";
            yellow = "";
            underline = "";
        }

        String reply_command = "\n";
        reply_command = reply_command + gold + "------------------------------------\nix = ITEMEX = Item Exchange v" + Itemex.version  + "\n------------------------------------\n.\n" + white;
        reply_command = reply_command + underline + "USAGE: " + "https://www.youtube.com/watch?v=053V13OrZWI\n." +
                "\n" + white + "/ix " + yellow + "help " + dark_gray + "| " + Itemex.language.getString("help_help") + "\n" +
                "\n" + white + "/ix " + gold + "gui " + dark_gray + "| " + Itemex.language.getString("help_gui") +

                "\n" + white + "/ix " + green + "buy " + dark_gray + "| " + Itemex.language.getString("help_ix_buy") +
                "\n" + white + "/ix " + red + "sell " + dark_gray + "| " + Itemex.language.getString("help_ix_sell") +
                "\n" + white + "/ix " + green + "buy " + white + "<itemname> <amount> <limit | market> <price> " + dark_gray + "| " + Itemex.language.getString("help_cbo") +
                "\n" + white + "/ix " + red + "sell " + white + "<itemname> <amount> <limit | market> <price> " + dark_gray + "| " + Itemex.language.getString("help_cso") + "\n." +

                "\n" + white + "/ix " + blue + "price " + dark_gray + "| " + Itemex.language.getString("help_price") +
                "\n" + white + "/ix " + blue + "price " + white + "<itemid> " + dark_gray + "| " + Itemex.language.getString("help_price_id") + "\n." +

                "\n" + white + "/ix " + light_purple + "order list " + white + "<buyordery | sellorders> *<item id>" + dark_gray + "| " + Itemex.language.getString("help_order_list") +
                "\n" + white + "/ix " + light_purple + "order close " + white + "<buyordery | sellorders> <order id> " + dark_gray + "| " + Itemex.language.getString("help_order_close") +

                "\n" + white + "/ix " + dark_aqua + "whatIsInMyRightHand" + dark_gray+ "| " + Itemex.language.getString("help_wiimrh") +

                "\n" + white + "/ix " + gold + "withdraw list " + dark_gray+ "| " + Itemex.language.getString("help_with_list") +
                "\n" + white + "/ix " + gold + "withdraw" + white + " <itemname> <amount> " + dark_gray + "| " + Itemex.language.getString("help_withdraw") + dark_purple +
                "\n.\n" + Itemex.language.getString("help_message") + white +" xcatpc@proton.me " + dark_purple + Itemex.language.getString("help_or_join_disc") + white + "https://discord.gg/rKEwQjpmXj" + white;
        reply_command = reply_command + "\n";
        return reply_command;
    } // end print_help






    public static void checkAndSendUsageCounts() {
        int totalCommandsEntered = 0;

        // Sum up the usage counts for each command
        for (int count : Itemex.commandUsageCounts.values()) {
            totalCommandsEntered += count;
        }

        if (totalCommandsEntered > 19) {
            // create a new thread to handle the HTTP request
            Thread httpRequestThread = new Thread(() -> {
                try {
                    URL url = new URL(Itemex.server_url + "/itemex");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);

                    // create map to hold counts and id
                    Map<String, Object> data = new HashMap<>();
                    data.put("cc", Itemex.commandUsageCounts);
                    data.put("jwt_token", Itemex.jwt_token);

                    // convert map to JSON string and write to output stream
                    String json = new Gson().toJson(data);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    // check response code and close connection
                    int responseCode = con.getResponseCode();
                    con.disconnect();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // start the thread
            httpRequestThread.start();

        }
    }




    public static String format_price(double price) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(Itemex.thousand_separator); // Thousands separator
        symbols.setDecimalSeparator(Itemex.decimal_separator); // Decimal separator

        StringBuilder pattern = new StringBuilder("###,##0.");
        for (int i = 0; i < Itemex.decimals; i++) {
            pattern.append("0");
        }

        DecimalFormat df = new DecimalFormat(pattern.toString(), symbols);
        String formattedValue = df.format(price);

        switch(Itemex.unitLocation.toLowerCase()) {
            case "right":
                return formattedValue + Itemex.currencySymbol; // Currency symbol at the end
            case "left":
                return Itemex.currencySymbol + formattedValue; // Currency symbol at the beginning
            default:
                return formattedValue; // No currency symbol if unitLocation is not "right" or "left"
        }
    }



    public static String create_order(Player p, String item_json, double price, int amount, String buy_or_sell, String market_option) {
        //getLogger().info("# DEBUG AT: create_order: " + amount + " item_json: " + item_json);
        String itemid = get_itemid(item_json);
        String reply_command = "";

        ix_command.Order order = new ix_command.Order();
        // create sell order (RAM)
        order.amount = amount;
        order.uuid = p.getUniqueId().toString();
        order.itemid = item_json;
        order.ordertype = buy_or_sell + ":" + market_option;
        order.price = price;

        sqliteDb db_order = new sqliteDb(order);

        if (buy_or_sell.equals("sell")) {
            if (db_order.createSellOrder() != -1) {
                reply_command = ChatColor.RED + Itemex.language.getString("sellorder_C") + ChatColor.WHITE + ChatColor.BOLD + "[" + amount + "] " + itemid.toUpperCase() + ChatColor.WHITE + " " + Itemex.language.getString("created");
            } else {
                reply_command = Itemex.language.getString("er_sello_ncreated");
            }

            //p.sendMessage("item_json: " + item_json);
            //p.sendMessage("amount: " + amount);

            int amountToRemove = amount;
            Inventory inv = p.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && identify_item(item).equals(item_json)) {
                    int newAmount = item.getAmount() - amountToRemove;
                    if (newAmount > 0) {
                        item.setAmount(newAmount);
                        break;
                    } else {
                        inv.setItem(i, null);
                        amountToRemove -= item.getAmount();
                    }
                    if (amountToRemove == 0) break;
                }
            }
        }


        else if (buy_or_sell.equals("buy")) {
            double buyer_balance = econ.getBalance(p);
            if ((amount * price) < buyer_balance) {
                if (db_order.createBuyOrder() != -1)
                    reply_command = ChatColor.GREEN + Itemex.language.getString("buyorder_C") + ChatColor.WHITE + Itemex.language.getString("created") + ChatColor.BOLD + " [" + amount + "] " + itemid + ChatColor.WHITE;
                else
                    reply_command = Itemex.language.getString("erbuyo_ncreated");
            } else {  //not enough money
                reply_command = ChatColor.RED + Itemex.language.getString("not_enough_money") + ChatColor.WHITE + Itemex.language.getString("you_need") + ChatColor.GREEN + format_price((amount * price)) + ChatColor.WHITE + Itemex.language.getString("but_you_only_have") + ChatColor.RED + " " + format_price(buyer_balance);
            }
        }
        return reply_command;
    }


}
