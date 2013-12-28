package com.devinxutal.tetris.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devinxutal.tetris.util.MathUtil;
import com.devinxutal.tetriscn.R;

public class SlideOptionDialog extends Dialog {

	public interface OnSlideOptionChangedListener {
		void optionChanged(int degree);
	}

	private OnSlideOptionChangedListener mListener;

	private class ColorPickerView extends View {

		private Paint paint;
		private OnSlideOptionChangedListener listener;

		ColorPickerView(Context c, OnSlideOptionChangedListener l, int angle) {
			super(c);
			listener = l;

			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		}

		private float centerX;
		private float centerY;
		private float radius;
		private float controlRadius;

		private float angleOffset;
		private int selectedAngle = -1;

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				for (int i = 0; i < angles.length; i++) {
					float angle = angles[i];
					float sin = (float) Math.sin(angle / 180 * Math.PI);
					float cos = (float) Math.cos(angle / 180 * Math.PI);
					float cx = centerX + radius * cos;
					float cy = centerX + radius * sin;
					if (MathUtil.distance(x, y, cx, cy) < 2 * controlRadius) {
						selectedAngle = i;
						float dx = event.getX() - centerX;
						float dy = event.getY() - centerY;
						double agl = Math.atan2(dy, dx);
						if (agl < 0) {
							agl = 2 * Math.PI + agl;
						}
						agl = agl / Math.PI * 180;
						angleOffset = (float) (agl - angles[i]);
						break;
					}
				}
				this.invalidate();
				return true;
			case MotionEvent.ACTION_MOVE:
				float dx = event.getX() - centerX;
				float dy = event.getY() - centerY;
				double angle = Math.atan2(dy, dx);
				if (angle < 0) {
					angle = 2 * Math.PI + angle;
				}
				angle = angle / Math.PI * 180;
				if (selectedAngle >= 0) {
					int index = selectedAngle;
					float newAngle = (float) (angle + angleOffset);
					if (newAngle < selectedAngle * 90 + 10) {
						newAngle = selectedAngle * 90 + 10;
						if (newAngle < selectedAngle * 90 + 1) {
							selectedAngle = -1;
						}
					} else if (newAngle > selectedAngle * 90 + 80) {
						newAngle = selectedAngle * 90 + 80;
						if (newAngle > selectedAngle * 90 + 89) {
							selectedAngle = -1;
						}
					}
					angles[index] = newAngle;
				}
				this.invalidate();
				return true;
			case MotionEvent.ACTION_UP:
				selectedAngle = -1;
				this.invalidate();
				return true;
			}
			return super.onTouchEvent(event);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			int width = getWidth();
			int height = getHeight();
			centerX = width / 2f;
			centerY = height / 2f;
			radius = Math.min(width, height) * 0.9f / 2;
			controlRadius = radius * 0.1f;
			paint.setStrokeWidth(radius / 100);
			drawCircle(canvas, centerX, centerY, radius, Color.DKGRAY,
					Color.WHITE, paint);

			for (float angle : angles) {
				float sin = (float) Math.sin(angle / 180 * Math.PI);
				float cos = (float) Math.cos(angle / 180 * Math.PI);
				float cx = centerX + radius * cos;
				float cy = centerX + radius * sin;
				paint.setColor(Color.WHITE);
				canvas.drawLine(centerX, centerY, cx, cy, paint);
				drawCircle(canvas, cx, cy, controlRadius, Color.BLUE,
						Color.WHITE, paint);
			}
			drawCircle(canvas, centerX, centerY, controlRadius, Color.LTGRAY,
					Color.WHITE, paint);

		}

		private void drawCircle(Canvas c, float x, float y, float r, int fill,
				int stoke, Paint p) {
			p.setAlpha(255);
			p.setStyle(Style.FILL);
			p.setColor(fill);
			c.drawCircle(x, y, r, p);
			p.setStyle(Style.STROKE);
			p.setColor(stoke);
			c.drawCircle(x, y, r, p);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(PICKER_RADIUS * 2, PICKER_RADIUS * 2);
		}

		private static final int PICKER_RADIUS = 150;

	}

	public SlideOptionDialog(Context context,
			OnSlideOptionChangedListener listener, int initialColor,
			float angles[]) {
		super(context);
		mListener = listener;
		this.initialColor = initialColor;
		this.angles = angles;
	}

	private int initialColor = 1;

	private float omitRadius = 10;
	private float[] angles = new float[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OnSlideOptionChangedListener l = new OnSlideOptionChangedListener() {
			public void optionChanged(int degree) {
				mListener.optionChanged(degree);
				dismiss();
			}
		};

		LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		TextView txt = new TextView(getContext());
		txt.setText("Hello world");
		setTitle("Pick a Color");

		layout.addView(txt);
		layout.addView(new ColorPickerView(getContext(), l, initialColor));
		LinearLayout buttons = new LinearLayout(getContext());
		buttons.setOrientation(LinearLayout.HORIZONTAL);
		createButton(buttons, R.string.common_ok);
		createButton(buttons, R.string.common_reset);
		createButton(buttons, R.string.common_cancel);
		layout.addView(buttons);
		setContentView(layout);
	}

	private void createButton(LinearLayout layout, int txtId) {
		Button btn = new Button(getContext());
		btn.setText(txtId);
		LinearLayout l = new LinearLayout(getContext());
		l.addView(btn, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
				LayoutParams.WRAP_CONTENT);
		p.width = 0;
		layout.addView(l, p);
	}
}