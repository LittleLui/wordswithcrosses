package com.adamrosenfield.wordswithcrosses.net.derstandard;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class HolidayUtilTest {

    @Parameterized.Parameter(0)
    public int year;

    @Parameterized.Parameter(1)
    public String easterMonday;

    @Parameterized.Parameter(2)
    public String ascension;

    @Parameterized.Parameter(3)
    public String pentecost;

    @Parameterized.Parameter(4)
    public String corpusChristi;

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        ArrayList<Object[]> l = new ArrayList<>();
        l.add(new Object[]{2005, "03-28", "05-05", "05-16", "05-26"});
        l.add(new Object[]{2012, "04-09", "05-17", "05-28", "06-07"});
        l.add(new Object[]{2013, "04-01", "05-09", "05-20", "05-30"});
        l.add(new Object[]{2019, "04-22", "05-30", "06-10", "06-20"});
        l.add(new Object[]{2025, "04-21", "05-29", "06-09", "06-19"});
        l.add(new Object[]{2040, "04-02", "05-10", "05-21", "05-31"});
        return l;
    }

    @Test
    public void easterMonday() {
        assertDate(HolidayUtil.getEasterMonday(year), easterMonday);
    }

    @Test
    public void ascension() {
        assertDate(HolidayUtil.getAscension(year), ascension);
    }

    @Test
    public void pentecost() {
        assertDate(HolidayUtil.getPentecostMonday(year), pentecost);
    }

    @Test
    public void corpusChristi() {
        assertDate(HolidayUtil.getCorpusChristi(year), corpusChristi);
    }


    private void assertDate(Calendar cal, String date) {
        String[] split = date.split("-");
        final int month = Integer.parseInt(split[0]);
        final int day = Integer.parseInt(split[1]);

        assertThat(cal.get(Calendar.YEAR), is(year));
        assertThat(cal.get(Calendar.MONTH) + 1, is(month));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), is(day));
    }


}
