package sh.ome.itemex.shedule;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class UpdateItemex {
    public UpdateItemex(String version) throws IOException {
        URL url = null;
        try {
            url = new URL("https://ome.sh/version.txt");
        } catch (MalformedURLException e) {
            //throw new RuntimeException(e);
        }

        String text = new Scanner( url.openStream() ).useDelimiter("\\A").next();
        if(!text.equalsIgnoreCase(version)) {
            System.out.println("\n ###--------------------------------------------------------------------###");
            System.out.println("\n");
            System.out.println("  88");
            System.out.println("  88    ,d");
            System.out.println("  88    88");
            System.out.println("  88  MM88MMM  ,adPPYba,  88,dPYba,,adPYba,    ,adPPYba,  8b,     ,d8");
            System.out.println("  88    88    a8P_____88  88P'   '88'    '8a  a8P_____88   `Y8, ,8P'");
            System.out.println("  88    88    8PP\"\"\"\"\"\"\"  88      88      88  8PP\"\"\"\"\"\"\"     )888(   ");
            System.out.println("  88    88,   '8b,   ,aa  88      88      88  \"8b,   ,aa   ,d8\" \"8b, ");
            System.out.println("  88    \"Y888  `\"Ybbd8\"'  88      88      88   `\"Ybbd8\"'  8P'     `Y8  ");
            System.out.println("");
            System.out.println("  __  _____  ___  ___ __________");
            System.out.println(" / / / / _ \\/ _ \\/ _ /_  __/ __/");
            System.out.println("/ /_/ / ___/ // / __ |/ / / _/");
            System.out.println("\\____/_/  /____/_/ |_/_/ /___/ ");
            System.out.println("\nAVAILABLE! You have " + version + " and " + text + " is ready to download from: \nhttps://www.spigotmc.org/resources/itemex-players-can-exchange-all-items-with-other-players-free-market.108398/");
            System.out.println("\n ###--------------------------------------------------------------------###\n");
        }
    }

}
