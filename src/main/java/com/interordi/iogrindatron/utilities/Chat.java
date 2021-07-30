package com.interordi.iogrindatron.utilities;

import com.interordi.iogrindatron.IOGrindatron;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Chat {
	
	
	public static void toAll(String message) {
		
		final String formattedMessage = format(message);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrindatron.instance, new Runnable() {
			@Override
			public void run() {
				for (Player player : IOGrindatron.instance.getServer().getOnlinePlayers()) {
					if (player == null)
						return;
					
					player.sendMessage(formattedMessage);
				}
			}
		}, 0L);
	}
	
	
	public static void toPlayer(String message, Player player, int delay) {
		
		final String formattedMessage = format(message);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrindatron.instance, new Runnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				player.sendMessage(formattedMessage);
			}
		}, delay * 0L);
	}

	public static void toPlayer(String message, Player player) {
		toPlayer(message, player, 0);
	}

	
	private static String format(String message) {
		return message.replace("&", "§");
	}

}
