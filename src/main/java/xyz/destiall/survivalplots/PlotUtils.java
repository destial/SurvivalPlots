package xyz.destiall.survivalplots;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class PlotUtils {
    public static Duration getDuration(String s) {
        ChronoUnit unit =
                s.endsWith("d") ? ChronoUnit.DAYS :
                        s.endsWith("h") ? ChronoUnit.HOURS :
                                s.endsWith("s") ? ChronoUnit.SECONDS :
                                        s.endsWith("m") ? ChronoUnit.MINUTES :
                                                s.endsWith("M") ? ChronoUnit.MONTHS :
                                                        ChronoUnit.MILLIS;

        StringBuilder numberString = new StringBuilder();
        int i = 0;
        while (true) {
            try {
                int n = Integer.parseInt(""+s.charAt(i++));
                numberString.append(n);
            } catch (Exception e) {
                break;
            }
        }
        try {
            return Duration.of(Integer.parseInt(numberString.toString()), unit);
        } catch (Exception e) {
            SurvivalPlotsPlugin.getInst().getLogger().severe("Unable to parse duration " + s);
            return Duration.ZERO;
        }
    }

    public static Duration relativeDuration(Date end) {
        if (end == null)
            return Duration.ZERO;

        Date start = new Date();
        long difference_In_Time = end.getTime() - start.getTime();
        return Duration.of(difference_In_Time, ChronoUnit.MILLIS);
    }

    public static String relativeDate(Date end) {
        if (end == null)
            return "N/A";

        Date start = new Date();
        // Calculate time difference
        // in milliseconds
        long difference_In_Time
                = end.getTime() - start.getTime();

        String format = "";


        long difference_In_Days
                = (difference_In_Time
                / (1000L * 60 * 60 * 24))
                % 365;

        if (difference_In_Days > 0) {
            format += difference_In_Days + " days ";
        }

        long difference_In_Hours
                = (difference_In_Time
                / (1000L * 60 * 60))
                % 24;

        if (difference_In_Hours > 0) {
            format += difference_In_Hours + " hrs ";
        }

        long difference_In_Minutes
                = (difference_In_Time
                / (1000L * 60))
                % 60;

        if (difference_In_Minutes > 0) {
            format += difference_In_Minutes + " mins ";
        }

        long difference_In_Seconds
                = (difference_In_Time
                / 1000L)
                % 60;

        if (difference_In_Seconds > 0) {
            format += difference_In_Seconds + " s";
        }

        return format.trim();
    }
}
