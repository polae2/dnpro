package com.ndn.diceView;

public class Dice {
	public int width = 60, height = 60;
	public float dx, dy;
	public float x, y;
	
	public Dice() {
		// set acceleration
		dx = (float) (15 + Math.random() * 40);
		dy = (float) (15 + Math.random() * 40);
	}
}
