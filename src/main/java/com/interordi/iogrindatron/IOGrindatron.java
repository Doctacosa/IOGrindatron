package com.interordi.iogrindatron;

import com.interordi.iogrindatron.structs.Target;
import com.interordi.iogrindatron.utilities.ActionBar;
import com.interordi.iogrindatron.utilities.Database;
import com.interordi.iogrindatron.utilities.Scores;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class IOGrindatron extends JavaPlugin {

	private static IOGrindatron instance;
	public PeriodManager periods;
	private PlayersMove playersMove;
	@SuppressWarnings("unused")
	private PlayerActions playerAction;
	public static Database db = null;
	Scores thisScores;

	
	public void onEnable() {
		instance = this;
		
		thisScores = new Scores("Targets Done");

		new LoginListener(this);
		periods = new PeriodManager();
		playersMove = new PlayersMove(this);
		playerAction = new PlayerActions(this);
		Players.setPeriodManager(periods);
		
		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();
		
		String dbHost = this.getConfig().getString("database.server", null);
		int dbPort = this.getConfig().getInt("database.port", 3306);
		String dbUsername = this.getConfig().getString("database.username");
		String dbPassword = this.getConfig().getString("database.password");
		String dbBase = this.getConfig().getString("database.base");

		if (dbHost == null)
			dbHost = this.getConfig().getString("database.server");

		db = new Database(this, dbHost, dbPort, dbUsername, dbPassword, dbBase);
		db.init();

		//Read progression gates
		Map< String, Integer > gates = new HashMap< String, Integer >();
		gates.put("access-nether", this.getConfig().getInt("gates.access-nether", 10));
		gates.put("access-end", this.getConfig().getInt("gates.access-end", 20));
		gates.put("craft-diamond", this.getConfig().getInt("gates.craft-diamond", 10));
		gates.put("craft-netherite", this.getConfig().getInt("gates.craft-netherite", 20));
		gates.put("equip-diamond", this.getConfig().getInt("gates.equip-diamond", 10));
		gates.put("equip-netherite", this.getConfig().getInt("gates.equip-netherite", 20));
		gates.put("equip-elytra", this.getConfig().getInt("gates.equip-elytra", 30));
		Players.setGates(gates);
		
		getLogger().info("IOGrindatron enabled");
		
		//Run initial required tasks once
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			PlayerWatcher.initScore(this);
		});
		
		//Once the server is running, check for new notifications every minute
		getServer().getScheduler().scheduleSyncRepeatingTask(this, periods, 30*20L, 60*20L);
		
		//Check for player movements every 10 seconds
		getServer().getScheduler().scheduleSyncRepeatingTask(this, playersMove, 10*20L, 10*20L);
	}
	
	
	public void onDisable() {
		getLogger().info("IOGrindatron disabled");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("target")) {
			
			Target target = IOGrindatron.db.getCycleTarget();
			
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


	public Scores getScores() {
		return thisScores;
	}


	//Get the plugin instance
	public static IOGrindatron getInstance() {
		return instance;
	}
}
