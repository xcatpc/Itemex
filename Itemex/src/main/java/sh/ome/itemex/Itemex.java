// new version change version in: pom.xml, Itemex.java

/* BUGS AND IMPROVEMENTS:

# Important #
- CHESTSHOP: protect chest from other players, remove all items from chest if open, load from db and insert, support hoppers,
- logging upgrade -> send all errors to the server
- implement /i and /i gui

# Nice to have #
- add goat_horns, suspicious_stew, painting support
- new orders must be sort down (Because old orders should be fulfilled first if price is equal)
- /ix fastsell
- ADMIN: error if no orders, admin enabled and then creation of chestshop!!! (seller uuid not given, its empty)
- ADMIN FUNCTION not working (no seller or buyer uuid at withdraw in sqlite)

# NOT Important #
- delete old plugin versions (how?)
- handle exception if update server not available
 */


/*
changelog 0.22.0
 - remove new debug messages
 - config.yml add: server_domain, promote_server
*/

/*
changelog 0.21.9
 - /ix stats parameter handling
 - /ix stats implementation of <mylastpurchases, mylastsells>
 - fast sell removed from GUI (not implemented)
 - new stats
 - default withdraw threshold: 2304
 - bugfix: close sell order refund amount
*/

/*
changelog 0.21.8
- database rewrite - much faster and efficient
- add /ix deposit all
- removed item_json from /ix price (debug message)
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
import sh.ome.itemex.commands.i_command;
import sh.ome.itemex.database.createDatabase;
import sh.ome.itemex.events.ChestShop;
import sh.ome.itemex.events.ClickGUI;
import sh.ome.itemex.events.SignShop;
import sh.ome.itemex.files.CategoryFile;
import sh.ome.itemex.commands.ix_command;
import sh.ome.itemex.functions.ix_autocompletation;
import sh.ome.itemex.functions.sqliteDb;
import sh.ome.itemex.shedule.DataDifferenceSender;
import sh.ome.itemex.shedule.Metrics;
import sh.ome.itemex.shedule.UpdateItemex;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public final class Itemex extends JavaPlugin implements Listener {

    FileConfiguration config = getConfig();

    private static Itemex plugin;
    public static Economy econ = null;
    public static String version = "0.22.0";
    public static String lang;
    public static String database_type;
    public static String db_name;
    public static String db_hostname;
    public static String db_port;
    public static String db_username;
    public static String db_passwd;

    public static boolean admin_function;
    public static double admin_function_spread_percentage;
    public static double admin_function_initial_last_price;
    public static double admin_function_price_change_percentage;

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
    public static boolean promote_server;
    public static String server_domain;
    public static String server_id;
    public static String jwt_token;
    public static boolean itemex_stats;
    public static String server_url = "https://ome.sh";
    //public static String server_url = "http://localhost:8000";
    public static YamlConfiguration language;
    public static String server_version;

    public Map<String, TopOrders> mtop = new HashMap<>();

    public static HashMap<String, Integer> commandUsageCounts = new HashMap<>();

    public static Connection c;




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

        server_version = Bukkit.getBukkitVersion().split("-")[0];
        //getLogger().info("Server version: " + server_version);
        String[] versionParts = server_version.split("\\.");
        //getLogger().info("Server main version: " + versionParts[0] + "." + versionParts[1]);

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

        getCommand("ix").setExecutor(new ix_command());
        getCommand("ix").setTabCompleter(new ix_autocompletation());
        getCommand("i").setExecutor(new i_command());
        getCommand("i").setTabCompleter(new ix_autocompletation());

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

        this.database_type = config.getString("database_type");
        this.db_name = config.getString("db_name");
        this.db_hostname = config.getString("db_hostname");
        this.db_port = config.getString("db_port");
        this.db_username = config.getString("db_username");
        this.db_passwd = config.getString("db_passwd");

        //this.admin_function = config.getBoolean("admin_function");
        this.admin_function = false;
        this.admin_function_spread_percentage = config.getDouble("admin_function_spread_percentage");
        this.admin_function_initial_last_price = config.getDouble("admin_function_initial_last_price");
        this.admin_function_price_change_percentage = config.getDouble("admin_function_price_change_percentage");
        this.broker_fee_buyer = config.getDouble("broker_fee_buyer");
        this.broker_fee_seller = config.getDouble("broker_fee_seller");
        this.bstats = config.getBoolean("bstats");
        this.promote_server = config.getBoolean("promote_server");
        this.server_domain = config.getString("server_domain");
        this.server_id = config.getString("id");
        this.itemex_stats = config.getBoolean("itemex_stats");
        this.buy_listing_fee = config.getDouble("buy_listing_fee");
        this.sell_listing_fee = config.getDouble("sell_listing_fee");

        this.currencySymbol = config.getString("currencySymbol");
        this.decimals = config.getInt("decimals");
        this.decimal_separator = config.getString("decimal_separator").charAt(0);
        this.thousand_separator = config.getString("thousand_separator").charAt(0);
        this.unitLocation = config.getString("unitLocation");



        // Check if the token already exists
        if (!config.contains("jwt_token")) {
            try {
                //getLogger().info("# DEBUG - jwt_missing: server_id: " + server_id);
                URL url = new URL(server_url + "/register");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                // Create a map to hold counts and id
                Map<String, Object> data = new HashMap<>();
                data.put("server_id", server_id);

                // Convert map to JSON string and write to output stream
                String json = new Gson().toJson(data);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                // Check response code
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // If successful, read the received token from the server
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }

                        // Assuming the response contains just the token
                        String trimmedResponse = response.toString().trim();

                        if (trimmedResponse.contains("eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9")) {
                            config.set("jwt_token", trimmedResponse);
                            this.jwt_token = config.getString("jwt_token");
                        }


                    }
                }

                // Close the connection
                con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Save the config
            saveConfig();
        }
        else
            this.jwt_token = config.getString("jwt_token");

        // end jwt token check


        // generate categories.yml
        CategoryFile.setup();

        CategoryFile.init_cat();

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
        if(database_type.equals("sqlite"))
            createDatabase.createDBifNotExists();
        else
            createDatabase.createDBifNotExists_mariadb();

        c = createDatabase.createConnection();



        plugin = this;  // make this private static Itemex accessable in other files


        // load best orders from db into ram
        getLogger().info("Loading all BestOrders into RAM...");
        sqliteDb.loadAllBestOrdersToRam(false);


        //Check update
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                new UpdateItemex(version);
            } catch (IOException e) {
                getLogger().info("Problem with Update Itemex Scheduler");
                throw new RuntimeException(e);
            }
        }, 100, 288000); //20 == 1 second -> 288000 = 4h


        if(itemex_stats) { // itemex_stats
            // send all database entries
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                //getLogger().info("# DEBUG  datadiffernecesender");
                DataDifferenceSender.sendDataDifferencesToServer();
            }, 200, 288000); //20 == 1 second -> 288000 = 4h
        }



    } // end onenable()

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
        if(itemex_stats) { //itemex_stats
            checkAndSendUsageCounts();
        }
        mtop = null;
        System.gc(); // Suggest JVM to run garbage collection
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
                    data.put("cc", commandUsageCounts);
                    data.put("jwt_token", jwt_token);

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
