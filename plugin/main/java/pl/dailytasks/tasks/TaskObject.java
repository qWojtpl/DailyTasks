package pl.dailytasks.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskObject {

    public String ID;
    public String event;
    public String initializedEvent;
    public int min;
    public int max;
    public int currentRandom;

    public TaskObject(String id, String event, int min, int max) {
        this.ID = id;
        this.event = event;
        this.min = min;
        this.max = max;
        this.Reinitialize();
        DailyTasks.TaskPool.add(this);
    }

    public void Reinitialize() {
        this.currentRandom = RandomNumber.randomInt(this.min, this.max);
        this.initializedEvent = this.event.replace("%rdm%", String.valueOf(this.currentRandom));
    }

    public void Complete(PlayerTasks pt) {
        if(!pt.completedTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            pt.completedTasks.put(DateManager.getFormattedDate("%Y/%M/%D"), new ArrayList<>());
        }
        pt.completedTasks.get(DateManager.getFormattedDate("%Y/%M/%D")).add(this);
    }

    public static void Check(Player p, String checkable) {
        for(TaskObject to : DailyTasks.todayTasks) {
            String[] taskEvent = to.initializedEvent.split(" ");
            String[] givenEvent = checkable.split(" ");
            if(!taskEvent[0].equalsIgnoreCase(givenEvent[0])) {
                continue;
            }
            if(!taskEvent[2].equalsIgnoreCase(givenEvent[1])) {
                continue;
            }
            PlayerTasks pt = PlayerTasks.Create(p);
            if(pt.checkIfCompleted(to)) {
                continue;
            }
            pt.InitializeProgress();
            int progress = pt.progress.get(DateManager.getFormattedDate("%Y/%M/%D")).getOrDefault(to, 0);
            progress++;
            if(progress >= to.currentRandom) {
                to.Complete(pt);
            }
            pt.progress.get(DateManager.getFormattedDate("%Y/%M/%D")).put(to, progress);
        }
    }

    public static void RandomizeTasks(int numberOfTasks) {
        DailyTasks.todayTasks = new ArrayList<>();
        int i = 0;
        int li = 0;
        while(true) {
            int rdm = RandomNumber.randomInt(0, DailyTasks.TaskPool.size()-1);
            TaskObject to = DailyTasks.TaskPool.get(rdm);
            if(!DailyTasks.todayTasks.contains(to)) {
                i++;
                to.Reinitialize();
                DailyTasks.todayTasks.add(to);
            }
            if(li > 255) {
                DailyTasks.main.getLogger().info("Tried to randomize " + numberOfTasks + ", but failed after " + li + " tries.");
                return;
            }
            if(i >= numberOfTasks) {
                break;
            }
            li++;
        }
    }

}
