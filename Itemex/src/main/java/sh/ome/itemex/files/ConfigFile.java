package sh.ome.itemex.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    private static File raw_file;
    private static FileConfiguration file_config;

    //proof if config file exists if not create
    public static void setup() {
        raw_file = new File( Bukkit.getPluginManager().getPlugin("Itemex").getDataFolder(), "config.yml");
        if(!raw_file.exists()) {
            try {
                raw_file.createNewFile();
            }
            catch (IOException e) {
                System.out.println("ERROR - Itemex can't create config.yml file!");
            }
        }
        file_config = YamlConfiguration.loadConfiguration((raw_file));
    } //setup end

    public static FileConfiguration get() {
        return file_config;
    }

    public static void save() {
        try {
            file_config.save(raw_file);
        }
        catch (IOException e) {
            System.out.println("Couldn save the conf file");
        }
    }

    public static void reload() {
        file_config = YamlConfiguration.loadConfiguration(raw_file);
    }




}
