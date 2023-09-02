package sh.ome.itemex.RAM;
import java.util.HashMap;
import java.util.Set;

public class CommandUsageCounts {
    private HashMap<String, Integer> counts;

    public CommandUsageCounts() {
        counts = new HashMap<>();
    }

    public void increment(String commandName) {
        if (!counts.containsKey(commandName)) {
            counts.put(commandName, 1);
        } else {
            int count = counts.get(commandName);
            counts.put(commandName, count + 1);
        }
    }

    public int getCount(String commandName) {
        if (!counts.containsKey(commandName)) {
            return 0;
        } else {
            return counts.get(commandName);
        }
    }

    public Set<String> getCommandNames() {
        return counts.keySet();
    }

}
