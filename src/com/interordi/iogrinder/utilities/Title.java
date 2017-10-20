package com.interordi.iogrinder.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.interordi.iogrinder.IOGrinder;

public class Title {
	
	
	public static void toAll(String title, String subtitle, int delay) {
		
		final String formattedTitle = format(title);
		final String formattedSubtitle = format(subtitle);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrinder.instance, new Runnable() {
			@Override
			public void run() {
				for (Player player : IOGrinder.instance.getServer().getOnlinePlayers()) {
					if (player == null)
						return;
					
					player.sendTitle(formattedTitle, formattedSubtitle, 2*20, 5*20, 2*20);
				}
			}
		}, delay*20L);
	}
	
	
	public static void toPlayer(String title, String subtitle, int delay, Player player) {
		
		final String formattedTitle = format(title);
		final String formattedSubtitle = format(subtitle);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrinder.instance, new Runnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				player.sendTitle(formattedTitle, formattedSubtitle, 2*20, delay*20, 2*20);
			}
		}, delay*20L);
	}
	
	
	private static String format(String message) {
		return message.replace("&", "§");
	}

}
