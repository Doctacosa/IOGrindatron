package com.interordi.iogrindatron;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.interordi.iogrindatron.utilities.ActionBar;

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


public class PlayerWatcher {
	
	private final static double maxEnergy = 4000.0;
	private double playerMaxEnergy = maxEnergy;
	Player player;
	Location position;
	double energy = maxEnergy;
	LocalDate lastDate = null;
	int lastCycle = -1;
	int score = 0;
	boolean currentDone = false;
	int consecutiveDays = 0;
	
	Map< String, BossBar > bars;
	static Map< String, Integer > scores = new HashMap< String, Integer>();
	
	
	public PlayerWatcher(Player player) {
		this(player, maxEnergy, null, -1, 0, false, 0);
	}
	
	
	public PlayerWatcher(Player player, double energy, LocalDate date, int cycle, int score, boolean currentDone, int nbDays) {
		this.player = player;
		this.energy = energy;
		this.lastDate = date;
		this.lastCycle = cycle;
		this.score = score;
		this.currentDone = currentDone;
		
		//Give a bonus on consecutive days, up to 25%
		playerMaxEnergy = maxEnergy + nbDays * 200.0;
		if (playerMaxEnergy > maxEnergy * 1.25)
			playerMaxEnergy = maxEnergy * 1.25;
		
		bars = new HashMap< String, BossBar >();
	}
	
	
	public void login() {
		
		if (energy > playerMaxEnergy)
			energy = playerMaxEnergy;
		else if (energy < 0.0)
			energy = 0.0;
		
		//Add the energy level bar
		BossBar bossBar = Bukkit.createBossBar("Energy", BarColor.BLUE, BarStyle.SEGMENTED_10 /* .SOLID */);
		bossBar.addPlayer(player);
		bossBar.setProgress(energy / playerMaxEnergy);
		bars.put("energy", bossBar);
		
		//Add the period indicator bar
		bossBar = Bukkit.createBossBar("Period progress", BarColor.YELLOW, BarStyle.SOLID);
		bossBar.addPlayer(player);
		bossBar.setProgress(PeriodManager.getPeriodProgress());
		bars.put("period", bossBar);
		
		updateScore();
		
		//Only refill if the player was last active in a previous cycle
		if (lastDate != null && lastCycle != -1 &&
			(!lastDate.equals(LocalDate.now()) || lastCycle != PeriodManager.getPeriod())) {
			fillEnergy();
		}
	}
	
	
	public void logout() {
		if (bars != null) {
			bars.clear();
		}
		removeScore();
	}
	
	
	//Update the progress of the current period
	public void updatePeriodProgress(double progress) {
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
		if (energy > playerMaxEnergy) {
			energy = playerMaxEnergy;
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
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 655200, 0));
			ActionBar.toPlayer("&4Out of energy!", player);
		}
		updateEnergy();
	}
	
	public void fillEnergy() {
		double oldEnergy = energy;
		energy = playerMaxEnergy;
		
		player.removePotionEffect(PotionEffectType.SLOW);
		player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		player.removePotionEffect(PotionEffectType.WEAKNESS);
		
		if (oldEnergy < playerMaxEnergy) {
			ActionBar.toPlayer("&aEnergy restored!", player);
		}
		updateEnergy();
	}
	
	
	//Update the player's energy
	public void updateEnergy() {
		BossBar bar = bars.get("energy");
		if (bar == null)
			return;
		
		double progress = energy / playerMaxEnergy;
		if (progress < 0)	progress = 0;
		if (progress > 1)	progress = 1;
		
		bar.setProgress(progress);
	}


	//Initialize the scoreboard
	public static void initScore() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = board.getObjective("score");
		
		if (objective != null)
			objective.unregister();
		
		objective = board.registerNewObjective("score", "dummy", "Score");
		board.clearSlot(DisplaySlot.SIDEBAR);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Players");
	}
	
	
	//Update a player's score on the global display
	public void updateScore() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = board.getObjective("score");
		if (objective != null) {
			Score myScore = objective.getScore(player.getDisplayName());
			myScore.setScore(this.score);
			
			scores.put(player.getDisplayName(), this.score);
		}
	}
	
	
	//Remove a player's score from the display
	public void removeScore() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = board.getObjective("score");
		if (objective != null) {
			//Remove a player then rebuild the scoreboard from the known data
			objective.unregister();
			objective = null;
			
			objective = board.registerNewObjective("score", "dummy", "Score");
			board.clearSlot(DisplaySlot.SIDEBAR);
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Players");
			
			scores.remove(player.getDisplayName());
			
			for (String key : scores.keySet()) {
				Score myScore = objective.getScore(key);
				myScore.setScore(scores.get(key));
			}
		}
	}
	
	
	//Getters/setters
	public Player getPlayer() {
		return player;
	}
	
	public double getEnergy() {
		return energy;
	}


	//Increase the player's score as needed
	public void completeTarget() {
		this.score++;
		this.currentDone = true;
		updateScore();
	}


	//Get / set the amount of consecutive days played
	public void giveConnectionBonus(int nbDays) {
		this.consecutiveDays = nbDays;
	}

	public int getConsecutiveDays() {
		return this.consecutiveDays;
	}


	//Reset a player's stats after a cycle
	public void resetCycle() {
		currentDone = false;
		fillEnergy();
	}
}
