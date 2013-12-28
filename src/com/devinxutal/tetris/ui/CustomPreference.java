package com.devinxutal.tetris.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

public class CustomPreference extends Preference {
	private float angle = 0.5f;

	// This is the constructor called by the inflater
	public CustomPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getFloat(index, 0);
	}

	@Override
	protected void onClick() {
		// TODO Auto-generated method stub
		SlideOptionDialog dialog = new SlideOptionDialog(getContext(),
				null, 10, new float[] { 45, 135, 225, 315 });
		dialog.show();
		super.onClick();
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			// Restore state
			angle = this.getPersistedFloat(angle);
		} else {
			// Set state
			float value = (Float) defaultValue;
			angle = value;
			persistFloat(value);
		}
	}
}