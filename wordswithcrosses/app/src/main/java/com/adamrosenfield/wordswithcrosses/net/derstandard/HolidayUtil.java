package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;

public class HolidayUtil {
    private static final int ASCENSION_DELTA = 38;
    private static final int PENTECOST_DELTA = 49;
    private static final int CORPUS_CHRISTI_DELTA = 59;


    public static Calendar getEasterMonday(int year) {
        int k = year / 100;
        int m = 15 + (3*k + 3) / 4 - (8*k + 13) / 25;
        int s = 2 - (3*k + 3) / 4;
        int a = year % 19;
        int d = (a*19 + m) % 30;
        int r = (d + a / 11) / 29;
        int og = 21 + d - r;
        int sz = 7 - (year + year/4 + s) % 7;
        int oe = 7 - (og - sz) % 7;
        int os = og + oe;

        int easterMonday = os + 1;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.MARCH);
        c.set(Calendar.DAY_OF_MONTH, easterMonday);

        return c;
    }

    public static Calendar getAscension(int year) {
        return getNoClone(getEasterMonday(year), ASCENSION_DELTA);
    }

    public static Calendar getAscension(Calendar easterMonday) {
        return get(easterMonday, ASCENSION_DELTA);
    }

    public static Calendar getPentecostMonday(int year) {
        return getNoClone(getEasterMonday(year), PENTECOST_DELTA);
    }

    public static Calendar getPentecostMonday(Calendar easterMonday) {
        return get(easterMonday, PENTECOST_DELTA);
    }

    public static Calendar getCorpusChristi(int year) {
        return getNoClone(getEasterMonday(year), CORPUS_CHRISTI_DELTA);
    }

    public static Calendar getCorpusChristi(Calendar easterMonday) {
        return get(easterMonday, CORPUS_CHRISTI_DELTA);
    }

    private static Calendar get(Calendar easterMonday, int delta) {
        Calendar c = (Calendar) easterMonday.clone();
        return getNoClone(c, delta);
    }

    private static Calendar getNoClone(Calendar easterMonday, int delta) {
        easterMonday.add(Calendar.DAY_OF_YEAR, delta);
        return easterMonday;
    }

}
