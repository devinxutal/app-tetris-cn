package com.devinxutal.tetris.ui;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.control.ButtonInfo;
import com.devinxutal.tetris.control.GameController;
import com.devinxutal.tetris.sound.SoundManager;
import com.devinxutal.tetris.util.MathUtil;
import com.devinxutal.tetriscn.R;

public class ControlView extends LinearLayout implements OnTouchListener,
		OnClickListener {
	private GameController controller;
	private List<GameControlListener> listeners = new LinkedList<GameControlListener>();

	public static final int BTN_PLAY = 3300;
	public static final int BTN_LEFT = 3301;
	public static final int BTN_RIGHT = 3302;
	public static final int BTN_TURN = 3303;
	public static final int BTN_DOWN = 3304;
	public static final int BTN_DIRECT_DOWN = 3305;
	public static final int BTN_HOLD = 3306;

	public static final int BTN_SOUND = 3401;
	public static final int BTN_MUSIC = 3402;

	private static final String TAG = "GameControlView";
	public static float SLIDE_THRESHOLD = 10;

	private List<ButtonInfo> buttons = new LinkedList<ButtonInfo>();

	private List<ImageButton> controlButtons = new LinkedList<ImageButton>();
	private ImageButton soundButton;
	private ImageButton musicButton;

	private Configuration config;
	private boolean dragMode = true;

	// metrics for drag mode
	private float slotGap = 0;
	private float dragDeltaX = 0;
	private boolean inDrag = false;

	public ControlView(Context context) {
		super(context);
		init();
		this.config = Configuration.config();
		this.dragMode = config.isDragMode();
		this.resetControlButtons();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		SLIDE_THRESHOLD = Math.min(w, h) / 50;
		this.slotGap = (float) (Math.min(w, this.controller.getPlayground()
				.getWidth() * 1.6) / this.controller.getPlayground().HORIZONTAL_BLOCKS);
		super.onSizeChanged(w, h, oldw, oldh);

	}

	public void setButtons(List<ButtonInfo> buttons) {
		this.buttons.clear();
		this.buttons.addAll(buttons);
	}

	public void setGameController(GameController controller) {
		this.controller = controller;
	}

	private void init() {
		this.soundButton = makeButton(BTN_SOUND, R.drawable.icon_sound_on);
		this.musicButton = makeButton(BTN_MUSIC, R.drawable.icon_music_on);
		controlButtons.add(soundButton);
		controlButtons.add(musicButton);
		for (ImageButton b : controlButtons) {
			this.addView(b);
		}
		this.setOnTouchListener(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	private float oldX = -1;
	private float oldY = -1;
	private float oldXforDrag = -1;
	private float oldYforDrag = -1;
	private float dragDeltaY = 0;

	public boolean onTouch(View arg0, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// for dragMode
			if (dragMode) {
				this.dragDeltaX = 0;
				this.dragDeltaY = 0;
			}
			// end for dragMode
			float x = event.getX();
			float y = event.getY();
			boolean notified = false;
			boolean touchOnButton = false;
			for (ButtonInfo button : buttons) {
				if (MathUtil.distance(x, y, button.x, button.y) <= button.radius
						&& !notified) {
					if (!button.pressed) {
						button.pressed = true;
						touchOnButton = true;
						this.notifyButtonPressed(button.buttonID);
					}
					notified = true;
				} else {
					if (button.pressed) {
						button.pressed = false;
						this.notifyButtonReleased(button.buttonID);
					}
				}
			}
			if (touchOnButton) {
				oldX = -1;
				oldY = -1;
				oldXforDrag = -1;
				oldYforDrag = -1;
			} else {
				oldX = x;
				oldY = y;
				if (dragMode) {
					oldXforDrag = x;
					oldYforDrag = y;
				}
			}

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// for dragMode
			this.dragDeltaX = 0;
			this.dragDeltaY = 0;
			if (this.inDrag && dragMode) {
				this.inDrag = false;
				return true;
			}
			this.inDrag = false;
			// end for dragMode
			float x = event.getX();
			float y = event.getY();
			for (ButtonInfo button : buttons) {
				if (button.pressed) {
					button.pressed = false;
					if (MathUtil.distance(x, y, button.x, button.y) <= button.radius) {
						this.notifyButtonReleased(button.buttonID);
						// this.notifyButtonClicked(button.buttonID);
					} else {
						this.notifyButtonReleased(button.buttonID);
					}
				}

			}
			if (oldX >= 0 && oldY >= 0) {
				float deltaX = x - oldX;
				float deltaY = y - oldY;
				Log.v(TAG, deltaX + "\t" + deltaY);
				if (Math.abs(deltaX) > 0.8 * Math.abs(deltaY)) { // left and
					// right
					if (Math.abs(deltaX) > SLIDE_THRESHOLD
							|| Math.abs(deltaY) > SLIDE_THRESHOLD) {
						if (deltaX > 0) {
							this.notifyButtonClicked(BTN_RIGHT);
						} else {
							this.notifyButtonClicked(BTN_LEFT);
						}
					} else {
						this.notifyButtonClicked(BTN_TURN);
					}

				} else {
					if (Math.abs(deltaY) > 1.8 * SLIDE_THRESHOLD) {
						if (deltaY > 0) {
							this.notifyButtonClicked(BTN_DIRECT_DOWN);
						} else {
							this.notifyButtonClicked(BTN_TURN);
						}
					} else {
						this.notifyButtonClicked(BTN_TURN);
					}
				}

			}
			oldX = -1;
			oldY = -1;
			oldXforDrag = -1;
			oldYforDrag = -1;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (oldXforDrag < 0 || oldYforDrag < 0 || !dragMode) {
				return true;
			}
			float deltaX = event.getX() - oldXforDrag;
			float deltaY = event.getY() - oldYforDrag;
			oldXforDrag = event.getX();
			oldYforDrag = event.getY();

			dragDeltaX = deltaX + dragDeltaX;
			dragDeltaY = deltaY + dragDeltaY;
			if (!inDrag) {
				if (Math.abs(dragDeltaX) > 1.5 * SLIDE_THRESHOLD
						&& Math.abs(dragDeltaX) > Math.abs(dragDeltaY)) {
					inDrag = true;
				}
			}
			if (inDrag) {
				while (dragDeltaX > 1.1 * slotGap) {
					dragDeltaX -= slotGap;
					this.notifyButtonClicked(BTN_RIGHT);
				}

				while (dragDeltaX < 1.1 * -slotGap) {
					dragDeltaX += slotGap;
					this.notifyButtonClicked(BTN_LEFT);
				}
			}
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!changed) {
			return;
		}

		soundButton.measure(r - l, b - t);
		int width = r - l;
		int height = b - t;
		int len = width > height ? height : width;
		int btn_len = soundButton.getMeasuredWidth();
		int margin = 5;
		int padding = 5;
		if (Math.min(width, height) < 300) {
			margin = padding = 1;
		}
		int index = -1;
		for (ImageButton btn : controlButtons) {
			index++;
			btn.layout(
					width - (margin + index * (padding + btn_len) + btn_len),
					margin, width - (margin + index * (padding + btn_len)),
					margin + btn_len);

		}
	}

	private ImageButton makeButton(int id, int resid) {
		ImageButton b = new ImageButton(getContext());
		b.setFocusable(false);
		b.setId(id);
		b.setBackgroundResource(R.drawable.transparent_button);
		b.setImageResource(resid);
		b.setOnClickListener(this);
		return b;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case BTN_SOUND:
			config.setSoundEffectsOn(!config.isSoundEffectsOn());

			resetControlButtons();
			break;
		case BTN_MUSIC:
			boolean soundon = !config.isBackgroundMusicOn();
			config.setBackgroundMusicOn(soundon);
			resetControlButtons();
			if (!soundon) {
				SoundManager.get((Activity) this.getContext())
						.stopBackgroundMusic();
			} else {
				SoundManager.get((Activity) this.getContext())
						.playBackgroundMusic();
			}
			break;
		default:
			notifyButtonClicked(view.getId());
		}
	}

	public void resetControlButtons() {
		if (config.isBackgroundMusicOn()) {
			musicButton.setImageResource(R.drawable.icon_music_on);
		} else {
			musicButton.setImageResource(R.drawable.icon_music_off);
		}
		if (config.isSoundEffectsOn()) {
			soundButton.setImageResource(R.drawable.icon_sound_on);
		} else {
			soundButton.setImageResource(R.drawable.icon_sound_off);
		}
		this.invalidate();
	}

	public interface GameControlListener {
		void buttonClickced(int id);

		void buttonPressed(int id);

		void buttonReleased(int id);
	}

	public boolean addGameControlListener(GameControlListener l) {
		return listeners.add(l);
	}

	public boolean removeGameControlListener(GameControlListener l) {
		return listeners.remove(l);
	}

	public void clearGameControlListener() {
		listeners.clear();
	}

	public void notifyButtonClicked(int id) {
		for (GameControlListener l : listeners) {
			l.buttonClickced(id);
		}
	}

	public void notifyButtonPressed(int id) {
		for (GameControlListener l : listeners) {
			l.buttonPressed(id);
		}
	}

	public void notifyButtonReleased(int id) {
		for (GameControlListener l : listeners) {
			l.buttonReleased(id);
		}
	}

	public void configurationChanged(Configuration config) {
		this.resetControlButtons();
		this.dragMode = config.isDragMode();
		Log.v(TAG, "drag mode : " + dragMode);
	}

}
