package com.interordi.iogrinder;

import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iogrinder.PeriodManager;
import com.interordi.iogrinder.utilities.Database;


public class IOGrinder extends JavaPlugin {

	public static IOGrinder instance;
	public PeriodManager periods;
	private PlayersMove playersMove;
	@SuppressWarnings("unused")
	private PlayerActions playerAction;
	public static Database db = null;
	
	
	public void onEnable() {
		new LoginListener(this);
		instance = this;
		periods = new PeriodManager();
		playersMove = new PlayersMove(this);
		playerAction = new PlayerActions(this);
		
		//TODO: Configurable DB info
		db = new Database("localhost", "root", "", "creeperslab");
		
		getLogger().info("IOGrinder enabled");
		
		//Once the server is running, check for new notifications every minute
		getServer().getScheduler().scheduleSyncRepeatingTask(this, periods, 30*20L, 60*20L);
		
		//Check for player movements every 10 seconds
		getServer().getScheduler().scheduleSyncRepeatingTask(this, playersMove, 10*20L, 10*20L);
	}
	
	
	public void onDisable() {
		getLogger().info("IOGrinder disabled");
	}
}
