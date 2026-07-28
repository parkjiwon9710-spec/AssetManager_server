package service;


import model.TimeFrame;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class CandleAggregator {


    public static LocalDateTime truncate(
            LocalDateTime time,
            TimeFrame tf
    ){


        switch(tf){


            case MIN1:

                return time.truncatedTo(
                        ChronoUnit.MINUTES
                );


            case MIN5:

                return time
                        .withMinute(
                                (time.getMinute()/5)*5
                        )
                        .withSecond(0)
                        .withNano(0);



            case MIN15:

                return time
                        .withMinute(
                                (time.getMinute()/15)*15
                        )
                        .withSecond(0)
                        .withNano(0);



            case MIN30:

                return time
                        .withMinute(
                                (time.getMinute()/30)*30
                        )
                        .withSecond(0)
                        .withNano(0);



            case HOUR1:

                return time
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);



            case DAY:

                return time
                        .toLocalDate()
                        .atStartOfDay();



            default:
                return time;

        }

    }

}