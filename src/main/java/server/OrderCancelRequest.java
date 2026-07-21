package server;

public class OrderCancelRequest {
    private String type = "ORDER_CANCEL_REQUEST";
    private int orderId;
    private int userId;

    public int getOrderId() { return orderId; }
    public int getUserId() { return userId; }
}