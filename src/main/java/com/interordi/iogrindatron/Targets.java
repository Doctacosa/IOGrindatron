package com.interordi.iogrindatron;

import com.interordi.iogrindatron.structs.Target;
import com.interordi.iogrindatron.utilities.ActionBar;
import com.interordi.iogrindatron.utilities.Title;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Targets {
	
	
	//Check the content of a player-provided drop in the enderchest
	public static boolean checkDrop(Player p, Inventory inv) {
		
		ItemStack[] content = inv.getContents();
		
		ItemStack stack = null;
		boolean match = false;
		Target target = IOGrindatron.db.getCycleTarget();
		
		//System.out.println("-----------");
		//System.out.println("Target: " + target.item.toLowerCase() + " x" + target.amount);
		
		for (ItemStack stackLoop : content) {
			if (stackLoop != null) {
				
				//System.out.println("Material: " + stackLoop.getType().name() + " x" + stackLoop.getAmount());
				
				stack = stackLoop;
				
				//Check if the current stack matches the item we want
				if (stack != null &&
					stack.getType().name().toLowerCase().equals(target.item.toLowerCase()) &&
					stack.getAmount() >= target.amount
					) {
					match = true;
				}
			}
		}
		
		PlayerWatcher pw = Players.getPlayerWatcher(p);
		
		//If match, clear the inventory and hand over a reward when earned
		if (match) {
			if (!pw.currentDone) {
				inv.clear();
				p.updateInventory();
				
				//Store the results in the DB
				IOGrindatron.db.savePlayerTarget(p, target);
				
				Title.toPlayer("", "Target complete!", 1, p);
				pw.completeTarget();
			} else {
				ActionBar.toPlayer("You've already completed this cycle's target.", p);
			}
		}
		
		return true;
	}

}
