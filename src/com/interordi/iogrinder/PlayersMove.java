package com.interordi.iogrinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.interordi.iogrinder.utilities.ActionBar;

@SuppressWarnings("unused")
public class PlayersMove implements Runnable, Listener {

	
	public PlayersMove(IOGrinder plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@Override
	public void run() {
		
		//Get the list of online players
		for (Player player : Bukkit.getOnlinePlayers()) {
			
			Location newPos = player.getLocation();
			Location oldPos = Players.getLastPosition(player);
			
			//Compute the distance between two events if possible
			if (oldPos != null && newPos != null && oldPos.getWorld() == newPos.getWorld()) {
				double movement = oldPos.distance(newPos);
				
				if (movement > 0.0) {
					//ActionBar.toPlayer("Movement: " + movement, player);
					Players.getPlayerWatcher(player).subEnergy(movement);
				}
			}
			
			Players.setPosition(player, newPos);
			
		}
	}
	
	
	//On teleport, immediately set the new position
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Players.setPosition(e.getPlayer(), e.getTo());
	}
}
