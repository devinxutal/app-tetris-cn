package com.devinxutal.tetris.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.widget.TextView;

public class GameTimer extends TextView {
	private boolean running = false;
	private Handler mHandler = new Handler();
	private long startTime;
	private long accumulatedTime = 0;

	public GameTimer(Context context) {
		super(context);
		this.setTextColor(Color.rgb(180, 150, 255));

		this.setShadowLayer(0.2f, 1, 1, Color.rgb(100, 100, 100));

		this.setText("00:00:00.0");
	}

	public long getTime() {
		long time = accumulatedTime;

		if (running) {
			time += System.currentTimeMillis() - startTime;
		}
		return time;
	}

	public void setTime(long time) {
		accumulatedTime = time;
		startTime = System.currentTimeMillis();
		this.setText(parseTime());
	}

	private String parseTime() {
		long ms = accumulatedTime;
		if (running) {
			ms += System.currentTimeMillis() - startTime;
		}

		int fraction = (int) (ms % 1000) / 100;
		int time = (int) ms / 1000;
		int h = (time / 3600) % 100;
		int m = (time / 60) % 60;
		int s = time % 60;
		String str = "";
		str += h < 10 ? "0" + h : "" + h;
		str += ":";
		str += m < 10 ? "0" + m : "" + m;
		str += ":";
		str += s < 10 ? "0" + s : "" + s;
		str += "." + fraction;
		return str;
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// Log.v(TAG, "on measure");
	// float w = getPaddingLeft() + getPaddingRight()
	// + paint.measureText("00:00:00.0");
	// float h = getPaddingTop() + getPaddingBottom() + paint.descent()
	// - paint.ascent();
	// this.setMeasuredDimension((int) w + 1, (int) h + 1);
	// }

	public void start() {
		if (running) {
			return;
		}
		running = true;
		this.startTime = System.currentTimeMillis();
		mHandler.postDelayed(cubeTimerTask, 100);
	}

	public void stop() {
		if (!running) {
			return;
		}
		running = false;
		this.accumulatedTime += System.currentTimeMillis() - startTime;
		this.startTime = -1;
	}

	public void reset() {
		if (running) {
			return;
		}
		this.accumulatedTime = 0;
		this.startTime = -1;
		this.setText(parseTime());
	}

	private Runnable cubeTimerTask = new Runnable() {
		public void run() {
			setText(parseTime());
			invalidate();
			if (running) {
				mHandler.postDelayed(this, 100);
			}
		}
	};

}
