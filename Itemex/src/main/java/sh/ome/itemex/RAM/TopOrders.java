package sh.ome.itemex.RAM;

// consumes at 1500 entries = ( 4*8 + 4*8 ) * 1500 = 432,000byte = 421.875kbyte

public class TopOrders {
    private float[] top_buyorder_prices = new float[5];
    private float[] top_sellorder_prices = new float[5];
    private int[] buyorder_amount = new int[5];
    private int[] sellorder_amount = new int[5];

    // Constructor
    public TopOrders () {}
    public TopOrders(float[] top_buyorder_prices, float[] top_sellorder_prices, int[] buyorder_amount, int[] sellorder_amount) {
        this.top_buyorder_prices = top_buyorder_prices;
        this.top_sellorder_prices = top_sellorder_prices;
        this.buyorder_amount = buyorder_amount;
        this.sellorder_amount = sellorder_amount;
    }

    public float[] get_top_buyorder_prices() {
        return this.top_buyorder_prices;
    }

    public float[] get_top_sellorder_prices() {
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
            return true;
        }
        else
            return false;
    }
}
