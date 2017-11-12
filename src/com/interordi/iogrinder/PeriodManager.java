package com.interordi.iogrinder;

import java.time.LocalDateTime;

import com.interordi.iogrinder.utilities.Title;


public class PeriodManager implements Runnable {
	
	
	private int currentPeriod = -1;
	
	final public int periodDuration = 4;
	
	
	public PeriodManager() {
		currentPeriod = getPeriod();
	}
	
	
	public int getPeriod() {
		
		LocalDateTime date = LocalDateTime.now();
		int hour = date.getHour();
		
		return hour / periodDuration;
	}


	@Override
	public void run() {
		//Check for period changes every minute
		
		int nowPeriod = getPeriod();
		
		if (nowPeriod != currentPeriod) {
			currentPeriod = nowPeriod;
			
			//TODO: Reset stats, it's a new period!
			Title.toAll("", "Now changing periods - stats reset!", 0);
		}
	}
}
