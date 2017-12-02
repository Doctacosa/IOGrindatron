package com.interordi.iogrinder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerActions implements Listener {

	public PlayerActions(IOGrinder plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Players.getPlayerWatcher(e.getPlayer()).subEnergy(1);
	}
	

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		@SuppressWarnings("unused")
		EntityType entity = event.getEntityType();
		
		if (event.getEntity() instanceof Player) {
			Player p = (Player)event.getEntity();
			Players.getPlayerWatcher(p).subEnergy(25);
		}
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
	 
		if (inv.getType() == InventoryType.ENDER_CHEST) {
			ItemStack item = null;
			
			if (event.getRawSlot() < inv.getSize() || event.isRightClick()) {
				item = event.getCursor();
			} else if (event.isShiftClick()) {
				item = event.getCurrentItem();
			}
			
			//Someone is moving an item in the enderchest, run checks
			if (item != null && item.getType() != Material.AIR) {
				//System.out.println("Material: " + item.getType().name() + " x" + item.getAmount());
				
				//If we have everything required, empty the inventory on the next tick
				Bukkit.getScheduler().runTaskLater(IOGrinder.instance, new Runnable() {
					@Override
					public void run() {
						Player p = ((Player)event.getWhoClicked());
						Targets.checkDrop(p, inv);
					}
				}, 1L);
			}
		}
	}
}
