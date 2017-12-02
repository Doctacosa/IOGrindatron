package com.interordi.iogrinder;

import java.time.LocalDateTime;

import com.interordi.iogrinder.structs.Target;
import com.interordi.iogrinder.utilities.ActionBar;
import com.interordi.iogrinder.utilities.Title;


public class PeriodManager implements Runnable {
	
	
	private int currentPeriod = -1;
	
	final public static int periodDuration = 1;	//TODO: Set to 4
	
	
	public PeriodManager() {
		currentPeriod = getPeriod();
	}
	
	
	//Get the number of the current period
	public static int getPeriod() {
		
		LocalDateTime date = LocalDateTime.now();
		int hour = date.getHour();
		
		return hour / periodDuration;
	}
	
	
	//Get the percentage of the current period's progress, from 0 to 1
	public static float getPeriodProgress() {
		
		LocalDateTime date = LocalDateTime.now();
		
		int currentPeriod = date.getHour() / periodDuration;
		int periodStartHour = currentPeriod * periodDuration;
		int progress = (date.getHour() - periodStartHour) * 60 + date.getMinute();
		
		return (float)progress / (float)(periodDuration * 60);
	}


	@Override
	public void run() {
		//Check for period changes every minute
		
		int nowPeriod = getPeriod();
		
		if (nowPeriod != currentPeriod) {
			currentPeriod = nowPeriod;
			
			Target target = IOGrinder.db.getCycleTarget();
			
			//Reset stats, it's a new period!
			Title.toAll("", "Now changing periods - stats reset!", 0);
			ActionBar.toAll("New target: &l" + target.label);
			Players.fillEnergy();
		}
		
		//Update the period progress bar on all players
		Players.updatePeriodProgress(getPeriodProgress());
	}
}
