package pl.dailytasks.util;

import org.bukkit.Bukkit;
import pl.dailytasks.DailyTasks;

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

    public static String getFormattedDate(String format) {
        format = format.replace("%Y", String.valueOf(getYear()));
        format = format.replace("%M", String.valueOf(getMonth()));
        format = format.replace("%D", String.valueOf(getDay()));
        format = format.replace("%h", String.valueOf(getHour()));
        format = format.replace("%m", String.valueOf(getMinute()));
        format = format.replace("%s", String.valueOf(getSecond()));
        return format;
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
        removeFakeCalendar();
        fakeCalendar = Calendar.getInstance();
        fakeCalendar.set(YEAR, year);
        fakeCalendar.set(MONTH, month);
        fakeCalendar.set(DAY_OF_MONTH, day);
        fakeCalendar.set(HOUR_OF_DAY, hour);
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