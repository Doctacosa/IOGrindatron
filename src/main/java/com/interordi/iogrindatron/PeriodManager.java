package com.interordi.iogrindatron;

import java.time.LocalDateTime;

import com.interordi.iogrindatron.structs.Target;
import com.interordi.iogrindatron.utilities.Title;


public class PeriodManager implements Runnable {
	
	
	private int currentPeriod = -1;
	
	final public static int periodDuration = 4;
	private static Target currentTarget = null;
	
	
	public PeriodManager() {
		currentPeriod = getPeriod();
	}
	
	
	//Get the number of the active period
	public int getCurrentPeriod() {
		return currentPeriod;
	}
	
	
	//Get which cycle should be per the clock
	public static int getPeriod() {
		
		LocalDateTime date = LocalDateTime.now();
		int hour = date.getHour();
		
		return hour / periodDuration;
	}
	
	
	//Get the percentage of the current period's progress, from 0 to 1
	public static double getPeriodProgress() {
		
		LocalDateTime date = LocalDateTime.now();
		
		int currentPeriod = date.getHour() / periodDuration;
		int periodStartHour = currentPeriod * periodDuration;
		int progress = (date.getHour() - periodStartHour) * 60 + date.getMinute();
		
		return (double)progress / (double)(periodDuration * 60);
	}


	//Get the current target
	public static Target getCurrentTarget(boolean force) {
		if (force || currentTarget == null)
			currentTarget = IOGrindatron.db.getCycleTarget();
		return currentTarget;
	}


	@Override
	public void run() {
		//Check for period changes every minute
		
		int nowPeriod = getPeriod();
		
		if (nowPeriod != currentPeriod) {
			currentPeriod = nowPeriod;
			
			currentTarget = getCurrentTarget(true);
			
			//Reset stats, it's a new period!
			Title.toAll("", "Now changing periods - stats reset!", 0);
			Players.resetCycle();
		}
		
		//Update the period progress bar on all players
		Players.updatePeriodProgress(getPeriodProgress());
	}
}
