package com.interordi.iogrindatron.utilities;

import net.md_5.bungee.api.chat.TextComponent;

import com.interordi.iogrindatron.IOGrindatron;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBar {
	
	
	public static void toAll(String message) {
		
		final String formattedMessage = format(message);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(IOGrindatron.instance, new Runnable() {
			@Override
			public void run() {
				for (Player player : IOGrindatron.instance.getServer().getOnlinePlayers()) {
					if (player == null)
						return;
					
					player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
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
				
				player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
			}
		}, delay * 20L);
	}

	public static void toPlayer(String message, Player player) {
		toPlayer(message, player, 0);
	}

	
	private static String format(String message) {
		return message.replace("&", "§");
	}

}
