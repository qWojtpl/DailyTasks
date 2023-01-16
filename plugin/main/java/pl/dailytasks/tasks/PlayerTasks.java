package pl.dailytasks.tasks;

import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataManager;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerTasks {

    public Player player;
    public HashMap<String, List<TaskObject>> completedTasks = new HashMap<>();
    public HashMap<String, HashMap<TaskObject, Integer>> progress = new HashMap<>();

    public PlayerTasks(Player p) {
        this.player = p;
        DailyTasks.playerTaskList.put(p, this);
        DataManager.createPlayer(p);
    }

    public static PlayerTasks Create(Player p) {
        if(DailyTasks.playerTaskList.containsKey(p)) {
            DailyTasks.playerTaskList.get(p).player = p;
            return DailyTasks.playerTaskList.get(p);
        }
        return new PlayerTasks(p);
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean checkIfCompletedDay(int day) {
        if(completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            if(completedTasks.get(DateManager.getFormattedDate("%Y/%M/" + day)).size() == 3) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfCompleted(TaskObject to) {
        if(completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            List<TaskObject> ct = completedTasks.get(DateManager.getFormattedDate("%Y/%M/%D"));
            for(TaskObject to2 : ct) {
                if(to2 == to) return true;
            }
        }
        return false;
    }

    public void InitializeProgress() {
        if(!this.progress.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            this.progress.put(DateManager.getFormattedDate("%Y/%M/%D"), new HashMap<>());
        }
    }

    public HashMap<TaskObject, Integer> getProgress() {
        if(!progress.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            progress.put(DateManager.getFormattedDate("%Y/%M/%D"), new HashMap<>());
        }
        return progress.get(DateManager.getFormattedDate("%Y/%M/%D"));
    }

    public List<TaskObject> getCompletedTasks(int day) {
        if(!completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            completedTasks.put(DateManager.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return completedTasks.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }
}
