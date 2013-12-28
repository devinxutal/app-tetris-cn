package com.devinxutal.tetris.record;

import java.io.Serializable;

public class SavableLocalScore implements Serializable, Comparable {
	private String player;
	private int score;

	public SavableLocalScore(String player, int score) {
		this.player = player;
		this.score = score;
	}

	public SavableLocalScore() {
		this("local player", 0);
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int compareTo(Object object) {
		if (object instanceof SavableLocalScore) {
			SavableLocalScore score = (SavableLocalScore) object;
			return -(this.score - score.getScore());
		}
		return 0;
	}
}