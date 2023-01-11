package pl.dailytasks;


import org.bukkit.Bukkit;

import java.util.Calendar;
import static java.util.Calendar.*;

public class DateManager {

    public static Calendar fakeCalendar = null;
    public static int fakeCalendarTask;

    public static int getDaysOfMonth() {
        return getCalendar().getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getYear() {
        return getCalendar().get(YEAR);
    }

    public static int getMonth() {
        return getCalendar().get(Calendar.MONTH)+1;
    }

    public static int getDay() {
        return getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    public static int getHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute() {
        return getCalendar().get(Calendar.MINUTE);
    }

    public static int getSecond() {
        return getCalendar().get(Calendar.SECOND);
    }

    public static String getFormattedDate(String delimiter) {
        return getYear() + delimiter + getMonth() + delimiter + getDay() + delimiter + getHour() + delimiter + getMinute() + delimiter + getSecond();
    }

    public static Calendar getCalendar() {
        Calendar cal;
        if(fakeCalendar != null) {
            cal = fakeCalendar;
        } else {
            cal = Calendar.getInstance();
        }
        return cal;
    }

    public static void createFakeCalendar(int year, int month, int day, int hour, int minute, int second) {
        fakeCalendar = Calendar.getInstance();
        fakeCalendar.set(YEAR, year);
        fakeCalendar.set(MONTH, hour);
        fakeCalendar.set(DAY_OF_MONTH, day);
        fakeCalendar.set(HOUR, hour);
        fakeCalendar.set(MINUTE, minute);
        fakeCalendar.set(SECOND, second);
        fakeCalendarTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(DailyTasks.main, (Runnable) () -> {
            fakeCalendar.set(SECOND, fakeCalendar.get(SECOND) + 1);
        }, 0L, 20L);
    }

    public static void removeFakeCalendar() {
        fakeCalendar = null;
        Bukkit.getScheduler().cancelTask(fakeCalendarTask);
    }

    public static boolean isUsingFakeCalendar() {
        return (fakeCalendar == null);
    }

}
