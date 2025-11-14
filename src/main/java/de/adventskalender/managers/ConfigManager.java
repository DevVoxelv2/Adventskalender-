package de.adventskalender.managers;

import de.adventskalender.AdventskalenderPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigManager {

    private final AdventskalenderPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration rewardsConfig;
    private File rewardsFile;

    public ConfigManager(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadRewardsConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadRewardsConfig();
    }

    private void loadRewardsConfig() {
        rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        if (!rewardsFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public String getGUITitle() {
        return config.getString("gui.title", "&6&lAdventskalender &7&l2025");
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
        // Lade Belohnungen aus rewards.yml
        List<String> rewards = rewardsConfig.getStringList(day + ".rewards");
        // Fallback auf config.yml falls rewards.yml leer ist
        if (rewards.isEmpty()) {
            rewards = config.getStringList("doors." + day + ".rewards");
        }
        return rewards;
    }

    public int getYear() {
        return config.getInt("year", 2025);
    }

    public String getTimezone() {
        return config.getString("timezone", "Europe/Berlin");
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

