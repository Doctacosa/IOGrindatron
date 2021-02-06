package com.interordi.iogrinder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.interordi.iogrinder.structs.Target;
import com.interordi.iogrinder.utilities.ActionBar;
import com.interordi.iogrinder.utilities.Chat;
import com.interordi.iogrinder.utilities.Title;


public class Players {

	static private Map< UUID , PlayerWatcher > players = new HashMap< UUID, PlayerWatcher >();
	
	
	//Keep a player watcher entry 
	public static void register(Player player) {
		UUID uuid = player.getUniqueId();
		PlayerWatcher instance = null;
		
		//First login for this server run, add an entry
		if (!players.containsKey(uuid)) {
			instance = IOGrinder.db.loadPlayer(player);
			players.put(uuid, instance);
			instance.login();
			
			Title.toPlayer("", "Welcome to... &4the Grindatron!", 5, player);
		} else {
			instance = players.get(uuid);
			
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
		
		//Give a connection bonus if one was earned
		if (instance != null && instance.getConsecutiveDays() >= 1) {
			PlayerInventory inv = instance.getPlayer().getInventory();
			String itemDesc = "";
			
			switch (instance.getConsecutiveDays()) {
				case 1:
					inv.addItem(new ItemStack(Material.IRON_INGOT));
					itemDesc = "1 iron ingot";
					break;
				case 2:
					inv.addItem(new ItemStack(Material.IRON_PICKAXE));
					itemDesc = "1 iron pickaxe";
					break;
				case 3:
					inv.addItem(new ItemStack(Material.COOKED_BEEF, 10));
					itemDesc = "10 cooked beef";
					break;
				case 4:
					inv.addItem(new ItemStack(Material.DIAMOND));
					itemDesc = "1 diamond";
					break;
				default:
					inv.addItem(new ItemStack(Material.DIAMOND_ORE));
					itemDesc = "1 diamond ore";
					break;
			};
			
			Chat.toPlayer("&e&lDaily connection bonus: &r[&b&l" + itemDesc + "&r]", instance.getPlayer(), 5);
			
		}
			
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
	public static void updatePeriodProgress(double progress) {
		for (Map.Entry< UUID, PlayerWatcher > entry : players.entrySet()) {
			PlayerWatcher instance = entry.getValue();
			if (instance != null)
				instance.updatePeriodProgress(progress);
		}
	}
	
	
	//Get the last known position of a player
	public static Location getLastPosition(Player player) {
		PlayerWatcher instance = players.get(player.getUniqueId());
		if (instance != null)
			return instance.getPosition();
		return null;
	}
	
	
	//Set the current position of a player
	public static void setPosition(Player player, Location pos) {
		PlayerWatcher instance = players.get(player.getUniqueId());
		if (instance != null)
			instance.setPosition(pos);
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

	
	//Reset stats at the beginning of a cycle
	public static void resetCycle() {
		for (Map.Entry< UUID, PlayerWatcher > entry : players.entrySet()) {
			PlayerWatcher instance = entry.getValue();
			if (instance != null)
				instance.resetCycle();
		}
	}
	
	
}