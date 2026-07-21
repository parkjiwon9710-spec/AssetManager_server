package server;

public class OrderResponse {

    private String type = "ORDER_RESPONSE";
    private boolean success;
    private String message;
    private int orderId;

    public OrderResponse(boolean success, String message, int orderId) {
        this.success = success;
        this.message = message;
        this.orderId = orderId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getOrderId() { return orderId; }
}