package com.interordi.iogrinder.utilities;

import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.interordi.iogrinder.IOGrinder;

public class ActionBar {
	
	
	public static void toAll(String message) {
		
		final String formattedMessage = format(message);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrinder.instance, new Runnable() {
			@Override
			public void run() {
				for (Player player : IOGrinder.instance.getServer().getOnlinePlayers()) {
					if (player == null)
						return;
					
					player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
				}
			}
		}, 0L);
	}
	
	
	public static void toPlayer(String message, Player player) {
		
		final String formattedMessage = format(message);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrinder.instance, new Runnable() {
			@Override
			public void run() {
				if (player == null)
					return;
				
				player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
			}
		}, 0L);
	}
	
	
	private static String format(String message) {
		return message.replace("&", "§");
	}

}
