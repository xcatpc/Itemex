package sh.ome.itemex.RAM;

public class Order {
    private double price;
    private int amount;
    private boolean isAdmin;

    public Order(double price, int amount, boolean isAdmin) {
        this.price = price;
        this.amount = amount;
        this.isAdmin = isAdmin;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

