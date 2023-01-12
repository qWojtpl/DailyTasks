package pl.dailytasks.gui;

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
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIHandler {

    public Player player;
    public Inventory inventory;
    public static List<GUIHandler> registeredInventories = new ArrayList<>();

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

    public Player getPlayer() {
        return this.player;
    }

    public void Create() {
        inventory = Bukkit.createInventory(null, 54, DailyTasks.getMessage("title"));
        for(int i = 0; i < 54; i++) {
            AddItem(i, Material.BLACK_STAINED_GLASS_PANE, 1, " ", " ", false);
        }
        int day = 1;
        int maxDays = DateManager.getDaysOfMonth();
        int currentDay = DateManager.getDay();
        for(int i = 1; i <= 43; i++) {
            if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36) continue;
            if(day > maxDays) {
                AddItem(i, Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", " ", false);
                continue;
            }
            Material m = Material.WHITE_CONCRETE;
            String task = DailyTasks.getMessage("tasks") + "%nl%";
            for(TaskObject to : DailyTasks.todayTasks) {
                PlayerTasks pt = PlayerTasks.Create(player);
                int playerProgress = pt.progress.getOrDefault(to, 0);
                int maxProgress = to.currentRandom;
                String progress = playerProgress + "/" + maxProgress;
                task = task + "§2" + to.initializedEvent + " " + progress + "%nl%";
            }
            if(day < currentDay) {
                PlayerTasks pt = DailyTasks.playerTaskList.get(this.getPlayer());
                if(pt.checkIfCompleted(day)) {
                    m = Material.GREEN_CONCRETE;
                    task = task + DailyTasks.getMessage("completed");
                } else {
                    m = Material.RED_CONCRETE;
                    task = task + DailyTasks.getMessage("not-completed");
                }
            } else if(day == currentDay) {
                m = Material.YELLOW_CONCRETE;
            } else {
                task = DailyTasks.getMessage("locked-task");
            }
            AddItem(i, m, day, DailyTasks.getMessage("day") + day, task, (day == currentDay));
            day++;
        }
        AddItem(49, Material.BOOKSHELF, 1, DailyTasks.getMessage("info-title"), DailyTasks.getMessage("info-description"), false);

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

    public static void closeAllInventories() {
        for(GUIHandler gui : GUIHandler.registeredInventories) {
            gui.getPlayer().closeInventory();
        }
    }

}
