package server;

import model.AdminDepositMonitoring;

import java.util.List;

public class DepositMonitoringResponse {
    public String type = "DEPOSIT_MONITORING_UPDATE";
    public List<AdminDepositMonitoring> deposits;
    public List<AdminDepositMonitoring> withdraws;

    public DepositMonitoringResponse(List<AdminDepositMonitoring> deposits, List<AdminDepositMonitoring> withdraws) {
        this.deposits = deposits;
        this.withdraws = withdraws;
    }
}
