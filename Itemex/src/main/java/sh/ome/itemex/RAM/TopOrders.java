package sh.ome.itemex.RAM;

// consumes at 1500 entries = ( 4*8 + 4*8 ) * 1500 = 432,000byte = 421.875kbyte

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

        if(this.top_buyorder_prices[0] >= this.top_sellorder_prices[0]) {
            //System.out.println("# DEBUG - Match inside TopOrders.java");
            return true;
        }
        else
            return false;
    }
}
