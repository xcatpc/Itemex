// new version change version in: pom.xml, Itemex.java


/* BUGS AND IMPROVEMENTS:
- market orders only with the command: /ix sell gold_ingot 1 market  -> dont work: there are no sell orders to buy
- max at /ix sell not anymore working thow an error
- GUI market: there are no orders (but there are)

- "confirm" on market order like /ix buy GOLD_INGOT 10 market confirm
- insertPayout - collect all payouts of a item in one entry
- collect all orders with the same price at /price and GUI

- /ix withdraw list must have a parameter of page. only 100 entries can be send to player. Alsa have an error!
- handle exception if update server not available
- new orders must be sort down (Because old orders should be fulfilled first if price is equal)
- /ix quicksell (own gui for quickselling all items)
- /ix gui orders (list all orders) or is inside the normal /ix gui which would be better
- at ix sell: If I hold something in the hand it most be in the list on the top
- add default prices that reflects on the reserve currency (DIAMOND) (useful if no buy and sellorders are available or only a buy or sellorder) - need statistics
- GUI: sort items by availibity

- add potions and enchanted items
- add enchantment items

 */

/*
changelog 0.18
- NEW GUI
- create a category.yml file which contains all categories and items from the creative menu (except potions and enchanted books at the moment)
- new order fulfillment (much more efficient)
- efficency updates -> Store TopOrders (4 sell and 4 buyorders) into ram of every item (takes at server start ~ 4 seconds = 1400 items); Minimize DB usage
- New GUI which is much more userfriendly and more efficient too
- add broker free for seller and buyer (config file: default value 0)
- change /ix price to get data from RAM
- remove autocomplete price at market orders
 */


package sh.ome.itemex;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import sh.ome.itemex.Listeners.PlayerJoin;
import sh.ome.itemex.RAM.TopOrders;
import sh.ome.itemex.events.ClickGUI;
import sh.ome.itemex.files.CategoryFile;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.functions.commandAutoComplete;
import sh.ome.itemex.functions.sqliteDb;
import sh.ome.itemex.shedule.Metrics;
import sh.ome.itemex.shedule.UpdateItemex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Itemex extends JavaPlugin implements Listener {

    FileConfiguration config = getConfig();

    private static Itemex plugin;
    public static Economy econ = null;
    public static String version = "0.18";

    public static boolean admin_function;
    public static double admin_function_percentage;
    public static double broker_fee_buyer;
    public static double broker_fee_seller;
    public static boolean bstats;

    //public TopOrders top[] = new TopOrders[1500]; // create 1500 objects for each item ( fill in onEnable() )
    public Map<String, TopOrders> mtop = new HashMap<>();




    @Override
    public void onEnable() {
        // Plugin startup logic

        // create all objects
        //Arrays.fill(top, new TopOrders());


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


        if (!setupEconomy() ) {
            getLogger().info("Disabled due to no Vault dependency found!");
            //getServer().getPluginManager().disablePlugin(this);
            return;
        }



        // generate config file
        config.options().copyDefaults(true);
        saveConfig();
        this.admin_function = config.getBoolean("admin_function");
        this.admin_function_percentage = config.getDouble("admin_function_percentage");
        this.broker_fee_buyer = config.getDouble("broker_fee_buyer");
        this.broker_fee_seller = config.getDouble("broker_fee_seller");
        this.bstats = config.getBoolean("bstats");

        // generate categories.yml
        CategoryFile.setup();
        CategoryFile.init();
        CategoryFile.get().options().copyDefaults(true);
        CategoryFile.save();


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
        getLogger().info("DONE!");



/*
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Itemex");
        //Fulfill Order
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new FulfillOrder();
            } catch (SQLException e) {
                getLogger().info("Problem with Fulfill Order Scheduler");
                throw new RuntimeException(e);
            }
        }, 0, 20); //20 == 1 second 40


  */



        //Check update

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new UpdateItemex(version);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            getLogger().info("Problem with Update Itemex Scheduler");

        }, 0, 288000); //20 == 1 second 1,728,000 = 24h */ 4h

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
        // Plugin shutdown logic
        getLogger().info("ITEMEX - Free Market Item Exchange Plugin unloaded");
    }

    public static Itemex getPlugin() {
        return plugin;
    }
}
