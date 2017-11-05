package net.lapismc.backpacks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class BackpackCommand implements CommandExecutor {

    private Backpacks plugin;

    BackpackCommand(Backpacks p) {
        plugin = p;
        Bukkit.getServer().getPluginCommand("backpacks").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("backpacks")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("Error.MustBePlayer"));
                return true;
            }
            Player p = (Player) sender;
            if (args.length < 1) {
                sendHelp(sender);
            } else if (args.length == 1) {
                plugin.reloadConfig();
                plugin.reloadMessages();
            } else if (args.length == 2) {
                //backpack large 2
                String size = args[0];
                String number = args[1];
                if (!isSafe(p, size, number)) {
                    return true;
                }
                if (!isPermitted(p, size, number)) {
                    p.sendMessage(plugin.getColoredMessage("Error.NoPermission"));
                    return true;
                }
                if (Integer.parseInt(number) > plugin.getConfig().getInt("BackpacksPerSize")) {
                    p.sendMessage(plugin.getColoredMessage("Error.NumberTooBig").replaceAll("%max%", plugin.getConfig().getInt("BackpacksPerSize") + ""));
                    return true;
                }
                Inventory i = getInv(p, p, number, size);
                p.sendMessage(plugin.getColoredMessage("InventoryOpen").replaceAll("%size%", size));
                p.openInventory(i);
            } else if (args.length == 3) {
                //backpack large 2 dart2112
                String size = args[0];
                if (!(p.hasPermission("backpacks.see") || p.hasPermission("backpacks.edit"))) {
                    p.sendMessage(plugin.getColoredMessage("Error.NoPermission"));
                    return true;
                }
                String number = args[1];
                if (!isSafe(p, size, number)) {
                    return true;
                }
                String name = args[2];
                if (Integer.parseInt(number) > plugin.getConfig().getInt("BackpacksPerSize")) {
                    p.sendMessage(plugin.getColoredMessage("Error.NumberTooBig").replaceAll("%max%", plugin.getConfig().getInt("BackpacksPerSize") + ""));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                if (target == null || !target.hasPlayedBefore()) {
                    p.sendMessage(plugin.getColoredMessage("Error.PlayerDoesntExist"));
                    return true;
                }
                Inventory i = getInv(p, target, number, size);
                p.sendMessage(plugin.getColoredMessage("InventoryOpen").replaceAll("%size%", size));
                p.openInventory(i);
            } else if (args.length == 4 && args[3].equalsIgnoreCase("clear")) {
                if (!p.hasPermission("backpacks.edit")) {
                    p.sendMessage(plugin.getColoredMessage("Error.NoPermission"));
                    return true;
                }
                String size = args[0];
                String number = args[1];
                if (!isSafe(p, size, number)) {
                    return true;
                }
                String name = args[2];
                if (Integer.parseInt(number) > plugin.getConfig().getInt("BackpacksPerSize")) {
                    p.sendMessage(plugin.getColoredMessage("Error.NumberTooBig").replaceAll("%max%", plugin.getConfig().getInt("BackpacksPerSize") + ""));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                if (target == null || !target.hasPlayedBefore()) {
                    p.sendMessage(plugin.getColoredMessage("Error.PlayerDoesntExist"));
                    return true;
                }
                Integer sizeValue = parseSize(size);
                Inventory inv = plugin.fUtils.getInventory(target, Integer.parseInt(number), sizeValue);
                inv.clear();
                plugin.fUtils.saveInventory(inv, target, Integer.parseInt(number));
                Map<OfflinePlayer, Integer> map = new HashMap<>();
                Integer configInventoryNumber = sizeValue * plugin.getConfig().getInt("BackpacksPerSize") + Integer.parseInt(number);
                map.put(target, configInventoryNumber);
                plugin.inventories.put(inv, map);
                p.sendMessage(plugin.getColoredMessage("Clear").replaceAll("%Player%", target.getName()));
            }
        }
        return true;
    }

    private Inventory getInv(Player p, OfflinePlayer op, String invNumber, String size) {
        int inventoryNumber;
        try {
            inventoryNumber = Integer.parseInt(invNumber);
            if (inventoryNumber < 0) {
                p.getPlayer().sendMessage(plugin.getColoredMessage("Error.NumberFormatException"));
                sendHelp(op.getPlayer());
            }
        } catch (NumberFormatException e) {
            //Invalid inventory number
            p.sendMessage(plugin.getColoredMessage("Error.NumberFormatException"));
            sendHelp(p);
            return null;
        }
        //parse size to int
        int sizeInt = parseSize(size);
        if (sizeInt == 0) {
            sendHelp(p);
            return null;
        }
        Integer configInventoryNumber = sizeInt * plugin.getConfig().getInt("BackpacksPerSize") + inventoryNumber;
        Inventory inv = plugin.fUtils.getInventory(op, inventoryNumber, sizeInt);
        Map<OfflinePlayer, Integer> map = new HashMap<>();
        map.put(op, configInventoryNumber);
        plugin.inventories.put(inv, map);
        return inv;
    }

    private boolean isSafe(Player p, String size, String invNumber) {
        try {
            if (parseSize(size) == 0) {
                p.sendMessage(plugin.getColoredMessage("Error.IncorrectSize"));
                return false;
            }
            Integer inventoryNumber = Integer.parseInt(invNumber);
            if (inventoryNumber > plugin.getConfig().getInt("BackpacksPerSize")) {
                p.sendMessage(plugin.getColoredMessage("Error.NumberTooBig").replaceAll("%max%", plugin.getConfig().getInt("BackpacksPerSize") + ""));
                return false;
            }
            if (inventoryNumber <= 0) {
                p.sendMessage(plugin.getColoredMessage("Error.NumberFormatException"));
                return false;
            }
        } catch (NumberFormatException e) {
            p.sendMessage(plugin.getColoredMessage("Error.NumberFormatException"));
            return false;
        }
        return true;
    }

    private int parseSize(String sizeString) {
        if (sizeString.equalsIgnoreCase("small")) {
            return 1;
        } else if (sizeString.equalsIgnoreCase("medium")) {
            return 2;
        } else if (sizeString.equalsIgnoreCase("large")) {
            return 3;
        }
        return 0;
    }

    private boolean isPermitted(Player p, String size, String numberString) {
        Integer sizeInt = parseSize(size);
        Integer number = Integer.parseInt(numberString);
        switch (sizeInt) {
            case 1:
                return p.hasPermission("backpacks.small." + number);
            case 2:
                return p.hasPermission("backpacks.medium." + number);
            case 3:
                return p.hasPermission("backpacks.large." + number);
            default:
                return false;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "[" + ChatColor.RED + "Backpacks" + ChatColor.GREEN + "]");
        sender.sendMessage(ChatColor.RED + "/" + ChatColor.YELLOW + "backpacks " + ChatColor.GRAY + "[size] [number]");
        sender.sendMessage(ChatColor.RED + "/" + ChatColor.YELLOW + "backpacks " + ChatColor.GRAY + "[size] [number] [player]");
        sender.sendMessage(ChatColor.RED + "/" + ChatColor.YELLOW + "backpacks " + ChatColor.GRAY + "[size] [number] [player] clear");
        sender.sendMessage(ChatColor.RED + "/" + ChatColor.YELLOW + "backpacks reload");
    }

}
