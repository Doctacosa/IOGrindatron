package com.interordi.iogrinder;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
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
	
	Set< BossBar > bars;
	
	
	public PlayerWatcher(Player player) {
		this.player = player;
		
		bars = new HashSet< BossBar >();
	}
	
	
	public void login() {
		BossBar bossBar = Bukkit.createBossBar("Energy", BarColor.BLUE, BarStyle.SEGMENTED_10 /* .SOLID */);
		bossBar.addPlayer(player);
		bossBar.setProgress(0.45);
		bars.add(bossBar);
		
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

}
