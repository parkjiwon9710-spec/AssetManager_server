package service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OvernightMarginStore {

    private static final Map<LocalDate, Map<Integer, Long>> usedMargin =
            new ConcurrentHashMap<>();

    public static long getUsedMargin(int userId){

        Map<Integer, Long> today =
                usedMargin.computeIfAbsent(
                        LocalDate.now(),
                        d -> new ConcurrentHashMap<>()
                );

        return today.getOrDefault(userId, 0L);
    }

    public static void addUsedMargin(
            int userId,
            long margin
    ){

        Map<Integer, Long> today =
                usedMargin.computeIfAbsent(
                        LocalDate.now(),
                        d -> new ConcurrentHashMap<>()
                );

        today.merge(
                userId,
                margin,
                Long::sum
        );
    }

    public static void clearUser(int userId){

        Map<Integer, Long> today =
                usedMargin.get(LocalDate.now());

        if(today != null){
            today.remove(userId);
        }
    }

    public static void clearAll() {
        usedMargin.clear();
    }
}