package Market;

public class FuturesContractCalculator {

    public enum ContractCycle { MONTHLY, QUARTERLY }

    private static final char[] MONTH_CODE = {
            'F','G','H','J','K','M','N','Q','U','V','X','Z'
    };
    private static final int[] QUARTERLY_MONTHS = {3, 6, 9, 12};

    // currentCode 예: "HSIQ26" -> base="HSI", monthChar='Q', year=26
    public static String getNextContractCode(String currentCode, ContractCycle cycle) {

        int len = currentCode.length();
        char monthChar = currentCode.charAt(len - 3);
        int year = Integer.parseInt(currentCode.substring(len - 2));
        String base = currentCode.substring(0, len - 3);

        int currentMonth = monthIndexOf(monthChar) + 1;   // 1~12

        int nextMonth;
        int nextYear = year;

        if (cycle == ContractCycle.MONTHLY) {
            nextMonth = currentMonth + 1;
            if (nextMonth > 12) {
                nextMonth = 1;
                nextYear++;
            }
        } else {   // QUARTERLY
            nextMonth = -1;
            for (int q : QUARTERLY_MONTHS) {
                if (q > currentMonth) {
                    nextMonth = q;
                    break;
                }
            }
            if (nextMonth == -1) {
                nextMonth = QUARTERLY_MONTHS[0];
                nextYear++;
            }
        }

        char nextMonthChar = MONTH_CODE[nextMonth - 1];
        return base + nextMonthChar + String.format("%02d", nextYear % 100);
    }

    private static int monthIndexOf(char c) {
        for (int i = 0; i < MONTH_CODE.length; i++) {
            if (MONTH_CODE[i] == c) return i;
        }
        throw new IllegalArgumentException("알 수 없는 월물 코드 문자: " + c);
    }
}