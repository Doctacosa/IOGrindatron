package com.interordi.iogrinder;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;


public class PlayerWatcher {
	
	Player player;
	Location position;
	
	Map< String, BossBar > bars;
	
	
	public PlayerWatcher(Player player) {
		this.player = player;
		
		bars = new HashMap< String, BossBar >();
	}
	
	
	public void login() {
		//Add the energy level bar
		BossBar bossBar = Bukkit.createBossBar("Energy", BarColor.BLUE, BarStyle.SEGMENTED_10 /* .SOLID */);
		bossBar.addPlayer(player);
		bossBar.setProgress(0.45);
		bars.put("energy", bossBar);
		
		//Add the period indicator bar
		bossBar = Bukkit.createBossBar("Period progress", BarColor.YELLOW, BarStyle.SOLID);
		bossBar.addPlayer(player);
		bossBar.setProgress(PeriodManager.getPeriodProgress());
		bars.put("period", bossBar);
		
		//Scoreboard board = this.plugin.getServer().getScoreboardManager().getMainScoreboard();
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		//board.resetScores("score");
		Objective objective = board.getObjective("score");
		
		if (objective == null)
			objective = board.registerNewObjective("score", "dummy");
		board.clearSlot(DisplaySlot.SIDEBAR);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Players");
		
		if (objective != null) {
			Score score = objective.getScore(player.getDisplayName());
			score.setScore(player.getLocation().getBlockX());
		}
	}
	
	
	public void logout() {
		if (bars != null) {
			bars.clear();
		}
	}
	
	
	//Update the progress of the current period
	public void updatePeriodProgress(float progress) {
		BossBar bar = bars.get("period");
		if (bar == null)
			return;
		
		bar.setProgress(progress);
	}
	
	
	//Get and set the last known position
	public Location getPosition() {
		return this.position;
	}
	
	public void setPosition(Location pos) {
		this.position = pos;
	}
}
