package com.interordi.iogrinder.structs;

public class Target {
	
	public String label = "";
	public String item = "";
	public int durability = 0;
	public int amount = 1;


	public Target(String label, String item, int durability, int amount) {
		this.label = label;
		this.item = item;
		this.durability = durability;
		this.amount = amount;
	}
}
