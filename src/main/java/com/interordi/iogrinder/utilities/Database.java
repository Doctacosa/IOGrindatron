package com.interordi.iogrinder.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;

import com.interordi.iogrinder.PeriodManager;
import com.interordi.iogrinder.PlayerWatcher;
import com.interordi.iogrinder.structs.Target;
import com.interordi.iogrinder.structs.PossibleTarget;

public class Database {
	
	//MySQL server information
	private String dbServer = "";
	private String dbUsername = "";
	private String dbPassword = "";
	private String dbBase = "";
	
	private String database = "";
	
	
	public Database(String server, String username, String password, String base) {
		
		this.dbServer = server;
		this.dbUsername = username;
		this.dbPassword = password;
		this.dbBase = base;
		
		database = "jdbc:mysql://" + dbServer + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword + "&useSSL=false";
	}
	
	
	//Load a player's information
	public PlayerWatcher loadPlayer(Player player) {
		Connection conn = null;
		String query = "";
		PlayerWatcher watcher = null;
		
		try {
			conn = DriverManager.getConnection(database);
			
			//Get the number of consecutive days with logins in a row BEFORE registering today
			PreparedStatement pstmt = conn.prepareStatement("" +
				"SELECT date " +
				"FROM grindatron__players_daily " +
				"WHERE uuid = ? " +
				"ORDER BY date DESC " +
				"LIMIT 5"
			);
			pstmt.setString(1, player.getUniqueId().toString());
			ResultSet rs = pstmt.executeQuery();
			int nbDays = 0;
			boolean giveConnectionBonus = false;
			LocalDate day = LocalDate.now().minusDays(1);
			while (rs.next()) {
				String entryDate = rs.getString("date");
				
				if (entryDate.equals(LocalDate.now().toString())) {
					//Player already earned his daily bonus, skip
					giveConnectionBonus = false;
					break;
				}
				
				if (entryDate.equals(day.toString())) {
					nbDays++;
					giveConnectionBonus = true;
				} else {
					break;
				}
				day = day.minusDays(1);
			}
			rs.close();
			
			
			//Mark this player's visit for the day
			pstmt = conn.prepareStatement("" +
				"INSERT IGNORE INTO grindatron__players_daily (uuid, date) " +
				"VALUES (?, ?)"
			);
			pstmt.setString(1, player.getUniqueId().toString());
			pstmt.setString(2, LocalDate.now().toString());
			pstmt.executeUpdate();
			
			//Get the player's data
			pstmt = conn.prepareStatement("" +
				"SELECT energy, last_date, last_cycle, " +
				"( " +
				"	SELECT COUNT(*) " +
				"	FROM grindatron__cycles_players " +
				"	WHERE uuid = ? " +
				"	  AND done = 1 " +
				") AS score, " +
				"( " +
				"	SELECT COUNT(*) " +
				"	FROM grindatron__cycles_players " +
				"	WHERE uuid = ? " +
				"	  AND done = 1 " +
				"	  AND date = ? " +
				"	  AND cycle = ? " +
				" ) AS current_done " +
				"FROM grindatron__players " +
				"WHERE uuid = ?"
			);
			
			pstmt.setString(1, player.getUniqueId().toString());
			pstmt.setString(2, player.getUniqueId().toString());
			pstmt.setString(3, LocalDate.now().toString());
			pstmt.setInt(4, PeriodManager.getPeriod());
			pstmt.setString(5, player.getUniqueId().toString());
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				watcher = new PlayerWatcher(
					player,
					rs.getDouble("energy"),
					LocalDate.parse(rs.getString("last_date")),
					rs.getInt("last_cycle"),
					rs.getInt("score"),
					rs.getInt("current_done") == 1,
					nbDays
				);
			}
			
			if (watcher == null) {
				watcher = new PlayerWatcher(player);
				System.out.println("Not FOUND");
			}
			
			if (giveConnectionBonus && nbDays > 0)
				watcher.giveConnectionBonus(nbDays);
			
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		return watcher;
	}
	
	
	//Save the player's current settings
	public void savePlayer(PlayerWatcher watcher, LocalDate date, int cycle) {

		Connection conn = null;
		String query = "";
		
		try {
			conn = DriverManager.getConnection(database);
			
			if (date == null)
				date = LocalDate.now();
			
			PreparedStatement pstmt = null;
			
			//Set or update the title
			pstmt = conn.prepareStatement("" +
				"INSERT INTO grindatron__players (uuid, energy, last_date, last_cycle) " +
				"VALUES (?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE energy = ?, last_date = ?, last_cycle = ? "
			);
			pstmt.setString(1, watcher.getPlayer().getUniqueId().toString());
			pstmt.setDouble(2, watcher.getEnergy());
			pstmt.setString(3, date.toString());
			pstmt.setInt(4, cycle);
			pstmt.setDouble(5, watcher.getEnergy());
			pstmt.setString(6, date.toString());
			pstmt.setInt(7, cycle);
				
			@SuppressWarnings("unused")
			int res = pstmt.executeUpdate();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	
	//Get the target of the current cycle
	public Target getCycleTarget(LocalDate date, int cycle) {
		
		Connection conn = null;
		String query = "";
		Target target = null;
		
		try {
			conn = DriverManager.getConnection(database);
			
			//Get the player's data
			PreparedStatement pstmt = conn.prepareStatement("" +
				"SELECT label, target, amount " + 
				"FROM grindatron__cycles " +
				"WHERE date = ? " +
				"  AND cycle = ? "
			);
			
			pstmt.setString(1, date.toString());
			pstmt.setInt(2, cycle);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				target = new Target(date, cycle, rs.getString("label"), rs.getString("target"), rs.getInt("amount"));
			}
			rs.close();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		//If no target has been defined, generate a new one
		if (target == null)
			target = generateTarget(date, cycle);

		return target;
	}

	public Target getCycleTarget() {
		return getCycleTarget(LocalDate.now(), PeriodManager.getPeriod());
	}
	
	
	//Generate a new target and save it to the database
	public Target generateTarget(LocalDate date, int cycle) {
		Connection conn = null;
		String query = "";
		
		//Get the list of potential targets
		Map< Integer, PossibleTarget > possibleTargets = new HashMap< Integer, PossibleTarget >();
		
		try {
			
			conn = DriverManager.getConnection(database);
			
			
			//Get the amount of days since this cycle started
			int nbDays = 1;
			PreparedStatement pstmt = conn.prepareStatement("" +
				"SELECT DATEDIFF(CURDATE(), MIN(date)) AS days "+
				"FROM grindatron__cycles "
			);
			
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				nbDays = rs.getInt("days");
			}
			rs.close();

			if (nbDays < 1)
				nbDays = 1;
			
			
			//Get the list of possible targets
			pstmt = conn.prepareStatement("" +
				"SELECT item, rarity, max, odds, label " + 
				"FROM grindatron__possible_targets " +
				"WHERE max > 0 " +
				"  AND rarity <= ? "
			);
			pstmt.setInt(1, nbDays);
			
			rs = pstmt.executeQuery();
			
			int i = 0;
			while (rs.next()) {
				possibleTargets.put(i, new PossibleTarget(rs.getString("item"), rs.getInt("rarity"), rs.getInt("max"), rs.getFloat("odds"), rs.getString("label")));
				i += 1;
			}
			rs.close();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		
		//Pick a random target and determine the amount
		int amount = 1;
		PossibleTarget selected = null;
		Random rand = new Random();
		int pos = rand.nextInt(possibleTargets.size());
		selected = possibleTargets.get(pos);

		//Roll again if the odds aren't in our favor
		while (rand.nextFloat() > selected.odds) {
			pos = rand.nextInt(possibleTargets.size());
			selected = possibleTargets.get(pos);
		}
		
		//Determine the amount
		amount = 1;
		double oddsAmount = 0;
		if (selected.rarity == 5 || selected.rarity == 4) {
			amount = 1;
		} else if (selected.rarity == 3) {
			oddsAmount = 0.2;
		} else if (selected.rarity == 2) {
			oddsAmount = 0.5;
		} else {
			oddsAmount = 0.9;
		}
		
		while (rand.nextDouble() < oddsAmount) {
			amount *= 2;
			if (amount >= 64) {
				amount = 64;
				break;
			}
		}
		if (amount > selected.max)
			amount = selected.max;
		
		//System.out.println(selected.item + " | " + selected.rarity + " > amount: " + amount + " (" + selected.max + ")");
		
		//Save this to the database
		try {
			PreparedStatement pstmt = null;
			
			//Set or update the title
			pstmt = conn.prepareStatement("" +
				"INSERT INTO grindatron__cycles (date, cycle, label, target, amount) " +
				"VALUES (?, ?, ?, ?, ?) "
			);
			pstmt.setString(1, date.toString());
			pstmt.setInt(2, cycle);
			pstmt.setString(3, selected.label);
			pstmt.setString(4, selected.item);
			pstmt.setInt(5, amount);
				
			@SuppressWarnings("unused")
			int res = pstmt.executeUpdate();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		Target target = new Target(date, cycle, selected.label, selected.item, amount);
		return target;
	}
	
	
	//Save a player's successful target
	public void savePlayerTarget(Player player, Target target) {

		Connection conn = null;
		String query = "";
		
		try {
			conn = DriverManager.getConnection(database);
			
			PreparedStatement pstmt = null;
			
			//Set or update the title
			pstmt = conn.prepareStatement("" +
				"INSERT IGNORE INTO grindatron__cycles_players (date, cycle, uuid, amount, done) " +
				"VALUES (?, ?, ?, ?, ?) "
			);
			pstmt.setString(1, target.date.toString());
			pstmt.setInt(2, target.cycle);
			pstmt.setString(3, player.getUniqueId().toString());
			pstmt.setDouble(4, target.amount);
			pstmt.setDouble(5, 1);
			
			@SuppressWarnings("unused")
			int res = pstmt.executeUpdate();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
}
