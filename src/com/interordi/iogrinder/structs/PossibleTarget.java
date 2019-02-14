package com.interordi.iogrinder.structs;

public class PossibleTarget {
	
	public String item;
	public int rarity;
	public int max;
	public String label;
	
	
	public PossibleTarget(String item, int rarity, int max, String label) {
		this.item = item;
		this.rarity = rarity;
		this.max = max;
		this.label = label;
	}
}
