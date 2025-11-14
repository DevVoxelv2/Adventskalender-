package de.adventskalender.listeners;

import de.adventskalender.AdventskalenderPlugin;
import de.adventskalender.gui.AdventskalenderGUI;
import de.adventskalender.managers.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final AdventskalenderPlugin plugin;
    private final ConfigManager configManager;
    private final AdventskalenderGUI gui;

    public GUIListener(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.gui = new AdventskalenderGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Prüfe ob es die Adventskalender GUI ist
        if (!title.contains("Adventskalender") && !title.contains("Advent Calendar") && 
            !title.contains("Calendrier de l'Avent")) {
            return;
        }

        // Verhindere alle Interaktionen mit der GUI
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            return;
        }
        
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        Material backgroundMaterial = Material.valueOf(configManager.getBackgroundItem());

        // Ignoriere Hintergrund-Items
        if (clickedItem.getType() == backgroundMaterial) {
            return;
        }

        // Finde die Türnummer basierend auf dem Slot
        int slot = event.getSlot();
        int day = getDayFromSlot(slot);

        if (day > 0 && day <= 24) {
            gui.openDoor(player, day);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        String title = event.getView().getTitle();
        
        // Prüfe ob es die Adventskalender GUI ist
        if (title.contains("Adventskalender") || title.contains("Advent Calendar") || 
            title.contains("Calendrier de l'Avent")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Stelle sicher, dass keine Items aus der GUI genommen werden können
        // (Zusätzliche Sicherheit, falls jemand versucht Items zu nehmen)
    }

    private int getDayFromSlot(int slot) {
        // Umgekehrte Berechnung von getSlotForDay
        int row = (slot - 10) / 9;
        int col = (slot - 10) % 9;
        
        if (row < 0 || row >= 4 || col < 0 || col >= 6) {
            return -1;
        }
        
        return (row * 6) + col + 1;
    }
}

