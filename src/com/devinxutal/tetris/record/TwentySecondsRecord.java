package com.devinxutal.tetris.record;

import java.util.Date;

public class TwentySecondsRecord {

	private String player;
	private long time;
	private Date commitTime;
	private int rank;
	private String description;

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getTime() {
		return time;
	}

	public String getTimeString() {
		return time / 1000 + "." + (time % 1000) / 10;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public Date getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toTabbedString() {
		String TAB = "\t";
		return rank + TAB + player + TAB + time + TAB + commitTime;
	}

	public static TwentySecondsRecord parse(String str) {
		TwentySecondsRecord record = new TwentySecondsRecord();
		String[] attrs = str.split("\t");
		try {
			record.setRank(Integer.valueOf(attrs[0]));
			record.setPlayer(attrs[1]);
			record.setTime(Integer.valueOf(attrs[2]));
			record.setDescription(attrs[6]);
			record.setCommitTime(new Date(Date.parse(attrs[7])));
		} catch (Exception e) {
		}
		return record;
	}
}
