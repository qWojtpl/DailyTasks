package pl.dailytasks.tasks;

import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataManager;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    public static HashMap<String, List<TaskObject>> todayTasks = new HashMap<>();

    public static void Check(Player p, String checkable) {
        for(TaskObject to : getTodayTasks()) {
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
            pt.getProgress().put(to, progress);
        }
    }

    public static void RandomizeTasks(int numberOfTasks) {
        /*if(todayTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            return;
        }*/
        List<TaskObject> pool = new ArrayList<>(DailyTasks.TaskPool);
        for(int i = 0; i < numberOfTasks; i++) {
            if(pool.size() == 0) break;
            int index = RandomNumber.randomInt(0, pool.size()-1);
            TaskObject to = pool.get(index);
            getTodayTasks().add(new TaskObject(to.ID, to.event, to.min, to.max));
            pool.remove(index);
        }
        DataManager.saveTodayTasks();
    }

    public static List<TaskObject> getTodayTasks() {
        if(!todayTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            todayTasks.put(DateManager.getFormattedDate("%Y/%M/%D"), new ArrayList<>());
        }
        return todayTasks.get(DateManager.getFormattedDate("%Y/%M/%D"));
    }

    public static List<String> getTodayTasksAsID() {
        if(!todayTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            todayTasks.put(DateManager.getFormattedDate("%Y/%M/%D"), new ArrayList<>());
        }
        List<String> ids = new ArrayList<>();
        for(TaskObject to : todayTasks.get(DateManager.getFormattedDate("%Y/%M/%D"))) {
            ids.add(to.ID);
        }
        return ids;
    }

    public static List<TaskObject> getTasks(int day) {
        if(!todayTasks.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            todayTasks.put(DateManager.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return todayTasks.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }

    public static void RemoveTodayTasks() {
        List<TaskObject> tasks = new ArrayList<>(getTodayTasks());
        for(TaskObject to : tasks) {
            getTodayTasks().remove(to);
        }
    }

    public static TaskObject getTaskFromID(String ID) {
        for(TaskObject to : DailyTasks.TaskPool) {
            if(to.ID.equals(ID)) {
                return to;
            }
        }
        return null;
    }

}
