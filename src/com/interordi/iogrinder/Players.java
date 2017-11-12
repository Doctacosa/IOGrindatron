package com.interordi.iogrinder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.interordi.iogrinder.utilities.Title;


public class Players {

	Map< UUID , PlayerWatcher > players;
	
	
	public Players() {
		players = new HashMap< UUID, PlayerWatcher >();
	}
	
	
	//Keep a player watcher entry 
	public void register(Player player) {
		UUID uuid = player.getUniqueId();
		
		//First login for this server run, add an entry
		if (!players.containsKey(uuid)) {
			PlayerWatcher instance = new PlayerWatcher(player);
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
				instance = new PlayerWatcher(player);
				players.put(uuid, instance);
				instance.login();
			}
		}
	}
	
	
	//Void a player's entry once he logs out 
	public void unregister(Player player) {
		PlayerWatcher instance = players.get(player.getUniqueId());
		if (instance != null) {
			instance.logout();
			players.put(player.getUniqueId(), null);
		}
	}
	
}
