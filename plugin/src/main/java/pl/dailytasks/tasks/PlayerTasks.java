package pl.dailytasks.tasks;

import lombok.Getter;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerTasks {

    private Player player;
    private final HashMap<String, List<Integer>> completedTasks = new HashMap<>();
    private final HashMap<String, List<Integer>> progress = new HashMap<>();

    public PlayerTasks(Player p) {
        this.player = p;
        DailyTasks.playerTaskList.put(p, this);
        DataHandler.loadPlayer(p);
    }

    public static PlayerTasks Create(Player p) {
        if(DailyTasks.playerTaskList.containsKey(p)) {
            DailyTasks.playerTaskList.get(p).player = p;
            return DailyTasks.playerTaskList.get(p);
        }
        return new PlayerTasks(p);
    }

    public boolean checkIfCompletedDay(int day) {
        if(completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            return (completedTasks.get(DateManager.getFormattedDate("%Y/%M/" + day)).size() >= 3);
        }
        return false;
    }

    public boolean checkIfCompletedDayByDate(String date) {
        if(completedTasks.containsKey(date)) {
            return (completedTasks.get(date).size() >= 3);
        }
        return false;
    }

    public boolean checkIfCompletedDayTask(int index) {
        if(completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            return (getProgress().get(index) >= DailyTasks.getInstance().getTaskManager().getTodayTasks().get(index).currentRandom);
        }
        return false;
    }

    public boolean checkIfCompletedMonth() {
        int c = 0;
        for(int i = 1; i <= DateManager.getDaysOfMonth(); i++) {
            if(checkIfCompletedDay(i)) {
                c++;
            }
        }
        return (c >= DateManager.getDaysOfMonth());
    }

    public List<Integer> getProgress() {
        if(!progress.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            List<Integer> a = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                a.add(0);
            }
            progress.put(DateManager.getFormattedDate("%Y/%M/%D"), a);
        }
        return progress.get(DateManager.getFormattedDate("%Y/%M/%D"));
    }

    public List<Integer> getProgressByDay(int day) {
        if(!progress.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            List<Integer> a = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                a.add(0);
            }
            progress.put(DateManager.getFormattedDate("%Y/%M/" + day), a);
        }
        return progress.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }

    public List<Integer> getCompletedTasks(int day) {
        if(!completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            completedTasks.put(DateManager.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return completedTasks.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashMap<String, List<Integer>> getSourceCompletedTasks() {
        return this.completedTasks;
    }

    public HashMap<String, List<Integer>> getSourceProgress() {
        return this.progress;
    }
}
