package pl.dailytasks.tasks;

import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.util.RandomNumber;

import java.util.ArrayList;
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
        this.currentRandom = RandomNumber.randomInt(min, max);
        this.initializedEvent = this.event.replace("%rdm%", String.valueOf(this.currentRandom));
        DailyTasks.TaskPool.add(this);
    }

    public static void Check(Player p, String checkable) {
        for(TaskObject to : DailyTasks.todayTasks) {
            String[] taskEvent = to.initializedEvent.split(" ");
            String[] givenEvent = checkable.split(" ");
            if(!taskEvent[0].equalsIgnoreCase(givenEvent[0])) {
                continue;
            }
            if(!taskEvent[1].equalsIgnoreCase(givenEvent[2])) {
                continue;
            }

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
