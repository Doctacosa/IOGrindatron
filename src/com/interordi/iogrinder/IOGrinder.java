package com.interordi.iogrinder;

import org.bukkit.plugin.java.JavaPlugin;


public class IOGrinder extends JavaPlugin {

	public static IOGrinder instance;
	
	
	public void onEnable() {
		new LoginListener(this);
		instance = this;
		
		getLogger().info("IOGrinder enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOGrinder disabled");
	}
}
