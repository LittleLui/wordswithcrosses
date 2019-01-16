package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CalendarDateToIdConverter implements DateToIdConverter {
    private final SynchronizationPoints sync = createSync();
    
    private final static int FIRST_YEAR = 2015;
    private final static int LAST_YEAR = 2100;

    private static SynchronizationPoints createSync() {
        SynchronizationPointsBuilder builder = new SynchronizationPointsBuilder("2019-01-01", 9069)
            .skipWeekday(Calendar.SUNDAY, FIRST_YEAR, LAST_YEAR)

      //    .skipYearly("01-01", FIRST_YEAR, LAST_YEAR) //Neujahr
            .skipYearly("01-06", FIRST_YEAR, LAST_YEAR) //Dreikönig
            .skipYearly("05-01", FIRST_YEAR, LAST_YEAR) //Staatsfeiertag
            .skipYearly("08-15", FIRST_YEAR, LAST_YEAR) //Mariä Himmelfahrt
            .skipYearly("10-26", FIRST_YEAR, LAST_YEAR) //Nationalfeiertag
            .skipYearly("11-01", FIRST_YEAR, LAST_YEAR) //Allerheiligen
            .skipYearly("12-08", FIRST_YEAR, LAST_YEAR) //Mariä Empfängnis
            .skipYearly("12-25", FIRST_YEAR, LAST_YEAR) //Weihnachten
            .skipYearly("12-26", FIRST_YEAR, LAST_YEAR); //Stefani

        for (int year = FIRST_YEAR; year <= LAST_YEAR; year++) {
            Calendar easterMonday = HolidayUtil.getEasterMonday(year);
            builder.skip(easterMonday);
            builder.skip(HolidayUtil.getAscension(easterMonday));
            builder.skip(HolidayUtil.getPentecostMonday(easterMonday));
            builder.skip(HolidayUtil.getCorpusChristi(easterMonday));
        }

        return builder
            .limit(FIRST_YEAR, LAST_YEAR)
            .build();
    }

    public int getId(Calendar date) {
        return sync.getId(unparse(date));
    }




    private static class SynchronizationPoints {
        private final SortedSet<String> knownDates = new TreeSet<>();
        private final Map<String, Integer> dateToId = new HashMap<>();
        
        public void addDate(String date, int id) {
            knownDates.add(date);
            dateToId.put(date, id);
        }

        public int getId(String date) {
            Integer known = dateToId.get(date);
            if (known != null) {
                return known;
            }
            
            if (date.compareTo(knownDates.first()) < 0) {
                return getIdBackward(date);
            } else {
                return getIdForward(date);
            }
        }
        
        private int getIdForward(String date) {
            SortedSet<String> befores = knownDates.headSet(date);
            
            if (befores.isEmpty()) {
                return NONE;  
            }
            
            String before = befores.last();
            
            Calendar cBefore = parse(before);
            Calendar cDate = parse(date);
            int id = dateToId.get(before);
            
            if (id == NONE) {
                return NONE;
            }
            
            int days = DateUtil.daysBetween(cBefore, cDate);
            
            return id + days;
        }
        
        private int getIdBackward(String date) {
            String earliestKnown = knownDates.first();
            
            Calendar cDate = parse(date);
            Calendar cAfter = parse(earliestKnown);
            int id = dateToId.get(earliestKnown);
            
            if (id == NONE) {
                return NONE;
            }
            
            int days = DateUtil.daysBetween(cDate, cAfter);
            
            return id - days;
        }
    }
    
    private static class SynchronizationPointsBuilder {
        private String limitBefore;
        private String limitAfter;
        
        private final String fixedDate;
        private final int fixedId;
        
        private final SortedSet<String> skipsBefore = new TreeSet<>(Collections.reverseOrder());
        private final SortedSet<String> skipsAfter = new TreeSet<>();
        
        
        public SynchronizationPointsBuilder(String date, int id) {
            fixedDate = date;
            fixedId = id;
        }
        
        public SynchronizationPointsBuilder limit(int firstYear, int lastYear) {
            limitBefore = (firstYear - 1) + "-12-31";
            limitAfter = (lastYear + 1) + "-01-01";
            return this;
        }

        public SynchronizationPointsBuilder skipYearly(String monthDay, int firstYear, int lastYear) {
            for (int year = firstYear; year <= lastYear; year ++) {
                skip(year + "-" + monthDay);
            }
            return this;
        }

        public SynchronizationPointsBuilder skipWeekday(int weekday, int fromYear, int toYear) {
            Calendar c = parse(fromYear+"-01-01");
            Calendar until = parse(toYear+"-12-31");
            
            c.set(Calendar.DAY_OF_WEEK, weekday);
            
            while (c.before(until)) {
                skip(unparse(c));
                c.add(Calendar.DAY_OF_WEEK, 7);
            }

            return this;
        }

        public SynchronizationPointsBuilder skip(Calendar c) {
            return skip(unparse(c));
        }

        public SynchronizationPointsBuilder skip(String date) {
            if (date.compareTo(fixedDate) < 0) {
                skipsBefore.add(date);
            } else {
                skipsAfter.add(date);
            }

            return this;
        }
        
        public SynchronizationPoints build() {
            SynchronizationPoints sp = new SynchronizationPoints();
            
            sp.addDate(fixedDate, fixedId);
            
            for (String skip : skipsAfter) {
                if (limitAfter != null && skip.compareTo(limitAfter) >= 0) {
                    continue;
                }
                
                int id = sp.getId(skip);
                
                if (id != NONE) {
                    sp.addDate(skip, NONE);
                    
                    Calendar c = parse(skip);

                    c.add(Calendar.DATE, 1);
                    sp.addDate(unparse(c), id);

                    c.add(Calendar.DATE, -2);
                    sp.addDate(unparse(c), id - 1);
                }
            }
            
            for (String skip : skipsBefore) {
                if (limitBefore != null && skip.compareTo(limitBefore) <= 0) {
                    continue;
                }

                
                int id = sp.getId(skip);
                
                if (id != NONE) {
                    sp.addDate(skip, NONE);
                    
                    Calendar c = parse(skip);

                    c.add(Calendar.DATE, -1);
                    sp.addDate(unparse(c), id);
                    
                    c.add(Calendar.DATE, 2);
                    sp.addDate(unparse(c), id + 1);
                    
                }
            }
            
            if (limitBefore != null) {
                sp.addDate(limitBefore, NONE);
            }

            if (limitBefore != null) {
                sp.addDate(limitAfter, NONE);
            }
            
            return sp;
        }
        
    }
    
    private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    
    static Calendar parse(String date) {
        try {
            Calendar c = new GregorianCalendar(); 
            c.setTime(df.parse(date));
            return c;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    static String unparse(Calendar c) {
        return df.format(c.getTime());
    }
    
}
