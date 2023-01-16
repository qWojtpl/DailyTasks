package pl.dailytasks.tasks;

import org.bukkit.Sound;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataManager;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

import java.text.MessageFormat;

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
    }

    public void Reinitialize() {
        this.currentRandom = RandomNumber.randomInt(this.min, this.max);
        this.initializedEvent = this.event.replace("%rdm%", String.valueOf(this.currentRandom));
    }

    public void Complete(PlayerTasks pt, int index) {
        pt.getCompletedTasks(Integer.parseInt(DateManager.getFormattedDate("%D"))).add(this);
        DataManager.addPlayerCompletedTask(pt, index);
    }

}
