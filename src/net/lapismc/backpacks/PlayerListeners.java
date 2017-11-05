package net.lapismc.backpacks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListeners implements Listener {

    private Backpacks plugin;

    PlayerListeners(Backpacks p) {
        plugin = p;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        Player p = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        boolean isStored = false;
        for (Inventory i : plugin.inventories.keySet()) {
            if (i.getName().equals(inv.getName())) {
                isStored = true;
            }
        }
        if (!isStored) return;
        plugin.fUtils.saveInventory(inv, plugin.fUtils.getPlayerFromMap(plugin.inventories.get(inv)), plugin.fUtils.getIntegerFromMap(plugin.inventories.get(inv)));
        p.sendMessage(plugin.getColoredMessage("InventoryClosed"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (plugin.inventories.containsKey(inv)) {
            if (event.getWhoClicked() instanceof Player) {
                Player p = (Player) event.getWhoClicked();
                if (plugin.fUtils.getPlayerFromMap(plugin.inventories.get(inv)).getUniqueId() != p.getUniqueId()) {
                    //check for perms to edit
                    if (!p.hasPermission("backpacks.edit")) {
                        event.setCancelled(true);
                        p.sendMessage(plugin.getColoredMessage("Error.NoPermission"));
                    }
                }
            }
        }
    }

    //This shouldn't be able to happen but just incase it does
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        Inventory inv = p.getOpenInventory().getTopInventory();
        boolean isStored = false;
        for (Inventory i : plugin.inventories.keySet()) {
            if (i.getName().equals(inv.getName())) {
                isStored = true;
            }
        }
        if (!isStored) return;
        plugin.fUtils.saveInventory(inv, plugin.fUtils.getPlayerFromMap(plugin.inventories.get(inv)), plugin.fUtils.getIntegerFromMap(plugin.inventories.get(inv)));
        plugin.inventories.remove(inv);
    }

}
