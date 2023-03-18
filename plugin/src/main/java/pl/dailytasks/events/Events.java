package pl.dailytasks.events;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskManager;

public class Events implements Listener {

    /*
    Why "priority = EventPriority.HIGHEST"?
    by Spigot documentation:
    "Event call is critical and must have the final say in what happens to the event"
    Source: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html
     */
    
    private final TaskManager tm = DailyTasks.getInstance().getTaskManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerTasks.Create(event.getPlayer());
        tm.Check(event.getPlayer(), "join server");
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if(event.getEntity().getKiller() != null) {
            tm.Check(event.getEntity().getKiller(), "kill " + event.getEntity().getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if(event.isCancelled()) return;
        tm.Check(event.getPlayer(), "break " + event.getBlock().getType().name());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) return;
        tm.Check(event.getPlayer(), "place " + event.getBlock().getType().name());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if(event.isCancelled()) return;
        if(event.getEntity() instanceof Player) {
            for(int i = 0; i < event.getItem().getItemStack().getAmount(); i++) {
                tm.Check((Player) event.getEntity(), "pickup " + event.getItem().getItemStack().getType());
            }
            tm.Check((Player) event.getEntity(), "T_pickup " + event.getItem().getItemStack().getType());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if(event.isCancelled()) return;
        for(int i = 0; i < event.getItemDrop().getItemStack().getAmount(); i++) {
            tm.Check(event.getPlayer(), "drop " + event.getItemDrop().getItemStack().getType());
        }
        tm.Check(event.getPlayer(), "T_drop " + event.getItemDrop().getItemStack().getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent event) {
        if(event.isCancelled()) return;
        ItemStack is;
        if(event.isShiftClick()) {
            is = getCraftedItemStack(event);
        } else {
            is = event.getCurrentItem();
        }
        for(int i = 0; i < is.getAmount(); i++) {
            tm.Check((Player) event.getWhoClicked(), "craft " + is.getType());
        }
        tm.Check((Player) event.getWhoClicked(), "T_craft " + is.getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchant(EnchantItemEvent event) {
        if(event.isCancelled()) return;
        tm.Check(event.getEnchanter(), "enchant " + event.getItem().getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        if(event.isCancelled()) return;
        if(event.getCaught() != null && event.getCaught() instanceof Item && event.getState().toString().equals("CAUGHT_FISH")) {
            tm.Check(event.getPlayer(), "fish " + ((Item) event.getCaught()).getItemStack().getType());
        }
        if(event.getCaught() != null && !event.getState().toString().equals("CAUGHT_FISH")) {
            tm.Check(event.getPlayer(), "catch " + event.getCaught().getType());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShootBow(EntityShootBowEvent event) {
        if(event.isCancelled()) return;
        if(event.getEntity() instanceof Player) {
            tm.Check((Player) event.getEntity(), "shoot " + event.getBow().getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThrow(ProjectileLaunchEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getEntity().getShooter() instanceof Player)) return;
        Player p = (Player) event.getEntity().getShooter();
        if(event.getEntity() instanceof Trident) {
            tm.Check(p, "throw trident");
        } else if(event.getEntity() instanceof Snowball) {
            tm.Check(p, "throw snowball");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if(event.isCancelled()) return;
        tm.Check(event.getPlayer(), "command " + event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if(event.isCancelled()) return;
        tm.Check(event.getPlayer(), "chat " + event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSign(SignChangeEvent event) {
        if(event.isCancelled()) return;
        if(event.getLines().length > 0) {
            String message = event.getLine(0);
            for (int i = 1; i < event.getLines().length; i++) {
                if(event.getLine(i).length() > 0 && !event.getLine(i).equals(" ")) {
                    message += " " + event.getLine(i);
                }
            }
            tm.Check(event.getPlayer(), "sign " + message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreed(EntityBreedEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getBreeder() instanceof Player)) return;
        tm.Check((Player) event.getBreeder(), "breed " + event.getEntity().getType().name());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEat(PlayerItemConsumeEvent event) {
        if(event.isCancelled()) return;
        tm.Check(event.getPlayer(), "eat " + event.getItem().getType().name());
    }

    // Method source: https://www.spigotmc.org/threads/get-accurate-crafting-result-from-shift-clicking.446520/
    private ItemStack getCraftedItemStack(CraftItemEvent event) {
        final ItemStack recipeResult = event.getRecipe().getResult();
        final int resultAmt = recipeResult.getAmount();
        int leastIngredient = -1;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                final int re = item.getAmount() * resultAmt;
                if (leastIngredient == -1 || re < leastIngredient) {
                    leastIngredient = item.getAmount() * resultAmt;
                }
            }
        }
        return new ItemStack(recipeResult.getType(), leastIngredient, recipeResult.getDurability());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        for(GUIHandler gui : GUIHandler.getRegisteredInventories()) { // Loop through registered inventories
            if (event.getInventory().equals(gui.getInventory())) { // If player's inventory is in registered inventories
                event.setCancelled(true); // Cancel drag event
                break; // Exit loop
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        for(GUIHandler gui : GUIHandler.getRegisteredInventories()) { // Loop through registered inventories
            if (event.getInventory().equals(gui.getInventory())) { // If player's inventory is in registered inventories
                event.setCancelled(true); // Cancel click event
                return;
            }
        }
        InventoryType invType = event.getInventory().getType();
        if(invType.equals(InventoryType.FURNACE) || invType.equals(InventoryType.BLAST_FURNACE) || invType.equals(InventoryType.SMOKER)) {
            if(event.getSlot() == 2) {
                ItemStack item = event.getInventory().getItem(2);
                if (item != null) {
                    for (int i = 0; i < item.getAmount(); i++) {
                        tm.Check((Player) event.getWhoClicked(), "furnace " + item.getType().name());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) { // Remove inventory from registered inventories on close
        for(GUIHandler gui : GUIHandler.getRegisteredInventories()) { // Loop through registered inventories
            if (event.getInventory().equals(gui.getInventory())) { // Check if looped inventory equals closed inventory
                GUIHandler.getRegisteredInventories().remove(gui); // Remove inventory from registered inventories
                break; // Exit loop
            }
        }
    }

}
