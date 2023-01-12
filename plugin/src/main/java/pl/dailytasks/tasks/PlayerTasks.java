package pl.dailytasks.tasks;

import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.util.DateManager;

import java.util.HashMap;

public class PlayerTasks {

    public Player player;
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>> completedDays = new HashMap<>();
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

    public boolean checkIfCompleted(int day) {
        if(completedDays.containsKey(DateManager.getYear()) && completedDays.containsKey(DateManager.getMonth()) && completedDays.containsKey(day)) {
            return completedDays.get(DateManager.getYear()).get(DateManager.getMonth()).get(day);
        }
        return false;
    }
}
