package com.devinxutal.tetris.record;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SavableLocalScores implements Serializable {
	private static final LinkedList<SavableLocalScore> robotScores;
	static {
		robotScores = new LinkedList<SavableLocalScore>();
		String robotName = "Tetris Robot";
		int score = 10;
		for (int i = 0; i < 20; i++) {
			robotScores.addFirst(new SavableLocalScore(robotName, score));
			if (score >= 1000) {
				score += 1000;
			} else if (score >= 100) {
				score += 100;
			} else {
				score += 10;
			}
			// if (i % 2 == 0) {
			// score *= 5;
			// } else {
			// score *= 2;
			// }
		}
	}
	private List<SavableLocalScore> scores;

	public SavableLocalScores() {
		this.scores = new LinkedList<SavableLocalScore>();
	}

	public void trim() {
		Collections.sort(scores);
		while (scores.size() > 20) {
			scores.remove(scores.size() - 1);
		}
	}

	public List<TwentySecondsRecord> getLocalRank(int from, int count) {
		List<TwentySecondsRecord> rank = new LinkedList<TwentySecondsRecord>();
		List<SavableLocalScore> list = new LinkedList<SavableLocalScore>();
		list.addAll(scores);
		list.addAll(robotScores);
		Collections.sort(list);
		for (int i = from; i < Math.min(list.size(), from + count); i++) {
			SavableLocalScore score = list.get(i);
			TwentySecondsRecord record = new TwentySecondsRecord();
			record.setPlayer(score.getPlayer());
			record.setTime(score.getScore());
			record.setRank(i + 1);
		}
		return rank;
	}

	public List<Map<String, String>> getLocalRankForList(int from, int count) {
		List<Map<String, String>> rank = new LinkedList<Map<String, String>>();
		List<SavableLocalScore> list = new LinkedList<SavableLocalScore>();
		list.addAll(scores);
		list.addAll(robotScores);
		Collections.sort(list);
		for (int i = from; i < Math.min(from + list.size(), from + count); i++) {
			SavableLocalScore score = list.get(i);
			Map<String, String> record = new HashMap<String, String>();
			record.put("rank", i + 1 + "");
			record.put("player", score.getPlayer());
			record.put("score", score.getScore() + "");
			rank.add(record);
		}
		return rank;
	}

	public void addScore(String player, int score) {
		this.scores.add(new SavableLocalScore(player, score));
	}
}
