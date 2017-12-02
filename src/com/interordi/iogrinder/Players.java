package com.interordi.iogrinder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.interordi.iogrinder.structs.Target;
import com.interordi.iogrinder.utilities.ActionBar;
import com.interordi.iogrinder.utilities.Title;


public class Players {

	static private Map< UUID , PlayerWatcher > players = new HashMap< UUID, PlayerWatcher >();
	
	
	//Keep a player watcher entry 
	public static void register(Player player) {
		UUID uuid = player.getUniqueId();
		
		//First login for this server run, add an entry
		if (!players.containsKey(uuid)) {
			PlayerWatcher instance = IOGrinder.db.loadPlayer(player);
			players.put(uuid, instance);
			instance.login();
			
			Title.toPlayer("", "Welcome to... &4the Grinder!", 5, player);
		} else {
			PlayerWatcher instance = players.get(uuid);
			
			if (instance != null) {
				//Player was already logged in
				instance.login();
			} else {
				//Renew an instance for this player and keep it
				instance = IOGrinder.db.loadPlayer(player);
				players.put(uuid, instance);
				instance.login();
			}
		}
		
		Target target = IOGrinder.db.getCycleTarget();
		ActionBar.toPlayer("Current target: &l" + target.label, player, 10);
	}
	
	
	//Void a player's entry once he logs out 
	public static void unregister(Player player) {
		PlayerWatcher instance = players.get(player.getUniqueId());
		if (instance != null) {
			instance.logout();
			IOGrinder.db.savePlayer(instance, LocalDate.now(), PeriodManager.getPeriod());
			players.put(player.getUniqueId(), null);
		}
	}
	
	
	//Update the period's progress for all online players
	public static void updatePeriodProgress(float progress) {
		for (Map.Entry< UUID, PlayerWatcher > entry : players.entrySet()) {
			PlayerWatcher instance = entry.getValue();
			if (instance != null)
				instance.updatePeriodProgress(progress);
		}
	}
	
	
	//Get the last known position of a player
	public static Location getLastPosition(Player player) {
		for (Map.Entry< UUID, PlayerWatcher > entry : players.entrySet()) {
			PlayerWatcher instance = entry.getValue();
			if (instance != null)
				return instance.getPosition();
		}
		return null;
	}
	
	
	//Set the current position of a player
	public static void setPosition(Player player, Location pos) {
		for (Map.Entry< UUID, PlayerWatcher > entry : players.entrySet()) {
			PlayerWatcher instance = entry.getValue();
			if (instance != null)
				instance.setPosition(pos);
		}
	}
	

	//Find the given PlayerWatcher instance
	public static PlayerWatcher getPlayerWatcher(Player player) {
		return players.get(player.getUniqueId());
	}

	
	//Fill everyone's energy on a period reset
	public static void fillEnergy() {
		for (Map.Entry< UUID, PlayerWatcher > entry : players.entrySet()) {
			PlayerWatcher instance = entry.getValue();
			if (instance != null)
				instance.fillEnergy();
		}
	}
	
	
}
