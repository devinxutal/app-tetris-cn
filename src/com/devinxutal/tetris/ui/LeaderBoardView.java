package com.devinxutal.tetris.ui;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.record.JScore;
import com.devinxutal.tetris.util.BitmapUtil;
import com.devinxutal.tetris.util.TextPainter;
import com.devinxutal.tetris.util.TextPainter.Align;
import com.devinxutal.tetriscn.GoTetrisApplication;

public class LeaderBoardView extends View implements OnTouchListener {
	public static final int BTN_SCOPE = 3300;
	public static final int BTN_MODE = 3401;
	public static final int BTN_SCORELOOP = 3402;

	public static final int BTN_PREV = 3303;
	public static final int BTN_NEXT = 3304;
	public static final int BTN_TOP = 3305;
	public static final int BTN_ME = 3306;

	private static final String TAG = "LeaderBoardView";

	private List<ButtonInfo> buttons = new LinkedList<ButtonInfo>();
	private Configuration config;

	private DrawingMetrics dm = new DrawingMetrics();

	private List<JScore> scores = new LinkedList<JScore>();

	private int itemNum = 10;

	private List<ButtonListener> listeners = new LinkedList<ButtonListener>();

	public LeaderBoardView(Context context) {
		super(context);
		this.config = Configuration.config();
		this.setOnTouchListener(this);
		dm.createButtons();
	}

	public void setScores(List<JScore> scores) {
		this.scores.clear();
		this.scores.addAll(scores);
		this.invalidate();
	}
	public void clearScores(){
		this.scores.clear();
		this.invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.dm.onSizeChanged(w, h);
		Log.v(TAG, "size changed : " + w + "," + h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.v(TAG, "onDraw");
		super.onDraw(canvas);
		dm.onDraw(canvas);
	}

	public boolean onTouch(View view, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.v(TAG, "on touch down: " + event.getX() + "," + event.getY());
			float x = event.getX();
			float y = event.getY();
			boolean notified = false;
			for (ButtonInfo button : buttons) {
				Log.v(TAG, "check button: " + button.bound);
				if (button.bound.contains((int) x, (int) y) && !notified) {
					if (!button.pressed && button.enabled) {
						button.pressed = true;
						Log.v(TAG, "notify button pressed");
						this.notifyButtonPressed(button.id);
					}
					notified = true;
				} else {
					if (button.pressed) {
						button.pressed = false;
						if (button.enabled) {
							this.notifyButtonReleased(button.id);
						}
					}
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {

			Log.v(TAG, "on touch up");
			float x = event.getX();
			float y = event.getY();
			for (ButtonInfo button : buttons) {
				if (button.pressed) {
					button.pressed = false;
					if (button.enabled) {
						if (button.bound.contains((int) x, (int) y)) {
							this.notifyButtonReleased(button.id);
							this.notifyButtonClicked(button.id);
						} else {
							this.notifyButtonReleased(button.id);
						}
					}
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float x = event.getX();
			float y = event.getY();
			for (ButtonInfo button : buttons) {
				if (button.pressed) {
					if (button.bound.contains((int) x, (int) y)) {
						// do nothing
					} else {
						button.pressed = false;
						if (button.enabled) {
							this.notifyButtonReleased(button.id);
						}
					}
				}
			}
		}
		return true;
	}

	public ButtonInfo getButtonInfo(int id) {
		for (ButtonInfo b : buttons) {
			if (b.id == id) {
				return b;
			}
		}
		return null;
	}

	public void setButtonText(int id, String text) {
		ButtonInfo b = getButtonInfo(id);
		if (b != null) {
			b.text = text;
		}
		this.invalidate();
	}

	public void setButtonEnabled(int id, boolean enabled) {
		ButtonInfo b = getButtonInfo(id);
		if (b != null) {
			b.enabled = enabled;
		}
		this.invalidate();
	}

	public void configurationChanged(Configuration config) {

	}

	protected class ButtonInfo {
		public int id;
		public String text;
		public Rect bound;
		public Drawable drawable;

		// for click recognition
		public boolean pressed = false;
		public boolean enabled = true;

		public void draw(Canvas canvas, Paint paint) {
			paint.setColor(dm.color_btn_normal);
			paint.setAlpha(255);
			paint.setAntiAlias(true);
			paint.setShadowLayer(4, 1, 1, Color.BLACK);
			if (!this.enabled) {
				paint.setColor(dm.color_btn_disabled);
			} else if (this.pressed) {
				paint.setColor(dm.color_btn_focused);
			}
			float r = getRound();
			canvas.drawRoundRect(new RectF(bound), r, r, paint);
			if (text != null) {
				dm.painter.setStrokeColor(Color.argb(150, 100, 100, 255));
				dm.painter.setTextColor(Color.WHITE);
				dm.painter.setTextSize(bound.height() / 3);
				dm.painter.setStrokeWidth(bound.height() / 20);
				dm.painter.drawText(canvas, text, new RectF(bound),
						Align.Center);
			}
		}

		public float getRound() {
			return bound.height() / 4;
		}
	}

	public class DrawingMetrics {
		Paint paint;
		TextPainter painter;

		private Bitmap bg;

		private Rect titleArea;
		private Rect selectionArea;
		private Rect scoreArea;
		private Rect buttonArea;

		private float round;
		private BitmapUtil util;

		private final int color_layer_mask = Color.argb(100, 0, 0, 0);
		private final int color_scoreboard = Color.argb(200, 0, 0, 0);
		private final int color_scoreboard_light = Color.argb(200, 80, 80, 80);
		private final int color_scoreboard_highlight = Color.rgb(100, 0, 150);
		private final int color_shaddow = Color.DKGRAY;

		private final int color_btn_normal = Color.rgb(150, 100, 255);
		private final int color_btn_focused = Color.rgb(200, 180, 255);
		private final int color_btn_disabled = Color.rgb(100, 100, 100);

		public DrawingMetrics() {
			paint = new Paint();
			paint.setAntiAlias(true);
			// paint.setFilterBitmap(true);

			painter = new TextPainter();
			util = GoTetrisApplication.getBitmapUtil();
		}

		public void onDraw(Canvas canvas) {
			paint.setAlpha(255);
			GoTetrisApplication.getBitmapUtil().drawBackgroundBitmap(canvas, getWidth(),
					getHeight(), paint);

			canvas.drawColor(color_layer_mask);
			paint.setAlpha(255);
			for (ButtonInfo b : buttons) {
				b.draw(canvas, paint);
			}
			this.drawScores(canvas);
		}

		public void drawScores(Canvas canvas) {
			Log.v(TAG, "Draw scores");
			paint.setColor(color_scoreboard);
			paint.setAlpha(150);
			paint.setAntiAlias(true);
			paint.setShadowLayer(5, 1, 1, color_shaddow);
			round = 5;
			if (!buttons.isEmpty()) {

				round = buttons.get(0).getRound();
			}
			canvas.drawRoundRect(new RectF(scoreArea), round, round, paint);
			// draw score
			int padding = 2;
			int l = scoreArea.left + padding;
			int r = scoreArea.right - padding;
			int h = (int) (scoreArea.height() - 2 * round) / (itemNum + 1);
			int t = scoreArea.top + (scoreArea.height() - (itemNum + 1) * h)
					/ 2;
			Rect rect = new Rect(l, t, r, t + h);
			int index = 0;
			drawScoreHeader(canvas, rect);
			for (index = 0; index < itemNum; index++) {
				JScore score = null;
				if (index < scores.size()) {
					score = scores.get(index);
				}
				rect.top += h;
				rect.bottom += h;
				drawScore(canvas, index, rect, score);
			}
		}

		public void drawScoreHeader(Canvas canvas, Rect rect) {

			painter.setStrokeWidth(util.dipToPixel(1f));
			painter.setTextSize(util.dipToPixel(14));
			int pad = (int) util.dipToPixel(5);
			// draw rank
			painter.drawText(canvas, "Rank", new RectF(rect.left + pad,
					rect.top, rect.right, rect.bottom), TextPainter.Align.Left);
			painter.drawText(canvas, "Player",
					new RectF(rect.left + rect.width() * 1 / 5, rect.top,
							rect.right, rect.bottom), TextPainter.Align.Left);
			painter.drawText(canvas, "Score",
					new RectF(rect.right - rect.width() * 3 / 10, rect.top,
							rect.right - pad, rect.bottom),
					TextPainter.Align.Right);

		}

		public void drawScore(Canvas canvas, int index, Rect rect, JScore score) {

			Log.v(TAG, "Draw score: " + score);

			int pad = (int) util.dipToPixel(5);

			paint.setColor(Color.WHITE);
			paint.setAlpha(200);
			paint.setAntiAlias(true);
			paint.setShadowLayer(0, 0, 0, 0);
			float round = rect.height() / 4;

			if (score != null && score.isHighlight()) {
				paint.setColor(color_scoreboard_highlight);
				canvas.drawRoundRect(new RectF(rect), round, round, paint);
			} else if (index % 2 == 0) {
				paint.setColor(color_scoreboard_light);
				canvas.drawRoundRect(new RectF(rect), round, round, paint);
			}
			if (score != null) {
				// draw score

				painter.setStrokeWidth(util.dipToPixel(1f));
				// draw rank
				painter.setTextSize(util.dipToPixel(10));
				painter.drawText(canvas, score.getRank() + "", new RectF(
						rect.left + pad, rect.top, rect.right, rect.bottom),
						TextPainter.Align.Left);
				// draw name
				
					painter.setTextSize(util.dipToPixel(12));
					painter.drawText(canvas, score.getPlayer(),
							new RectF(rect.left + rect.width() * 1 / 5,
									rect.top, rect.right, rect.bottom),
							TextPainter.Align.Left);
			
				// draw score
				painter.setTextSize(util.dipToPixel(12));
				painter.drawText(canvas, ("" + score.getScore()),
						new RectF(rect.right - rect.width() * 3 / 10, rect.top,
								rect.right - pad, rect.bottom),
						TextPainter.Align.Right);
			}

		}

		public void onSizeChanged(int width, int height) {
			int unit = height / 10;
			int gapV = unit / 4;
			int gapH = width / 40;
			int l = gapH, t = gapV, w = width - 2 * gapH, h = unit;
			// title
			// titleArea = new Rect(l, t, l + w, t + h);
			// selection area
			//t += h + gapV;
			selectionArea = new Rect(l, t, l + w, t + h);
			// score area
			t += h + gapV;
			scoreArea = new Rect(l, t, l + w, t + unit * 7);
			// button area
			t = scoreArea.bottom + gapV;
			buttonArea = new Rect(l, t, l + w, t + h);
			adjustButtons();
		}

		public void adjustButtons() {
			int gap = selectionArea.left;
			int unit = (selectionArea.width() - 2 * gap) / 6;
			getButtonInfo(BTN_SCOPE).bound = new Rect(selectionArea.left,
					selectionArea.top, selectionArea.left + 2 * unit,
					selectionArea.bottom);

			getButtonInfo(BTN_MODE).bound = new Rect(selectionArea.left + 2
					* unit + gap, selectionArea.top, selectionArea.left + 2
					* unit + gap + 3 * unit, selectionArea.bottom);
			getButtonInfo(BTN_SCORELOOP).bound = new Rect(selectionArea.right
					- unit, selectionArea.top, selectionArea.right,
					selectionArea.bottom);

			// buttons
			int ids[] = new int[] { BTN_PREV, BTN_ME, BTN_TOP, BTN_NEXT };
			int btn_w = (buttonArea.width() - 3 * gap) / 4;
			for (int i = 0; i < 4; i++) {
				getButtonInfo(ids[i]).bound = new Rect(buttonArea.left + i
						* (gap + btn_w), buttonArea.top, buttonArea.left + i
						* (gap + btn_w) + btn_w, buttonArea.bottom);
			}
		}

		public void createButtons() {
			buttons.clear();
			Rect rect = new Rect();
			buttons.add(this.createSelectionButton(BTN_SCOPE, "Global", rect,
					null));

			buttons.add(this.createSelectionButton(BTN_MODE, "Normal Mode",
					rect, null));
			buttons.add(this.createSelectionButton(BTN_SCORELOOP, "SL", rect,
					null));

			// buttons
			int ids[] = new int[] { BTN_PREV, BTN_ME, BTN_TOP, BTN_NEXT };
			Drawable drawables[] = new Drawable[] { null, null, null, null };
			String texts[] = new String[] { "<", "Me", "Top", ">" };
			for (int i = 0; i < 4; i++) {
				buttons.add(this.createNavigationButton(ids[i], texts[i], rect,
						drawables[i]));
			}

		}

		private ButtonInfo createSelectionButton(int id, String text,
				Rect bound, Drawable drawable) {
			ButtonInfo b = this.createButton(id, text, bound, drawable);
			return b;
		}

		private ButtonInfo createNavigationButton(int id, String text,
				Rect bound, Drawable drawable) {
			ButtonInfo b = this.createButton(id, text, bound, drawable);
			return b;
		}

		private ButtonInfo createButton(int id, String text, Rect bound,
				Drawable drawable) {
			ButtonInfo b = new ButtonInfo();
			b.id = id;
			b.text = text;
			b.bound = bound;
			b.drawable = drawable;
			return b;
		}
	}

	public interface ButtonListener {
		void buttonClickced(int id);

		void buttonPressed(int id);

		void buttonReleased(int id);
	}

	public boolean addButtonListener(ButtonListener l) {
		return listeners.add(l);
	}

	public boolean removeButtonListener(ButtonListener l) {
		return listeners.remove(l);
	}

	public void clearButtonListener() {
		listeners.clear();
	}

	public void notifyButtonClicked(int id) {
		for (ButtonListener l : listeners) {
			l.buttonClickced(id);
		}
		this.invalidate();
	}

	public void notifyButtonPressed(int id) {
		for (ButtonListener l : listeners) {
			l.buttonPressed(id);
		}
		this.invalidate();
	}

	public void notifyButtonReleased(int id) {
		for (ButtonListener l : listeners) {
			l.buttonReleased(id);
		}
		this.invalidate();
	}
}
