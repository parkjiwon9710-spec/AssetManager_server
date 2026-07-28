package server;

import model.Order;

import java.util.List;

public class PendingOrdersResponse {

    private String type = "PENDING_ORDERS_RESPONSE";
    private List<Order> orders;

    public PendingOrdersResponse(List<Order> orders) {
        this.orders = orders;
    }

    public String getType() {
        return type;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
