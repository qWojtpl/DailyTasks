package pl.dailytasks.tasks;

import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
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
        DailyTasks.getInstance().getPlayerTaskList().put(p, this);
        DailyTasks.getInstance().getDataHandler().loadPlayer(p);
    }

    public static PlayerTasks Create(Player p) {
        HashMap<Player, PlayerTasks> playerTaskList = DailyTasks.getInstance().getPlayerTaskList();
        if(playerTaskList.containsKey(p)) {
            playerTaskList.get(p).player = p;
            return playerTaskList.get(p);
        }
        return new PlayerTasks(p);
    }

    public boolean checkIfCompletedDay(int day) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(completedTasks.containsKey(dm.getFormattedDate("%Y/%M/" + day))) {
            return (completedTasks.get(dm.getFormattedDate("%Y/%M/" + day)).size() >= 3);
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
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(completedTasks.containsKey(dm.getFormattedDate("%Y/%M/%D"))) {
            return (getProgress().get(index) >= DailyTasks.getInstance().getTaskManager().getTodayTasks().get(index).getCurrentRandom());
        }
        return false;
    }

    public boolean checkIfCompletedMonth() {
        int c = 0;
        DateManager dm = DailyTasks.getInstance().getDateManager();
        for(int i = 1; i <= dm.getDaysOfMonth(); i++) {
            if(checkIfCompletedDay(i)) {
                c++;
            }
        }
        return (c >= dm.getDaysOfMonth());
    }

    public List<Integer> getProgress() {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!progress.containsKey(dm.getFormattedDate("%Y/%M/%D"))) {
            List<Integer> a = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                a.add(0);
            }
            progress.put(dm.getFormattedDate("%Y/%M/%D"), a);
        }
        return progress.get(dm.getFormattedDate("%Y/%M/%D"));
    }

    public List<Integer> getProgressByDay(int day) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!progress.containsKey(dm.getFormattedDate("%Y/%M/" + day))) {
            List<Integer> a = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                a.add(0);
            }
            progress.put(dm.getFormattedDate("%Y/%M/" + day), a);
        }
        return progress.get(dm.getFormattedDate("%Y/%M/" + day));
    }

    public List<Integer> getCompletedTasks(int day) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!completedTasks.containsKey(dm.getFormattedDate("%Y/%M/" + day))) {
            completedTasks.put(dm.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return completedTasks.get(dm.getFormattedDate("%Y/%M/" + day));
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
