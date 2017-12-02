package com.interordi.iogrinder.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.bukkit.entity.Player;

import com.interordi.iogrinder.PeriodManager;
import com.interordi.iogrinder.PlayerWatcher;
import com.interordi.iogrinder.structs.Target;

public class Database {
	
	//MySQL server information
	private String dbServer = "";
	private String dbUsername = "";
	private String dbPassword = "";
	private String dbBase = "";
	
	
	public Database(String server, String username, String password, String base) {
		
		this.dbServer = server;
		this.dbUsername = username;
		this.dbPassword = password;
		this.dbBase = base;
	}
	
	
	//Load a player's information
	public PlayerWatcher loadPlayer(Player player) {
		Connection conn = null;
		String query = "";
		PlayerWatcher watcher = null;
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + dbServer + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword);
			
			//Get the player's data
			PreparedStatement pstmt = conn.prepareStatement("" +
				"SELECT energy, last_date, last_cycle " + 
				"FROM grindatron__players " +
				"WHERE uuid = ?"
			);
			
			pstmt.setString(1, player.getUniqueId().toString());
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				watcher = new PlayerWatcher(player, rs.getDouble("energy"), LocalDate.parse(rs.getString("last_date")), rs.getInt("last_cycle"));
			}
			rs.close();
			
			if (watcher == null) {
				watcher = new PlayerWatcher(player);
				System.out.println("Not FOUND");
			}
			
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
			conn = DriverManager.getConnection("jdbc:mysql://" + dbServer + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword);
			
			//TODO: Save actual date
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
			conn = DriverManager.getConnection("jdbc:mysql://" + dbServer + "/" + dbBase + "?user=" + dbUsername + "&password=" + dbPassword);
			
			//Get the player's data
			PreparedStatement pstmt = conn.prepareStatement("" +
				"SELECT label, target, durability, amount " + 
				"FROM grindatron__cycles " +
				"WHERE date = ? " +
				"  AND cycle = ? "
			);
			
			pstmt.setString(1, date.toString());
			pstmt.setInt(2, cycle);
			System.out.println(pstmt.toString());
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				target = new Target(rs.getString("label"), rs.getString("target"), rs.getInt("durability"), rs.getInt("amount"));
			}
			rs.close();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Query: " + query);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		//TODO: Better fallback
		if (target == null)
			target = new Target("a torch", "torch", -1, 1);

		return target;
	}

	public Target getCycleTarget() {
		return getCycleTarget(LocalDate.now(), PeriodManager.getPeriod());
	}
}
