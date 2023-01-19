package pl.dailytasks.tasks;

import pl.dailytasks.data.DataHandler;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;


public class TaskObject {

    public String event;
    public String initializedEvent;
    public int min;
    public int max;
    public int currentRandom;

    public TaskObject(String event, int min, int max) {
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
        pt.getCompletedTasks(Integer.parseInt(DateManager.getFormattedDate("%D"))).add(index);
        DataHandler.addPlayerCompletedTask(pt, index);
    }

}
