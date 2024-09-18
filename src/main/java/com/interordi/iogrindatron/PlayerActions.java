package com.interordi.iogrindatron;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
		//Don't allow enderchests to be broken
		if (e.getBlock().getType() == Material.ENDER_CHEST)
			e.setCancelled(true);
		else
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
		int targets = Players.getPlayerWatcher(event.getWhoClicked().getUniqueId()).getNbTargets();

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

		} else if (event.getSlotType() == InventoryType.SlotType.RESULT) {

			if (targets < 10 && event.getCurrentItem().getType().toString().startsWith("DIAMOND_")) {
				event.setCancelled(true);
				event.getWhoClicked().sendMessage(ChatColor.RED + "You must have completed at least 10 targets to craft this.");
				return;

			} else if (targets < 20 && event.getCurrentItem().getType().toString().startsWith("NETHERITE_")) {
				event.setCancelled(true);
				event.getWhoClicked().sendMessage(ChatColor.RED + "You must have completed at least 20 targets to craft this.");
				return;
			}

		} else if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
			boolean toCancel = checkItemEquip(event.getCursor(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
			event.setCancelled(toCancel);
		}
	}


	@EventHandler
	public void onInventoryDragEvent(InventoryDragEvent event) {
		Player target = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
		if (target == null)
			return;

		for (int pos : event.getInventorySlots()) {
			ItemStack item = target.getInventory().getItem(pos);
			boolean toCancel = checkItemEquip(item, target);
			if (toCancel)
				event.setCancelled(toCancel);
		}
	}


	//Holding in hand and right-clicking armor
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			boolean toCancel = checkItemEquip(event.getItem(), event.getPlayer());
			event.setCancelled(toCancel);
		}
	}


	//For dispensers
	@EventHandler
	public void onBlockDispenseArmorEvent(BlockDispenseArmorEvent event) {
		Player target = Bukkit.getPlayer(event.getTargetEntity().getUniqueId());
		if (target == null)
			return;

		boolean toCancel = checkItemEquip(event.getItem(), target);
		event.setCancelled(toCancel);
	}


	//Check if the player is trying to equip something
	//Returns true if forbidden
	public boolean checkItemEquip(ItemStack item, Player player) {
		if (item == null)
			return false;

		int targets = Players.getPlayerWatcher(player).getNbTargets();

		if (targets < 10 && item.getType().toString().startsWith("DIAMOND_")) {
			player.sendMessage(ChatColor.RED + "You must have completed at least 10 targets to equip this.");
			return true;

		} else if (targets < 20 && (
			item.getType().toString().startsWith("NETHERITE_") ||
			item.getType() == Material.ELYTRA)) {
			player.sendMessage(ChatColor.RED + "You must have completed at least 20 targets to equip this.");
			return true;
		}

		return false;
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
