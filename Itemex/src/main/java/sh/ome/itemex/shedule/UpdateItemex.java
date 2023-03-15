package sh.ome.itemex.shedule;

import sh.ome.itemex.Itemex;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static org.bukkit.Bukkit.getLogger;

public class UpdateItemex {
    public UpdateItemex(String version) throws IOException {
        URL url = null;
        String file_url;
        String server_version = null;


        String ANSI_RESET = "\u001B[0m";
        String ANSI_BLACK = "\u001B[30m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_PURPLE = "\u001B[35m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_WHITE = "\u001B[37m";


        url = new URL("https://ome.sh/itemex/version.txt");
        file_url = "https://ome.sh/itemex/jar";


        try {
            server_version = new Scanner( url.openStream() ).useDelimiter("\\A").next();
        }
        catch (Exception e){
            getLogger().info("Exception: " + e);
        }


        if(!server_version.equalsIgnoreCase(version)) {

            try (BufferedInputStream in = new BufferedInputStream(new URL(file_url).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream("./plugins/itemex-" + server_version + ".jar")) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                getLogger().info("Unable to download new version.");
            }

            getLogger().info("\n ###--------------------------------------------------------------------###");
            getLogger().info("\n");
            getLogger().info("  88");
            getLogger().info("  88    ,d");
            getLogger().info("  88    88");
            getLogger().info("  88  MM88MMM  ,adPPYba,  88,dPYba,,adPYba,    ,adPPYba,  8b,     ,d8");
            getLogger().info("  88    88    a8P_____88  88P'   '88'    '8a  a8P_____88   `Y8, ,8P'");
            getLogger().info("  88    88    8PP\"\"\"\"\"\"\"  88      88      88  8PP\"\"\"\"\"\"\"     )888(   ");
            getLogger().info("  88    88,   '8b,   ,aa  88      88      88  \"8b,   ,aa   ,d8\" \"8b, ");
            getLogger().info("  88    \"Y888  `\"Ybbd8\"'  88      88      88   `\"Ybbd8\"'  8P'     `Y8  ");
            getLogger().info("");
            getLogger().info(ANSI_YELLOW +"  __  _____  ___  ___ __________" + ANSI_RESET);
            getLogger().info(ANSI_YELLOW + " / / / / _ \\/ _ \\/ _ /_  __/ __/" + ANSI_RESET);
            getLogger().info(ANSI_YELLOW + "/ /_/ / ___/ // / __ |/ / / _/" + ANSI_RESET);
            getLogger().info(ANSI_YELLOW + "\\____/_/  /____/_/ |_/_/ /___/ " + ANSI_RESET);
            getLogger().info(ANSI_GREEN + "Successfully downloaded new version: " + server_version  + ". " + ANSI_RED + "Reload necessary!" + ANSI_RESET);
            //getLogger().info("\n" + ANSI_YELLOW + "AVAILABLE! You have " + version + " and " + server_version + " is ready to download from: " + ANSI_RESET + " \n" + ANSI_GREEN + "https://www.spigotmc.org/resources/itemex-players-can-exchange-all-items-with-other-players-free-market.108398/" + ANSI_RESET);
            getLogger().info("\n ###--------------------------------------------------------------------###\n");
        }
        else {
            getLogger().info(ANSI_GREEN + "You have already the newest Itemex version: v" + server_version + ANSI_RESET);
        }
    }

}
