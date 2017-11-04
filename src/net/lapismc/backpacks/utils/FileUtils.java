package net.lapismc.backpacks.utils;

import net.lapismc.backpacks.Backpacks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileUtils {

    private final File dir;
    private Backpacks plugin;

    public FileUtils(Backpacks p) {
        plugin = p;
        dir = new File(plugin.getDataFolder() + File.separator + "players");
    }

    public void saveInventory(Inventory inv, OfflinePlayer player, int number) {
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, player.getUniqueId().toString() + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        saveInventory(inv, config, number + "");

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Inventory getInventory(OfflinePlayer player, int number, int size) {
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, player.getUniqueId().toString() + ".yml");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Integer invSize;
        String sizeName;
        if (size == 1) {
            invSize = plugin.getConfig().getInt("Size.small");
            sizeName = plugin.getColoredMessage("Small");
        } else if (size == 2) {
            invSize = plugin.getConfig().getInt("Size.medium");
            sizeName = plugin.getColoredMessage("Medium");
        } else if (size == 3) {
            invSize = plugin.getConfig().getInt("Size.large");
            sizeName = plugin.getColoredMessage("Large");
        } else {
            invSize = plugin.getConfig().getInt("Size.small");
            sizeName = "NULL";
        }
        Integer configInventoryNumber = size * plugin.getConfig().getInt("BackpacksPerSize") + number;
        Inventory inv = getInventory(config, configInventoryNumber + "");
        if (inv == null) {
            return Bukkit.createInventory(null, invSize, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("InventoryNameFormat")
                    .replaceAll("%player%", player.getName()).replaceAll("%number%", number + "").replaceAll("%size%", sizeName)));
        } else {
            return inv;
        }
    }

    private void saveInventory(Inventory inventory, FileConfiguration file, String path) {
        file.set(path, null);
        List<ItemStack> inv = Arrays.asList(inventory.getContents());
        file.set(path + ".contents", inv);
        file.set(path + ".maxstacksize", inventory.getMaxStackSize());
        file.set(path + ".inventorytitle", inventory.getTitle());
        file.set(path + ".inventorysize", inventory.getSize());
    }

    private Inventory getInventory(FileConfiguration file, String path) {
        if (file.contains(path)) {
            Inventory inv = Bukkit.createInventory(null, file.getInt(path + ".inventorysize"), file.getString(path + ".inventorytitle"));
            inv.setMaxStackSize(file.getInt(path + ".maxstacksize"));
            @SuppressWarnings("unchecked") List<ItemStack> items = (List<ItemStack>) file.get(path + ".contents");
            ItemStack[] itemsArray = new ItemStack[items.size()];
            itemsArray = items.toArray(itemsArray);
            inv.setContents(itemsArray);
            return inv;
        }
        return null;
    }

    public OfflinePlayer getPlayerFromMap(Map<OfflinePlayer, Integer> map) {
        return (OfflinePlayer) map.keySet().toArray()[0];
    }

    public Integer getIntegerFromMap(Map<OfflinePlayer, Integer> map) {
        return (Integer) map.values().toArray()[0];
    }

}
