package com.devinxutal.tetris.record;

import cn.perfectgames.amaze.record.Record;

import com.scoreloop.client.android.core.model.Score;

public class JScore {
	private String player;
	private int rank;
	private int level;
	private int score;
	
	private boolean highlight;
	
	public String getPlayer() {
		return player;
	}
	public void setPlayer(String player) {
		this.player = player;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	
	
	public boolean isHighlight() {
		return highlight;
	}
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}
	public static Record toAmazeRecord(JScore score){
		//TODO implement it
		return null;
	}
	
	public static JScore fromAmazeRecord(Record record){
		JScore score = new JScore();
		score.setRank(record.getRank());
		score.setPlayer(record.getPlayer());
		score.setScore((int)Math.round(record.getResult()));
		score.setLevel(record.getLevel());
		return score;
	}
	
	public static Score toScoreloopScore(JScore score){
		// TODO implement it 
		return null;
	}
	
	public static JScore fromScoreloopScore(Score slScore){
		JScore score = new JScore();
		score.setRank(slScore.getRank());
		try{
		score.setPlayer(slScore.getUser().getDisplayName());
		}catch(Exception e){
			score.setPlayer("Unknown");
		}
		score.setScore((int)Math.round(slScore.getResult()));
		score.setLevel(slScore.getLevel());
		return score;
	}
}
