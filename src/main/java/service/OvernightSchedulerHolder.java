package service;

public class OvernightSchedulerHolder {

    private static OvernightScheduler scheduler;

    public static void init(OvernightScheduler s) {
        scheduler = s;
    }

    public static OvernightScheduler get() {
        return scheduler;
    }
}