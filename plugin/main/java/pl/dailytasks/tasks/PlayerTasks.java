package pl.dailytasks.tasks;

import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PlayerTasks {

    public Player player;
    public HashMap<String, List<TaskObject>> completedTasks = new HashMap<>();
    public HashMap<TaskObject, Integer> progress = new HashMap<>();

    public PlayerTasks(Player p) {
        this.player = p;
        DailyTasks.playerTaskList.put(p, this);
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
        if(completedTasks.containsKey(DateManager.getFormattedDate("%H/%M/" + day))) {
            if(completedTasks.get(DateManager.getFormattedDate("%H/%M/" + day)).size() == 3) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfCompleted(TaskObject to) {
        if(completedTasks.containsKey(DateManager.getFormattedDate("%H/%M/%D"))) {
            List<TaskObject> ct = completedTasks.get(DateManager.getFormattedDate("%H/%M/%D"));
            for(TaskObject to2 : ct) {
                if(to2 == to) return true;
            }
        }
        return false;
    }
}
