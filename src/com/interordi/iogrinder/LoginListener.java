package com.interordi.iogrinder;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.interordi.iogrinder.utilities.Title;


public class LoginListener implements Listener {
	
	Map< Player, BossBar > bars;
	boolean firstLogin = true;
	
	
	@SuppressWarnings("unused")
	private IOGrinder plugin;
	
	public LoginListener(IOGrinder plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		bars = new HashMap< Player, BossBar >();
	}
	
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		BossBar bossBar = Bukkit.createBossBar("Energy", BarColor.BLUE, BarStyle.SEGMENTED_10 /* .SOLID */);
		bossBar.addPlayer(event.getPlayer());
		bossBar.setProgress(0.45);
		bars.put(event.getPlayer(), bossBar);
		
		//Scoreboard board = this.plugin.getServer().getScoreboardManager().getMainScoreboard();
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		//board.resetScores("score");
		Objective objective = board.getObjective("score");
		
		if (firstLogin) {
			firstLogin = false;
			
			if (objective == null)
				objective = board.registerNewObjective("score", "dummy");
			board.clearSlot(DisplaySlot.SIDEBAR);
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Players");
		}
		
		if (objective != null) {
			Score score = objective.getScore(event.getPlayer().getDisplayName());
			score.setScore(event.getPlayer().getLocation().getBlockX());
		}
		
		Title.toPlayer("", "Welcome to... &4the Grinder!", 5, event.getPlayer());
	}
	
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		BossBar bar = bars.remove(event.getPlayer());
		if (bar != null) {
			bar.removeAll();
		}
	}
}
