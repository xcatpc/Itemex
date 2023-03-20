package sh.ome.itemex.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CategoryFile {

    private static File raw_file;
    private static FileConfiguration file_config;

    //proof if config file exists if not create
    public static boolean setup() {
        raw_file = new File( Bukkit.getPluginManager().getPlugin("Itemex").getDataFolder(), "categories.yml");
        if(!raw_file.exists()) {
            try {
                raw_file.createNewFile();
                file_config = YamlConfiguration.loadConfiguration((raw_file));
                return true;
            }
            catch (IOException e) {
                System.out.println("ERROR - Itemex can't create config.yml file!");
            }
        }
        file_config = YamlConfiguration.loadConfiguration((raw_file));
        return false;

    } //setup end

    public static FileConfiguration get() {
        return file_config;
    }

    public static void save() {
        try {
            file_config.save(raw_file);
        }
        catch (IOException e) {
            System.out.println("Could not save the conf file");
        }
    }

    public static void reload() {
        file_config = YamlConfiguration.loadConfiguration(raw_file);
    }

    public static void init() {
        Map<String, List<String>> map_categories = new HashMap<String, List<String>>();


        List<String> categories = new ArrayList<String>();
        categories.add("SPAWN_EGGS:PIG_SPAWN_EGG");
        categories.add("INGREDIENTS:IRON_INGOT");
        categories.add("FOOD_AND_DRINKS:GOLDEN_APPLE");
        categories.add("COMBAT:NETHERITE_SWORD");
        categories.add("TOOLS_AND_UTILITIES:DIAMOND_PICKAXE");
        categories.add("REDSTONE_BLOCKS:REDSTONE");
        categories.add("FUNCTIONAL_BLOCKS:OAK_SIGN");
        categories.add("NATURAL_BLOCKS:GRASS_BLOCK");
        categories.add("COLORED_BLOCKS:CYAN_WOOL");
        categories.add("BUILDING_BLOCKS:BRICKS");
        map_categories.put("CATEGORY_NAMES", categories);


        List<String> building_blocks = new ArrayList<String>();
        building_blocks.add("OAK_LOG");
        building_blocks.add("OAK_WOOD");
        map_categories.put("BUILDING_BLOCKS", building_blocks);

        List<String> colored_blocks = new ArrayList<String>();
        colored_blocks.add("DIAMOND");
        colored_blocks.add("GOLD_INGOT");
        colored_blocks.add("IRON_INGOT");
        map_categories.put("COLORED_BLOCKS", colored_blocks);

        List<String> natural_blocks = new ArrayList<String>();
        natural_blocks.add("DIAMOND");
        natural_blocks.add("GOLD_INGOT");
        natural_blocks.add("IRON_INGOT");
        map_categories.put("NATURAL_BLOCKS", natural_blocks);

        List<String> functional_blocks = new ArrayList<String>();
        functional_blocks.add("DIAMOND");
        functional_blocks.add("GOLD_INGOT");
        functional_blocks.add("IRON_INGOT");
        map_categories.put("FUNCTIONAL_BLOCKS", functional_blocks);

        List<String> redstone_blocks = new ArrayList<String>();
        redstone_blocks.add("DIAMOND");
        redstone_blocks.add("GOLD_INGOT");
        redstone_blocks.add("IRON_INGOT");
        map_categories.put("REDSTONE_BLOCKS", redstone_blocks);

        List<String> tools_and_utilities = new ArrayList<String>();
        tools_and_utilities.add("DIAMOND");
        tools_and_utilities.add("GOLD_INGOT");
        tools_and_utilities.add("IRON_INGOT");
        map_categories.put("TOOLS_AND_UTILITIES", tools_and_utilities);

        List<String> combat = new ArrayList<String>();
        combat.add("DIAMOND");
        combat.add("GOLD_INGOT");
        combat.add("IRON_INGOT");
        map_categories.put("COMBAT", combat);

        List<String> food_and_drinks = new ArrayList<String>();
        food_and_drinks.add("DIAMOND");
        food_and_drinks.add("GOLD_INGOT");
        food_and_drinks.add("IRON_INGOT");
        map_categories.put("FOOD_AND_DRINKS", food_and_drinks);

        List<String> ingredients = new ArrayList<String>();
        ingredients.add("DIAMOND");
        ingredients.add("GOLD_INGOT");
        ingredients.add("IRON_INGOT");
        map_categories.put("INGREDIENTS", ingredients);

        List<String> spawn_eggs = new ArrayList<String>();
        spawn_eggs.add("DIAMOND");
        spawn_eggs.add("GOLD_INGOT");
        spawn_eggs.add("IRON_INGOT");
        map_categories.put("SPAWN_EGGS", spawn_eggs);

        file_config.set("categories", map_categories);

        save();

    }




}
