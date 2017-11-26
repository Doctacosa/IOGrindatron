package com.interordi.iogrinder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerActions implements Listener {

	public PlayerActions(IOGrinder plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Players.getPlayerWatcher(e.getPlayer()).subEnergy(1);
	}
	

}
