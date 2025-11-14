package de.adventskalender.commands;

import de.adventskalender.AdventskalenderPlugin;
import de.adventskalender.gui.AdventskalenderGUI;
import de.adventskalender.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdventskalenderCommand implements CommandExecutor {

    private final AdventskalenderPlugin plugin;
    private final LanguageManager languageManager;
    private final AdventskalenderGUI gui;

    public AdventskalenderCommand(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.gui = new AdventskalenderGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("adventskalender.reload")) {
                sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        languageManager.getPrefix() + languageManager.getMessage("no-permission")));
                return true;
            }

            plugin.reload();
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    languageManager.getPrefix() + languageManager.getMessage("config-reloaded")));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    languageManager.getPrefix() + languageManager.getMessage("player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("adventskalender.use")) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    languageManager.getPrefix() + languageManager.getMessage("no-permission")));
            return true;
        }

        player.openInventory(gui.createGUI(player));
        return true;
    }
}

