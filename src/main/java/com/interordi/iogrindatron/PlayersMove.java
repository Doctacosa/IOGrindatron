package com.interordi.iogrindatron;

import com.interordi.iogrindatron.utilities.ActionBar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@SuppressWarnings("unused")
public class PlayersMove implements Runnable, Listener {

	
	public PlayersMove(IOGrindatron plugin) {
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
				
				if (movement > 0.5 && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {
					//ActionBar.toPlayer("Movement: " + movement, player);
					if (player.isInsideVehicle())
						Players.getPlayerWatcher(player).subEnergy(movement / 2);
					else if (player.isGliding())
						Players.getPlayerWatcher(player).subEnergy(movement / 4);
					else
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
	
	
	//Avoid a double penalty for the death AND the respawn afterwards
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Players.setPosition(e.getPlayer(), e.getRespawnLocation());
	}
	
	
	//Avoid a double penalty for the death AND the respawn afterwards
	@EventHandler
	public void onPlayerPortalEvent(PlayerPortalEvent e) {

		int targets = Players.getPlayerWatcher(e.getPlayer()).getNbTargets();
		
		if (targets < 10 && e.getCause() == TeleportCause.NETHER_PORTAL) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "You must have completed at least 10 targets to enter this portal.");
			return;
		
		} else if (targets < 30 && e.getCause() == TeleportCause.END_PORTAL) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "You must have completed at least 30 targets to enter this portal.");
			return;
		}
	}
}
