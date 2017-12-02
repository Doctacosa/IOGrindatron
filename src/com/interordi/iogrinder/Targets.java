package com.interordi.iogrinder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.interordi.iogrinder.structs.Target;

public class Targets {
	
	
	//Check the content of a player-provided drop in the enderchest
	public static boolean checkDrop(Player p, Inventory inv) {
		
		ItemStack[] content = inv.getContents();
		
		ItemStack stack = null;
		boolean match = false;
		Target target = IOGrinder.db.getCycleTarget();
		
		
		for (ItemStack stackLoop : content) {
			if (stackLoop != null) {
				stackLoop.getType().name();
				
				System.out.println("Material: " + stackLoop.getType().name() + " (" + stackLoop.getDurability() + ") x" + stackLoop.getAmount());
				
				stack = stackLoop;
				
				//Check if the current stack matches the item we want
				if (stack != null &&
					stack.getType().name().toLowerCase().equals(target.item.toLowerCase()) &&
					(target.durability == -1 || target.durability == stack.getDurability()) &&
					stack.getAmount() >= target.amount
					) {
					match = true;
				}
			}
		}
		
		//If match, clear the inventory and hand over a reward when earned
		if (match) {
			inv.clear();
			p.updateInventory();
			
			//TODO: Update DB to store the results
			
			//TODO: Actual reward when warranted
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:give " + p.getDisplayName() + " skull 1 3 {display:{Name:\"Bright Star\"},SkullOwner:{Id:\"d98b77c6-ebde-4582-b1e0-f1e94e220d44\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZkZmI1ZWE5M2VlOWQ3ZTYyMTc0ZjI1MmM3M2M0NjU5NDliYjVhMzFhOTJjMzkyN2M4ZDhmYTQ4YjZjIn19fQ==\"}]}}}");
		}
		
		return true;
	}

}
