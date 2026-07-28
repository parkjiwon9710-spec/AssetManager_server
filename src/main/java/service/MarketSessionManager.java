package service;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MarketSessionManager {


    private static final Set<String> lockedSymbols =
            ConcurrentHashMap.newKeySet();





    public static void lockSymbol(String symbol){

        lockedSymbols.add(symbol);

    }

    public static void unlockSymbol(String symbol){

        lockedSymbols.remove(symbol);

    }

    public static boolean isSymbolLocked(String symbol){

        return lockedSymbols.contains(symbol);

    }


}