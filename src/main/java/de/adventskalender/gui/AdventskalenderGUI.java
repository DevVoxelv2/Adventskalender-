package de.adventskalender.gui;

import de.adventskalender.AdventskalenderPlugin;
import de.adventskalender.managers.ConfigManager;
import de.adventskalender.managers.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdventskalenderGUI {

    private final AdventskalenderPlugin plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;

    public AdventskalenderGUI(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.languageManager = plugin.getLanguageManager();
    }

    public Inventory createGUI(Player player) {
        String title = languageManager.getMessage("gui-title");
        if (title.equals("&cMessage not found: gui-title")) {
            title = configManager.getGUITitle();
        }
        
        Inventory gui = Bukkit.createInventory(null, configManager.getGUISize(), 
                org.bukkit.ChatColor.translateAlternateColorCodes('&', title));

        // Hintergrund (Glas) nur rundherum füllen
        Material backgroundMaterial = Material.valueOf(configManager.getBackgroundItem());
        ItemStack background = new ItemStack(backgroundMaterial);
        ItemMeta backgroundMeta = background.getItemMeta();
        if (backgroundMeta != null) {
            backgroundMeta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', 
                    configManager.getBackgroundName()));
            background.setItemMeta(backgroundMeta);
        }

        // Nur Rand-Slots mit Glas füllen
        int size = gui.getSize();
        int rows = size / 9;
        
        for (int i = 0; i < size; i++) {
            if (isBorderSlot(i, rows)) {
                gui.setItem(i, background);
            }
        }

        // Türen platzieren (1-24)
        for (int day = 1; day <= 24; day++) {
            int slot = getSlotForDay(day);
            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, createDoorItem(player, day));
            }
        }

        return gui;
    }

    private boolean isBorderSlot(int slot, int rows) {
        int row = slot / 9;
        int col = slot % 9;
        
        // Obere oder untere Reihe
        if (row == 0 || row == rows - 1) {
            return true;
        }
        
        // Linke oder rechte Spalte
        if (col == 0 || col == 8) {
            return true;
        }
        
        return false;
    }

    private int getSlotForDay(int day) {
        // 6x4 Grid für 24 Türen
        // Zeile 1: Tage 1-6
        // Zeile 2: Tage 7-12
        // Zeile 3: Tage 13-18
        // Zeile 4: Tage 19-24
        
        int row = (day - 1) / 6;
        int col = (day - 1) % 6;
        
        // Start bei Slot 10 (2. Zeile, 2. Spalte)
        return 10 + (row * 9) + col;
    }

    private ItemStack createDoorItem(Player player, int day) {
        boolean hasOpened = plugin.getDatabaseManager().hasOpenedDoor(player.getUniqueId(), day);
        boolean isAvailable = isDayAvailable(day);
        
        Material material;
        String name;
        List<String> lore = new ArrayList<>();

        if (hasOpened) {
            // Bereits geöffnet - grüne Wolle
            material = Material.GREEN_WOOL;
            name = languageManager.getMessage("door-opened-state");
            lore.add(languageManager.getMessage("door-opened-state"));
        } else if (isAvailable) {
            // Verfügbar zum Öffnen
            material = Material.valueOf(configManager.getDoorItem(day));
            name = configManager.getDoorName(day);
            List<String> configLore = configManager.getDoorLore(day);
            lore.addAll(configLore);
            lore.add(languageManager.getMessage("door-available"));
        } else {
            // Noch nicht verfügbar - graue Wolle
            material = Material.GRAY_WOOL;
            name = configManager.getDoorName(day);
            lore.add(languageManager.getMessage("door-locked-state"));
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
            
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
            
            // Verhindere, dass Items aus der GUI genommen werden können
            meta.setUnbreakable(true);
            
            item.setItemMeta(meta);
        }

        return item;
    }

    private boolean isDayAvailable(int day) {
        if (!configManager.isDayRestrictionEnabled()) {
            return true;
        }

        if (configManager.isAllowPastDays()) {
            return day <= getCurrentDay();
        }

        return day == getCurrentDay();
    }

    private int getCurrentDay() {
        try {
            ZoneId zoneId = ZoneId.of(configManager.getTimezone());
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            LocalDate localDate = now.toLocalDate();
            int configuredYear = configManager.getYear();
            
            // Prüfe ob wir im richtigen Jahr und Dezember sind
            if (localDate.getYear() == configuredYear && localDate.getMonth() == Month.DECEMBER && localDate.getDayOfMonth() <= 24) {
                return localDate.getDayOfMonth();
            }
            
            // Wenn nicht im konfigurierten Jahr/Dezember oder nach dem 24., zeige alle Türen als verfügbar (für Tests)
            if (configManager.isAllowPastDays()) {
                return 24;
            }
            
            // Wenn nicht im richtigen Zeitraum, zeige keine Türen als verfügbar
            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Zeitzone: " + configManager.getTimezone() + " - Verwende System-Zeitzone");
            // Fallback auf System-Zeitzone
            LocalDate now = LocalDate.now();
            int configuredYear = configManager.getYear();
            
            if (now.getYear() == configuredYear && now.getMonth() == Month.DECEMBER && now.getDayOfMonth() <= 24) {
                return now.getDayOfMonth();
            }
            
            if (configManager.isAllowPastDays()) {
                return 24;
            }
            
            return 0;
        }
    }

    public void openDoor(Player player, int day) {
        // Prüfe ob bereits geöffnet
        if (plugin.getDatabaseManager().hasOpenedDoor(player.getUniqueId(), day)) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    languageManager.getPrefix() + languageManager.getMessage("door-already-opened")));
            return;
        }

        // Prüfe ob verfügbar
        if (!isDayAvailable(day)) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    languageManager.getPrefix() + languageManager.getMessage("door-not-available")));
            return;
        }

        // Öffne Tür
        plugin.getDatabaseManager().setDoorOpened(player.getUniqueId(), day);

        // Gebe Belohnungen
        giveRewards(player, day);

        // Nachricht
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                languageManager.getPrefix() + languageManager.getMessage("door-opened", "day", String.valueOf(day))));
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                languageManager.getPrefix() + languageManager.getMessage("door-reward")));

        // GUI aktualisieren
        player.openInventory(createGUI(player));
    }

    private void giveRewards(Player player, int day) {
        List<String> rewards = configManager.getDoorRewards(day);
        for (String reward : rewards) {
            reward = reward.replace("{player}", player.getName());
            
            // Parse und gebe Belohnung
            if (reward.startsWith("give ")) {
                String[] parts = reward.substring(5).split(" ");
                if (parts.length >= 2) {
                    try {
                        Material material = Material.valueOf(parts[0].toUpperCase());
                        int amount = Integer.parseInt(parts[1]);
                        ItemStack item = new ItemStack(material, amount);
                        player.getInventory().addItem(item);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Fehler beim Geben der Belohnung: " + reward);
                    }
                }
            } else if (reward.startsWith("money ") && plugin.isVaultEnabled() && plugin.getEconomy() != null) {
                try {
                    double amount = Double.parseDouble(reward.substring(6));
                    plugin.getEconomy().depositPlayer(player, amount);
                } catch (Exception e) {
                    plugin.getLogger().warning("Fehler beim Geben von Geld: " + reward);
                }
            } else {
                // Führe als Befehl aus
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward);
            }
        }
    }
}

