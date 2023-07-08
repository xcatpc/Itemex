// new version change version in: pom.xml, Itemex.java


/* BUGS AND IMPROVEMENTS:

- insertPayout - collect all payouts of an item in one entry
- collect all orders with the same price at /price and GUI
- delete old versions (how?)
- /ix withdraw list must have a parameter of page. only 100 entries can be sent to player. Also has an error!
- handle exception if update server not available
- new orders must be sort down (Because old orders should be fulfilled first if price is equal)
- /ix quicksell (own gui for quickselling all items)
- /ix gui orders (list all orders) or is inside the normal /ix gui which would be better
- at ix sell: If I hold something in the hand it must be in the list on the top (autocomplete)
- add default prices that reflects on the reserve currency (DIAMOND) (useful if no buy and sellorders are available or only a buy or sellorder) - need statistics
- GUI: sort items by availability
- add potions and enchanted items
- add enchantment items

 */


/*
changelog 0.20.1
- Multi Language support (en, de, cn, fr, es, ru)
- bugfix at close sell order
 */


/*
changelog 0.20.0
- Limit chest shop bugfix (can't sell to chest limit, player get the items)
- optional listing fee added for sell- or buyorders to config.yml: default: 0.0
 */




package sh.ome.itemex;

import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import sh.ome.itemex.Listeners.PlayerJoin;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.events.ChestShop;
import sh.ome.itemex.events.ClickGUI;
import sh.ome.itemex.events.SignShop;
import sh.ome.itemex.files.CategoryFile;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.functions.commandAutoComplete;
import sh.ome.itemex.functions.sqliteDb;
import sh.ome.itemex.shedule.Metrics;
import sh.ome.itemex.shedule.UpdateItemex;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Itemex extends JavaPlugin implements Listener {

    FileConfiguration config = getConfig();

    private static Itemex plugin;
    public static Economy econ = null;
    public static String version = "0.20.2";
    public static String lang;

    public static boolean admin_function;
    public static double admin_function_percentage;

    public static String currencySymbol;
    public static int decimals;
    public static char decimal_separator;
    public static char thousand_separator;
    public static String unitLocation;

    public static double broker_fee_buyer;
    public static double broker_fee_seller;
    public static double sell_listing_fee;
    public static double buy_listing_fee;
    public static boolean bstats;
    public static String server_id;
    public static boolean itemex_stats;
    public static String server_url = "https://ome.sh";
    public static YamlConfiguration language;

    public Map<String, TopOrders> mtop = new HashMap<>();

    public static HashMap<String, Integer> commandUsageCounts = new HashMap<>();




    @Override
    public void onEnable() {

        Metrics metrics = new Metrics(this, 17928);
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));

        String ANSI_RESET = "\u001B[0m";
        String ANSI_BLACK = "\u001B[30m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_PURPLE = "\u001B[35m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_WHITE = "\u001B[37m";

        

        getLogger().info("\n\n");
        getLogger().info(ANSI_CYAN + "  88" + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88    ,d"  + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88    88" + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88  MM88MMM  ,adPPYba,  88,dPYba,,adPYba,  " + ANSI_BLUE + "  ,adPPYba,  8b,     ,d8" + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88    88    a8P_____88  88P'   '88'    '8a " + ANSI_BLUE + " a8P_____88   `Y8, ,8P'" + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88    88    8PP\"\"\"\"\"\"\"  88      88      88 " + ANSI_BLUE + " 8PP\"\"\"\"\"\"\"     )888(   " + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88    88,   '8b,   ,aa  88      88      88  " + ANSI_BLUE + "\"8b,   ,aa   ,d8\" \"8b, " + ANSI_RESET);
        getLogger().info(ANSI_CYAN + "  88    \"Y888  `\"Ybbd8\"'  88      88      88 " + ANSI_BLUE + "  `\"Ybbd8\"'  8P'     `Y8  " + ANSI_RESET);
        getLogger().info("");

        getLogger().info(ANSI_YELLOW + "ITEMEX v" + version + " - Free Market Item Exchange Plugin loaded. Usage: /ix help" + ANSI_RESET);
        getLogger().info("\n\n");

        getCommand("ix").setExecutor(new ItemexCommand());
        getCommand("ix").setTabCompleter(new commandAutoComplete());
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new ClickGUI(), this);
        getServer().getPluginManager().registerEvents(new SignShop(), this);
        getServer().getPluginManager().registerEvents(new ChestShop(), this);


        if (!setupEconomy() ) {
            getLogger().info("Disabled due to no Vault dependency found!");
            //getServer().getPluginManager().disablePlugin(this);
            return;
        }



        // generate config file
        config.options().copyDefaults(true);
        config.addDefault("id", getAlphaNumericString(15));
        saveConfig();
        this.lang = config.getString("lang");
        this.admin_function = config.getBoolean("admin_function");
        this.admin_function_percentage = config.getDouble("admin_function_percentage");
        this.broker_fee_buyer = config.getDouble("broker_fee_buyer");
        this.broker_fee_seller = config.getDouble("broker_fee_seller");
        this.bstats = config.getBoolean("bstats");
        this.server_id = config.getString("id");
        this.itemex_stats = config.getBoolean("itemex_stats");
        this.buy_listing_fee = config.getDouble("buy_listing_fee");
        this.sell_listing_fee = config.getDouble("sell_listing_fee");

        this.currencySymbol = config.getString("currencySymbol");
        this.decimals = config.getInt("decimals");
        this.decimal_separator = config.getString("decimal_separator").charAt(0);
        this.thousand_separator = config.getString("thousand_separator").charAt(0);
        this.unitLocation = config.getString("unitLocation");


        // generate categories.yml
        CategoryFile.setup();
        CategoryFile.init();
        CategoryFile.get().options().copyDefaults(true);
        CategoryFile.save();

        // load lang files from resource folder
        File en_lang_file = new File(getDataFolder(), "lang_en.yml");
        saveResource("lang_en.yml", true);
        YamlConfiguration en_lang = YamlConfiguration.loadConfiguration(en_lang_file);

        File de_lang_file = new File(getDataFolder(), "lang_de.yml");
        saveResource("lang_de.yml", true);
        YamlConfiguration de_lang = YamlConfiguration.loadConfiguration(de_lang_file);

        File es_lang_file = new File(getDataFolder(), "lang_es.yml");
        saveResource("lang_es.yml", true);
        YamlConfiguration es_lang = YamlConfiguration.loadConfiguration(es_lang_file);

        File fr_lang_file = new File(getDataFolder(), "lang_fr.yml");
        saveResource("lang_fr.yml", true);
        YamlConfiguration fr_lang = YamlConfiguration.loadConfiguration(fr_lang_file);

        File cn_lang_file = new File(getDataFolder(), "lang_cn.yml");
        saveResource("lang_cn.yml", true);
        YamlConfiguration cn_lang = YamlConfiguration.loadConfiguration(cn_lang_file);

        File ru_lang_file = new File(getDataFolder(), "lang_ru.yml");
        saveResource("lang_ru.yml", true);
        YamlConfiguration ru_lang = YamlConfiguration.loadConfiguration(ru_lang_file);


        // set language from config.yml to global language
        switch(this.lang) {
            case "cn":
                this.language = cn_lang;
                break;
            case "de":
                this.language = de_lang;
                break;
            case "en":
                this.language = en_lang;
                break;
            case "es":
                this.language = es_lang;
                break;
            case "fr":
                this.language = fr_lang;
                break;
            case "ru":
                this.language = ru_lang;
                break;
        }


        // checks database
        sqliteDb.createDBifNotExists();



        // if admin function enabled -> create admin function for every item
        if( Itemex.admin_function ) {
            // look if admin function already exists each item
            // if yes skip; else look if already a buy and a sell order exists ;;;; procentrual_increase = admin_function_percentage (from config file)
            // if only buy order ( =100/ (1/100*120+1)*1    |   #1 procentual_decrease = 100 / (1 / 100 * procentrual_increase + 1) * 1
            //                                              |   #2 sellorder_price = buyorder_price / 100 * procentual_decrease
            // if only sell order (buyorder_price = sellorder_price / 100 * procentrual_increase + sellorder_price)
            // if both:  # 1. take the best buy order and decrease by the half of the percentage of config file (calculate procentual_decrease of half of procentrual_increase), that is the buy admin order. Then increate by in the config given value: that is the buy admin order
            // if nothing # admin_function_initial_buy_price and calulate sell price
        }

        plugin = this;  // make this private static Itemex accessable in other files

        // load best orders from db into ram
        getLogger().info("Loading all BestOrders into RAM...");
        sqliteDb.loadAllBestOrdersToRam(false);

        //Check update
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new UpdateItemex(version);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            getLogger().info("Problem with Update Itemex Scheduler");

        }, 100, 288000); //20 == 1 second 1,728,000 = 24h */ 4h

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("VAULT not found");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().info("VAULT rsp = null");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }


    @Override
    public void onDisable() {
        if(itemex_stats) {
            checkAndSendUsageCounts();
        }
        getLogger().info("ITEMEX - Free Market Item Exchange Plugin unloaded");
    }

    public static Itemex getPlugin() {
        return plugin;
    }




    public void checkAndSendUsageCounts() {
        int totalCommandsEntered = 0;

        // Sum up the usage counts for each command
        for (int count : Itemex.commandUsageCounts.values()) {
            totalCommandsEntered += count;
        }

            // create a new thread to handle the HTTP request
            Thread httpRequestThread = new Thread(() -> {
                try {
                    URL url = new URL(server_url +"/itemex");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);

                    // create map to hold counts and id
                    Map<String, Object> data = new HashMap<>();
                    data.put("com", Itemex.commandUsageCounts);
                    data.put("id", Itemex.server_id);

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





    // function to generate a random string of length n
    static String getAlphaNumericString(int n)
    {

        // choose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

}
