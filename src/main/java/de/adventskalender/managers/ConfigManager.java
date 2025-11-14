package de.adventskalender.managers;

import de.adventskalender.AdventskalenderPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final AdventskalenderPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public String getGUITitle() {
        return config.getString("gui.title", "&6&lAdventskalender &7&l2024");
    }

    public int getGUISize() {
        return config.getInt("gui.size", 54);
    }

    public String getBackgroundItem() {
        return config.getString("gui.background-item", "GRAY_STAINED_GLASS_PANE");
    }

    public String getBackgroundName() {
        return config.getString("gui.background-name", "&r");
    }

    public String getDoorItem(int day) {
        return config.getString("doors." + day + ".item", "GREEN_WOOL");
    }

    public String getDoorName(int day) {
        return config.getString("doors." + day + ".name", "&a&lTag " + day);
    }

    public List<String> getDoorLore(int day) {
        return config.getStringList("doors." + day + ".lore");
    }

    public List<String> getDoorRewards(int day) {
        return config.getStringList("doors." + day + ".rewards");
    }

    public boolean isDayRestrictionEnabled() {
        return config.getBoolean("conditions.day-restriction", true);
    }

    public boolean isAllowPastDays() {
        return config.getBoolean("conditions.allow-past-days", false);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}

