package sh.ome.itemex.RAM;

// consumes at 1500 entries = ( 4*8 + 4*8 ) * 1500 = 432,000byte = 421.875kbyte

import sh.ome.itemex.Itemex;

import java.util.*;

import static java.lang.Math.random;
import static sh.ome.itemex.functions.sqliteDb.getLastPrice;

public class TopOrders {
    private double[] top_buyorder_prices = new double[5];
    private double[] top_sellorder_prices = new double[5];
    private int[] buyorder_amount = new int[5];
    private int[] sellorder_amount = new int[5];
    private int[] last_trade_timestamp = new int[5];
    private double[] last_trade_price = new double[5];
    public double last_price;

    // Constructor
    public TopOrders () {
        create_empty_topOrder();
    }
    public TopOrders(String itemid, double[] top_buyorder_prices, double[] top_sellorder_prices, int[] buyorder_amount, int[] sellorder_amount, double[] last_trade_price, int[] last_trade_timestamp) {
        this.top_buyorder_prices = top_buyorder_prices;
        this.top_sellorder_prices = top_sellorder_prices;
        this.buyorder_amount = buyorder_amount;
        this.sellorder_amount = sellorder_amount;
        this.last_trade_price = last_trade_price;
        this.last_trade_timestamp = last_trade_timestamp;
        gen_admin_orders(itemid);
    }
    public boolean update_topOrders(String itemid, double[] top_buyorder_prices, double[] top_sellorder_prices, int[] buyorder_amount, int[] sellorder_amount, double[] last_trade_price, int[] last_trade_timestamp) {
        this.top_buyorder_prices = top_buyorder_prices;
        this.top_sellorder_prices = top_sellorder_prices;
        this.buyorder_amount = buyorder_amount;
        this.sellorder_amount = sellorder_amount;
        adjust_admin_orders(itemid);
        return true;
    }


    private boolean create_empty_topOrder() {
        for(int x=0; x<=3; x++){
            this.top_buyorder_prices[x] = 0;
            this.top_sellorder_prices[x] = 0;
            this.buyorder_amount[x] = 0;
            this.sellorder_amount[x] = 0;
            this.last_trade_timestamp[x] = 0;
            this.last_trade_price[x] = 0;
        }

        return true;
    }


    private boolean adjust_admin_orders(String itemid) {
        if(Itemex.admin_function) {
            double lastPrice = this.last_price;
            double spread = Itemex.admin_function_spread_percentage / 200.0;                    // Halve the spread to share it on both sides
            this.top_buyorder_prices[4] = lastPrice / (1 + spread);                             // 1000 / (1 + 0.5) = 666.67
            this.top_sellorder_prices[4] = top_buyorder_prices[4] * (1 + 2 * spread);           // 666.67 * (1 + 1) = 1333.33
            this.sellorder_amount[4] = 1;
            this.buyorder_amount[4] = 1;
        }
        return true;
    }

    private boolean gen_admin_orders(String itemid) {
        if(Itemex.admin_function) {
            String[] parts = getLastPrice(itemid).split(":");
            double lastPrice = Double.parseDouble(parts[0]);
            // if admin then reduce or increase price
            if (parts.length > 1 && parts[1].equals("admin_seller")) {
                lastPrice = lastPrice + lastPrice/100*Itemex.admin_function_price_change_percentage;
                //System.out.println("# DEBUG: (seller admin) LAST PRICE: " + lastPrice);
            }
            else if (parts.length > 1 && parts[1].equals("admin_buyer")) {
                lastPrice = lastPrice - lastPrice/100*Itemex.admin_function_price_change_percentage;
                //System.out.println("# DEBUG: (buyer admin) LAST PRICE: " + lastPrice);
            }

            if(lastPrice == 0)
                lastPrice = Itemex.admin_function_initial_last_price;

            double spread = Itemex.admin_function_spread_percentage / 200.0; // Halve the spread to share it on both sides
            this.last_price = lastPrice;

            this.top_buyorder_prices[4] = lastPrice / (1 + spread);  // 1000 / (1 + 0.5) = 666.67
            this.top_sellorder_prices[4] = top_buyorder_prices[4] * (1 + 2 * spread); // 666.67 * (1 + 1) = 1333.33
            this.sellorder_amount[4] = 1;
            this.buyorder_amount[4] = 1;
        } // end admin_function
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

        // Add up to 4 orders to the map
        for (int i = 0; i < 4; i++) {
            if (i < top_buyorder_prices.length && buyorder_amount[i] != 0) {
                orders.put(top_buyorder_prices[i], new Order(top_buyorder_prices[i], buyorder_amount[i], false));
            } else {
                // Adds an order with 0 price and 0 quantity when there's no valid order
                orders.put(0.0 + i / 10000.0, new Order(0.0, 0, false));
            }
        }

        // If admin function is active, add the admin order to the map with amount 1
        if (is_admin_function) {
            orders.put(top_buyorder_prices[4], new Order(top_buyorder_prices[4], 1, true));
        }

        // Convert the sorted orders to a list of Order objects
        List<Order> output = new ArrayList<>(orders.values());

        return output;
    }


    public int[] get_last_timestamp() {
        return this.last_trade_timestamp;
    }

    public double[] get_last_trade_price() {
        return this.last_trade_price;
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

