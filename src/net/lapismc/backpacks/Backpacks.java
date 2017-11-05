package net.lapismc.backpacks;

import net.lapismc.backpacks.utils.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Backpacks extends JavaPlugin {

    FileUtils fUtils;
    private YamlConfiguration messages;
    public HashMap<Inventory, Map<OfflinePlayer, Integer>> inventories = new HashMap<>();

    public void onEnable() {
        fUtils = new FileUtils(this);
        new BackpackCommand(this);
        new PlayerListeners(this);

        saveDefaultConfig();
        saveDefaultMessages();
    }

    public void onDisable() {
        //Save inventories that are loaded
        for (Inventory inv : inventories.keySet()) {
            fUtils.saveInventory(inv, fUtils.getPlayerFromMap(inventories.get(inv)), fUtils.getIntegerFromMap(inventories.get(inv)));
        }
    }

    void reloadMessages() {
        try {
            messages.load(new File(getDataFolder(), "Messages.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultMessages() {
        File messagesFile = new File(getDataFolder(), "Messages.yml");
        if (!messagesFile.exists()) {
            saveResource("Messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getColoredMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(path));
    }

    String getMessage() {
        return ChatColor.stripColor(getColoredMessage("Error.MustBePlayer"));
    }

}
