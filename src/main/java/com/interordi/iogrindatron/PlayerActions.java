package com.interordi.iogrindatron;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerActions implements Listener {

	IOGrindatron plugin;

	public PlayerActions(IOGrindatron plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		//Don't allow enderchests to be placed
		if (e.getBlock().getType() == Material.ENDER_CHEST)
			e.setCancelled(true);
		else
			Players.getPlayerWatcher(e.getPlayer()).subEnergy(0.17);
	}
	

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Players.getPlayerWatcher(e.getPlayer()).subEnergy(0.5);
	}
	

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		@SuppressWarnings("unused")
		EntityType entity = event.getEntityType();
		
		if (event.getEntity() instanceof Player) {
			Player p = (Player)event.getEntity();
			Players.getPlayerWatcher(p).subEnergy(1000);
		}
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
	 
		if (inv.getType() == InventoryType.ENDER_CHEST) {
			@SuppressWarnings("unused")
			ItemStack item = null;
			
			if (event.getRawSlot() < inv.getSize() || event.isRightClick()) {
				item = event.getCursor();
			} else if (event.isShiftClick()) {
				item = event.getCurrentItem();
			}
			
			//Someone is moving an item in the enderchest, run checks
			//Do it on the next tick so that the item actually drops
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					Player p = ((Player)event.getWhoClicked());
					Targets.checkDrop(p, inv);
				}
			}, 1L);
		}
	}
	
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		//Don't allow milk buckets to be consumed when no energy left (fixes temp removal of debuffs)
		if (event.getItem().getType() != Material.MILK_BUCKET)
			return;
		
		double energy = Players.getPlayerWatcher(event.getPlayer()).getEnergy();
		if (energy <= 0.0)
			event.setCancelled(true);
	}
	
	
	@EventHandler
	public void onPlayerFishEvent(PlayerFishEvent event) {
		double energy = Players.getPlayerWatcher(event.getPlayer()).getEnergy();
		if (energy > 0.0)
			Players.getPlayerWatcher(event.getPlayer()).subEnergy(1.0);
		else
			event.setCancelled(true);
	}
}
