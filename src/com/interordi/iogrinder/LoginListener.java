package com.interordi.iogrinder;

import org.bukkit.event.player.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class LoginListener implements Listener {
	
	@SuppressWarnings("unused")
	private IOGrinder plugin;
	
	public LoginListener(IOGrinder plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		Players.register(event.getPlayer());
	}
	
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		Players.unregister(event.getPlayer());
	}
}
