package de.adventskalender.managers;

import de.adventskalender.AdventskalenderPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LanguageManager {

    private final AdventskalenderPlugin plugin;
    private FileConfiguration languageConfig;
    private String currentLanguage;
    private File languageFile;

    public LanguageManager(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage(String language) {
        // Lösche alte Sprachdatei wenn Sprache geändert wird
        if (currentLanguage != null && !currentLanguage.equals(language) && languageFile != null && languageFile.exists()) {
            try {
                Files.deleteIfExists(languageFile.toPath());
                plugin.getLogger().info("Alte Sprachdatei (" + currentLanguage + ") wurde gelöscht.");
            } catch (Exception e) {
                plugin.getLogger().warning("Konnte alte Sprachdatei nicht löschen: " + e.getMessage());
            }
        }

        currentLanguage = language;
        languageFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");

        // Erstelle Sprachdatei falls sie nicht existiert
        if (!languageFile.exists()) {
            plugin.saveResource("messages_" + language + ".yml", false);
            plugin.getLogger().info("Sprachdatei messages_" + language + ".yml wurde erstellt.");
        }

        // Lade Sprachdatei
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Lade Standardwerte aus JAR
        InputStream defaultStream = plugin.getResource("messages_" + language + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            languageConfig.setDefaults(defaultConfig);
        }
    }

    public String getMessage(String key) {
        if (languageConfig == null) {
            return "&cLanguage not loaded!";
        }
        String message = languageConfig.getString(key);
        if (message == null) {
            return "&cMessage not found: " + key;
        }
        return message.replace("{prefix}", getPrefix());
    }

    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }

    public String getPrefix() {
        return languageConfig != null ? languageConfig.getString("prefix", "&8[&6Adventskalender&8] &r") : "&8[&6Adventskalender&8] &r";
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}

