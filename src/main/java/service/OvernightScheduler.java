package service;


import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class OvernightScheduler {


    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(
                    Runtime.getRuntime().availableProcessors() * 4
            );

    private final OvernightProcessor overnightProcessor;
    private final MarketSpecDAO marketSpecDAO;
    private final Map<String, ScheduledFuture<?>> futures =
            new ConcurrentHashMap<>();


    private ScheduledFuture<?> resetFuture;


    public OvernightScheduler(
            OvernightProcessor overnightProcessor,
            MarketSpecDAO marketSpecDAO
    ){

        this.overnightProcessor = overnightProcessor;
        this.marketSpecDAO = marketSpecDAO;

    }



    public void start() {
        for (String symbol : marketSpecDAO.getOverseasSymbols()) schedule(symbol);
        for (String symbol : marketSpecDAO.getDomesticSymbols()) schedule(symbol);
        for (String symbol : marketSpecDAO.getOptionSymbols()) schedule(symbol);

        scheduleReset();
    }

    private void schedule(String symbol) {

        ScheduledFuture<?> old = futures.remove(symbol);

        if (old != null) {
            old.cancel(false);
        }


        LocalTime closeTime =
                marketSpecDAO.getOvernightCloseTime(symbol);

        if (closeTime == null)
            return;

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime next =
                now.withHour(closeTime.getHour())
                        .withMinute(closeTime.getMinute())
                        .withSecond(0)
                        .withNano(0);

        if (next.isBefore(now)) {

            next = next.plusDays(1);

        }

        long delay =
                Duration.between(now, next).toMillis();

        System.out.println(
                "[SCHEDULER] "
                        + symbol
                        + " 예약 "
                        + next
        );

        ScheduledFuture<?> future =
                scheduler.schedule(() -> {

                    try {

                        overnightProcessor.process(symbol);

                    } finally {

                        futures.remove(symbol);

                        schedule(symbol);

                    }

                }, delay, TimeUnit.MILLISECONDS);



    }

    // 🔥 추가 — 매일 07:00 사용담보 초기화
    private void scheduleReset() {

        LocalTime resetTime = LocalTime.of(7, 0);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime next = now.withHour(resetTime.getHour())
                .withMinute(resetTime.getMinute())
                .withSecond(0)
                .withNano(0);

        if (next.isBefore(now)) {
            next = next.plusDays(1);
        }

        long delay = Duration.between(now, next).toMillis();

        System.out.println("[SCHEDULER] 사용담보 초기화 예약 " + next);

        resetFuture = scheduler.schedule(() -> {
            try {
                OvernightMarginStore.clearAll();
                System.out.println("[OVERNIGHT] 사용담보 초기화 완료 (07:00)");
            } finally {
                scheduleReset();   // 다음날 07:00 재예약
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public synchronized void reload() {
        futures.values().forEach(f -> f.cancel(false));
        futures.clear();

        if (resetFuture != null) {
            resetFuture.cancel(false);   // 🔥 추가
        }

        start();
    }




}