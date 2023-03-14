// new version change version in: pom.xml, Itemex.java

//TODO
// handle exception if update server not available
// new orders must be sort down (Because old orders should be fulfilled first if price is equal)
// implement order (edit, list, close) !already started!
// autoupdate


/*
changelog 0.16
- implement /ix order list  <buyorders | sellorders> *<itemid>  | * optional
- implement /ix order edit <buyorders | sellorders> <order id>
- implement config.yml

changelog 0.15:
- implement autoupdate (server must be restarted, but reload plugin if a updates is downloaded)
- implement bstats

changelog 0.14:
- market orders (sell and buy orders with this type will get fulfilled with every order) (The GUI generates only market orders)
- implemented /ix order <edit/list/close> with autocomplete
- if buyer don't have enough money -> close order
- moved all db related functions (methods) to the sqliteDb class (more order)
- moved the database into the plugin folder (Itemex)
- added a config file (does not much right now)
- improved update checker (checks on start and every 24h)
- nice update message
- rewrite a lot of code for better readability despaghettification^^
- print help and open gui on command /ix if user is Player


 */


package sh.ome.itemex;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import sh.ome.itemex.Listeners.PlayerJoin;
import sh.ome.itemex.shedule.FulfillOrder;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.commands.commandAutoComplete;
import sh.ome.itemex.commands.sqliteDb;
import sh.ome.itemex.events.clickEventGUI;
import sh.ome.itemex.shedule.Metrics;
import sh.ome.itemex.shedule.UpdateItemex;

import java.io.IOException;
import java.sql.SQLException;

public final class Itemex extends JavaPlugin implements Listener {

    FileConfiguration config = getConfig();

    private static Itemex plugin;
    public static Economy econ = null;
    public static String version = "0.16";

    public static boolean admin_function;
    public static double admin_function_percentage;
    public static double broker_fee;
    public static boolean bstats;



    @Override
    public void onEnable() {
        // Plugin startup logic

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

        System.out.println("\n\n");
        System.out.println(ANSI_CYAN + "  88" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88    ,d"  + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88    88" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88  MM88MMM  ,adPPYba,  88,dPYba,,adPYba,  " + ANSI_BLUE + "  ,adPPYba,  8b,     ,d8" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88    88    a8P_____88  88P'   '88'    '8a " + ANSI_BLUE + " a8P_____88   `Y8, ,8P'" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88    88    8PP\"\"\"\"\"\"\"  88      88      88 " + ANSI_BLUE + " 8PP\"\"\"\"\"\"\"     )888(   " + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88    88,   '8b,   ,aa  88      88      88  " + ANSI_BLUE + "\"8b,   ,aa   ,d8\" \"8b, " + ANSI_RESET);
        System.out.println(ANSI_CYAN + "  88    \"Y888  `\"Ybbd8\"'  88      88      88 " + ANSI_BLUE + "  `\"Ybbd8\"'  8P'     `Y8  " + ANSI_RESET);
        System.out.println("");

        System.out.println(ANSI_YELLOW + "ITEMEX v" + version + " - Free Market Item Exchange Plugin loaded. Usage: /ix help" + ANSI_RESET);
        System.out.println("\n\n");

        getCommand("ix").setExecutor(new ItemexCommand());
        getCommand("ix").setTabCompleter(new commandAutoComplete());
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new clickEventGUI(), this);




        // generate config file
        config.options().copyDefaults(true);
        saveConfig();
        System.out.println("ITEMEX version: " + config.getString("version") );
        this.admin_function = config.getBoolean("admin_function");
        this.admin_function_percentage = config.getDouble("admin_function_percentage");
        this.broker_fee = config.getDouble("broker_fee");
        this.bstats = config.getBoolean("bstats");

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

        if (!setupEconomy() ) {
            System.out.println("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Itemex");
        //Fulfill Order
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new FulfillOrder();
            } catch (SQLException e) {
                System.out.println("Problem with Fulfill Order Scheduler");
                throw new RuntimeException(e);
            }
        }, 0, 40); //20 == 1 second 40


        //Check update

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new UpdateItemex(version);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Problem with Update Itemex Scheduler");

        }, 0, 1728000); //20 == 1 second 1,728,000 = 24h */

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
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
        System.out.println("ITEMEX - Free Market Item Exchange Plugin unloaded");
    }

    public static Itemex getPlugin() {
        return plugin;
    }
}
