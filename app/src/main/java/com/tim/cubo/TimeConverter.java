package com.tim.cubo;


import java.util.TimeZone;

public class TimeConverter {

    private static long localeOffset;

    static {
        localeOffset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
    }

    public static long getSeconds(long mills) {
        return mills % 60;
    }

    public static long getMinutes(long mills) {
        return (mills / 60) % 60;
    }

    public static long getHours(long mills) {
        return (mills / (60 * 60)) % 24;
    }

    public static long adjustTimestamp(long mills) {
        return mills + localeOffset;
    }
}
