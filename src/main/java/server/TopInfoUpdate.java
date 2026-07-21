package server;


public class TopInfoUpdate {
    private String type = "TOP_INFO_UPDATE";
    private long collateral;
    private long losscut;
    private double unrealized;
    private double realized;

    public TopInfoUpdate(long collateral, long losscut, double unrealized, double realized) {
        this.collateral = collateral;
        this.losscut = losscut;
        this.unrealized = unrealized;
        this.realized = realized;
    }

    public String getType() {
        return type;
    }

    public long getCollateral() {
        return collateral;
    }

    public long getLosscut() {
        return losscut;
    }

    public double getUnrealized() {
        return unrealized;
    }

    public double getRealized() {
        return realized;
    }
}
