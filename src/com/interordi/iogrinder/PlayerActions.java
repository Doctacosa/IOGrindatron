package com.interordi.iogrinder;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

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
}
