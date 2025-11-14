package de.adventskalender;

import de.adventskalender.commands.AdventskalenderCommand;
import de.adventskalender.database.DatabaseManager;
import de.adventskalender.gui.AdventskalenderGUI;
import de.adventskalender.listeners.GUIListener;
import de.adventskalender.managers.ConfigManager;
import de.adventskalender.managers.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class AdventskalenderPlugin extends JavaPlugin {

    private static AdventskalenderPlugin instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private Economy economy;
    private boolean vaultEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        
        // Config laden
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Sprache laden
        languageManager = new LanguageManager(this);
        languageManager.loadLanguage(configManager.getLanguage());
        
        // Datenbank initialisieren
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        // Vault Economy setup (optional)
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultEnabled = setupEconomy();
            if (vaultEnabled) {
                getLogger().info("Vault Economy wurde erfolgreich geladen!");
            }
        } else {
            getLogger().info("Vault wurde nicht gefunden. Economy-Funktionen sind deaktiviert.");
        }
        
        // Commands registrieren
        getCommand("adventskalender").setExecutor(new AdventskalenderCommand(this));
        
        // Listener registrieren
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info("Adventskalender Plugin wurde erfolgreich geladen!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("Adventskalender Plugin wurde entladen!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        String newLanguage = configManager.getLanguage();
        String oldLanguage = languageManager.getCurrentLanguage();
        
        if (!newLanguage.equals(oldLanguage)) {
            languageManager.loadLanguage(newLanguage);
        }
    }

    public static AdventskalenderPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
}

