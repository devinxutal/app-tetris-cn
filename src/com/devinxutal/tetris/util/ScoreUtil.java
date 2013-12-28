package com.devinxutal.tetris.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.os.Environment;
import android.util.Log;

import com.devinxutal.tetris.cfg.Constants;
import com.devinxutal.tetris.record.SavableLocalScores;

public class ScoreUtil {

	public static SavableLocalScores loadLocalScores() {
		Log.v("ScoreUtil", "now load local scores");
		File file = Environment.getExternalStorageDirectory();
		try {
			File dataFile = new File(file, Constants.DATA_DIR + "/"
					+ Constants.SCORE_SAVING_FILE);
			if (!dataFile.exists()) {
				return new SavableLocalScores();
			}
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					dataFile));
			SavableLocalScores scores = (SavableLocalScores) in.readObject();

			Log.v("ScoreUtil", "arrive here local scores null? "
					+ (scores == null));
			if (scores != null) {

				Log.v("ScoreUtil", "not null, score num: "
						+ (scores.getLocalRankForList(0, 20).size()));
			}
			in.close();
			return scores;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SavableLocalScores();
	}

	public static void saveCubeState(String player, int score) {
		File file = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(file, Constants.DATA_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File dataFile = new File(dir, Constants.SCORE_SAVING_FILE);
			SavableLocalScores scores = loadLocalScores();
			if (scores == null) {
				scores = new SavableLocalScores();
			}
			if (player == null || player.length() == 0) {
				player = "Local Player";
			}
			scores.addScore(player, score);
			scores.trim();
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(dataFile));
			out.writeObject(scores);
			out.close();
			Log.v("ScoreUtil", "save score succeed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
