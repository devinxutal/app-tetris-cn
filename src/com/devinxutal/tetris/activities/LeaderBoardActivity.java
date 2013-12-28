package com.devinxutal.tetris.activities;

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.perfectgames.amaze.record.LocalRecordManager;
import cn.perfectgames.amaze.record.Record;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.cfg.Constants;
import com.devinxutal.tetris.record.JScore;
import com.devinxutal.tetris.ui.LeaderBoardView;
import com.devinxutal.tetris.ui.LeaderBoardView.ButtonListener;
import com.devinxutal.tetris.util.AdDaemon;
import com.devinxutal.tetris.util.AdUtil;
import com.devinxutal.tetris.util.PreferenceUtil;
import com.devinxutal.tetriscn.GoTetrisApplication;
import com.devinxutal.tetriscn.R;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerException;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.SearchList;
import com.scoreloop.client.android.core.model.Session;

public class LeaderBoardActivity extends BaseActivity implements
		OnClickListener, ButtonListener {

	private static final String TAG = "LeaderBoardActivity";

	public static final int ITEMS_PER_PAGE = 10;

	private Handler adHandler = new Handler();
	private AdDaemon adDaemon;

	private LocalRecordManager localRecordManager;

	enum CurrentOperationType {
		NONE, OTHER, ME, GET_USER_INFO, UPDATE_USER_INFO;
	}

	private static final int FIXED_OFFSET = 3;

	private CurrentOperationType currentOperation = CurrentOperationType.NONE;
	private ScoresController scoresController;
	private UserController userController;

	private LeaderBoardView leaderBoard;


	private enum SL {
		Local, Global, Buddies
	};

	private SL searchList = SL.Local;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.highscore);
		
		leaderBoard = new LeaderBoardView(this);
		leaderBoard.addButtonListener(this);
		((LinearLayout)this.findViewById(R.id.content_area)).addView(leaderBoard);

		
		userController = new UserController(new UserUpdateObserver());


		//
		this.scoresController = new ScoresController(
				new ScoresControllerObserver());
		this.scoresController.setRangeLength(ITEMS_PER_PAGE);

		this.scoresController.setSearchList(SearchList
				.getLocalScoreSearchList());

		//
		this.localRecordManager = GoTetrisApplication.getLocalRecordManager();
		this.localRecordManager.setRangeLength(ITEMS_PER_PAGE);
		
		// ad 
		AdUtil.determineAd(this, R.id.ad_area);
		adDaemon = new AdDaemon("highscore", this, this
				.findViewById(Constants.ADVIEW_ID), adHandler);
		
		// set language
		preferenceChanged();

	}

	@Override
	protected void onStart() {
		updateButtonText();
		loadListFromStart();
		this.adDaemon.run();
		super.onStart();
	}

	private boolean alreadyLoading() {
		return currentOperation != CurrentOperationType.NONE;
	}

	private void loadListFromStart() {
		loadTopRange();
	}

	private void loadNextRange() {
		if (searchList == SL.Local) {
			leaderBoard.setScores(convertFromAmazeRecords(localRecordManager
					.loadNextPage()));
			updateButtonStatus();
		} else {
			if (alreadyLoading() || !scoresController.hasNextRange()) {
				return;
			}
			showDialog(DIALOG_PROGRESS);
			currentOperation = CurrentOperationType.OTHER;

			scoresController.loadNextRange();
		}
	}

	private void loadPreviousRange() {
		if (searchList == SL.Local) {
			leaderBoard.setScores(convertFromAmazeRecords(localRecordManager
					.loadPreviousPage()));
			updateButtonStatus();
		} else {
			if (alreadyLoading() || !scoresController.hasPreviousRange()) {
				return;
			}
			showDialog(DIALOG_PROGRESS);
			currentOperation = CurrentOperationType.OTHER;
			scoresController.loadPreviousRange();
		}
	}

	private void loadTopRange() {
		if (searchList == SL.Local) {
			leaderBoard.setScores(convertFromAmazeRecords(localRecordManager
					.loadPageAtRank(1)));
			updateButtonStatus();
		} else {
			if (alreadyLoading()) {
				return;
			}
			showDialog(DIALOG_PROGRESS);
			currentOperation = CurrentOperationType.OTHER;
			scoresController.loadRangeAtRank(1);
		}
	}

	private void loadRangeForUser() {
		if (searchList == SL.Local) {
			leaderBoard.setScores(convertFromAmazeRecords(localRecordManager
					.loadPageAtRank(1)));
			updateButtonStatus();
		} else {
			if (alreadyLoading()) {
				return;
			}
			showDialog(DIALOG_PROGRESS);
			currentOperation = CurrentOperationType.ME;
			scoresController.loadRangeForUser(Session.getCurrentSession()
					.getUser());
		}
	}

	@Override
	protected void onDestroy() {
		adDaemon.stop();
		super.onDestroy();
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

	}

	public void buttonClickced(int id) {
		// Log.v(TAG, "Button Clicked : " + id);
		switch (id) {
		case LeaderBoardView.BTN_ME:
			loadRangeForUser();
			break;
		case LeaderBoardView.BTN_NEXT:
			loadNextRange();
			break;
		case LeaderBoardView.BTN_PREV:
			loadPreviousRange();
			break;
		case LeaderBoardView.BTN_TOP:
			loadTopRange();
			break;
		case LeaderBoardView.BTN_MODE:
			//showModeChooser();
			break;
		case LeaderBoardView.BTN_SCOPE:
			showScopeChooser();
			break;
		case LeaderBoardView.BTN_SCORELOOP:
			updateUserInfo();
			break;
		}
	}

	public void buttonPressed(int id) {
		// Log.v(TAG, "Button Pressed : " + id);

	}

	public void buttonReleased(int id) {

		// Log.v(TAG, "Button Realeased : " + id);

	}

	private class UserUpdateObserver implements RequestControllerObserver {
		public void requestControllerDidFail(
				final RequestController requestController,
				final Exception exception) {
			if (exception instanceof RequestControllerException) {
				RequestControllerException ctrlException = (RequestControllerException) exception;
				if (ctrlException
						.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_EMAIL_TAKEN)) {
					userControllerDidFailOnEmailAlreadyTaken();
				} else if (ctrlException
						.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_INVALID_EMAIL)) {
					userControllerDidFailOnInvalidEmailFormat();
				} else if (ctrlException
						.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_INVALID_USERNAME)
						| ctrlException
								.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_USERNAME_TAKEN)
						| ctrlException
								.hasDetail(RequestControllerException.DETAIL_USER_UPDATE_REQUEST_USERNAME_TOO_SHORT)) {
					userControllerDidFailOnUsernameAlreadyTaken();
				} else {
					requestControllerDidReceiveGeneralError(exception);
				}
			} else {
				requestControllerDidReceiveGeneralError(exception);
			}
			currentOperation = CurrentOperationType.NONE;
		}

		public void requestControllerDidReceiveResponse(
				final RequestController requestController) {
			dismissDialog(DIALOG_PROGRESS);
			if (currentOperation == CurrentOperationType.GET_USER_INFO) {
				showUserInfoDialog(null);
			} else if (currentOperation == CurrentOperationType.UPDATE_USER_INFO) {
				showToast("User infomation has been updated successfully.");
				currentOperation = CurrentOperationType.NONE;
			}
		}

		private void requestControllerDidReceiveGeneralError(Exception exception) {
			dismissDialog(DIALOG_PROGRESS);
			if (isRequestCancellation(exception)) {
				return;
			}
			showDialog(BaseActivity.DIALOG_ERROR_NETWORK);
			Log.w("Scoreloop", exception);
		}

		private void userControllerDidFailOnEmailAlreadyTaken() {
			dismissDialog(BaseActivity.DIALOG_PROGRESS);
			showUserInfoDialog("User information update failed, the email has already been taken.");

		}

		private void userControllerDidFailOnInvalidEmailFormat() {
			dismissDialog(BaseActivity.DIALOG_PROGRESS);
			showUserInfoDialog("User information update failed, the email address is in invalid format.");
		}

		private void userControllerDidFailOnUsernameAlreadyTaken() {
			dismissDialog(BaseActivity.DIALOG_PROGRESS);
			showUserInfoDialog("User information update failed, the user name has already been taken.");
		}
	}

	private class ScoresControllerObserver implements RequestControllerObserver {

		public void requestControllerDidFail(
				final RequestController requestController,
				final Exception exception) {
			leaderBoard.clearScores();
			dismissDialog(DIALOG_PROGRESS);
			currentOperation = CurrentOperationType.NONE;
			exception.printStackTrace();
			showToast("Error occured while loading scores, please check your network connection.");
			updateButtonStatus();
		}

		public void requestControllerDidReceiveResponse(
				final RequestController requestController) {
			final List<Score> scores = scoresController.getScores();
			Log.v(TAG, "successfully get scores, size = " + scores.size());
			leaderBoard.setScores(convertFromScoreloopScores(scores));

			updateButtonStatus();

			if (currentOperation == CurrentOperationType.ME) {
				boolean loginFound = false;
				int idx = 0;
				for (final Score score : scores) {
					if (score.getUser().equals(
							Session.getCurrentSession().getUser())) {
						loginFound = true;
						break;
					}
					++idx;
				}

				if (!loginFound) {
					// TODO show error message

				}

			}

			currentOperation = CurrentOperationType.NONE;
			dismissDialog(DIALOG_PROGRESS);
		}

	}

	private void showUserInfoDialog(String errMsg) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Update Scoreloop Profile");

		LinearLayout view = new LinearLayout(this);
		view.setPadding(5, 5, 5, 5);
		view.setOrientation(LinearLayout.VERTICAL);
		
		if (errMsg != null) {
			TextView errLabel = new TextView(this);
			errLabel.setTextColor(Color.rgb(255, 255, 100));
			errLabel.setPadding(5, 5, 5, 5);
			errLabel.setText(errMsg);
			view.addView(errLabel, new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
		}
		
		// name
		TextView nameLabel = new TextView(this);
		nameLabel.setText("Name");
		nameLabel.setPadding(0,5, 0, 5);
		view.addView(nameLabel);
		final EditText name = new EditText(this);

		name.setText(Session.getCurrentSession().getUser().getLogin());
		view.addView(name, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		
		// email
		TextView emailLabel = new TextView(this);
		emailLabel.setText("Email");
		emailLabel.setPadding(0,5, 0, 5);
		view.addView(emailLabel);
		final EditText email = new EditText(this);
		email.setText(Session.getCurrentSession().getUser().getEmailAddress());
		view.addView(email, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		builder.setView(view)
				.setPositiveButton(R.string.common_submit,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								Session.getCurrentSession()
										.getUser()
										.setEmailAddress(
												email.getText().toString());
								Session.getCurrentSession().getUser()
										.setLogin(name.getText().toString());
								currentOperation = CurrentOperationType.UPDATE_USER_INFO;
								setProgressDialogMessage("Updating profile, please wait...");
								showDialog(BaseActivity.DIALOG_PROGRESS);
								userController.submitUser();
							}
						})
				.setNegativeButton(R.string.common_cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {

							}
						}).setIcon(R.drawable.icon);
		final AlertDialog alert = builder.create();
		alert.show();
	}


	private void showScopeChooser() {
		final CharSequence[] items = new CharSequence[] { SL.Local.toString(),
				SL.Global.toString(), SL.Buddies.toString() };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Show rankings for");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				SearchList sl = null;
				SL ssl = null;
				switch (item) {
				case 0:
					sl = SearchList.getLocalScoreSearchList();
					ssl = SL.Local;
					break;
				case 1:
					sl = SearchList.getGlobalScoreSearchList();
					ssl = SL.Global;
					break;
				case 2:
					sl = SearchList.getBuddiesScoreSearchList();
					ssl = SL.Buddies;
					break;
				}
				if (searchList != ssl) {
					searchList = ssl;
					updateButtonText();
					scoresController.setSearchList(sl);
					loadTopRange();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void updateUserInfo() {
		this.setProgressDialogMessage("Retrieving user profile, please wait");
		showDialog(BaseActivity.DIALOG_PROGRESS);
		this.currentOperation = CurrentOperationType.GET_USER_INFO;
		userController.loadUser();

	}

	private void updateButtonText() {

		leaderBoard.setButtonText(LeaderBoardView.BTN_SCOPE,
				searchList.toString());
		leaderBoard.setButtonText(LeaderBoardView.BTN_MODE,
				"Normal Mode");

	}

	private void updateButtonStatus() {
		if (searchList == SL.Local) {
			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_ME, false);
			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_TOP, true);

			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_PREV,
					localRecordManager.hasPreviousRange());
			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_NEXT,
					localRecordManager.hasNextRange());
		} else {
			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_ME, true);
			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_TOP, true);

			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_PREV,
					scoresController.hasPreviousRange());
			leaderBoard.setButtonEnabled(LeaderBoardView.BTN_NEXT,
					scoresController.hasNextRange());
		}
	}

	private List<JScore> convertFromAmazeRecords(List<Record> records) {
		LinkedList<JScore> scores = new LinkedList<JScore>();
		for (Record r : records) {
			scores.add(JScore.fromAmazeRecord(r));
		}
		return scores;
	}

	private List<JScore> convertFromScoreloopScores(List<Score> records) {
		LinkedList<JScore> scores = new LinkedList<JScore>();
		for (Score r : records) {
			JScore score = JScore.fromScoreloopScore(r);
			if (r.getUser() != null
					&& r.getUser()
							.equals(Session.getCurrentSession().getUser())) {
				score.setHighlight(true);
			}
			scores.add(score);
		}
		return scores;
	}
	protected void preferenceChanged() {
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());
		
	}

}
