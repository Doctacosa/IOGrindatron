package com.interordi.iogrindatron.structs;

import java.time.LocalDate;

public class Target {
	
	public LocalDate date = LocalDate.now();
	public int cycle = 0;
	public String label = "";
	public String item = "";
	public int amount = 1;


	public Target(LocalDate date, int cycle, String label, String item, int amount) {
		this.date = date;
		this.cycle = cycle;
		this.label = label;
		this.item = item;
		this.amount = amount;
		
		if (this.label.isEmpty())
			this.label = item.replace('_', ' ');
		
		this.label = this.amount + " x " + this.label;
	}
}
