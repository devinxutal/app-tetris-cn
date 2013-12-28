/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.devinxutal.tetris.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;

public abstract class BaseActivity extends Activity {
	public static final int PREFERENCE_REQUEST_CODE = 0x100;

	static final int DIALOG_ERROR_EMAIL_ALREADY_TAKEN = 8;
	static final int DIALOG_ERROR_INSUFFICIENT_BALANCE = 9;
	static final int DIALOG_ERROR_INVALID_EMAIL_FORMAT = 10;
	static final int DIALOG_ERROR_NAME_ALREADY_TAKEN = 11;
	static final int DIALOG_ERROR_NETWORK = 3;
	static final int DIALOG_ERROR_NOT_ON_HIGHSCORE_LIST = 1;
	static final int DIALOG_ERROR_REQUEST_CANCELLED = 2;
	static final int DIALOG_INFO = 13;
	static final int DIALOG_PROGRESS = 12;
	String infoDialogMessage;
	private String progressDialogMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}

	protected void setProgressDialogMessage(String msg){
		progressDialogMessage = msg;
	}

	
	protected void showToast (final int resId){
		this.showToast(resId, 3000);
	}
	protected void showToast(final String msg){
		this.showToast(msg,3000);
	}
	protected void showToast(final int resId, int time){
		this.showToast(getResources().getString(resId), time);
	}
	protected void showToast(final String msg, int time){
		Toast toast = Toast.makeText(this, msg, time);
		toast.show();
	}
	protected Dialog createErrorDialog(final int resId) {
		return this.createErrorDialog(this.getResources().getString(resId));
	}

	protected Dialog createErrorDialog(final String err_msg) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(err_msg);
		final Dialog dialog = builder.create();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	protected Dialog createInfoDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("[TEXT]");
		final Dialog dialog = builder.create();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	protected Dialog createProgressDialog() {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		if(progressDialogMessage != null){
			dialog.setMessage(progressDialogMessage);
		}else{
		dialog.setMessage("Operation in progress...");
		
		}
		return dialog;
	}

	private void hideProgressIndicatorAndShowDialog(final int dialogId) {
		dismissDialog(DIALOG_PROGRESS);
		showDialog(dialogId);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_ERROR_NOT_ON_HIGHSCORE_LIST:
			return createErrorDialog("You are not ont the high score list now.");
		case DIALOG_ERROR_REQUEST_CANCELLED:
			return createErrorDialog("Request canceled");
		case DIALOG_ERROR_NETWORK:
			return createErrorDialog("Network error");
		case DIALOG_ERROR_EMAIL_ALREADY_TAKEN:
			return createErrorDialog("Email already taken");
		case DIALOG_ERROR_NAME_ALREADY_TAKEN:
			return createErrorDialog("Name already taken");
		case DIALOG_ERROR_INVALID_EMAIL_FORMAT:
			return createErrorDialog("Invalid email format");
		case DIALOG_PROGRESS:
			return createProgressDialog();
		case DIALOG_INFO:
			return createInfoDialog();
		default:
			return null;
		}
	}

	protected void onPrepareDialog(final int id, final Dialog dialog) {
		if (id == DIALOG_INFO) {
			((AlertDialog) dialog).setMessage(infoDialogMessage);
		}
	}

	boolean isRequestCancellation(final Exception e) {
		/*
		if (e instanceof RequestCancelledException) {
			showDialog(DIALOG_ERROR_REQUEST_CANCELLED);
			return true;
		}
		*/
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PREFERENCE_REQUEST_CODE) {
			this.preferenceChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	protected abstract void preferenceChanged();

	
	
}
