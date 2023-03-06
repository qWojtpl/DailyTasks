package pl.dailytasks.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class GUIHandler {

    private final Player player;
    private Inventory inventory;
    private final static List<GUIHandler> registeredInventories = new ArrayList<>();

    public GUIHandler(Player p) {
        this.player = p;
        this.Create();
    }

    public static void New(Player p) {
        GUIHandler handler = new GUIHandler(p);
        registeredInventories.add(handler);
        handler.Open();
        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F); // Play sound
    }

    public void Open() {
        this.getPlayer().openInventory(this.inventory);
    }

    public void Create() {
        Messages messages = DailyTasks.getInstance().getMessages();
        inventory = Bukkit.createInventory(null, 54, messages.getMessage("title"));
        for(int i = 0; i < 54; i++) {
            AddItem(i, Material.BLACK_STAINED_GLASS_PANE, 1, " ", " ", false);
        }
        int day = 1;
        DateManager dm = DailyTasks.getInstance().getDateManager();
        int maxDays = dm.getDaysOfMonth();
        int currentDay = dm.getDay();
        for(int i = 1; i <= 43; i++) {
            if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36) continue;
            if(day > maxDays) {
                AddItem(i, Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", " ", false);
                continue;
            }
            Material m = Material.WHITE_CONCRETE;
            String task = "";
            PlayerTasks pt = PlayerTasks.Create(player);
            if(day <= currentDay) {
                TaskManager tm = DailyTasks.getInstance().getTaskManager();
                task = messages.getMessage("tasks") + "%nl%";
                int j = 0;
                if(tm.getTasks(day).size() == 0) {
                    task = task + "§c" + messages.getMessage("day-without-task");
                } else {
                    for (TaskObject to : tm.getTasks(day)) {
                        int playerProgress = pt.getProgressByDay(day).get(j);
                        int maxProgress = to.currentRandom;
                        String progress = playerProgress + "/" + maxProgress;
                        task = task + "§2" + to.initializedEvent + " " + progress + "%nl%";
                        j++;
                    }
                }
                if(day == currentDay) {
                    m = Material.YELLOW_CONCRETE;
                }
                if (pt.checkIfCompletedDay(day)) {
                    m = Material.GREEN_CONCRETE;
                    task = task + messages.getMessage("completed");
                } else if(day != currentDay) {
                    m = Material.RED_CONCRETE;
                    task = task + messages.getMessage("not-completed");
                }
            } else {
                task = messages.getMessage("locked-task");
            }
            AddItem(i, m, day, messages.getMessage("day") + day, task, (day == currentDay));
            day++;
        }
        AddItem(49, Material.BOOKSHELF, 1,
                messages.getMessage("info-title"), messages.getMessage("info-description"), false);

    }

    public void AddItem(int slot, Material material, int amount, String name, String lore, boolean isGlowing) {
        ItemStack item = new ItemStack(material, amount); // Create item
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name); // Set name
        meta.setLore(Arrays.asList(lore.split("%nl%")));
        if(isGlowing) { // Create glow effect
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);

        inventory.setItem(slot, item); // Add item to GUI
    }

    public static List<GUIHandler> getRegisteredInventories() {
        return registeredInventories;
    }

    public static void closeAllInventories() {
        for(GUIHandler gui : GUIHandler.getRegisteredInventories()) {
            gui.getPlayer().closeInventory();
        }
    }

}
