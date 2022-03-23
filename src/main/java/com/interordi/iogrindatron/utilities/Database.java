package com.interordi.iogrindatron.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.interordi.iogrindatron.IOGrindatron;
import com.interordi.iogrindatron.PeriodManager;
import com.interordi.iogrindatron.PlayerWatcher;
import com.interordi.iogrindatron.structs.PossibleTarget;
import com.interordi.iogrindatron.structs.Target;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Database {
	
	private IOGrindatron plugin = null;
	private String database = "";
	
	
	public Database(IOGrindatron plugin, String dbHost, int dbPort, String dbUsername, String dbPassword, String dbBase) {

		this.plugin = plugin;
		
		database = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword + "&useSSL=false";
	}
	
	
	//Initialize the database
	public boolean init() {

		//Create or update the required database table
		//A failure indicates that the database wasn't configured properly
		Connection conn = null;
		PreparedStatement pstmt = null;
		String query = "";
		
		try {
			conn = DriverManager.getConnection(database);
			
			query = "" +
				"CREATE TABLE IF NOT EXISTS `grindatron__cycles` ( " +
				"  `date` date NOT NULL, " +
				"  `cycle` tinyint(2) NOT NULL, " +
				"  `label` varchar(30) NOT NULL, " +
				"  `target` varchar(40) NOT NULL, " +
				"  `amount` int(11) NOT NULL, " +
				"  PRIMARY KEY (`date`,`cycle`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8; "
			;
			pstmt = conn.prepareStatement(query);
			pstmt.executeUpdate();
			
			query = "" +
				"CREATE TABLE IF NOT EXISTS `grindatron__cycles_players` ( " +
				"  `date` date NOT NULL, " +
				"  `cycle` tinyint(2) NOT NULL, " +
				"  `uuid` varchar(36) NOT NULL, " +
				"  `amount` int(11) NOT NULL, " +
				"  `done` tinyint(1) NOT NULL, " +
				"  PRIMARY KEY (`date`,`cycle`,`uuid`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8; "
			;
			pstmt = conn.prepareStatement(query);
			pstmt.executeUpdate();
			
			query = "" +
				"CREATE TABLE IF NOT EXISTS `grindatron__players` ( " +
				"  `uuid` varchar(36) NOT NULL, " +
				"  `energy` float NOT NULL, " +
				"  `last_date` date NOT NULL, " +
				"  `last_cycle` tinyint(2) NOT NULL, " +
				"  PRIMARY KEY (`uuid`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8; "
			;
			pstmt = conn.prepareStatement(query);
			pstmt.executeUpdate();
			
			query = "" +
				"CREATE TABLE IF NOT EXISTS `grindatron__players_daily` ( " +
				"  `uuid` varchar(36) NOT NULL, " +
				"  `date` date NOT NULL, " +
				"  PRIMARY KEY (`uuid`,`date`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8; "
			;
			pstmt = conn.prepareStatement(query);
			pstmt.executeUpdate();
			
			query = "" +
				"CREATE TABLE IF NOT EXISTS `grindatron__possible_targets` ( " +
				"  `item` varchar(50) NOT NULL, " +
				"  `rarity` tinyint(4) NOT NULL DEFAULT 1, " +
				"  `max` tinyint(4) NOT NULL DEFAULT -1, " +
				"  `odds` float NOT NULL DEFAULT 1, " +
				"  `label` varchar(50) NOT NULL, " +
				"  PRIMARY KEY (`item`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8; "
			;
			pstmt = conn.prepareStatement(query);
			pstmt.executeUpdate();


			//Load the list of possible targets if missing
			pstmt = conn.prepareStatement("" +
				"SELECT COUNT(*) AS amount " +
				"FROM grindatron__possible_targets "
			);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				if (rs.getInt("amount") == 0) {

					//Read the SQL query from the file
					try {
						StringBuilder builder = new StringBuilder();
						InputStream stream = getClass().getResourceAsStream("/targets.sql");
						BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line + System.lineSeparator());
						}
						query = builder.toString();

						//If everything checked out, insert the targets
						pstmt = conn.prepareStatement(query);
						pstmt.executeUpdate();
							
					} catch (IOException e) {
						Bukkit.getLogger().info("Failed to read targets.sql.");
						return false;
					}
				}
			}
			rs.close();

			
		} catch (SQLException ex) {
			Bukkit.getLogger().severe("Query: " + query);
			Bukkit.getLogger().severe("SQLException: " + ex.getMessage());
			Bukkit.getLogger().severe("SQLState: " + ex.getSQLState());
			Bukkit.getLogger().severe("VendorError: " + ex.getErrorCode());
			return false;
		}


		return true;
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
				"ORDER BY date DESC "
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
				Bukkit.getLogger().warning("Not FOUND");
			}
			
			if (giveConnectionBonus && nbDays > 0)
				watcher.setConnectionBonus(nbDays);
			
			
		} catch (SQLException ex) {
			// handle any errors
			Bukkit.getLogger().warning("Query error for " + plugin.getName() + ": " + query);
			Bukkit.getLogger().warning("Error " + ex.getErrorCode() + ": " + ex.getMessage());
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
			Bukkit.getLogger().warning("Query error for " + plugin.getName() + ": " + query);
			Bukkit.getLogger().warning("Error " + ex.getErrorCode() + ": " + ex.getMessage());
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
			Bukkit.getLogger().warning("Query error for " + plugin.getName() + ": " + query);
			Bukkit.getLogger().warning("Error " + ex.getErrorCode() + ": " + ex.getMessage());
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
			int maxDifficulty = 1;
			PreparedStatement pstmt = conn.prepareStatement("" +
				"SELECT DATEDIFF(CURDATE(), MIN(date)) AS days "+
				"FROM grindatron__cycles "
			);
			
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				maxDifficulty = rs.getInt("days");
			}
			rs.close();

			//Cut by two, max difficulty only possible after 10 days
			maxDifficulty /= 2;

			if (maxDifficulty < 1)
				maxDifficulty = 1;
			
			
			//Get the list of possible targets
			pstmt = conn.prepareStatement("" +
				"SELECT item, rarity, max, odds, label " + 
				"FROM grindatron__possible_targets " +
				"WHERE max > 0 " +
				"  AND rarity <= ? " +
				"  AND item NOT IN " +
				"  ( " +
				"    SELECT target " +
				"    FROM grindatron__cycles " +
				"    WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
				"  ) "
			);
			pstmt.setInt(1, maxDifficulty);
			
			rs = pstmt.executeQuery();
			
			int i = 0;
			while (rs.next()) {
				possibleTargets.put(i, new PossibleTarget(rs.getString("item"), rs.getInt("rarity"), rs.getInt("max"), rs.getFloat("odds"), rs.getString("label")));
				i += 1;
			}
			rs.close();
			
		} catch (SQLException ex) {
			// handle any errors
			Bukkit.getLogger().warning("Query error for " + plugin.getName() + ": " + query);
			Bukkit.getLogger().warning("Error " + ex.getErrorCode() + ": " + ex.getMessage());
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
		
		//Bukkit.getLogger().info(selected.item + " | " + selected.rarity + " > amount: " + amount + " (" + selected.max + ")");
		
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
			Bukkit.getLogger().warning("Query error for " + plugin.getName() + ": " + query);
			Bukkit.getLogger().warning("Error " + ex.getErrorCode() + ": " + ex.getMessage());
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
			Bukkit.getLogger().warning("Query error for " + plugin.getName() + ": " + query);
			Bukkit.getLogger().warning("Error " + ex.getErrorCode() + ": " + ex.getMessage());
		}
	}
}
