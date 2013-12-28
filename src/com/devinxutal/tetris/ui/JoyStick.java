package com.devinxutal.tetris.ui;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class JoyStick extends View implements OnTouchListener {
	public interface JoyStickListener {
		void joyStickMoved(float dx, float dy);
	}

	private Paint paint;

	private List<JoyStickListener> listeners = new LinkedList<JoyStickListener>();

	public JoyStick(Context context) {
		super(context);
		this.setOnTouchListener(this);

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(18);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

	// drawing metrics
	int height;
	int width;
	int centerX;
	int centerY;
	int stickRadius;
	int panelRadius;
	int colorPanel = Color.argb(100, 255, 255, 255);
	int colorEdge = Color.argb(255, 255, 255, 255);
	int colorStick = Color.argb(200, 255, 150, 150);

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint.setColor(colorPanel);
		canvas.drawCircle(centerX, centerY, panelRadius, paint);

		paint.setColor(colorEdge);
		canvas.drawCircle(centerX + x, centerY + y, stickRadius + 2, paint);
		paint.setColor(colorStick);
		canvas.drawCircle(centerX + x, centerY + y, stickRadius, paint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		height = h;
		width = w;
		panelRadius = Math.min(height, width) / 2 - 1;
		stickRadius = panelRadius / 3;
		centerX = w / 2;
		centerY = h / 2;
		x = 0;
		y = 0;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private float x = 0;
	private float y = 0;

	public boolean onTouch(View view, MotionEvent event) {
		float dx = event.getX() - centerX;
		float dy = event.getY() - centerY;
		double distance = Math.sqrt(dx * dx + dy * dy);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (distance > panelRadius) {
				return true;
			} else {
				ajustStickPosition(dx, dy);
			}
		case MotionEvent.ACTION_MOVE:
			ajustStickPosition(dx, dy);
			break;
		case MotionEvent.ACTION_UP:
			x = 0;
			y = 0;
			notifyJoyStickMoved();
			break;
		}
		return true;
	}

	public float getDeltaX() {
		return x / (float) (panelRadius - stickRadius) * 1.5f;
	}

	public float getDeltaY() {
		return y / (float) (panelRadius - stickRadius) * 1.5f;
	}

	private void ajustStickPosition(float dx, float dy) {
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance > (panelRadius - stickRadius)) {
			float scale = (float) ((panelRadius - stickRadius) / distance);
			dx = dx * scale;
			dy = dy * scale;
		}
		if (Math.abs(x - dx) < 2 || Math.abs(y - dy) < 2) {
			return;
		}
		x = dx;
		y = dy;
		notifyJoyStickMoved();
	}

	public boolean addJoyStickListener(JoyStickListener l) {
		return listeners.add(l);
	}

	public boolean removeJoyStickListener(JoyStickListener l) {
		return listeners.remove(l);
	}

	public void clearJoyStickListener() {
		listeners.clear();
	}

	protected void notifyJoyStickMoved() {
		invalidate();
		for (JoyStickListener l : listeners) {
			l.joyStickMoved(getDeltaX(), getDeltaY());
		}
	}
}
