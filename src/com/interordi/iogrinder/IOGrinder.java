package com.interordi.iogrinder;

import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iogrinder.PeriodManager;


public class IOGrinder extends JavaPlugin {

	public static IOGrinder instance;
	public PeriodManager periods;
	
	
	public void onEnable() {
		new LoginListener(this);
		instance = this;
		periods = new PeriodManager();
		
		getLogger().info("IOGrinder enabled");
		
		//Once the server is running, check for new notifications every minute
		getServer().getScheduler().scheduleSyncRepeatingTask(this, periods, 30*20L, 60*20L);
	}
	
	
	public void onDisable() {
		getLogger().info("IOGrinder disabled");
	}
}
