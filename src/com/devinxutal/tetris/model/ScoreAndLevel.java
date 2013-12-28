package com.devinxutal.tetris.model;

import java.io.Serializable;

import com.devinxutal.tetris.cfg.Configuration;

public class ScoreAndLevel implements Serializable {
	public static final int MAX_LEVEL = 15;
	int level;
	int score;
	int totalLines;
	int currentLines;

	public void reset() {
		level = 1;
		score = 0;
		totalLines = 0;
		currentLines = 0;
		if (Configuration.config() != null) {
			level = Configuration.config().getStartLevel();
		}
	}

	public int getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}

	public int getCurrentLines() {
		return currentLines;
	}

	public int addScore(int score) {
		return (this.score += score);
	}

	public int getScore() {
		return score;
	}

	public int getLevel() {
		return level;
	}

	public int getGoal() {
		return level * 5;
	}

	public int getGoalRemained() {
		return Math.max(getGoal() - getCurrentLines(), 0);
	}

	public void eliminateLines(int lineNum, boolean combo) {
		int addScore = 0;
		switch (lineNum) {
		case 1:
			addScore = 20;
			break;
		case 2:
			addScore = 60;
			break;
		case 3:
			addScore = 120;
			break;
		case 4:
			addScore = 200;
			break;
		}
		if (combo) {
			addScore = addScore * 5;
		}
		addScore(addScore);
		currentLines += lineNum;
		totalLines += lineNum;
		if (currentLines >= getGoal() && level < MAX_LEVEL) {
			level++;
			currentLines = 0;
		}
	}

	public void rowsFallDown(int rows) {
		int addScore = (rows / 4);
		addScore(addScore);
	}

	public String getScroreString(int digits) {
		String s = "";
		long temp = score;
		while (temp > 0) {
			s = temp % 10 + s;
			temp = temp / 10;
		}
		while (s.length() < digits) {
			s = "0" + s;
		}
		return s;
	}

	public float getMaxLevel() {
		return MAX_LEVEL;
	}
}
