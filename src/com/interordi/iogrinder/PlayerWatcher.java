package com.interordi.iogrinder;

import java.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.interordi.iogrinder.utilities.ActionBar;


public class PlayerWatcher {
	
	private final static double maxEnergy = 100.0;
	Player player;
	Location position;
	double energy = maxEnergy;
	LocalDate lastDate = null;
	int lastCycle = -1;
	
	Map< String, BossBar > bars;
	
	
	public PlayerWatcher(Player player) {
		this(player, maxEnergy, null, -1);
	}
	
	
	public PlayerWatcher(Player player, double energy, LocalDate date, int cycle) {
		this.player = player;
		this.energy = energy;
		this.lastDate = date;
		this.lastCycle = cycle;
		
		bars = new HashMap< String, BossBar >();
	}
	
	
	public void login() {
		//Add the energy level bar
		BossBar bossBar = Bukkit.createBossBar("Energy", BarColor.BLUE, BarStyle.SEGMENTED_10 /* .SOLID */);
		bossBar.addPlayer(player);
		bossBar.setProgress(energy / maxEnergy);
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
		
		//Only refill if the player was last active in a previous cycle
		if (lastDate != null && lastCycle != -1 &&
			(lastDate != LocalDate.now() || lastCycle != PeriodManager.getPeriod()))
			fillEnergy();
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
		
		if (progress < 0)	progress = 0;
		if (progress > 1)	progress = 1;
		
		bar.setProgress(progress);
	}
	
	
	//Get and set the last known position
	public Location getPosition() {
		return this.position;
	}
	
	public void setPosition(Location pos) {
		this.position = pos;
	}
	
	
	public void addEnergy(double amount) {
		@SuppressWarnings("unused")
		double oldEnergy = energy;
		energy += amount;
		if (energy > maxEnergy) {
			energy = maxEnergy;
		}
		updateEnergy();
	}
	
	public void subEnergy(double amount) {
		@SuppressWarnings("unused")
		double oldEnergy = energy;
		energy -= amount;
		if (energy <= 0) {
			energy = 0;
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 655200, 2));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 655200, 3));
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 655200, 4));
			ActionBar.toPlayer("&4Out of energy!", player);
		}
		updateEnergy();
	}
	
	public void fillEnergy() {
		double oldEnergy = energy;
		energy = maxEnergy;
		
		player.removePotionEffect(PotionEffectType.SLOW);
		player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		player.removePotionEffect(PotionEffectType.WEAKNESS);
		
		if (oldEnergy < maxEnergy) {
			ActionBar.toPlayer("&aEnergy restored!", player);
		}
		updateEnergy();
	}
	
	
	//Update the player's energy
	public void updateEnergy() {
		BossBar bar = bars.get("energy");
		if (bar == null)
			return;
		
		double progress = energy / maxEnergy;
		if (progress < 0)	progress = 0;
		if (progress > 1)	progress = 1;
		
		bar.setProgress(progress);
	}
	
	
	//Getters/setters
	public Player getPlayer() {
		return player;
	}
	
	public double getEnergy() {
		return energy;
	}
}