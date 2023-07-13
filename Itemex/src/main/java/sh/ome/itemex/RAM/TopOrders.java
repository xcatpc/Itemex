package sh.ome.itemex.RAM;

// consumes at 1500 entries = ( 4*8 + 4*8 ) * 1500 = 432,000byte = 421.875kbyte

import sh.ome.itemex.Itemex;

import java.util.*;

public class TopOrders {
    private double[] top_buyorder_prices = new double[5];
    private double[] top_sellorder_prices = new double[5];
    private int[] buyorder_amount = new int[5];
    private int[] sellorder_amount = new int[5];

    // Constructor
    public TopOrders () {}
    public TopOrders(double[] top_buyorder_prices, double[] top_sellorder_prices, int[] buyorder_amount, int[] sellorder_amount) {
        this.top_buyorder_prices = top_buyorder_prices;
        this.top_sellorder_prices = top_sellorder_prices;
        this.buyorder_amount = buyorder_amount;
        this.sellorder_amount = sellorder_amount;
    }

    public boolean admin_function(double buy_price, double sell_price) {
        this.top_buyorder_prices[4] = buy_price;
        this.top_sellorder_prices[4] = sell_price;
        this.buyorder_amount[4] = 1;
        this.sellorder_amount[4] = 1;
        return true;
    }

    public List<Order> get_top_sell(boolean is_admin_function) {
        // Create a TreeMap with a custom comparator that puts 0 at the top
        TreeMap<Double, Order> orders = new TreeMap<>((a, b) -> {
            if (a == 0) return -1;
            else if (b == 0) return 1;
            else return b.compareTo(a);
        });

        // Add first 4 orders to the map
        for (int i = 0; i < 4; i++) {
            orders.put(top_sellorder_prices[i], new Order(top_sellorder_prices[i], sellorder_amount[i], false));
        }

        // If admin function is active, add the admin order to the map with amount 1
        if (is_admin_function) {
            orders.put(top_sellorder_prices[4], new Order(top_sellorder_prices[4], 1, true));
        }

        // Convert the sorted orders to a list of Order objects
        List<Order> output = new ArrayList<>(orders.values());

        return output;
    }

    public List<Order> get_top_buy(boolean is_admin_function) {
        // Create a TreeMap where key is price and value is amount. The map is sorted in reverse order (highest price first)
        TreeMap<Double, Order> orders = new TreeMap<>(Comparator.reverseOrder());

        // Add first 4 orders to the map
        for (int i = 0; i < 4; i++) {
            orders.put(top_buyorder_prices[i], new Order(top_buyorder_prices[i], buyorder_amount[i], false));
        }

        // If admin function is active, add the admin order to the map with amount 1
        if (is_admin_function) {
            orders.put(top_buyorder_prices[4], new Order(top_buyorder_prices[4], 1, true));
        }

        // Convert the sorted orders to a list of Order objects
        List<Order> output = new ArrayList<>(orders.values());

        return output;
    }



    public double[] get_top_buyorder_prices() {
        return this.top_buyorder_prices;
    }

    public double[] get_top_sellorder_prices() {
        return this.top_sellorder_prices;
    }

    public int[] get_buyorder_amount() {
        return this.buyorder_amount;
    }

    public int[] get_sellorder_amount() {
        return this.sellorder_amount;
    }

    public boolean find_order_match() {
        //System.out.println("# DEBUG - BEST BUY PRICE: " + this.top_buyorder_prices[0]);
        //System.out.println("# DEBUG - BEST SELLER PRICE: " + this.top_sellorder_prices[0]);

        if(this.top_buyorder_prices[0] >= this.top_sellorder_prices[0] || Itemex.admin_function) {
            //System.out.println("# DEBUG - Match inside TopOrders.java");
            return true;
        }
        else
            return false;
    }
}

