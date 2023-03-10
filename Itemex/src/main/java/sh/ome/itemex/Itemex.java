package sh.ome.itemex;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import sh.ome.itemex.Listeners.PlayerJoin;
import sh.ome.itemex.commands.FulfillOrder;
import sh.ome.itemex.commands.ItemexCommand;
import sh.ome.itemex.commands.commandAutoComplete;
import sh.ome.itemex.commands.sqliteDb;
import sh.ome.itemex.events.clickEventGUI;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Scanner;

public final class Itemex extends JavaPlugin implements Listener {

    private static Itemex plugin;
    public static Economy econ = null;


    @Override
    public void onEnable() {
        String version = "0.13";
        // Plugin startup logic

        System.out.println("\n\n\n\n\n");
        System.out.println("  88");
        System.out.println("  88    ,d");
        System.out.println("  88    88");
        System.out.println("  88  MM88MMM  ,adPPYba,  88,dPYba,,adPYba,    ,adPPYba,  8b,     ,d8");
        System.out.println("  88    88    a8P_____88  88P'   '88'    '8a  a8P_____88   `Y8, ,8P'");
        System.out.println("  88    88    8PP\"\"\"\"\"\"\"  88      88      88  8PP\"\"\"\"\"\"\"     )888(   ");
        System.out.println("  88    88,   '8b,   ,aa  88      88      88  \"8b,   ,aa   ,d8\" \"8b, ");
        System.out.println("  88    \"Y888  `\"Ybbd8\"'  88      88      88   `\"Ybbd8\"'  8P'     `Y8  ");
        System.out.println("");

        System.out.println("ITEMEX v0.13 - Free Market Item Exchange Plugin loaded.");

        URL url = null;
        try {
            url = new URL("https://www.omesh.io/itemex/version.txt");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            String text = new Scanner( url.openStream() ).useDelimiter("\\A").next();
            if(!text.equalsIgnoreCase(version))
                System.out.println(" ### UPDATE AVAILABLE! ### You have " + version + " and " + text + " is ready to download from: \nhttps://www.spigotmc.org/resources/itemex-players-can-exchange-all-items-with-other-players-free-market.108398/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n\n\n\n\n");



        System.out.println("");

        getCommand("ix").setExecutor(new ItemexCommand());
        getCommand("ix").setTabCompleter(new commandAutoComplete());
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new clickEventGUI(), this);

        getConfig().options().copyDefaults();
        sqliteDb.createDBifNotExists();

        plugin = this;  // make this private static Itemex accessable in other files

        if (!setupEconomy() ) {
            System.out.println("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Itemex");
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new FulfillOrder();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 0, 40); //20 == 1 second

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
