package com.devinxutal.tetris.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.youmi.android.spot.SpotManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.cfg.Constants;
import com.devinxutal.tetris.sound.SoundManager;
import com.devinxutal.tetris.util.AdDaemon;
import com.devinxutal.tetris.util.AdUtil;
import com.devinxutal.tetris.util.DialogUtil;
import com.devinxutal.tetris.util.PreferenceUtil;
import com.devinxutal.tetriscn.R;
import com.scoreloop.client.android.core.controller.TermsOfServiceController;
import com.scoreloop.client.android.core.controller.TermsOfServiceControllerObserver;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.TermsOfService;
import com.scoreloop.client.android.core.model.TermsOfService.Status;

public class MainActivity extends BaseActivity implements OnClickListener {
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAit2OZ8OaxRwh8B9Du45ejqWe4XaWVp4RwD4Du0j1S7ZokOAJqYlu4rEQwdz9mGLqLV3I9fJhmgryXjqMpxl5+wODxS7FNvy6uRNzVLTbXdD/vvIt5CucAXRs9xFHj1gT212m59q2dw6iAT4E6dRhnzWIT48v9YYVW4iWymgnQYW7WMxTuk56bBp37VW0qZ9V+D+PrYlRkklSezBe2fVsOVuv09nLkD2sRTQVyvm0fW4P6Q9/7yUx2HJ5p1TQvtHe2ZaRUJuRG5+ZR3/gdGcaEfRXopB59S0DWhNyzMkw2RZRzUtg0N6H9PIbodHab04/qZCM86jTtRUDmQFFnp4/ZQIDAQAB";

	private static final int PREFERENCE_REQUEST_CODE = 0x100;

	private static final byte[] SALT = new byte[] { -46, 65, 30, -128, -103,
			-57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64,
			89 };

	private static final String TAG = "MainActvity";

	private Typeface buttonFont;
	private Dialog progressDialog;

	public static final int PLAYGROUND_ACTIVITY_ID = 1151;

	private Handler adHandler;

	private AdDaemon adDaemon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // (NEW)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // (NEW)
		Configuration.config()
				.setSharedPreferences(
						PreferenceManager
								.getDefaultSharedPreferences(getBaseContext()));
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());

		net.youmi.android.YoumiAdManager.getInstance(this).init("9a569a351928555b", "779567818294d49a", false); 
		SpotManager.getInstance(this).loadSpotAds();
		SpotManager.getInstance(this).showSpotAds(this);
		this.setContentView(R.layout.main);

		SoundManager.init(this);
		customizeButtons();
		int ids[] = new int[] { R.id.main_btn_play_game,
				R.id.main_btn_preference, R.id.main_btn_rank,
				R.id.main_btn_help };
		for (int id : ids) {
			try {
				this.findViewById(id).setOnClickListener(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		AdUtil.determineAd(this, R.id.ad_area);
		// for lisencing
		// Try to use more data here. ANDROID_ID is a single point of attack.
		String deviceId = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);
		//
		showNoticeDialog();
		// AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR,
		// "7037AF0B100BC4F3CC9F4B5401F96685" });
		adHandler = new Handler();
		this.adDaemon = new AdDaemon("main", this, this
				.findViewById(Constants.ADVIEW_ID), adHandler);
		adDaemon.run();

		// check scoreloop TOS
		TermsOfService tos = Session.getCurrentSession()
				.getUsersTermsOfService();
		if (tos.getStatus() != Status.ACCEPTED) {
			// tos.getStatus() is one of Status.ACCEPTED, Status.PENDING,
			// Status.REJECTED, Status.OUTDATED
			final TermsOfServiceController controller = new TermsOfServiceController(
					new TermsOfServiceControllerObserver() {

						public void termsOfServiceControllerDidFinish(
								final TermsOfServiceController controller,
								final Boolean accepted) {
							if (accepted != null) {
								// we have conclusive result.
								if (accepted) {
									// user did accept
								} else {
									// user did reject
								}
							}
						}
					});
			controller.query(this);

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adDaemon.stop();
		SoundManager.release();
	}

	@Override
	protected void onPause() {
		adDaemon.stop();
		super.onPause();
	}

	protected void onResume() {
		adDaemon.run();
		super.onResume();
	}

	public void onClick(View view) {
		Intent i = null;
		int startID = -1;
		Log.v(TAG, "before play effect");
		SoundManager.get(this).playButtonClickEffect();
		Log.v(TAG, "after play effect");
		switch (view.getId()) {
		case R.id.main_btn_play_game:
			i = new Intent(this, PlaygroundActivity.class);
			startID = PLAYGROUND_ACTIVITY_ID;
			break;

		case R.id.main_btn_preference:
			i = new Intent(this, Preferences.class);
			startID = PREFERENCE_REQUEST_CODE;
			break;

		case R.id.main_btn_rank:
			i = new Intent(this, LeaderBoardActivity.class);
			// DialogUtil.showRankDialog(this);
			break;
		case R.id.main_btn_help:
			DialogUtil.showDialogWithView(this, getResources().getString(
					R.string.help_info_title), R.layout.help);
			return;
		}
		if (startID >= 0) {
			this.startActivityForResult(i, startID);
		} else {
			startActivity(i);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PREFERENCE_REQUEST_CODE) {
			this.preferenceChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
			Log.v(TAG, "back key down");

			this.showQuitDialog();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void preferenceChanged() {
		Log.v(TAG, "preference changed");
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());
	}
	
	public void customizeButtons() {
		if (buttonFont == null) {

			buttonFont = Typeface.createFromAsset(getAssets(),
					Constants.FONT_PATH_COMIC);
		}
		customizeButton((Button) this.findViewById(R.id.main_btn_play_game));
		customizeButton((Button) this.findViewById(R.id.main_btn_preference));
		customizeButton((Button) this.findViewById(R.id.main_btn_rank));
		customizeButton((Button) this.findViewById(R.id.main_btn_help));
	}


	private void customizeButton(Button button) {
		button.setTypeface(buttonFont);
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		int width = Math.min(display.getWidth(), display.getHeight());
		if (width < 320) {
			button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
		}
		// button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
	}

	private void showQuitDialog() {

		Resources res = this.getResources();
		final CharSequence[] items = {
				res.getString(R.string.common_back_game),
				res.getString(R.string.common_quit_game) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 1) {

					MainActivity.this.finish();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showCommitReportDialog() {

	}

	public void submitReport() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Feedback");
		final EditText input = new EditText(this);
		input.setText("");
		LinearLayout layout = new LinearLayout(this);
		layout.setPadding(20, 10, 20, 10);
		layout.addView(input, ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		alert.setView(layout);

		alert.setPositiveButton(R.string.common_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						submitReport(input.getText().toString());
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

	private void submitReport(String report) {
		progressDialog = ProgressDialog.show(this, "", getResources()
				.getString(R.string.record_submit_submitting_record), true);
		new SubmitRecordThread(report).start();
	}

	class SubmitRecordThread extends Thread {
		private String report;

		public SubmitRecordThread(String report) {
			this.report = report;
		}

		private String getInfo() {
			String info = "";
			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int w = display.getWidth();
			int h = display.getHeight();
			info += "SCREEN[" + Math.min(w, h) + "x" + Math.max(w, h) + "]";
			info += ", ANDROID[" + android.os.Build.VERSION.SDK + "]";
			return info;
		}

		@Override
		public void run() {
			String url = Constants.URL_COMMIT_REPORT;

			Map<String, String> data = new HashMap<String, String>();
			data.put("app", "Go Tetris");
			data.put("report", report);
			data.put("info", getInfo());
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
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					if (progressDialog != null) {
						progressDialog.cancel();
						progressDialog = null;
					}
					if (sc == HttpStatus.SC_OK) {
						Toast.makeText(MainActivity.this,
								"Your feedback has been submitted. Thankyou!",
								1000).show();
					} else {
						Toast
								.makeText(
										MainActivity.this,
										"Submit failed, please check your network connection.",
										1000).show();
					}
				}

			});
		}
	}

	private void showNoticeDialog() {
		if (PreferenceUtil.canShowUpgradeNotice(MainActivity.this)) {
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout l = new LinearLayout(this);
			inflater.inflate(R.layout.whats_new, l);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("What's New").setCancelable(false)
					.setPositiveButton(R.string.common_ok, null)
					.setPositiveButton("OK", null).setNegativeButton(
							"Never Show Again",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									PreferenceUtil.setShowUpgradeNotice(
											MainActivity.this, false);
								}
							}).setView(l);
			AlertDialog alert = builder.create();

			// alert.setContentView(view_id);
			alert.show();
		}
	}
}
