package com.interordi.iogrinder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.interordi.iogrinder.PeriodManager;
import com.interordi.iogrinder.structs.Target;
import com.interordi.iogrinder.utilities.ActionBar;
import com.interordi.iogrinder.utilities.Database;


public class IOGrinder extends JavaPlugin implements Runnable {

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
		//db = new Database("localhost", "root", "", "creeperslab");
		db = new Database("localhost", "creeperslab", "***REMOVED***", "creeperslab");
		
		getLogger().info("IOGrinder enabled");
		
		//Run initial required tasks once
		getServer().getScheduler().scheduleSyncDelayedTask(this, this);
		
		//Once the server is running, check for new notifications every minute
		getServer().getScheduler().scheduleSyncRepeatingTask(this, periods, 30*20L, 60*20L);
		
		//Check for player movements every 10 seconds
		getServer().getScheduler().scheduleSyncRepeatingTask(this, playersMove, 10*20L, 10*20L);
	}
	
	
	public void onDisable() {
		getLogger().info("IOGrinder disabled");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("target")) {
			
			Target target = IOGrinder.db.getCycleTarget();
			
			//Fancy display to players, basic for others like the console
			if (sender instanceof Player) {
				Player player = (Player)sender;
				ActionBar.toPlayer("Current target: &l" + target.label, player);
			} else {
				sender.sendMessage("Current target: " + target.label);
				return true;
			}
			
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public void run() {
		//Add the basic scoreboard when the server is loaded
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		//Scoreboard board = this.plugin.getServer().getScoreboardManager().getMainScoreboard();
		//board.resetScores("score");
		Objective objective = board.getObjective("score");
		
		if (objective != null)
			objective.unregister();
		
		objective = board.registerNewObjective("score", "dummy");
		board.clearSlot(DisplaySlot.SIDEBAR);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Players");
	}
}
