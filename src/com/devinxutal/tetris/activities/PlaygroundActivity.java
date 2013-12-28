package com.devinxutal.tetris.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.perfectgames.amaze.record.Record;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.cfg.Constants;
import com.devinxutal.tetris.control.Command;
import com.devinxutal.tetris.control.GameController;
import com.devinxutal.tetris.control.GameController.GameListener;
import com.devinxutal.tetris.model.SavablePlayground;
import com.devinxutal.tetris.sound.SoundManager;
import com.devinxutal.tetris.ui.ControlView;
import com.devinxutal.tetris.ui.ControlView.GameControlListener;
import com.devinxutal.tetris.ui.JoyStick.JoyStickListener;
import com.devinxutal.tetris.util.AdDaemon;
import com.devinxutal.tetris.util.AdUtil;
import com.devinxutal.tetris.util.PreferenceUtil;
import com.devinxutal.tetris.util.ScoreUtil;
import com.devinxutal.tetriscn.GoTetrisApplication;
import com.devinxutal.tetriscn.R;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoreController;
import com.scoreloop.client.android.core.model.Score;

public class PlaygroundActivity extends BaseActivity {
	public static final String TAG = "PlaygroundActivity";

	public static final int PREFERENCE_REQUEST_CODE = 0x100;

	public enum State {
		INIT, PLAY, PAUSED, ENDING, END
	}

	private GameController gameController;
	private ControlView controlView;
	private ViewGroup successScreen;
	private ViewGroup pausedScreen;
	private Toast toast;
	private Button successScreenBackButton;
	private Button successScreenSubmitButton;
	private Button successScreenReplayButton;
	// private Button successScreenCheckinButton;
	private Button pauseScreenOptionButton;
	private Button pauseScreenQuitButton;
	private Button pauseScreenResumeButton;
	// private Button pauseScreenCheckinButton;

	private Dialog progressDialog;

	private State state = State.INIT;

	// used only when restore state;
	private long elapsedTime = 0;
	private boolean collapsed = true;

	// user interfaces

	private SensorManager sensorManager;
	private Sensor sensor;
	private boolean sensorRunning = false;
	private boolean useJoyStick = true;
	private boolean useAccelerometer = false;
	private AccelerometerListener accelerometerListener = new AccelerometerListener();
	private MyJoyStickListener joyStickListener = new MyJoyStickListener();

	private Typeface typeface;

	// for ad animation
	private Handler adHandler = new Handler();
	private AdDaemon adDaemonPause;
	private AdDaemon adDaemonSuccess;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration.config()
				.setSharedPreferences(
						PreferenceManager
								.getDefaultSharedPreferences(getBaseContext()));

		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // (NEW)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // (NEW)
		setScreenOrientation();
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());

		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		gameController = new GameController(this);
		controlView = gameController.getControlView();
		toast = Toast.makeText(this, "", 5000);
		controlView.addGameControlListener(new ControlButtonClicked());
		controlView.setGameController(gameController);
		FrameLayout layout = new FrameLayout(this);
		layout.addView(gameController.getPlaygroundView());
		gameController.addGameListener(new GameFinished());

		layout.addView(controlView);

		successScreen = new LinearLayout(this);
		layout.addView(successScreen);
		pausedScreen = new LinearLayout(this);
		layout.addView(pausedScreen);
		this.setContentView(layout);

		// add successScreen
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.succeedscreen, successScreen);
		// successScreen = (ViewGroup) this.findViewById(R.id.succeed_screen);
		successScreenBackButton = (Button) this.findViewById(R.id.back_button);
		successScreenSubmitButton = (Button) this
				.findViewById(R.id.submit_button);
		successScreenReplayButton = (Button) this
				.findViewById(R.id.replay_button);
		// successScreenCheckinButton = (Button) this
		// .findViewById(R.id.heyzap_button_2);
		OnClickListener l = new ButtonsOnClick();
		successScreenBackButton.setOnClickListener(l);
		successScreenSubmitButton.setOnClickListener(l);
		successScreenReplayButton.setOnClickListener(l);
		// successScreenCheckinButton.setOnClickListener(l);
		AdUtil.determineAd(this, R.id.ss_ad_area);

		// add pauseScreen
		inflater.inflate(R.layout.pausescreen, pausedScreen);
		pauseScreenOptionButton = (Button) this
				.findViewById(R.id.options_button);
		pauseScreenQuitButton = (Button) this.findViewById(R.id.quit_button);
		pauseScreenResumeButton = (Button) this
				.findViewById(R.id.resume_button);
		// pauseScreenCheckinButton = (Button) this
		// .findViewById(R.id.heyzap_button_1);
		pauseScreenOptionButton.setOnClickListener(l);
		pauseScreenQuitButton.setOnClickListener(l);
		pauseScreenResumeButton.setOnClickListener(l);
		// pauseScreenCheckinButton.setOnClickListener(l);
		AdUtil.determineAd(this, R.id.ps_ad_area);

		//
		preferenceChanged();
		// this.customizeButtons();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		hideSuccessScreen();
		hidePauseScreen();

		Log.v(TAG, "Sequence Test: onCreate");
		if (savedInstanceState == null) {
			Log.v(TAG, "Saved Instance is null");
			switchState(State.INIT);
		}
		//

		View view = (View) PlaygroundActivity.this
				.findViewById(R.id.ss_ad_area);
		View ad = null;
		if (view != null) {
			ad = view.findViewById(Constants.ADVIEW_ID);
		}
		adDaemonSuccess = new AdDaemon("success", this, ad, adHandler);
		view = (View) PlaygroundActivity.this.findViewById(R.id.ps_ad_area);
		if (view != null) {
			ad = view.findViewById(Constants.ADVIEW_ID);
		}
		adDaemonPause = new AdDaemon("pause", this, ad, adHandler);

		adDaemonSuccess.run();
		adDaemonPause.run();
	}

	@Override
	protected void onDestroy() {

		adDaemonSuccess.stop();
		adDaemonPause.stop();
		super.onDestroy();
	}

	private void initByRestoredState() {
		Log.v(TAG, "initByRestoredState: " + state);
		if (state == State.PLAY) {
			state = State.PAUSED;
		}
		switchState(state);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.v(TAG, "key up " + event.getKeyCode());
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_DPAD_UP:
			gameController.processCommand(Command.TURN);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			gameController.processCommand(Command.DOWN);
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			gameController.processCommand(Command.DOWN);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			gameController.processCommand(Command.LEFT);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			gameController.processCommand(Command.RIGHT);
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((this.pausedScreen.getVisibility() == View.INVISIBLE && this.successScreen
				.getVisibility() == View.INVISIBLE)
				&& (event.getKeyCode() == KeyEvent.KEYCODE_MENU || event
						.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
			Log.v(TAG, "back key down");
			pause();
			return false;
		} else if (this.successScreen.getVisibility() == View.INVISIBLE) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setScreenOrientation() {
		String att = Configuration.config().getScreenOrientation();
		if (att.equals("auto")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else if (att.equals("portrait")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (att.equals("landscape")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	private void switchState(State to) {
		switchState(to, true);
	}

	private void switchState(State to, boolean showPauseScreen) {
		boolean needAssignState = true;
		if (to == State.INIT) {
			hideSuccessScreen();
			hidePauseScreen();
			continueConfirm();
			needAssignState = false;
		} else if (to == State.PAUSED) {
			gameController.pause();
			SoundManager.get(this).pauseBackgroundMusic();
			if (showPauseScreen) {
				showPauseScreen();
			}
		} else if (to == State.PLAY) {
			gameController.start();
			SoundManager.get(this).playBackgroundMusic();
			hidePauseScreen();
			hideSuccessScreen();
		} else if (to == State.ENDING) {
			SoundManager.get(this).stopBackgroundMusic();
		} else if (to == State.END) {
			showSuccessScreen();
			hidePauseScreen();
			SoundManager.get(this).stopBackgroundMusic();
		}
		if (needAssignState) {
			this.state = to;
		}
		this.controlView.invalidate();
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		Log.v(TAG, "option menu closed");
		super.onOptionsMenuClosed(menu);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v(TAG, "onSaveInstanceState");
		this.gameController.finishAnimation();
		outState.putSerializable(Constants.PLAYGROUND_STATE, gameController
				.getPlayground().getSavablePlayground());
		outState.putSerializable("state", state);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.v(TAG, "onRestoreInstanceState");

		Log.v(TAG, "Sequence Test: onRestoreInstanseState");
		SavablePlayground sp = (SavablePlayground) (savedInstanceState
				.getSerializable(Constants.PLAYGROUND_STATE));

		if (sp != null) {
			this.gameController.getPlayground().restoreSavablePlayground(sp);
		}
		this.state = (State) savedInstanceState.getSerializable("state");
		if (this.state == null) {
			this.state = State.INIT;
		}
		initByRestoredState();
		super.onRestoreInstanceState(savedInstanceState);

	}

	@Override
	protected void onPause() {
		Log.v(TAG, "pause");
		adDaemonSuccess.stop();
		adDaemonPause.stop();
		if (this.state != State.END) {
			switchState(State.PAUSED, false);
		}
		SoundManager.get(this).stopBackgroundMusic();
		this.stopListening();
		super.onPause();
	}

	@Override
	protected void onResume() {
		adDaemonSuccess.run();
		adDaemonPause.run();
		Log.v(TAG, "on Resume , current state: " + state);
		if (this.state == State.PAUSED) {
			showPauseScreen();
		} else {
			hidePauseScreen();
		}
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PREFERENCE_REQUEST_CODE) {
			this.preferenceChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	class ControlButtonClicked implements GameControlListener {
		public void buttonClickced(int id) {
			switch (id) {
			case ControlView.BTN_LEFT:
				gameController.processCommand(Command.LEFT);
				break;
			case ControlView.BTN_RIGHT:
				gameController.processCommand(Command.RIGHT);
				break;
			case ControlView.BTN_TURN:
				gameController.processCommand(Command.TURN);
				break;
			case ControlView.BTN_DOWN:
				gameController.processCommand(Command.DOWN);
				break;
			case ControlView.BTN_DIRECT_DOWN:
				gameController.processCommand(Command.DIRECT_DOWN);
				break;
			case ControlView.BTN_HOLD:
				gameController.processCommand(Command.HOLD);
				break;
			}
		}

		public void buttonPressed(int id) {
			Log.v(TAG, "button down : " + id);
			switch (id) {
			case ControlView.BTN_LEFT:
				gameController.processCommand(Command.LEFT_DOWN);
				break;
			case ControlView.BTN_RIGHT:
				gameController.processCommand(Command.RIGHT_DOWN);
				break;
			case ControlView.BTN_TURN:
				gameController.processCommand(Command.TURN_DOWN);
				break;
			case ControlView.BTN_DOWN:
				gameController.processCommand(Command.DOWN_DOWN);
				break;
			case ControlView.BTN_DIRECT_DOWN:
				gameController.processCommand(Command.DIRECT_DOWN);
				break;
			}
		}

		public void buttonReleased(int id) {

			Log.v(TAG, "button up : " + id);
			switch (id) {
			case ControlView.BTN_LEFT:
				gameController.processCommand(Command.LEFT_UP);
				break;
			case ControlView.BTN_RIGHT:
				gameController.processCommand(Command.RIGHT_UP);
				break;
			case ControlView.BTN_TURN:
				gameController.processCommand(Command.TURN_UP);
				break;
			case ControlView.BTN_DOWN:
				gameController.processCommand(Command.DOWN_UP);
				break;
			case ControlView.BTN_HOLD:
				gameController.processCommand(Command.HOLD);
				break;
			}
		}
	}

	class GameFinished implements GameListener {

		public void gameFinishing() {

			PlaygroundActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					stop();
				}
			});

		}

		public void gameFinished() {

			PlaygroundActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					int score = gameController.getPlayground()
							.getScoreAndLevel().getScore();
					if (score > 0) {
						ScoreUtil.saveCubeState("Player", score);
					}
					deleteSavedGame();
					showSuccessScreen();
					switchState(State.END);
				}
			});

		}
	}

	class ButtonsOnClick implements OnClickListener {
		public void onClick(View view) {
			SoundManager.get(PlaygroundActivity.this).playButtonClickEffect();
			if (view.getId() == R.id.replay_button) {
				hideSuccessScreen();
				replay();
			} else if (view.getId() == R.id.submit_button) {
				if (((Button) view).getText().equals(
						getResources().getString(
								R.string.success_screen_submit_score))) {
					PlaygroundActivity.this.submitRecord();

				} else {
					Intent intent = new Intent(PlaygroundActivity.this,
							LeaderBoardActivity.class);
					startActivity(intent);
				}

			} else if (view.getId() == R.id.back_button) {
				PlaygroundActivity.this.finish();
			} else if (view.getId() == R.id.quit_button) {
				if (state == State.PAUSED
						&& !gameController.getPlayground().isFinished()) {
					saveGame();
				}
				int score = gameController.getPlayground().getScoreAndLevel()
						.getScore();
				if (score > 0) {
					ScoreUtil.saveCubeState("Player", score);
				}
				PlaygroundActivity.this.finish();
			} else if (view.getId() == R.id.resume_button) {
				resume();
			} else if (view.getId() == R.id.options_button) {
				Intent intent = new Intent(PlaygroundActivity.this,
						Preferences.class);
				PlaygroundActivity.this.startActivityForResult(intent,
						PREFERENCE_REQUEST_CODE);
			}

			// else if (view.getId() == R.id.heyzap_button_1
			// || view.getId() == R.id.heyzap_button_2) {
			// try {
			// HeyzapLib.checkin(PlaygroundActivity.this,
			// "I just stepped into level "
			// + gameController.getPlayground()
			// .getScoreAndLevel().getLevel()
			// + " with a score of "
			// + gameController.getPlayground()
			// .getScoreAndLevel().getScore()
			// + "!");
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
		}
	}

	private void showSuccessScreen() {
		Log.v(TAG, "showing screen");
		this.successScreen.setVisibility(View.VISIBLE);
		this.customizeView(((TextView) this
				.findViewById(R.id.success_screen_title)));

		TextView scoreText = ((TextView) this.findViewById(R.id.score_text));
		scoreText.setText(gameController.getPlayground().getScoreAndLevel()
				.getScore()
				+ "");
		this.successScreenSubmitButton
		 .setText(R.string.success_screen_submit_score);
	}

	private void hideSuccessScreen() {
		Log.v(TAG, "hiding screen");
		this.successScreen.setVisibility(View.INVISIBLE);
	}

	private void showPauseScreen() {
		Log.v(TAG, "showing pause screen");

		this.customizeView(((TextView) this
				.findViewById(R.id.paused_screen_title)));
		this.pausedScreen.setVisibility(View.VISIBLE);
	}

	private void hidePauseScreen() {
		Log.v(TAG, "hiding pause screen");
		this.pausedScreen.setVisibility(View.INVISIBLE);
	}

	public void continueConfirm() {
		if (!tryLoadGame()) {
			replay();
			return;
		}
		final CharSequence[] items = {
				this.getResources().getString(R.string.game_init_continue),
				this.getResources().getString(R.string.game_init_new) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getResources().getString(R.string.app_name))
				.setCancelable(false);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					loadGame();
				} else if (item == 1) {
					replay();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void play() {
		if (this.state != State.PAUSED && this.state != State.END
				&& this.state != State.INIT) {
			return;
		}
		switchState(State.PLAY);
	}

	public void pause() {
		switchState(State.PAUSED);
	}

	public void resume() {
		switchState(State.PLAY);
	}

	public void replay() {
		gameController.reset();
		play();
	}

	public void stop() {
		switchState(State.ENDING);
	}

	public boolean tryLoadGame() {
		File file = Environment.getExternalStorageDirectory();
		SavablePlayground game = null;
		ObjectInputStream in = null;
		try {
			File dataFile = new File(file, Constants.DATA_DIR + "/"
					+ Constants.GAME_SAVING_FILE);
			if (dataFile.exists()) {
				in = new ObjectInputStream(new FileInputStream(dataFile));
				game = (SavablePlayground) in.readObject();
				if (game != null) {
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void deleteSavedGame() {
		File file = Environment.getExternalStorageDirectory();
		SavablePlayground game = null;
		try {
			File dataFile = new File(file, Constants.DATA_DIR + "/"
					+ Constants.GAME_SAVING_FILE);
			if (dataFile.exists()) {
				dataFile.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadGame() {
		File file = Environment.getExternalStorageDirectory();
		SavablePlayground game = null;
		try {
			File dataFile = new File(file, Constants.DATA_DIR + "/"
					+ Constants.GAME_SAVING_FILE);
			if (dataFile.exists()) {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(dataFile));
				game = (SavablePlayground) in.readObject();
				in.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (game != null) {
			this.gameController.getPlayground().restoreSavablePlayground(game);
			play();
		} else {
			replay();
		}
	}

	public void saveGame() {
		File file = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(file, Constants.DATA_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File dataFile = new File(dir, Constants.GAME_SAVING_FILE);

			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(dataFile));
			out.writeObject(this.gameController.getPlayground()
					.getSavablePlayground());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void preferenceChanged() {
		Log.v(TAG, "preference changed");
		// /////////////
		if (this.gameController != null) {
			gameController.configurationChanged(Configuration.config());
		}
		this.setScreenOrientation();
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());
	}

	class SubmitRecordThread extends Thread {
		private String player;

		public SubmitRecordThread(String player) {
			this.player = player;
		}

		@Override
		public void run() {
			String url = Constants.URL_COMMIT_RECORD;
			Map<String, String> data = new HashMap<String, String>();
			data.put("player", player);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			ArrayList<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
			for (Map.Entry<String, String> m : data.entrySet()) {
				postData.add(new BasicNameValuePair(m.getKey(), m.getValue()));
			}
			int statusCode = HttpStatus.SC_ACCEPTED;
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						postData, HTTP.UTF_8);

				httpPost.setEntity(entity);
				HttpResponse response = httpClient.execute(httpPost);
				statusCode = response.getStatusLine().getStatusCode();

			} catch (Exception e) {
				e.printStackTrace();
			}
			final int sc = statusCode;
			PlaygroundActivity.this.runOnUiThread(new Runnable() {

				public void run() {
					if (progressDialog != null) {
						progressDialog.cancel();
						progressDialog = null;
					}
					if (sc == HttpStatus.SC_OK) {
						toast.setText("Your record has been submitted.");
						toast.show();
						successScreenSubmitButton
								.setText(R.string.success_screen_world_rank);
					} else {
						toast.setText("Submit failed, please check your network connection.");
						toast.show();
					}
				}

			});
		}
	}

	// accelerometer

	public void startListening() {
		sensorManager = (SensorManager) gameController.getPlaygroundView()
				.getContext().getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			sensorRunning = sensorManager.registerListener(
					accelerometerListener, sensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
	}

	/**
	 * Unregisters listeners
	 */
	public void stopListening() {
		sensorRunning = false;
		try {
			if (sensorManager != null && accelerometerListener != null) {
				sensorManager.unregisterListener(accelerometerListener);
			}
		} catch (Exception e) {
		}
	}

	class AccelerometerListener implements SensorEventListener {

		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		public void onSensorChanged(SensorEvent event) {
			if (!useAccelerometer) {
				return;
			}
			float x = event.values[0];
			float y = event.values[1];
		}
	}

	class MyJoyStickListener implements JoyStickListener {

		public void joyStickMoved(float dx, float dy) {
			Log.v(TAG, "joy stick moved: dx=" + dx + ", dy=" + dy);
			if (!useJoyStick) {
				return;
			}
			if (Math.abs(dx) > Math.abs(dy)) {
				if (dx > 0.5f) {
					gameController.processCommand(Command.RIGHT);
				} else if (dx < -0.5f) {
					gameController.processCommand(Command.LEFT);
				}
			} else {
				if (dy < 0 - .5f) {
					gameController.processCommand(Command.TURN);
				}
			}
			gameController.getPlaygroundView().invalidate();

		}

	}

	public void submitRecord() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.record_submit_input_name_title);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout l = new LinearLayout(this);
		inflater.inflate(R.layout.submit_score, l);
		final EditText playerName = ((EditText) l
				.findViewById(R.id.player_name));
		final CheckBox submitToGlobal = ((CheckBox) l
				.findViewById(R.id.submit_to_global));
		alert.setView(l);

		playerName.setText(PreferenceUtil.readPlayerName(this));
		alert.setPositiveButton(R.string.common_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						PreferenceUtil.writePlayerName(PlaygroundActivity.this,
								playerName.getText().toString());
						submitRecord(playerName.getText().toString(),
								submitToGlobal.isChecked());
					}
				});

		alert.setNegativeButton(R.string.common_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						return;
					}
				});
		alert.show();
	}

	private void submitRecord(String player, boolean global) {
		int score = gameController.getPlayground().getScoreAndLevel()
				.getScore();
		if (score > 0) {
			if (global) {
				// score loop score
				Score sc = new Score((double) score, null);
				sc.setMode(0);
				sc.setLevel(gameController.getPlayground().getScoreAndLevel()
						.getLevel());

				final ScoreController scoreController = new ScoreController(
						new ScoreSubmitObserver());
				scoreController.submitScore(sc);
				showDialog(DIALOG_PROGRESS);
			}
			// local score
			Record record = new Record();
			record.setMode(0);
			record.setLevel(gameController.getPlayground().getScoreAndLevel()
					.getLevel());
			record.setResult(score);
			record.setPlayer(player);

			GoTetrisApplication.getLocalRecordManager().submitRecord(record);
			if (!global) {
				this.successScreenSubmitButton
						.setText(R.string.success_screen_world_rank);
			}
		}
	}

	public void customizeButtons() {
		if (typeface == null) {

			typeface = Typeface.createFromAsset(getAssets(),
					Constants.FONT_PATH_COMIC);
		}
		customizeButton((Button) this.findViewById(R.id.submit_button));
		customizeButton((Button) this.findViewById(R.id.replay_button));
		customizeButton((Button) this.findViewById(R.id.back_button));
	}

	public void customizeView(TextView extview) {
		if (typeface == null) {

			typeface = Typeface.createFromAsset(getAssets(),
					Constants.FONT_PATH_COMIC);
		}
		extview.setTypeface(typeface);
	}

	private void customizeButton(Button button) {
		button.setTypeface(typeface);
		// button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
	}

	private class ScoreSubmitObserver implements RequestControllerObserver {

		public void requestControllerDidFail(
				final RequestController requestController,
				final Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}

			showToast("Score submission failed due to unknown problem");
			Log.w("Scoreloop", exception);
		}

		public void requestControllerDidReceiveResponse(
				final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
			showToast("Your score has been submitted successfully.");
			successScreenSubmitButton
					.setText(R.string.success_screen_world_rank);
		}
	}
}