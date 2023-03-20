package pl.dailytasks.tasks;

import lombok.Getter;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

@Getter
public class TaskObject {

    private final String id;
    private final String event;
    private String initializedEvent;
    private final int min;
    private final int max;
    private int currentRandom;

    public TaskObject(String id, String event, int min, int max) {
        this.id = id;
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
        DateManager dm = DailyTasks.getInstance().getDateManager();
        pt.getCompletedTasks(Integer.parseInt(dm.getFormattedDate("%D"))).add(index);
        DailyTasks.getInstance().getDataHandler().addPlayerCompletedTask(pt, index);
    }

}
