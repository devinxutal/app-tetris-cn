package com.devinxutal.tetris.control;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.model.Playground;
import com.devinxutal.tetris.sound.SoundManager;
import com.devinxutal.tetris.ui.ControlView;
import com.devinxutal.tetris.ui.PlaygroundView;

public class GameController {
	public static final int CONTROL_STICK_PERIOD = 100;
	public static final int BASIC_INTERVAL = 100;

	public static final int INTERVAL_CONTROL_INIT = 400;
	public static final int INTERVAL_CONTROL_STICK = 100;
	public static final int INTERVAL_STEP_NORMAL = 700;
	public static final int INTERVAL_STEP_ANIMATION = 150;

	public static final int IC_STEP = 5;
	private static final String TAG = "GameController";

	public interface GameListener {
		public void gameFinishing();

		public void gameFinished();
	}

	private SoundManager soundManager;

	private PlaygroundView playgroundView;
	private ControlView controlView;
	private Playground playground;
	private List<GameListener> listeners = new LinkedList<GameListener>();

	private Handler handler;

	private boolean playing = true;
	private StepRunnable stepRunnable = new StepRunnable();
	private ControlRunnable controlRunnable = new ControlRunnable();

	private Command pendingCommand = null;

	public void start() {
		playing = true;
		postStepDelay(INTERVAL_STEP_NORMAL);
	}

	public void pause() {
		playing = false;
		this.clearPendingCommand();
	}

	public void stop() {
		playing = false;
	}

	public void reset() {
		playground.reset();
		this.playgroundView.reset();
		this.clearPendingCommand();
	}

	public void finishAnimation() {
		while (this.playground.isInAnimation()) {
			this.playground.moveOn();
		}
	}

	public void processCommand(Command cmd) {
		if (!this.playing || playground.isFinished()) {
			return;
		}
		switch (cmd) {
		case DOWN_UP:
			pendingCommand = cmd = null;
			break;
		case TURN_UP:
			pendingCommand = cmd = null;
			break;
		case LEFT_UP:
			pendingCommand = cmd = null;
			break;
		case RIGHT_UP:
			pendingCommand = cmd = null;
			break;
		case DOWN_DOWN:
			pendingCommand = cmd = Command.DOWN;
			break;
		case TURN_DOWN:
			pendingCommand = cmd = Command.TURN;
			break;
		case LEFT_DOWN:
			pendingCommand = cmd = Command.LEFT;
			break;
		case RIGHT_DOWN:
			pendingCommand = cmd = Command.RIGHT;
			break;
		default:
			pendingCommand = null;
		}
		if (cmd != null) {
			boolean success = this.letPlaygroundProcessCommand(cmd);
			this.playgroundView.invalidate();
			if (success && (cmd == Command.DOWN || cmd == Command.DIRECT_DOWN)) {
				postStepDelay(getStepDelay());
			}
		}
		if (pendingCommand != null) {
			postControlDelay(INTERVAL_CONTROL_INIT);
		}
	}

	private int getStepDelay() {
		return (int) (INTERVAL_STEP_NORMAL * playground.getSpeedScale());
	}

	public void clearPendingCommand() {
		this.pendingCommand = null;
	}

	public PlaygroundView getPlaygroundView() {
		return playgroundView;
	}

	public boolean addGameListener(GameListener l) {
		return listeners.add(l);
	}

	public boolean removeGameListener(GameListener l) {
		return listeners.remove(l);
	}

	public void clearGameListeners(GameListener l) {
		listeners.clear();
	}

	protected void notifyGameFinished() {
		for (GameListener l : listeners) {
			l.gameFinished();
		}
	}

	protected void notifyGameFinishing() {
		for (GameListener l : listeners) {
			l.gameFinishing();
		}
	}

	public void setPlaygroundView(PlaygroundView playgroundView) {
		this.playgroundView = playgroundView;
	}

	public Playground getPlayground() {
		return playground;
	}

	public void setPlayground(Playground playground) {
		this.playground = playground;
	}

	public GameController(Context context) {
		this.playground = new Playground();
		this.playgroundView = new PlaygroundView(context);
		this.playgroundView.setPlayground(playground);
		this.playgroundView.setGameController(this);
		this.controlView = new ControlView(context);
		this.playgroundView.setControlView(this.controlView);
		this.handler = new Handler();
		this.soundManager = SoundManager.get((Activity) context);
	}

	public void destroy() {
		this.playing = false;
	}

	public void configurationChanged(Configuration config) {
		this.controlView.configurationChanged(config);
		this.playgroundView.configurationChanged(config);
		this.playground.configurationChanged(config);
	}

	private void postControlDelay(int delay) {
		this.controlRunnable.increaseIDToProcess();
		handler.postDelayed(controlRunnable, delay);
	}

	private void postStepDelay(int delay) {
		this.stepRunnable.increaseIDToProcess();
		handler.postDelayed(stepRunnable, delay);
	}

	class StepRunnable implements Runnable {
		private long idToProcess = 0;
		private long idProcessed = 0;

		public synchronized void increaseIDToProcess() {
			idToProcess++;
		}

		public void run() {
			synchronized (this) {
				idProcessed++;
				if (idProcessed != idToProcess) {
					return;
				}
			}
			playground.moveOn();
			playgroundView.invalidate();
			if (playground.isFinishingElimination()) {
				soundManager.playEliminationEffect();
			}
			if (playground.isFinished()) {
				playing = false;
				GameController.this.notifyGameFinished();
				Log.v(TAG, "finish detected");
			} else {
				if (playing) {
					if (playground.isInAnimation()) {
						postStepDelay(INTERVAL_STEP_ANIMATION);
					} else {
						postStepDelay(getStepDelay());
					}
				}
			}

		}

	}

	class ControlRunnable implements Runnable {
		private long idToProcess = 0;
		private long idProcessed = 0;

		public synchronized void increaseIDToProcess() {
			idToProcess++;
		}

		public void run() {
			synchronized (this) {
				idProcessed++;
				if (idProcessed != idToProcess) {
					return;
				}
			}
			if (pendingCommand != null) {
				boolean success = letPlaygroundProcessCommand(pendingCommand);
				playgroundView.invalidate();
				postControlDelay(INTERVAL_CONTROL_STICK);
				if (success) {
					postStepDelay(getStepDelay());
				}
			}

		}

	}

	private boolean letPlaygroundProcessCommand(Command command) {
		boolean succeed = playground.processCommand(command);
		if (succeed) {
			switch (command) {
			case LEFT:
				soundManager.playMoveEffect();
				break;
			case RIGHT:
				soundManager.playMoveEffect();
				break;
			case TURN:
				soundManager.playTurnEffect();
				break;
			case DOWN:
				soundManager.playMoveEffect();
				break;
			case DIRECT_DOWN:
				soundManager.playDownEffect();
				break;
			case HOLD:
				soundManager.playTurnEffect();
				break;
			}
		}
		return succeed;
	}

	public ControlView getControlView() {
		return this.controlView;
	}
}
